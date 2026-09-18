package com.aryaxzell.keyglass.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "KeyGlass")
    object Onboarding : Screen("onboarding", "Setup")
    object Appearance : Screen("appearance", "Appearance")
    object KeyboardCustomization : Screen("keyboard_customization", "Keyboard Customization")
    object TypingBehavior : Screen("typing_behavior", "Typing Behavior")
    object Gestures : Screen("gestures", "Gestures")
    object General : Screen("general", "General")
    object PersonalDictionary : Screen("personal_dictionary", "Personal Dictionary")
    object About : Screen("about", "About KeyGlass")
    object Search : Screen("search", "Search Settings")
}
