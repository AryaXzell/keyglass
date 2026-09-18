package com.aryaxzell.keyglass.data.engine

import com.aryaxzell.keyglass.data.db.PersonalDictionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SuggestionCandidate(
    val word: String,
    val score: Float,
    val isAutoCorrect: Boolean = false
)

class PredictionEngine(
    private val personalDictionaryRepository: PersonalDictionaryRepository
) {
    private val englishTrie = Trie()
    private val indonesianTrie = Trie()
    private val englishWords = mutableListOf<String>()
    private val indonesianWords = mutableListOf<String>()

    init {
        // Load English corpus into Trie and list
        CorpusData.ENGLISH_WORDS.forEach { (word, freq) ->
            englishTrie.insert(word, freq)
            englishWords.add(word)
        }
        // Load Indonesian corpus into Trie and list
        CorpusData.INDONESIAN_WORDS.forEach { (word, freq) ->
            indonesianTrie.insert(word, freq)
            indonesianWords.add(word)
        }
    }

    suspend fun getSuggestions(
        currentWord: String,
        previousText: String,
        language: String = "en",
        typoCorrectionEnabled: Boolean = true
    ): List<SuggestionCandidate> = withContext(Dispatchers.Default) {
        val trimmedWord = currentWord.trim().lowercase()
        val trie = if (language.startsWith("in", ignoreCase = true)) indonesianTrie else englishTrie
        val wordList = if (language.startsWith("in", ignoreCase = true)) indonesianWords else englishWords
        val bigramMap = if (language.startsWith("in", ignoreCase = true)) CorpusData.INDONESIAN_BIGRAMS else CorpusData.ENGLISH_BIGRAMS

        // If currently typing a word (non-empty)
        if (trimmedWord.isNotEmpty()) {
            val candidates = mutableMapOf<String, Float>()
            val isAutoCorrectCandidate = mutableSetOf<String>()

            // First check if word is in personal dictionary
            val isWhitelisted = personalDictionaryRepository.containsWord(trimmedWord)

            // Stage 1: Typo Detection (Levenshtein Distance)
            // If not found in dictionary or whitelist, find closest match with edit distance <= 2
            if (!isWhitelisted && !trie.contains(trimmedWord) && trimmedWord.length >= 2) {
                var bestDist = 3
                var bestCandidate: String? = null
                val len = trimmedWord.length
                for (dictWord in wordList) {
                    if (Math.abs(dictWord.length - len) <= 2) {
                        val dist = Levenshtein.distance(trimmedWord, dictWord)
                        if (dist in 1..2 && dist < bestDist) {
                            bestDist = dist
                            bestCandidate = dictWord
                        }
                    }
                }
                if (bestCandidate != null) {
                    candidates[bestCandidate] = 60f
                    if (typoCorrectionEnabled) {
                        isAutoCorrectCandidate.add(bestCandidate)
                    }
                }
            }

            // Stage 2: Prefix Matching (Trie)
            val prefixMatches = trie.findByPrefix(trimmedWord, limit = 10)
            for ((word, freq) in prefixMatches) {
                val existing = candidates[word] ?: 0f
                candidates[word] = maxOf(existing, freq.toFloat())
            }

            // Context scoring from previous words
            val contextKey = extractContextKey(previousText)
            val contextNextWords = if (contextKey != null) bigramMap[contextKey] ?: emptyList() else emptyList()
            val contextSet = contextNextWords.map { it.first }.toSet()

            // Stage 4: Ranking & Scoring
            // score = (frequency_weight * 0.4) + (context_match_weight * 0.4) + (edit_distance_penalty * 0.2)
            val scoredList = candidates.map { (word, rawFreq) ->
                val freqWeight = (rawFreq / 100f).coerceIn(0f, 1f)
                val contextMatchWeight = if (contextSet.contains(word)) 1.0f else 0.0f
                val editDist = Levenshtein.distance(trimmedWord, word)
                val editDistPenalty = (1.0f - (editDist * 0.35f)).coerceIn(0f, 1f)

                val finalScore = (freqWeight * 0.4f) + (contextMatchWeight * 0.4f) + (editDistPenalty * 0.2f)
                SuggestionCandidate(
                    word = word,
                    score = finalScore,
                    isAutoCorrect = isAutoCorrectCandidate.contains(word)
                )
            }.sortedByDescending { it.score }

            // If empty or user typed an exact prefix, ensure user's typed word can also be preserved
            val topResults = scoredList.take(3).toMutableList()
            if (topResults.isEmpty() || (topResults.none { it.word.equals(trimmedWord, ignoreCase = true) } && trimmedWord.isNotEmpty())) {
                topResults.add(0, SuggestionCandidate(trimmedWord, 1.0f, false))
            }
            topResults.take(3)
        } else {
            // Empty current word: Stage 3 Next-Word Prediction (Bigram / Trigram)
            val contextKey = extractContextKey(previousText)
            val nextWords = if (contextKey != null) bigramMap[contextKey] ?: emptyList() else emptyList()

            if (nextWords.isNotEmpty()) {
                nextWords.take(3).map { (word, freq) ->
                    SuggestionCandidate(word = word, score = freq / 100f, isAutoCorrect = false)
                }
            } else {
                // Fallback to top unigrams for current language
                val topUnigrams = if (language.startsWith("in", ignoreCase = true)) {
                    listOf("yang", "dan", "di")
                } else {
                    listOf("the", "to", "and")
                }
                topUnigrams.map { word ->
                    SuggestionCandidate(word = word, score = 0.5f, isAutoCorrect = false)
                }
            }
        }
    }

    private fun extractContextKey(previousText: String): String? {
        val trimmed = previousText.trim()
        if (trimmed.isEmpty()) return null
        val words = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (words.isEmpty()) return null

        if (words.size >= 2) {
            val trigramKey = "${words[words.size - 2].lowercase()} ${words[words.size - 1].lowercase()}"
            return trigramKey
        }
        return words.last().lowercase()
    }
}
