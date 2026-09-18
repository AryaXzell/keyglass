package com.aryaxzell.keyglass.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.data.db.PersonalDictionaryRepository
import com.aryaxzell.keyglass.ui.components.higEdgeSwipeBack
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.localization.getStringsForLanguage
import com.aryaxzell.keyglass.ui.screens.AboutScreen
import com.aryaxzell.keyglass.ui.screens.AppearanceScreen
import com.aryaxzell.keyglass.ui.screens.DashboardScreen
import com.aryaxzell.keyglass.ui.screens.GeneralScreen
import com.aryaxzell.keyglass.ui.screens.GesturesScreen
import com.aryaxzell.keyglass.ui.screens.KeyboardCustomizationScreen
import com.aryaxzell.keyglass.ui.screens.OnboardingScreen
import com.aryaxzell.keyglass.ui.screens.PersonalDictionaryScreen
import com.aryaxzell.keyglass.ui.screens.SettingsSearchScreen
import com.aryaxzell.keyglass.ui.screens.TypingBehaviorScreen
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import com.aryaxzell.keyglass.ui.theme.KeyGlassTheme
import kotlinx.coroutines.launch

@Composable
fun KeyGlassApp(
    preferencesRepository: PreferencesRepository,
    personalDictionaryRepository: PersonalDictionaryRepository
) {
    val settings by preferencesRepository.settingsFlow.collectAsState(initial = KeyGlassSettings())
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    KeyGlassTheme(
        themeMode = settings.themeMode,
        accentColorHex = settings.accentColorHex,
        fontSource = settings.fontSource
    ) {
        val strings = getStringsForLanguage(settings.appLanguage)

        CompositionLocalProvider(LocalStrings provides strings) {
            val startRoute = if (settings.onboardingCompleted) Screen.Dashboard.route else Screen.Onboarding.route

        val springSpec = spring<androidx.compose.ui.unit.IntOffset>(
            dampingRatio = 0.88f,
            stiffness = 450f
        )
        val fadeSpec = spring<Float>(
            dampingRatio = 0.88f,
            stiffness = 450f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HIGTheme.colors.groupedBackground)
        ) {
            NavHost(
                navController = navController,
                startDestination = startRoute,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = springSpec
                    ) + fadeIn(animationSpec = fadeSpec)
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = springSpec
                    ) + fadeOut(animationSpec = fadeSpec, targetAlpha = 0.7f)
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = springSpec
                    ) + fadeIn(animationSpec = fadeSpec, initialAlpha = 0.7f)
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = springSpec
                    ) + fadeOut(animationSpec = fadeSpec)
                }
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            scope.launch {
                                preferencesRepository.setOnboardingCompleted(true)
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        settings = settings,
                        preferencesRepository = preferencesRepository,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.Appearance.route) {
                    AppearanceScreen(
                        settings = settings,
                        preferencesRepository = preferencesRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.KeyboardCustomization.route) {
                    KeyboardCustomizationScreen(
                        settings = settings,
                        preferencesRepository = preferencesRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.TypingBehavior.route) {
                    TypingBehaviorScreen(
                        settings = settings,
                        preferencesRepository = preferencesRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.Gestures.route) {
                    GesturesScreen(
                        settings = settings,
                        preferencesRepository = preferencesRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.General.route) {
                    GeneralScreen(
                        settings = settings,
                        preferencesRepository = preferencesRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.PersonalDictionary.route) {
                    PersonalDictionaryScreen(
                        repository = personalDictionaryRepository,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.About.route) {
                    AboutScreen(
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }

                composable(Screen.Search.route) {
                    SettingsSearchScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.higEdgeSwipeBack { navController.popBackStack() }
                    )
                }
            }
        }
        }
    }
}
