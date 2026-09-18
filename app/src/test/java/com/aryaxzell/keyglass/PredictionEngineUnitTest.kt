package com.aryaxzell.keyglass

import com.aryaxzell.keyglass.data.engine.CorpusData
import com.aryaxzell.keyglass.data.engine.Levenshtein
import com.aryaxzell.keyglass.data.engine.Trie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictionEngineUnitTest {

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, Levenshtein.distance("hello", "hello"))
        assertEquals(1, Levenshtein.distance("helo", "hello"))
        assertEquals(1, Levenshtein.distance("hllo", "hello"))
        assertEquals(1, Levenshtein.distance("hellp", "hello"))
        assertEquals(2, Levenshtein.distance("hleo", "hello"))
    }

    @Test
    fun testTriePrefixMatching() {
        val trie = Trie()
        trie.insert("apple", 100)
        trie.insert("application", 90)
        trie.insert("banana", 80)

        assertTrue(trie.contains("apple"))
        assertFalse(trie.contains("app"))

        val prefixMatches = trie.findByPrefix("app", limit = 5)
        assertEquals(2, prefixMatches.size)
        assertEquals("apple", prefixMatches[0].first)
        assertEquals("application", prefixMatches[1].first)
    }

    @Test
    fun testCorpusDataIntegrity() {
        assertTrue(CorpusData.ENGLISH_WORDS.any { it.first == "the" })
        assertTrue(CorpusData.ENGLISH_WORDS.any { it.first == "keyboard" })
        assertTrue(CorpusData.INDONESIAN_WORDS.any { it.first == "yang" })
        assertTrue(CorpusData.INDONESIAN_WORDS.any { it.first == "keyboard" })
    }
}
