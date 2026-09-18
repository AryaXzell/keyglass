package com.aryaxzell.keyglass.data.engine

import java.util.PriorityQueue

class TrieNode(
    val char: Char? = null,
    var isEndOfWord: Boolean = false,
    var frequency: Int = 0,
    var fullWord: String? = null
) {
    var children: HashMap<Char, TrieNode>? = null

    fun getChild(c: Char): TrieNode? = children?.get(c)

    fun getOrCreateChild(c: Char): TrieNode {
        var map = children
        if (map == null) {
            map = HashMap(4, 0.75f)
            children = map
        }
        return map.getOrPut(c) { TrieNode(char = c) }
    }
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String, frequency: Int) {
        val lower = word.lowercase()
        var current = root
        for (i in 0 until lower.length) {
            current = current.getOrCreateChild(lower[i])
        }
        current.isEndOfWord = true
        current.frequency = maxOf(current.frequency, frequency)
        current.fullWord = lower
    }

    fun contains(word: String): Boolean {
        val lower = word.lowercase()
        var current = root
        for (i in 0 until lower.length) {
            current = current.getChild(lower[i]) ?: return false
        }
        return current.isEndOfWord
    }

    fun getFrequency(word: String): Int {
        val lower = word.lowercase()
        var current = root
        for (i in 0 until lower.length) {
            current = current.getChild(lower[i]) ?: return 0
        }
        return if (current.isEndOfWord) current.frequency else 0
    }

    fun findByPrefix(prefix: String, limit: Int = 20): List<Pair<String, Int>> {
        val lower = prefix.lowercase()
        var current = root
        for (i in 0 until lower.length) {
            current = current.getChild(lower[i]) ?: return emptyList()
        }

        val pq = PriorityQueue<Pair<String, Int>>(limit + 1, compareBy { it.second })
        collectWordsBounded(current, pq, limit)

        val result = ArrayList<Pair<String, Int>>(pq.size)
        while (pq.isNotEmpty()) {
            result.add(pq.poll()!!)
        }
        result.reverse()
        return result
    }

    private fun collectWordsBounded(node: TrieNode, pq: PriorityQueue<Pair<String, Int>>, limit: Int) {
        if (node.isEndOfWord && node.fullWord != null) {
            pq.offer(Pair(node.fullWord!!, node.frequency))
            if (pq.size > limit) {
                pq.poll()
            }
        }
        val map = node.children ?: return
        for (child in map.values) {
            collectWordsBounded(child, pq, limit)
        }
    }
}
