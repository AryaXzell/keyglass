# KeyGlass ProGuard / R8 Optimization Rules

# Keep Room DB entities and DAOs
-keep class com.aryaxzell.keyglass.data.db.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Keep DataStore models and Settings
-keep class com.aryaxzell.keyglass.data.datastore.** { *; }
-keep class com.aryaxzell.keyglass.data.engine.** { *; }

# Keep Input Method Service
-keep class com.aryaxzell.keyglass.ime.KeyGlassInputMethodService { *; }

# Jetpack Compose rules
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Kotlin Coroutines and Serialization
-keepclassmembers class * {
    kotlinx.coroutines.internal.MainDispatcherFactory *;
}

# Preserve stacktraces for debugging
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,Signature
-renamesourcefileattribute SourceFile
