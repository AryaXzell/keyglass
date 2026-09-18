package com.aryaxzell.keyglass.ime

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.data.db.KeyGlassDatabase
import com.aryaxzell.keyglass.data.db.PersonalDictionaryRepository
import com.aryaxzell.keyglass.data.engine.PredictionEngine
import com.aryaxzell.keyglass.data.engine.SuggestionCandidate
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGButtonStyle
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import com.aryaxzell.keyglass.ui.theme.KeyGlassKeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KeyGlassInputMethodService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var personalDictionaryRepository: PersonalDictionaryRepository
    private lateinit var predictionEngine: PredictionEngine
    private lateinit var audioHapticFeedback: AudioHapticFeedback
    private lateinit var clipboardManager: ClipboardManager

    // Dynamic keyboard states
    private var currentSettings by mutableStateOf(KeyGlassSettings())
    private var isPasswordField by mutableStateOf(false)
    private var shiftState by mutableStateOf(ShiftState.SHIFT_ONCE)
    private var keyboardMode by mutableStateOf(KeyboardMode.ALPHA)
    private var enterActionLabel by mutableStateOf("return")
    private var suggestions by mutableStateOf<List<SuggestionCandidate>>(emptyList())
    private var clipboardPreview by mutableStateOf<String?>(null)

    // Autocorrect state tracker for auto-revert & learn
    private var lastAutoCorrection: Pair<String, String>? = null // original to corrected

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        updateClipboardPreview()
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        preferencesRepository = PreferencesRepository(this)
        val db = KeyGlassDatabase.getInstance(this)
        personalDictionaryRepository = PersonalDictionaryRepository(db.personalWordDao())
        predictionEngine = PredictionEngine(personalDictionaryRepository)
        audioHapticFeedback = AudioHapticFeedback(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipListener)

        serviceScope.launch {
            preferencesRepository.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                updateAutoCapitalize()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        try {
            clipboardManager.removePrimaryClipChangedListener(clipListener)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@KeyGlassInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@KeyGlassInputMethodService)
            setContent {
                KeyGlassKeyboardTheme(settings = currentSettings) {
                    if (currentSettings.temporaryDisabled) {
                        // Paused State Bar with Fallback Option
                        val colors = HIGTheme.colors
                        val strings = LocalStrings.current
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(colors.keyboardBackground)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = strings.keyboardPausedStatus,
                                    style = HIGTheme.typography.headline,
                                    color = colors.primaryLabel
                                )
                                Text(
                                    text = strings.keyboardPausedSub,
                                    style = HIGTheme.typography.footnote,
                                    color = colors.secondaryLabel
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HIGButton(
                                        text = strings.enableKeyGlass,
                                        onClick = {
                                            serviceScope.launch {
                                                preferencesRepository.updateTemporaryDisabled(false)
                                            }
                                        },
                                        style = HIGButtonStyle.FILLED
                                    )
                                    HIGButton(
                                        text = strings.switchToKeyGlass,
                                        onClick = {
                                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                            imm?.showInputMethodPicker()
                                        },
                                        style = HIGButtonStyle.TINTED
                                    )
                                }
                            }
                        }
                    } else {
                        KeyboardView(
                            settings = currentSettings,
                            isPasswordField = isPasswordField,
                            shiftState = shiftState,
                            keyboardMode = keyboardMode,
                            enterActionLabel = enterActionLabel,
                            suggestions = suggestions,
                            clipboardPreview = clipboardPreview,
                            onCharTyped = { char -> handleCharTyped(char) },
                            onBackspace = { handleBackspace() },
                            onSpace = { handleSpace() },
                            onEnter = { handleEnter() },
                            onShiftClick = { handleShiftClick() },
                            onShiftDoubleClick = { handleShiftDoubleClick() },
                            onModeChange = { mode -> keyboardMode = mode },
                            onLanguageSwitch = { handleLanguageSwitch() },
                            onSuggestionClicked = { candidate -> handleSuggestionClicked(candidate) },
                            onPasteClicked = { handlePaste() },
                            onCursorMoved = { offset -> handleCursorMoved(offset) },
                            onKeyFeedback = {
                                audioHapticFeedback.triggerKeyFeedback(
                                    window?.window?.decorView,
                                    hapticEnabled = currentSettings.hapticFeedbackEnabled,
                                    hapticIntensity = currentSettings.hapticIntensity,
                                    soundEnabled = currentSettings.soundFeedbackEnabled,
                                    soundVolume = currentSettings.soundVolume
                                )
                            }
                        )
                    }
                }
            }
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // 1. Password detection
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        isPasswordField = (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD) ||
                (variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ||
                (variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) ||
                (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)

        // 2. Action Label
        val action = info.imeOptions and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        enterActionLabel = when (action) {
            EditorInfo.IME_ACTION_GO -> "go"
            EditorInfo.IME_ACTION_SEARCH -> "search"
            EditorInfo.IME_ACTION_SEND -> "send"
            EditorInfo.IME_ACTION_NEXT -> "next"
            EditorInfo.IME_ACTION_DONE -> "done"
            else -> "return"
        }

        keyboardMode = KeyboardMode.ALPHA
        lastAutoCorrection = null
        updateClipboardPreview()
        updateAutoCapitalize()
        updatePredictions()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lastAutoCorrection = null
    }

    private fun handleCharTyped(char: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(char, 1)

        // If shift was once, reset to lowercase
        if (shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.LOWERCASE
        }
        lastAutoCorrection = null
        updatePredictions()
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return

        // Auto-revert rule (PRD §5): If user immediately presses backspace right after an autocorrection
        if (lastAutoCorrection != null) {
            val (original, corrected) = lastAutoCorrection!!
            val textBefore = ic.getTextBeforeCursor(corrected.length + 1, 0)?.toString() ?: ""
            if (textBefore.endsWith("$corrected ")) {
                // Delete the space and the corrected word
                ic.deleteSurroundingText(corrected.length + 1, 0)
                // Restore original mistyped word
                ic.commitText(original, 1)
                // Add original to personal dictionary so it's not corrected again
                serviceScope.launch {
                    personalDictionaryRepository.addWord(original)
                }
                lastAutoCorrection = null
                updatePredictions()
                return
            }
        }

        ic.deleteSurroundingText(1, 0)
        lastAutoCorrection = null
        updateAutoCapitalize()
        updatePredictions()
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return

        // Check if typo correction should be applied on space
        if (currentSettings.typoCorrectionEnabled && !isPasswordField && suggestions.isNotEmpty()) {
            val autoCandidate = suggestions.firstOrNull { it.isAutoCorrect }
            if (autoCandidate != null) {
                val currentWord = getCurrentWordBeforeCursor()
                if (currentWord.isNotEmpty() && !currentWord.equals(autoCandidate.word, ignoreCase = true)) {
                    ic.deleteSurroundingText(currentWord.length, 0)
                    ic.commitText(autoCandidate.word + " ", 1)
                    lastAutoCorrection = Pair(currentWord, autoCandidate.word)
                    updateAutoCapitalize()
                    updatePredictions()
                    return
                }
            }
        }

        ic.commitText(" ", 1)
        lastAutoCorrection = null
        updateAutoCapitalize()
        updatePredictions()
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions ?: 0
        val maskAction = action and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)

        if (maskAction != 0 && maskAction != EditorInfo.IME_ACTION_NONE && maskAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(maskAction)
        } else {
            ic.commitText("\n", 1)
        }
        lastAutoCorrection = null
        updateAutoCapitalize()
        updatePredictions()
    }

    private fun handleShiftClick() {
        shiftState = when (shiftState) {
            ShiftState.LOWERCASE -> ShiftState.SHIFT_ONCE
            ShiftState.SHIFT_ONCE -> ShiftState.LOWERCASE
            ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
        }
    }

    private fun handleShiftDoubleClick() {
        shiftState = if (shiftState == ShiftState.CAPS_LOCK) ShiftState.LOWERCASE else ShiftState.CAPS_LOCK
    }

    private fun handleLanguageSwitch() {
        val languages = currentSettings.typingLanguages.toList()
        if (languages.size > 1) {
            val currentIndex = languages.indexOf(currentSettings.activeTypingLanguage)
            val nextIndex = (currentIndex + 1) % languages.size
            val nextLang = languages[nextIndex]
            serviceScope.launch {
                preferencesRepository.updateActiveTypingLanguage(nextLang)
            }
        }
    }

    private fun handleSuggestionClicked(candidate: SuggestionCandidate) {
        val ic = currentInputConnection ?: return
        val currentWord = getCurrentWordBeforeCursor()
        if (currentWord.isNotEmpty()) {
            ic.deleteSurroundingText(currentWord.length, 0)
        }
        ic.commitText(candidate.word + " ", 1)
        lastAutoCorrection = null
        updateAutoCapitalize()
        updatePredictions()
    }

    private fun handlePaste() {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this)?.toString()
            if (!text.isNullOrEmpty()) {
                currentInputConnection?.commitText(text, 1)
                updatePredictions()
            }
        }
    }

    private fun handleCursorMoved(steps: Int) {
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0) ?: return
        val currentPos = extracted.selectionStart
        val newPos = (currentPos + steps).coerceIn(0, extracted.text.length)
        ic.setSelection(newPos, newPos)
        updatePredictions()
    }

    private fun updateAutoCapitalize() {
        if (!currentSettings.autoCapitalizeEnabled || shiftState == ShiftState.CAPS_LOCK) return
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(4, 0)?.toString() ?: ""

        // At start of field or preceded by sentence end (". ", "? ", "! ")
        if (textBefore.isEmpty() || textBefore.matches(".*[.?!]\\s+$".toRegex()) || textBefore.endsWith("\n")) {
            shiftState = ShiftState.SHIFT_ONCE
        } else {
            shiftState = ShiftState.LOWERCASE
        }
    }

    private fun updatePredictions() {
        if (!currentSettings.predictiveTextEnabled || isPasswordField) {
            suggestions = emptyList()
            return
        }

        val ic = currentInputConnection ?: return
        val currentWord = getCurrentWordBeforeCursor()
        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        val previousText = if (textBefore.length > currentWord.length) {
            textBefore.substring(0, textBefore.length - currentWord.length)
        } else ""

        serviceScope.launch {
            val results = predictionEngine.getSuggestions(
                currentWord = currentWord,
                previousText = previousText,
                language = currentSettings.activeTypingLanguage,
                typoCorrectionEnabled = currentSettings.typoCorrectionEnabled
            )
            suggestions = results
        }
    }

    private fun getCurrentWordBeforeCursor(): String {
        val ic = currentInputConnection ?: return ""
        val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: return ""
        val match = "\\b\\w+$".toRegex().find(textBefore)
        return match?.value ?: ""
    }

    private fun updateClipboardPreview() {
        if (isPasswordField) {
            clipboardPreview = null
            return
        }
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this)?.toString()
            clipboardPreview = if (!text.isNullOrBlank()) text else null
        } else {
            clipboardPreview = null
        }
    }
}
