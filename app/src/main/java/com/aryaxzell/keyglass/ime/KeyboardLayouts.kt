package com.aryaxzell.keyglass.ime

enum class ShiftState {
    LOWERCASE,
    SHIFT_ONCE,
    CAPS_LOCK
}

enum class KeyboardMode {
    ALPHA,
    SYMBOLS_1,
    SYMBOLS_2,
    EMOJI
}

object KeyboardLayouts {
    // Row 1: Q W E R T Y U I O P (10 keys)
    val ALPHA_ROW_1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")

    // Row 2: A S D F G H J K L (9 keys, centered)
    val ALPHA_ROW_2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")

    // Row 3: Z X C V B N M (7 keys between Shift and Backspace)
    val ALPHA_ROW_3 = listOf("z", "x", "c", "v", "b", "n", "m")

    // Symbols Page 1 (PRD §4.4: numbers 0-9 plus - / : ; ( ) $ & @ " and #+= key, plus . , ? ! ')
    val SYMBOLS_1_ROW_1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val SYMBOLS_1_ROW_2 = listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\"")
    val SYMBOLS_1_ROW_3 = listOf(".", ",", "?", "!", "'")

    // Symbols Page 2 (PRD §4.4: [ ] { } # % ^ * + = _ \ | ~ < > and 123 key)
    val SYMBOLS_2_ROW_1 = listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "=")
    val SYMBOLS_2_ROW_2 = listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "•")
    val SYMBOLS_2_ROW_3 = listOf(".", ",", "?", "!", "'")

    // Accented variants mapping (PRD §4.7)
    val ACCENT_VARIANTS: Map<String, List<String>> = mapOf(
        "e" to listOf("é", "è", "ê", "ë", "ē", "ė", "ę"),
        "a" to listOf("á", "à", "â", "ä", "æ", "ã", "å", "ā"),
        "i" to listOf("í", "ì", "î", "ï", "ī", "į"),
        "o" to listOf("ó", "ò", "ô", "ö", "õ", "œ", "ø", "ō"),
        "u" to listOf("ú", "ù", "û", "ü", "ū"),
        "c" to listOf("ç", "ć", "č"),
        "n" to listOf("ñ", "ń"),
        "s" to listOf("ß", "ś", "š"),
        "y" to listOf("ÿ", "ý"),
        "z" to listOf("ž", "ź", "ż"),
        "d" to listOf("ð"),
        "l" to listOf("ł"),
        "$" to listOf("$", "€", "£", "¥", "₩", "₽", "¢"),
        "?" to listOf("?", "¿"),
        "!" to listOf("!", "¡"),
        "0" to listOf("0", "°")
    )
}
