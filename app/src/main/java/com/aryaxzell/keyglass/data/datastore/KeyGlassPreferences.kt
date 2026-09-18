package com.aryaxzell.keyglass.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyglass_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class FontSource {
    APP_FONT, SYSTEM_FONT
}

enum class KeyboardBackgroundType {
    PRESET, CUSTOM_IMAGE
}

enum class KeyboardPresetTheme {
    GLASS_DARK,
    GLASS_LIGHT,
    FOOTBALL,
    CYBERPUNK,
    SUNSET,
    OCEAN,
    EMERALD,
    AURORA,
    MIDNIGHT,
    PASTEL_BLOOM
}

data class KeyGlassSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val keyCornerRadiusDp: Float = 5f,
    val accentColorHex: String = "#007AFF",
    val fontSource: FontSource = FontSource.APP_FONT,
    val backgroundType: KeyboardBackgroundType = KeyboardBackgroundType.PRESET,
    val presetTheme: KeyboardPresetTheme = KeyboardPresetTheme.GLASS_DARK,
    val customBackgroundPath: String = "",
    val backgroundOverlayDim: Float = 0.35f,
    val appLanguage: String = "system", // "system", "en", "in"
    val typoCorrectionEnabled: Boolean = true,
    val predictiveTextEnabled: Boolean = true,
    val keyPopupBubbleEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val hapticIntensity: Float = 0.5f,
    val soundFeedbackEnabled: Boolean = false,
    val soundVolume: Float = 0.5f,
    val autoCapitalizeEnabled: Boolean = true,
    val typingLanguages: Set<String> = setOf("en", "in"),
    val activeTypingLanguage: String = "en",
    val spaceBarCursorSensitivity: Float = 1.0f,
    val longPressDurationMs: Long = 400L,
    val batteryReminderDismissCount: Int = 0,
    val onboardingCompleted: Boolean = false
)

object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_CORNER_RADIUS = floatPreferencesKey("key_corner_radius")
    val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
    val FONT_SOURCE = stringPreferencesKey("font_source")
    val BACKGROUND_TYPE = stringPreferencesKey("background_type")
    val PRESET_THEME = stringPreferencesKey("preset_theme")
    val CUSTOM_BACKGROUND_PATH = stringPreferencesKey("custom_background_path")
    val BACKGROUND_OVERLAY_DIM = floatPreferencesKey("background_overlay_dim")
    val APP_LANGUAGE = stringPreferencesKey("app_language")
    val TYPO_CORRECTION = booleanPreferencesKey("typo_correction")
    val PREDICTIVE_TEXT = booleanPreferencesKey("predictive_text")
    val KEY_POPUP_BUBBLE = booleanPreferencesKey("key_popup_bubble")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    val HAPTIC_INTENSITY = floatPreferencesKey("haptic_intensity")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val SOUND_VOLUME = floatPreferencesKey("sound_volume")
    val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
    val TYPING_LANGUAGES = stringSetPreferencesKey("typing_languages")
    val ACTIVE_TYPING_LANGUAGE = stringPreferencesKey("active_typing_language")
    val SPACE_BAR_SENSITIVITY = floatPreferencesKey("space_bar_sensitivity")
    val LONG_PRESS_DURATION = longPreferencesKey("long_press_duration")
    val BATTERY_REMINDER_COUNT = intPreferencesKey("battery_reminder_count")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}

