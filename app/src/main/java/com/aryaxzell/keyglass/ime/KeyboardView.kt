package com.aryaxzell.keyglass.ime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.data.datastore.FontSource
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.engine.SuggestionCandidate
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import com.aryaxzell.keyglass.ui.theme.KeyGlassKeyboardTheme
import com.aryaxzell.keyglass.ui.theme.KeyGlassTheme
import com.aryaxzell.keyglass.ui.theme.parseHexColor
import androidx.compose.foundation.border
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun KeyboardView(
    settings: KeyGlassSettings,
    isPasswordField: Boolean,
    shiftState: ShiftState,
    keyboardMode: KeyboardMode,
    enterActionLabel: String,
    suggestions: List<SuggestionCandidate>,
    clipboardPreview: String?,
    onCharTyped: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onShiftClick: () -> Unit,
    onShiftDoubleClick: () -> Unit,
    onModeChange: (KeyboardMode) -> Unit,
    onLanguageSwitch: () -> Unit,
    onSuggestionClicked: (SuggestionCandidate) -> Unit,
    onPasteClicked: () -> Unit,
    onCursorMoved: (Int) -> Unit,
    onKeyFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeyGlassKeyboardTheme(
        settings = settings
    ) {
        val colors = HIGTheme.colors
        val cornerRadius = settings.keyCornerRadiusDp.dp

        // Active popup bubble state (character, display coordinates)
        var popupChar by remember { mutableStateOf<String?>(null) }
        var popupOffset by remember { mutableStateOf(Offset.Zero) }

        // Accents popup state
        var activeAccentList by remember { mutableStateOf<List<String>?>(null) }
        var selectedAccentIndex by remember { mutableIntStateOf(0) }
        var accentAnchorOffset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            // Background wallpaper / theme renderer
            KeyboardBackgroundRenderer(settings = settings)

            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Suggestion Bar (if predictive text is enabled and not password)
                if (settings.predictiveTextEnabled && !isPasswordField && keyboardMode != KeyboardMode.EMOJI) {
                    SuggestionBar(
                        suggestions = suggestions,
                        clipboardPreview = clipboardPreview,
                        onSuggestionClicked = {
                            onKeyFeedback()
                            onSuggestionClicked(it)
                        },
                        onPasteClicked = {
                            onKeyFeedback()
                            onPasteClicked()
                        }
                    )
                } else {
                    // Small spacer for clean margin
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 2. Keyboard Body
                when (keyboardMode) {
                    KeyboardMode.ALPHA -> {
                        AlphaKeyboardLayout(
                            settings = settings,
                            shiftState = shiftState,
                            cornerRadius = cornerRadius,
                            enterLabel = enterActionLabel,
                            onCharTyped = onCharTyped,
                            onBackspace = onBackspace,
                            onSpace = onSpace,
                            onEnter = onEnter,
                            onShiftClick = onShiftClick,
                            onShiftDoubleClick = onShiftDoubleClick,
                            onSwitchToSymbols = { onModeChange(KeyboardMode.SYMBOLS_1) },
                            onSwitchToEmoji = { onModeChange(KeyboardMode.EMOJI) },
                            onLanguageSwitch = onLanguageSwitch,
                            onKeyFeedback = onKeyFeedback,
                            onCursorMoved = onCursorMoved,
                            onShowPopup = { char, offset ->
                                if (settings.keyPopupBubbleEnabled) {
                                    popupChar = char
                                    popupOffset = offset
                                }
                            },
                            onDismissPopup = { popupChar = null },
                            onShowAccents = { list, offset ->
                                activeAccentList = list
                                selectedAccentIndex = 0
                                accentAnchorOffset = offset
                            },
                            onAccentSelected = { char ->
                                onCharTyped(char)
                                activeAccentList = null
                            },
                            onDismissAccents = { activeAccentList = null }
                        )
                    }
                    KeyboardMode.SYMBOLS_1 -> {
                        SymbolsKeyboardLayout(
                            page = 1,
                            settings = settings,
                            cornerRadius = cornerRadius,
                            enterLabel = enterActionLabel,
                            onCharTyped = onCharTyped,
                            onBackspace = onBackspace,
                            onSpace = onSpace,
                            onEnter = onEnter,
                            onSwitchToAlpha = { onModeChange(KeyboardMode.ALPHA) },
                            onSwitchToPage2 = { onModeChange(KeyboardMode.SYMBOLS_2) },
                            onSwitchToEmoji = { onModeChange(KeyboardMode.EMOJI) },
                            onLanguageSwitch = onLanguageSwitch,
                            onKeyFeedback = onKeyFeedback,
                            onCursorMoved = onCursorMoved,
                            onShowPopup = { char, offset ->
                                if (settings.keyPopupBubbleEnabled) {
                                    popupChar = char
                                    popupOffset = offset
                                }
                            },
                            onDismissPopup = { popupChar = null }
                        )
                    }
                    KeyboardMode.SYMBOLS_2 -> {
                        SymbolsKeyboardLayout(
                            page = 2,
                            settings = settings,
                            cornerRadius = cornerRadius,
                            enterLabel = enterActionLabel,
                            onCharTyped = onCharTyped,
                            onBackspace = onBackspace,
                            onSpace = onSpace,
                            onEnter = onEnter,
                            onSwitchToAlpha = { onModeChange(KeyboardMode.ALPHA) },
                            onSwitchToPage2 = { onModeChange(KeyboardMode.SYMBOLS_1) },
                            onSwitchToEmoji = { onModeChange(KeyboardMode.EMOJI) },
                            onLanguageSwitch = onLanguageSwitch,
                            onKeyFeedback = onKeyFeedback,
                            onCursorMoved = onCursorMoved,
                            onShowPopup = { char, offset ->
                                if (settings.keyPopupBubbleEnabled) {
                                    popupChar = char
                                    popupOffset = offset
                                }
                            },
                            onDismissPopup = { popupChar = null }
                        )
                    }
                    KeyboardMode.EMOJI -> {
                        EmojiKeyboardLayout(
                            onEmojiSelected = { emoji ->
                                onKeyFeedback()
                                onCharTyped(emoji)
                            },
                            onBackspace = onBackspace,
                            onSwitchToAlpha = { onModeChange(KeyboardMode.ALPHA) },
                            onKeyFeedback = onKeyFeedback
                        )
                    }
                }
            }

            // 3. Popup Bubble Layer (Spring preview on press)
            popupChar?.let { char ->
                KeyPopupBubble(
                    char = char,
                    anchorOffset = popupOffset,
                    cornerRadius = cornerRadius
                )
            }

            // 4. Accent Variants Popup Layer
            activeAccentList?.let { list ->
                AccentPopupRow(
                    variants = list,
                    selectedIndex = selectedAccentIndex,
                    anchorOffset = accentAnchorOffset,
                    onSelect = { char ->
                        onCharTyped(char)
                        activeAccentList = null
                    },
                    onDismiss = { activeAccentList = null }
                )
            }
        }
    }
}

