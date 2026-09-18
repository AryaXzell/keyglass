package com.aryaxzell.keyglass.data.engine

class TrieNode(
    val char: Char? = null,
    var isEndOfWord: Boolean = false,
    var frequency: Int = 0,
    var fullWord: String? = null
) {
    val children = mutableMapOf<Char, TrieNode>()
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String, frequency: Int) {
        val lower = word.lowercase()
        var current = root
        for (char in lower) {
            current = current.children.getOrPut(char) { TrieNode(char = char) }
        }
        current.isEndOfWord = true
        current.frequency = maxOf(current.frequency, frequency)
        current.fullWord = lower
    }

    fun contains(word: String): Boolean {
        val lower = word.lowercase()
        var current = root
        for (char in lower) {
            current = current.children[char] ?: return false
        }
        return current.isEndOfWord
    }

    fun getFrequency(word: String): Int {
        val lower = word.lowercase()
        var current = root
        for (char in lower) {
            current = current.children[char] ?: return 0
        }
        return if (current.isEndOfWord) current.frequency else 0
    }

    fun findByPrefix(prefix: String, limit: Int = 20): List<Pair<String, Int>> {
        val lower = prefix.lowercase()
        var current = root
        for (char in lower) {
            current = current.children[char] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Int>>()
        collectWords(current, results)
        return results.sortedByDescending { it.second }.take(limit)
    }

    private fun collectWords(node: TrieNode, results: MutableList<Pair<String, Int>>) {
        if (node.isEndOfWord && node.fullWord != null) {
            results.add(Pair(node.fullWord!!, node.frequency))
        }
        for (child in node.children.values) {
            collectWords(child, results)
        }
    }
}