class PreferencesRepository(private val context: Context) {
    val settingsFlow: Flow<KeyGlassSettings> = context.dataStore.data.map { prefs ->
        KeyGlassSettings(
            themeMode = ThemeMode.valueOf(prefs[PreferenceKeys.THEME_MODE] ?: ThemeMode.DARK.name),
            keyCornerRadiusDp = prefs[PreferenceKeys.KEY_CORNER_RADIUS] ?: 5f,
            accentColorHex = prefs[PreferenceKeys.ACCENT_COLOR_HEX] ?: "#007AFF",
            fontSource = FontSource.valueOf(prefs[PreferenceKeys.FONT_SOURCE] ?: FontSource.APP_FONT.name),
            backgroundType = try {
                KeyboardBackgroundType.valueOf(prefs[PreferenceKeys.BACKGROUND_TYPE] ?: KeyboardBackgroundType.PRESET.name)
            } catch (e: Exception) {
                KeyboardBackgroundType.PRESET
            },
            presetTheme = try {
                KeyboardPresetTheme.valueOf(prefs[PreferenceKeys.PRESET_THEME] ?: KeyboardPresetTheme.GLASS_DARK.name)
            } catch (e: Exception) {
                KeyboardPresetTheme.GLASS_DARK
            },
            customBackgroundPath = prefs[PreferenceKeys.CUSTOM_BACKGROUND_PATH] ?: "",
            backgroundOverlayDim = prefs[PreferenceKeys.BACKGROUND_OVERLAY_DIM] ?: 0.35f,
            appLanguage = prefs[PreferenceKeys.APP_LANGUAGE] ?: "system",
            typoCorrectionEnabled = prefs[PreferenceKeys.TYPO_CORRECTION] ?: true,
            predictiveTextEnabled = prefs[PreferenceKeys.PREDICTIVE_TEXT] ?: true,
            keyPopupBubbleEnabled = prefs[PreferenceKeys.KEY_POPUP_BUBBLE] ?: true,
            hapticFeedbackEnabled = prefs[PreferenceKeys.HAPTIC_ENABLED] ?: true,
            hapticIntensity = prefs[PreferenceKeys.HAPTIC_INTENSITY] ?: 0.5f,
            soundFeedbackEnabled = prefs[PreferenceKeys.SOUND_ENABLED] ?: false,
            soundVolume = prefs[PreferenceKeys.SOUND_VOLUME] ?: 0.5f,
            autoCapitalizeEnabled = prefs[PreferenceKeys.AUTO_CAPITALIZE] ?: true,
            typingLanguages = prefs[PreferenceKeys.TYPING_LANGUAGES] ?: setOf("en", "in"),
            activeTypingLanguage = prefs[PreferenceKeys.ACTIVE_TYPING_LANGUAGE] ?: "en",
            spaceBarCursorSensitivity = prefs[PreferenceKeys.SPACE_BAR_SENSITIVITY] ?: 1.0f,
            longPressDurationMs = prefs[PreferenceKeys.LONG_PRESS_DURATION] ?: 400L,
            batteryReminderDismissCount = prefs[PreferenceKeys.BATTERY_REMINDER_COUNT] ?: 0,
            onboardingCompleted = prefs[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferenceKeys.THEME_MODE] = mode.name }
    }

    suspend fun updateKeyCornerRadius(radius: Float) {
        context.dataStore.edit { it[PreferenceKeys.KEY_CORNER_RADIUS] = radius }
    }

    suspend fun updateAccentColor(hex: String) {
        context.dataStore.edit { it[PreferenceKeys.ACCENT_COLOR_HEX] = hex }
    }

    suspend fun updateFontSource(source: FontSource) {
        context.dataStore.edit { it[PreferenceKeys.FONT_SOURCE] = source.name }
    }

    suspend fun updateBackgroundType(type: KeyboardBackgroundType) {
        context.dataStore.edit { it[PreferenceKeys.BACKGROUND_TYPE] = type.name }
    }

    suspend fun updatePresetTheme(theme: KeyboardPresetTheme) {
        context.dataStore.edit { it[PreferenceKeys.PRESET_THEME] = theme.name }
    }

    suspend fun updateCustomBackgroundPath(path: String) {
        context.dataStore.edit { it[PreferenceKeys.CUSTOM_BACKGROUND_PATH] = path }
    }

    suspend fun updateBackgroundOverlayDim(dim: Float) {
        context.dataStore.edit { it[PreferenceKeys.BACKGROUND_OVERLAY_DIM] = dim }
    }

