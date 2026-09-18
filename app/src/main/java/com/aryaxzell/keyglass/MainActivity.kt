package com.aryaxzell.keyglass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.data.db.KeyGlassDatabase
import com.aryaxzell.keyglass.data.db.PersonalDictionaryRepository
import com.aryaxzell.keyglass.ui.navigation.KeyGlassApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val preferencesRepository = PreferencesRepository(this)
        val database = KeyGlassDatabase.getInstance(this)
        val personalDictionaryRepository = PersonalDictionaryRepository(database.personalWordDao())

        setContent {
            KeyGlassApp(
                preferencesRepository = preferencesRepository,
                personalDictionaryRepository = personalDictionaryRepository
            )
        }
    }
}
