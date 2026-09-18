package com.aryaxzell.keyglass.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.ConcurrentHashMap

class PersonalDictionaryRepository(private val dao: PersonalWordDao) {
    private val memoryCache = ConcurrentHashMap.newKeySet<String>()

    val allWords: Flow<List<PersonalWord>> = dao.getAllWords().onEach { list ->
        memoryCache.clear()
        list.forEach { memoryCache.add(it.word.trim().lowercase()) }
    }

    suspend fun getAllWordsList(): List<String> = dao.getAllWordsList()

    suspend fun containsWord(word: String): Boolean {
        val trimmed = word.trim().lowercase()
        if (memoryCache.contains(trimmed)) return true
        val inDb = dao.containsWord(word)
        if (inDb) memoryCache.add(trimmed)
        return inDb
    }

    suspend fun addWord(word: String): Long {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return -1L
        memoryCache.add(trimmed.lowercase())
        return dao.insertWord(PersonalWord(word = trimmed))
    }

    suspend fun deleteWordById(id: Long) {
        dao.deleteWordById(id)
    }

    suspend fun deleteWord(word: String) {
        val trimmed = word.trim()
        memoryCache.remove(trimmed.lowercase())
        dao.deleteWord(trimmed)
    }

    suspend fun clearAll() {
        memoryCache.clear()
        dao.clearAll()
    }
}