    suspend fun updateKeyboardBackgroundConfig(
        type: KeyboardBackgroundType,
        preset: KeyboardPresetTheme,
        customPath: String,
        dim: Float
    ) {
        context.dataStore.edit {
            it[PreferenceKeys.BACKGROUND_TYPE] = type.name
            it[PreferenceKeys.PRESET_THEME] = preset.name
            it[PreferenceKeys.CUSTOM_BACKGROUND_PATH] = customPath
            it[PreferenceKeys.BACKGROUND_OVERLAY_DIM] = dim
        }
    }

    suspend fun updateAppLanguage(lang: String) {
        context.dataStore.edit { it[PreferenceKeys.APP_LANGUAGE] = lang }
    }

    suspend fun updateTypoCorrection(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.TYPO_CORRECTION] = enabled }
    }

    suspend fun updatePredictiveText(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.PREDICTIVE_TEXT] = enabled }
    }

    suspend fun updateKeyPopupBubble(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.KEY_POPUP_BUBBLE] = enabled }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun updateHapticIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferenceKeys.HAPTIC_INTENSITY] = intensity }
    }

    suspend fun updateSoundFeedback(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun updateSoundVolume(volume: Float) {
        context.dataStore.edit { it[PreferenceKeys.SOUND_VOLUME] = volume }
    }

    suspend fun updateAutoCapitalize(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.AUTO_CAPITALIZE] = enabled }
    }

    suspend fun updateTypingLanguages(languages: Set<String>) {
        context.dataStore.edit { it[PreferenceKeys.TYPING_LANGUAGES] = languages }
    }

    suspend fun updateActiveTypingLanguage(language: String) {
        context.dataStore.edit { it[PreferenceKeys.ACTIVE_TYPING_LANGUAGE] = language }
    }

    suspend fun updateSpaceBarSensitivity(sensitivity: Float) {
        context.dataStore.edit { it[PreferenceKeys.SPACE_BAR_SENSITIVITY] = sensitivity }
    }

    suspend fun updateLongPressDuration(durationMs: Long) {
        context.dataStore.edit { it[PreferenceKeys.LONG_PRESS_DURATION] = durationMs }
    }

    suspend fun incrementBatteryReminderCount() {
        context.dataStore.edit {
            val current = it[PreferenceKeys.BATTERY_REMINDER_COUNT] ?: 0
            it[PreferenceKeys.BATTERY_REMINDER_COUNT] = current + 1
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs.clear()
            prefs[PreferenceKeys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun exportSettingsJson(settings: KeyGlassSettings): String {
        val json = JSONObject().apply {
            put("themeMode", settings.themeMode.name)
            put("keyCornerRadiusDp", settings.keyCornerRadiusDp.toDouble())
            put("accentColorHex", settings.accentColorHex)
            put("fontSource", settings.fontSource.name)
            put("backgroundType", settings.backgroundType.name)
            put("presetTheme", settings.presetTheme.name)
            put("customBackgroundPath", settings.customBackgroundPath)
            put("backgroundOverlayDim", settings.backgroundOverlayDim.toDouble())
            put("appLanguage", settings.appLanguage)
            put("typoCorrectionEnabled", settings.typoCorrectionEnabled)
            put("predictiveTextEnabled", settings.predictiveTextEnabled)
            put("keyPopupBubbleEnabled", settings.keyPopupBubbleEnabled)
            put("hapticFeedbackEnabled", settings.hapticFeedbackEnabled)
            put("hapticIntensity", settings.hapticIntensity.toDouble())
            put("soundFeedbackEnabled", settings.soundFeedbackEnabled)
            put("soundVolume", settings.soundVolume.toDouble())
            put("autoCapitalizeEnabled", settings.autoCapitalizeEnabled)
            val langArray = JSONArray()
            settings.typingLanguages.forEach { langArray.put(it) }
            put("typingLanguages", langArray)
            put("activeTypingLanguage", settings.activeTypingLanguage)
            put("spaceBarCursorSensitivity", settings.spaceBarCursorSensitivity.toDouble())
            put("longPressDurationMs", settings.longPressDurationMs)
        }
        return json.toString(2)
    }

    suspend fun importSettingsJson(jsonString: String): Boolean {
        return try {
            val json = JSONObject(jsonString)
            context.dataStore.edit { prefs ->
                if (json.has("themeMode")) prefs[PreferenceKeys.THEME_MODE] = json.getString("themeMode")
                if (json.has("keyCornerRadiusDp")) prefs[PreferenceKeys.KEY_CORNER_RADIUS] = json.getDouble("keyCornerRadiusDp").toFloat()
                if (json.has("accentColorHex")) prefs[PreferenceKeys.ACCENT_COLOR_HEX] = json.getString("accentColorHex")
                if (json.has("fontSource")) prefs[PreferenceKeys.FONT_SOURCE] = json.getString("fontSource")
                if (json.has("backgroundType")) prefs[PreferenceKeys.BACKGROUND_TYPE] = json.getString("backgroundType")
                if (json.has("presetTheme")) prefs[PreferenceKeys.PRESET_THEME] = json.getString("presetTheme")
                if (json.has("customBackgroundPath")) prefs[PreferenceKeys.CUSTOM_BACKGROUND_PATH] = json.getString("customBackgroundPath")
                if (json.has("backgroundOverlayDim")) prefs[PreferenceKeys.BACKGROUND_OVERLAY_DIM] = json.getDouble("backgroundOverlayDim").toFloat()
                if (json.has("appLanguage")) prefs[PreferenceKeys.APP_LANGUAGE] = json.getString("appLanguage")
                if (json.has("typoCorrectionEnabled")) prefs[PreferenceKeys.TYPO_CORRECTION] = json.getBoolean("typoCorrectionEnabled")
                if (json.has("predictiveTextEnabled")) prefs[PreferenceKeys.PREDICTIVE_TEXT] = json.getBoolean("predictiveTextEnabled")
                if (json.has("keyPopupBubbleEnabled")) prefs[PreferenceKeys.KEY_POPUP_BUBBLE] = json.getBoolean("keyPopupBubbleEnabled")
                if (json.has("hapticFeedbackEnabled")) prefs[PreferenceKeys.HAPTIC_ENABLED] = json.getBoolean("hapticFeedbackEnabled")
                if (json.has("hapticIntensity")) prefs[PreferenceKeys.HAPTIC_INTENSITY] = json.getDouble("hapticIntensity").toFloat()
                if (json.has("soundFeedbackEnabled")) prefs[PreferenceKeys.SOUND_ENABLED] = json.getBoolean("soundFeedbackEnabled")
                if (json.has("soundVolume")) prefs[PreferenceKeys.SOUND_VOLUME] = json.getDouble("soundVolume").toFloat()
                if (json.has("autoCapitalizeEnabled")) prefs[PreferenceKeys.AUTO_CAPITALIZE] = json.getBoolean("autoCapitalizeEnabled")
                if (json.has("typingLanguages")) {
                    val arr = json.getJSONArray("typingLanguages")
                    val set = mutableSetOf<String>()
                    for (i in 0 until arr.length()) {
                        set.add(arr.getString(i))
                    }
                    if (set.isNotEmpty()) prefs[PreferenceKeys.TYPING_LANGUAGES] = set
                }
                if (json.has("activeTypingLanguage")) prefs[PreferenceKeys.ACTIVE_TYPING_LANGUAGE] = json.getString("activeTypingLanguage")
                if (json.has("spaceBarCursorSensitivity")) prefs[PreferenceKeys.SPACE_BAR_SENSITIVITY] = json.getDouble("spaceBarCursorSensitivity").toFloat()
                if (json.has("longPressDurationMs")) prefs[PreferenceKeys.LONG_PRESS_DURATION] = json.getLong("longPressDurationMs")
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
