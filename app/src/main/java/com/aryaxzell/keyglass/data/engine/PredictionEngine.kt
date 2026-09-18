package com.aryaxzell.keyglass.data.engine

import android.util.LruCache
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
    private val englishWords = CorpusData.ENGLISH_WORDS.map { it.first }
    private val indonesianWords = CorpusData.INDONESIAN_WORDS.map { it.first }

    // Fast in-memory LRU prediction cache
    private val predictionCache = LruCache<String, List<SuggestionCandidate>>(32)

    init {
        // Load English corpus into Trie
        CorpusData.ENGLISH_WORDS.forEach { (word, freq) ->
            englishTrie.insert(word, freq)
        }
        // Load Indonesian corpus into Trie
        CorpusData.INDONESIAN_WORDS.forEach { (word, freq) ->
            indonesianTrie.insert(word, freq)
        }
    }

    suspend fun getSuggestions(
        currentWord: String,
        previousText: String,
        language: String = "en",
        typoCorrectionEnabled: Boolean = true
    ): List<SuggestionCandidate> = withContext(Dispatchers.Default) {
        val trimmedWord = currentWord.trim().lowercase()
        val contextKey = extractContextKey(previousText)
        val cacheKey = "$language|$typoCorrectionEnabled|$contextKey|$trimmedWord"

        val cached = predictionCache.get(cacheKey)
        if (cached != null) {
            return@withContext cached
        }

        val trie = if (language.startsWith("in", ignoreCase = true)) indonesianTrie else englishTrie
        val wordList = if (language.startsWith("in", ignoreCase = true)) indonesianWords else englishWords
        val bigramMap = if (language.startsWith("in", ignoreCase = true)) CorpusData.INDONESIAN_BIGRAMS else CorpusData.ENGLISH_BIGRAMS

        val results: List<SuggestionCandidate> = if (trimmedWord.isNotEmpty()) {
            val candidates = mutableMapOf<String, Float>()
            val isAutoCorrectCandidate = mutableSetOf<String>()

            val isWhitelisted = personalDictionaryRepository.containsWord(trimmedWord)

            // Stage 1: Typo Detection (Levenshtein Distance)
            if (!isWhitelisted && !trie.contains(trimmedWord) && trimmedWord.length >= 2) {
                var bestDist = 3
                var bestCandidate: String? = null
                val len = trimmedWord.length
                val firstChar = trimmedWord[0]

                val candidateSearchPool = wordList.take(200)
                for (dictWord in candidateSearchPool) {
                    if (Math.abs(dictWord.length - len) <= 2) {
                        val sameFirstChar = dictWord.isNotEmpty() && dictWord[0] == firstChar
                        if (sameFirstChar || Math.abs(dictWord.length - len) <= 1) {
                            val dist = Levenshtein.distance(trimmedWord, dictWord)
                            if (dist in 1..2 && dist < bestDist) {
                                bestDist = dist
                                bestCandidate = dictWord
                                if (dist == 1 && sameFirstChar) break
                            }
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
            val contextNextWords = if (contextKey != null) bigramMap[contextKey] ?: emptyList() else emptyList()

            // Stage 4: Ranking & Scoring
            val scoredList = candidates.map { (word, rawFreq) ->
                val freqWeight = (rawFreq / 100f).coerceIn(0f, 1f)
                val contextMatchWeight = if (contextNextWords.any { it.first.equals(word, ignoreCase = true) }) 1.0f else 0.0f
                val editDist = if (word.equals(trimmedWord, ignoreCase = true)) 0 else Levenshtein.distance(trimmedWord, word)
                val editDistPenalty = (1.0f - (editDist * 0.35f)).coerceIn(0f, 1f)

                val finalScore = (freqWeight * 0.4f) + (contextMatchWeight * 0.4f) + (editDistPenalty * 0.2f)
                SuggestionCandidate(
                    word = word,
                    score = finalScore,
                    isAutoCorrect = isAutoCorrectCandidate.contains(word)
                )
            }.sortedByDescending { it.score }

            val topResults = scoredList.take(3).toMutableList()
            if (topResults.isEmpty() || (topResults.none { it.word.equals(trimmedWord, ignoreCase = true) } && trimmedWord.isNotEmpty())) {
                topResults.add(0, SuggestionCandidate(trimmedWord, 1.0f, false))
            }
            topResults.take(3)
        } else {
            // Empty current word: Stage 3 Next-Word Prediction
            val nextWords = if (contextKey != null) bigramMap[contextKey] ?: emptyList() else emptyList()

            if (nextWords.isNotEmpty()) {
                nextWords.take(3).map { (word, freq) ->
                    SuggestionCandidate(word = word, score = freq / 100f, isAutoCorrect = false)
                }
            } else {
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

        predictionCache.put(cacheKey, results)
        results
    }

    private fun extractContextKey(previousText: String): String? {
        val trimmed = previousText.trim()
        if (trimmed.isEmpty()) return null
        val lastSpace = trimmed.lastIndexOf(' ')
        val lastWord = if (lastSpace >= 0) trimmed.substring(lastSpace + 1) else trimmed
        return if (lastWord.isNotEmpty()) lastWord.lowercase() else null
    }
}
