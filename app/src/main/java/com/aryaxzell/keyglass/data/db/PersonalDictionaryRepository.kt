package com.aryaxzell.keyglass.data.db

import kotlinx.coroutines.flow.Flow

class PersonalDictionaryRepository(private val dao: PersonalWordDao) {
    val allWords: Flow<List<PersonalWord>> = dao.getAllWords()

    suspend fun getAllWordsList(): List<String> = dao.getAllWordsList()

    suspend fun containsWord(word: String): Boolean = dao.containsWord(word)

    suspend fun addWord(word: String): Long {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return -1L
        return dao.insertWord(PersonalWord(word = trimmed))
    }

    suspend fun deleteWordById(id: Long) {
        dao.deleteWordById(id)
    }

    suspend fun deleteWord(word: String) {
        dao.deleteWord(word.trim())
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
