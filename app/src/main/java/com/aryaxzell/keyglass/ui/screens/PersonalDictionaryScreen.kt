package com.aryaxzell.keyglass.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.data.db.PersonalDictionaryRepository
import com.aryaxzell.keyglass.data.db.PersonalWord
import com.aryaxzell.keyglass.ui.components.HIGBookIcon
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGDialog
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.components.HIGTrashIcon
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch

@Composable
fun PersonalDictionaryScreen(
    repository: PersonalDictionaryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val words by repository.allWords.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    val collapseProgress = if (listState.firstVisibleItemIndex > 0) 1f else (listState.firstVisibleItemScrollOffset / 120f).coerceIn(0f, 1f)

    var showAddDialog by remember { mutableStateOf(false) }
    var newWordText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.personalDictionaryTitle,
            largeTitle = true,
            collapseProgress = collapseProgress,
            onBackClick = onBack,
            trailingAction = {
                var isAddPressed by remember { mutableStateOf(false) }
                val addScale by animateFloatAsState(
                    targetValue = if (isAddPressed) 0.92f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "addScale"
                )
                Text(
                    text = strings.addWordBtn,
                    style = HIGTheme.typography.body,
                    color = colors.accent,
                    modifier = Modifier
                        .scale(addScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isAddPressed = true
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        isAddPressed = false
                                    }
                                },
                                onTap = {
                                    newWordText = ""
                                    showAddDialog = true
                                }
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        )

        if (words.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    HIGBookIcon(color = colors.accent, size = 52.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.emptyDictionaryTitle,
                        style = HIGTheme.typography.title2,
                        color = colors.primaryLabel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.emptyDictionaryDesc,
                        style = HIGTheme.typography.footnote,
                        color = colors.secondaryLabel,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    HIGButton(
                        text = strings.addCustomWordTitle,
                        onClick = {
                            newWordText = ""
                            showAddDialog = true
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp)
            ) {
                item {
                    HIGGroupedSection(
                        header = "${words.size} ${strings.whitelistedWordsHeader}",
                        footer = strings.dictionaryFooter,
                        staggerIndex = 0
                    ) {
                        words.forEachIndexed { index, item ->
                            HIGRow(
                                title = item.word,
                                showDivider = index < words.size - 1,
                                trailingAccessory = {
                                    var isTrashPressed by remember { mutableStateOf(false) }
                                    val trashScale by animateFloatAsState(
                                        targetValue = if (isTrashPressed) 0.82f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                                        label = "trashScale"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .scale(trashScale)
                                            .clip(CircleShape)
                                            .pointerInput(item.id) {
                                                detectTapGestures(
                                                    onPress = {
                                                        isTrashPressed = true
                                                        try {
                                                            tryAwaitRelease()
                                                        } finally {
                                                            isTrashPressed = false
                                                        }
                                                    },
                                                    onTap = {
                                                        scope.launch { repository.deleteWordById(item.id) }
                                                    }
                                                )
                                             }
                                            .padding(6.dp)
                                    ) {
                                        HIGTrashIcon(color = colors.destructive, size = 16.dp)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    HIGDialog(
        show = showAddDialog,
        onDismissRequest = { showAddDialog = false }
    ) {
        Column {
            Text(
                text = strings.addCustomWordTitle,
                style = HIGTheme.typography.headline,
                color = colors.primaryLabel
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.enterWordPrompt,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = newWordText,
                    onValueChange = { newWordText = it },
                    singleLine = true,
                    textStyle = HIGTheme.typography.body.copy(color = colors.primaryLabel),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HIGButton(
                text = strings.saveWordBtn,
                enabled = newWordText.isNotBlank(),
                onClick = {
                    scope.launch {
                        repository.addWord(newWordText.trim())
                        showAddDialog = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
