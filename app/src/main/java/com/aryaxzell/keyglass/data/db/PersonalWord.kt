package com.aryaxzell.keyglass.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_words",
    indices = [Index(value = ["word"], unique = true)]
)
data class PersonalWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val timestamp: Long = System.currentTimeMillis()
)
