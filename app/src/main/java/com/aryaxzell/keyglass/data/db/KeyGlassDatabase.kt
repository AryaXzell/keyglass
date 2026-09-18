package com.aryaxzell.keyglass.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PersonalWord::class], version = 1, exportSchema = false)
abstract class KeyGlassDatabase : RoomDatabase() {
    abstract fun personalWordDao(): PersonalWordDao

    companion object {
        @Volatile
        private var INSTANCE: KeyGlassDatabase? = null

        fun getInstance(context: Context): KeyGlassDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyGlassDatabase::class.java,
                    "keyglass_personal_dict.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