@Composable
private fun SuggestionBar(
    suggestions: List<SuggestionCandidate>,
    clipboardPreview: String?,
    onSuggestionClicked: (SuggestionCandidate) -> Unit,
    onPasteClicked: () -> Unit
) {
    val colors = HIGTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clipboard paste chip if available
            if (!clipboardPreview.isNullOrEmpty()) {
                val cleanPreview = clipboardPreview.replace("\n", " ").take(18)
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.accent.copy(alpha = 0.2f))
                        .clickable { onPasteClicked() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Paste: \"$cleanPreview\"",
                        style = HIGTheme.typography.caption1.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Suggestions (up to 3)
            suggestions.take(3).forEachIndexed { index, candidate ->
                if (index > 0 || !clipboardPreview.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(colors.separator)
                    )
                }

                val isCenterCandidate = index == 1 || (suggestions.size == 1 && index == 0)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSuggestionClicked(candidate) },
                    contentAlignment = Alignment.Center
                ) {
                    val displayText = if (candidate.isAutoCorrect) "\"${candidate.word}\"" else candidate.word
                    Text(
                        text = displayText,
                        style = HIGTheme.typography.body.copy(
                            fontWeight = if (isCenterCandidate || candidate.isAutoCorrect) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        ),
                        color = if (candidate.isAutoCorrect) colors.accent else colors.primaryLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlphaKeyboardLayout(
    settings: KeyGlassSettings,
    shiftState: ShiftState,
    cornerRadius: Dp,
    enterLabel: String,
    onCharTyped: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onShiftClick: () -> Unit,
    onShiftDoubleClick: () -> Unit,
    onSwitchToSymbols: () -> Unit,
    onSwitchToEmoji: () -> Unit,
    onLanguageSwitch: () -> Unit,
    onKeyFeedback: () -> Unit,
    onCursorMoved: (Int) -> Unit,
    onShowPopup: (String, Offset) -> Unit,
    onDismissPopup: () -> Unit,
    onShowAccents: (List<String>, Offset) -> Unit,
    onAccentSelected: (String) -> Unit,
    onDismissAccents: () -> Unit
) {
    val isShiftActive = shiftState != ShiftState.LOWERCASE
    val rowSpacing = 8.dp
    val keySpacing = 5.dp
    val keyHeight = 44.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Row 1: Q W E R T Y U I O P (10 keys)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            KeyboardLayouts.ALPHA_ROW_1.forEach { char ->
                val displayChar = if (isShiftActive) char.uppercase() else char
                KeyboardKey(
                    label = displayChar,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    longPressVariants = KeyboardLayouts.ACCENT_VARIANTS[char],
                    onTap = {
                        onKeyFeedback()
                        onCharTyped(displayChar)
                    },
                    onLongPress = { variants, offset ->
                        onKeyFeedback()
                        onShowAccents(variants, offset)
                    },
                    onShowPopup = onShowPopup,
                    onDismissPopup = onDismissPopup
                )
            }
        }

        // Row 2: A S D F G H J K L (9 keys, centered with padding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            KeyboardLayouts.ALPHA_ROW_2.forEach { char ->
                val displayChar = if (isShiftActive) char.uppercase() else char
                KeyboardKey(
                    label = displayChar,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    longPressVariants = KeyboardLayouts.ACCENT_VARIANTS[char],
                    onTap = {
                        onKeyFeedback()
                        onCharTyped(displayChar)
                    },
                    onLongPress = { variants, offset ->
                        onKeyFeedback()
                        onShowAccents(variants, offset)
                    },
                    onShowPopup = onShowPopup,
                    onDismissPopup = onDismissPopup
                )
            }
        }

        // Row 3: Shift, Z X C V B N M (7 keys), Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift Key (special dark)
            ShiftKey(
                shiftState = shiftState,
                modifier = Modifier.weight(1.3f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onShiftClick()
                },
                onDoubleClick = {
                    onKeyFeedback()
                    onShiftDoubleClick()
                }
            )

            // Letters Z X C V B N M
            KeyboardLayouts.ALPHA_ROW_3.forEach { char ->
                val displayChar = if (isShiftActive) char.uppercase() else char
                KeyboardKey(
                    label = displayChar,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    longPressVariants = KeyboardLayouts.ACCENT_VARIANTS[char],
                    onTap = {
                        onKeyFeedback()
                        onCharTyped(displayChar)
                    },
                    onLongPress = { variants, offset ->
                        onKeyFeedback()
                        onShowAccents(variants, offset)
                    },
                    onShowPopup = onShowPopup,
                    onDismissPopup = onDismissPopup
                )
            }

            // Backspace Key
            BackspaceKey(
                modifier = Modifier.weight(1.3f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onBackspace = {
                    onKeyFeedback()
                    onBackspace()
                }
            )
        }

        // Row 4: 123, Globe (if > 1 lang) / Emoji, Space Bar, Return
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 123 Switch Key
            FunctionKey(
                label = "123",
                modifier = Modifier.weight(1.2f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onSwitchToSymbols()
                }
            )

            // Multi-language / Globe Key if multiple languages enabled (PRD §4.1, §4.9)
            if (settings.typingLanguages.size > 1) {
                GlobeKey(
                    modifier = Modifier.weight(0.9f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    onClick = {
                        onKeyFeedback()
                        onLanguageSwitch()
                    }
                )
            }

            // Emoji Key
            EmojiKeyButton(
                modifier = Modifier.weight(0.9f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onSwitchToEmoji()
                }
            )

            // Space Bar (Trackpad capable!)
            SpaceBarKey(
                modifier = Modifier.weight(3.6f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                languageLabel = if (settings.activeTypingLanguage.startsWith("in", ignoreCase = true)) "Indonesia" else "space",
                sensitivity = settings.spaceBarCursorSensitivity,
                onTap = {
                    onKeyFeedback()
                    onSpace()
                },
                onCursorMoved = onCursorMoved
            )

            // Return / Enter Key
            ReturnKey(
                label = enterLabel,
                modifier = Modifier.weight(1.5f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onEnter()
                }
            )
        }
    }
}

