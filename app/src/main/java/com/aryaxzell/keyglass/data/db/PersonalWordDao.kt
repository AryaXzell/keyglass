package com.aryaxzell.keyglass.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalWordDao {
    @Query("SELECT * FROM personal_words ORDER BY timestamp DESC")
    fun getAllWords(): Flow<List<PersonalWord>>

    @Query("SELECT word FROM personal_words")
    suspend fun getAllWordsList(): List<String>

    @Query("SELECT COUNT(*) > 0 FROM personal_words WHERE LOWER(word) = LOWER(:word)")
    suspend fun containsWord(word: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: PersonalWord): Long

    @Query("DELETE FROM personal_words WHERE id = :id")
    suspend fun deleteWordById(id: Long)

    @Query("DELETE FROM personal_words WHERE LOWER(word) = LOWER(:word)")
    suspend fun deleteWord(word: String)

    @Query("DELETE FROM personal_words")
    suspend fun clearAll()
}