@Composable
private fun SymbolsKeyboardLayout(
    page: Int,
    settings: KeyGlassSettings,
    cornerRadius: Dp,
    enterLabel: String,
    onCharTyped: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSwitchToAlpha: () -> Unit,
    onSwitchToPage2: () -> Unit,
    onSwitchToEmoji: () -> Unit,
    onLanguageSwitch: () -> Unit,
    onKeyFeedback: () -> Unit,
    onCursorMoved: (Int) -> Unit,
    onShowPopup: (String, Offset) -> Unit,
    onDismissPopup: () -> Unit
) {
    val rowSpacing = 8.dp
    val keySpacing = 5.dp
    val keyHeight = 44.dp

    val row1 = if (page == 1) KeyboardLayouts.SYMBOLS_1_ROW_1 else KeyboardLayouts.SYMBOLS_2_ROW_1
    val row2 = if (page == 1) KeyboardLayouts.SYMBOLS_1_ROW_2 else KeyboardLayouts.SYMBOLS_2_ROW_2
    val row3 = if (page == 1) KeyboardLayouts.SYMBOLS_1_ROW_3 else KeyboardLayouts.SYMBOLS_2_ROW_3
    val pageToggleLabel = if (page == 1) "#+=" else "123"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            row1.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    onTap = {
                        onKeyFeedback()
                        onCharTyped(char)
                    },
                    onShowPopup = onShowPopup,
                    onDismissPopup = onDismissPopup
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing)
        ) {
            row2.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    onTap = {
                        onKeyFeedback()
                        onCharTyped(char)
                    },
                    onShowPopup = onShowPopup,
                    onDismissPopup = onDismissPopup
                )
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page toggle (#+= or 123)
            FunctionKey(
                label = pageToggleLabel,
                modifier = Modifier.weight(1.4f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onSwitchToPage2()
                }
            )

            // Punctuation keys
            row3.forEach { char ->
                KeyboardKey(
                    label = char,
                    modifier = Modifier.weight(1f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    onTap = {
                        onKeyFeedback()
                        onCharTyped(char)
                    },
                    onShowPopup = onShowPopup,
                    onDismissPopup = onDismissPopup
                )
            }

            // Backspace
            BackspaceKey(
                modifier = Modifier.weight(1.4f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onBackspace = {
                    onKeyFeedback()
                    onBackspace()
                }
            )
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ABC switch key
            FunctionKey(
                label = "ABC",
                modifier = Modifier.weight(1.2f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onSwitchToAlpha()
                }
            )

            if (settings.typingLanguages.size > 1) {
                GlobeKey(
                    modifier = Modifier.weight(0.9f),
                    height = keyHeight,
                    cornerRadius = cornerRadius,
                    onClick = {
                        onKeyFeedback()
                        onLanguageSwitch()
                    }
                )
            }

            EmojiKeyButton(
                modifier = Modifier.weight(0.9f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onSwitchToEmoji()
                }
            )

            SpaceBarKey(
                modifier = Modifier.weight(3.6f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                languageLabel = if (settings.activeTypingLanguage.startsWith("in", ignoreCase = true)) "Indonesia" else "space",
                sensitivity = settings.spaceBarCursorSensitivity,
                onTap = {
                    onKeyFeedback()
                    onSpace()
                },
                onCursorMoved = onCursorMoved
            )

            ReturnKey(
                label = enterLabel,
                modifier = Modifier.weight(1.5f),
                height = keyHeight,
                cornerRadius = cornerRadius,
                onClick = {
                    onKeyFeedback()
                    onEnter()
                }
            )
        }
    }
}

@Composable
private fun EmojiKeyboardLayout(
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit,
    onSwitchToAlpha: () -> Unit,
    onKeyFeedback: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(EmojiCategory.SMILEYS) }
    val colors = HIGTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 8.dp)
    ) {
        // Emoji Grid
        val currentEmojis = EmojiData.EMOJI_MAP[selectedCategory] ?: emptyList()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 40.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp
                    )
                }
            }
        }

        // Bottom Category Bar & ABC / Backspace row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(colors.keyboardBackground),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ABC Switch
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable {
                        onKeyFeedback()
                        onSwitchToAlpha()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABC",
                    style = HIGTheme.typography.headline.copy(fontSize = 15.sp),
                    color = colors.primaryLabel
                )
            }

            // Categories horizontal bar
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(EmojiData.CATEGORIES) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.primaryLabel.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                onKeyFeedback()
                                selectedCategory = category
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.icon,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Backspace key
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable {
                        onKeyFeedback()
                        onBackspace()
                    },
                contentAlignment = Alignment.Center
            ) {
                BackspaceIcon(color = colors.primaryLabel)
            }
        }
    }
}

@Composable
private fun KeyboardKey(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    longPressVariants: List<String>? = null,
    onTap: () -> Unit,
    onLongPress: ((List<String>, Offset) -> Unit)? = null,
    onShowPopup: ((String, Offset) -> Unit)? = null,
    onDismissPopup: (() -> Unit)? = null
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "keyPressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) colors.keyPressedBackground else colors.keyLetterBackground,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "keyBg"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(label, longPressVariants) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        onShowPopup?.invoke(label, offset)
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            onDismissPopup?.invoke()
                        }
                    },
                    onTap = { onTap() },
                    onLongPress = { offset ->
                        if (longPressVariants != null && onLongPress != null) {
                            onLongPress(longPressVariants, offset)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = HIGTheme.typography.keyLabel,
            color = colors.primaryLabel,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FunctionKey(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onClick: () -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "funcPressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) colors.keyPressedBackground else colors.keyDarkBackground,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "funcKeyBg"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = HIGTheme.typography.headline.copy(fontSize = 15.sp),
            color = colors.primaryLabel
        )
    }
}

@Composable
private fun ShiftKey(
    shiftState: ShiftState,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }

    val isCaps = shiftState == ShiftState.CAPS_LOCK
    val isShifted = shiftState != ShiftState.LOWERCASE

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "shiftPressScale"
    )

    val bgColor = if (isShifted) {
        if (colors.isDark) Color.White else Color.Black
    } else {
        if (isPressed) colors.keyPressedBackground else colors.keyDarkBackground
    }
    val iconColor = if (isShifted) {
        if (colors.isDark) Color.Black else Color.White
    } else {
        colors.primaryLabel
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onDoubleTap = { onDoubleClick() },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ShiftArrowIcon(color = iconColor, isFilled = isShifted)
            if (isCaps) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .background(iconColor, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
private fun BackspaceKey(
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onBackspace: () -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var repeatJob by remember { mutableStateOf<Job?>(null) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "bkPressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) colors.keyPressedBackground else colors.keyDarkBackground,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "bkKeyBg"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onBackspace()
                        // Accelerating repeat job (PRD §4.5)
                        repeatJob = scope.launch {
                            delay(400) // initial hold threshold
                            var currentDelay = 150L
                            while (isActive && isPressed) {
                                onBackspace()
                                delay(currentDelay)
                                if (currentDelay > 35L) {
                                    currentDelay -= 15L
                                }
                            }
                        }
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            repeatJob?.cancel()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        BackspaceIcon(color = colors.primaryLabel)
    }
}

@Composable
private fun SpaceBarKey(
    languageLabel: String,
    sensitivity: Float,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onTap: () -> Unit,
    onCursorMoved: (Int) -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }
    var isTrackpadActive by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "spacePressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed || isTrackpadActive) colors.keyPressedBackground else colors.keyLetterBackground,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "spaceBg"
    )

    val stepPx = (20f / sensitivity).coerceAtLeast(6f)

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(sensitivity) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            isTrackpadActive = false
                            dragAccumulator = 0f
                        }
                    },
                    onTap = { onTap() }
                )
            }
            .pointerInput(sensitivity) {
                // Space bar trackpad cursor movement (PRD §4.6)
                detectDragGestures(
                    onDragStart = { isTrackpadActive = true },
                    onDragEnd = { isTrackpadActive = false; dragAccumulator = 0f },
                    onDragCancel = { isTrackpadActive = false; dragAccumulator = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount.x
                        if (Math.abs(dragAccumulator) >= stepPx) {
                            val steps = (dragAccumulator / stepPx).toInt()
                            if (steps != 0) {
                                onCursorMoved(steps)
                                dragAccumulator -= steps * stepPx
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = languageLabel,
            style = HIGTheme.typography.body.copy(fontSize = 15.sp),
            color = colors.secondaryLabel
        )
    }
}

@Composable
private fun ReturnKey(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onClick: () -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }

    // Check if this action is an accented action (Search, Send, Go, Done)
    val isPrimaryAction = label.equals("search", ignoreCase = true) ||
            label.equals("send", ignoreCase = true) ||
            label.equals("go", ignoreCase = true) ||
            label.equals("done", ignoreCase = true)

    val normalBg = if (isPrimaryAction) colors.accent else colors.keyDarkBackground
    val textCol = if (isPrimaryAction) Color.White else colors.primaryLabel

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "returnPressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) normalBg.copy(alpha = 0.75f) else normalBg,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "returnBg"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = HIGTheme.typography.headline.copy(fontSize = 15.sp),
            color = textCol
        )
    }
}

@Composable
private fun GlobeKey(
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onClick: () -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "globePressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) colors.keyPressedBackground else colors.keyDarkBackground,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "globeBg"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        GlobeIcon(color = colors.primaryLabel)
    }
}

@Composable
private fun EmojiKeyButton(
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    cornerRadius: Dp = 5.dp,
    onClick: () -> Unit
) {
    val colors = HIGTheme.colors
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
        label = "emojiPressScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) colors.keyPressedBackground else colors.keyDarkBackground,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "emojiBg"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = colors.keyShadow)
            .border(width = 1.dp, color = colors.keyBorder, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        EmojiKeyIcon(color = colors.primaryLabel)
    }
}

@Composable
private fun KeyPopupBubble(
    char: String,
    anchorOffset: Offset,
    cornerRadius: Dp
) {
    val colors = HIGTheme.colors

    Box(
        modifier = Modifier
            .offset { IntOffset(0, -60) }
            .size(width = 54.dp, height = 62.dp)
            .shadow(8.dp, RoundedCornerShape(10.dp))
            .background(colors.keyPressedBackground, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            style = HIGTheme.typography.title1,
            color = colors.primaryLabel
        )
    }
}

@Composable
private fun AccentPopupRow(
    variants: List<String>,
    selectedIndex: Int,
    anchorOffset: Offset,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = HIGTheme.colors

    Box(
        modifier = Modifier
            .offset { IntOffset(0, -56) }
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .background(colors.secondaryBackground, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            variants.forEachIndexed { index, char ->
                val isSelected = index == selectedIndex
                val animScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
                    label = "accentScale"
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(animScale)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) colors.accent else Color.Transparent)
                        .clickable { onSelect(char) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = HIGTheme.typography.title3,
                        color = if (isSelected) Color.White else colors.primaryLabel
                    )
                }
            }
        }
    }
}

// Icons for Shift, Backspace, Globe
@Composable
fun ShiftArrowIcon(
    color: Color,
    isFilled: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.55f)
            lineTo(w * 0.65f, h * 0.55f)
            lineTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.55f)
            lineTo(w * 0.15f, h * 0.55f)
            close()
        }
        if (isFilled) {
            drawPath(path = path, color = color)
        } else {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun BackspaceIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.8f)
            lineTo(w * 0.3f, h * 0.8f)
            lineTo(w * 0.1f, h * 0.5f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Inner X
        val xMin = w * 0.45f
        val xMax = w * 0.7f
        val yMin = h * 0.35f
        val yMax = h * 0.65f
        drawLine(color, Offset(xMin, yMin), Offset(xMax, yMax), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(xMax, yMin), Offset(xMin, yMax), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun GlobeIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = w * 0.42f
        val center = Offset(w * 0.5f, h * 0.5f)
        drawCircle(color = color, radius = r, center = center, style = Stroke(width = 1.6.dp.toPx()))
        // Horizontal line
        drawLine(color, Offset(center.x - r, center.y), Offset(center.x + r, center.y), strokeWidth = 1.5.dp.toPx())
        // Vertical ellipse curve
        drawOval(
            color = color,
            topLeft = Offset(center.x - r * 0.45f, center.y - r),
            size = Size(r * 0.9f, r * 2),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun EmojiKeyIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 19.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val r = w * 0.44f
        // Face outline
        drawCircle(color = color, radius = r, center = center, style = Stroke(width = 1.6.dp.toPx()))
        // Eyes
        drawCircle(color = color, radius = 1.3.dp.toPx(), center = Offset(w * 0.35f, h * 0.38f))
        drawCircle(color = color, radius = 1.3.dp.toPx(), center = Offset(w * 0.65f, h * 0.38f))
        // Smile curve
        val smile = Path().apply {
            moveTo(w * 0.3f, h * 0.58f)
            cubicTo(w * 0.36f, h * 0.76f, w * 0.64f, h * 0.76f, w * 0.7f, h * 0.58f)
        }
        drawPath(path = smile, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}
