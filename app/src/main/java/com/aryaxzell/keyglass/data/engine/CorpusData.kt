package com.aryaxzell.keyglass.data.engine

object CorpusData {
    // Top common English words with frequency scores (normalized 1..100)
    val ENGLISH_WORDS: List<Pair<String, Int>> = listOf(
        "the" to 100, "be" to 95, "to" to 95, "of" to 92, "and" to 92, "a" to 90, "in" to 88,
        "that" to 85, "have" to 82, "i" to 80, "it" to 80, "for" to 78, "not" to 76, "on" to 75,
        "with" to 74, "he" to 72, "as" to 70, "you" to 70, "do" to 68, "at" to 68, "this" to 66,
        "but" to 65, "his" to 64, "by" to 63, "from" to 62, "they" to 61, "we" to 60, "say" to 59,
        "her" to 58, "she" to 57, "or" to 56, "an" to 55, "will" to 54, "my" to 53, "one" to 52,
        "all" to 51, "would" to 50, "there" to 49, "their" to 48, "what" to 47, "so" to 46,
        "up" to 45, "out" to 44, "if" to 43, "about" to 42, "who" to 41, "get" to 40, "which" to 39,
        "go" to 38, "me" to 37, "when" to 36, "make" to 35, "can" to 34, "like" to 33, "time" to 32,
        "no" to 31, "just" to 30, "him" to 29, "know" to 28, "take" to 27, "people" to 26, "into" to 25,
        "year" to 24, "your" to 23, "good" to 22, "some" to 21, "could" to 20, "them" to 19, "see" to 18,
        "other" to 17, "than" to 16, "then" to 15, "now" to 14, "look" to 13, "only" to 12, "come" to 11,
        "its" to 10, "over" to 10, "think" to 10, "also" to 10, "back" to 10, "after" to 10, "use" to 10,
        "two" to 10, "how" to 10, "our" to 10, "work" to 10, "first" to 10, "well" to 10, "way" to 10,
        "even" to 10, "new" to 10, "want" to 10, "because" to 10, "any" to 10, "these" to 10, "give" to 10,
        "day" to 10, "most" to 10, "us" to 10, "great" to 9, "keyboard" to 9, "glass" to 9, "phone" to 9,
        "screen" to 8, "apple" to 8, "android" to 8, "design" to 8, "beautiful" to 8, "style" to 8,
        "clean" to 8, "simple" to 8, "hello" to 8, "world" to 8, "test" to 8, "message" to 8,
        "please" to 8, "thanks" to 8, "today" to 8, "tomorrow" to 8, "night" to 7, "morning" to 7,
        "happy" to 7, "love" to 7, "home" to 7, "friend" to 7, "system" to 7, "setting" to 7
    )

    // English Bigrams (prevWord -> list of next words with scores)
    val ENGLISH_BIGRAMS: Map<String, List<Pair<String, Int>>> = mapOf(
        "how" to listOf("are" to 95, "is" to 80, "to" to 70, "can" to 60, "do" to 55),
        "how are" to listOf("you" to 100, "things" to 60, "we" to 50),
        "i" to listOf("am" to 95, "have" to 90, "will" to 85, "think" to 80, "want" to 75, "love" to 70, "don't" to 65),
        "i am" to listOf("doing" to 80, "going" to 75, "here" to 70, "good" to 65),
        "thank" to listOf("you" to 100, "god" to 50),
        "thank you" to listOf("so" to 90, "very" to 85, "for" to 80),
        "good" to listOf("morning" to 90, "night" to 85, "job" to 80, "luck" to 75, "idea" to 70),
        "what" to listOf("is" to 95, "are" to 85, "do" to 80, "happened" to 60),
        "see" to listOf("you" to 95, "it" to 70, "what" to 60),
        "see you" to listOf("later" to 95, "tomorrow" to 90, "soon" to 85),
        "let" to listOf("me" to 95, "us" to 85),
        "let me" to listOf("know" to 100, "see" to 70, "check" to 65),
        "nice" to listOf("to" to 95),
        "nice to" to listOf("meet" to 100, "see" to 80),
        "take" to listOf("care" to 95, "it" to 80, "your" to 70),
        "the" to listOf("best" to 70, "keyboard" to 65, "new" to 60, "first" to 60, "world" to 55),
        "have" to listOf("a" to 95, "to" to 90, "been" to 80, "you" to 75),
        "have a" to listOf("great" to 95, "good" to 90, "nice" to 85, "wonderful" to 80),
        "where" to listOf("are" to 90, "is" to 85),
        "we" to listOf("are" to 90, "will" to 85, "can" to 80, "have" to 75)
    )

    // Top Indonesian words with frequency scores
    val INDONESIAN_WORDS: List<Pair<String, Int>> = listOf(
        "yang" to 100, "di" to 95, "dan" to 95, "ini" to 92, "itu" to 90, "dengan" to 88,
        "untuk" to 85, "tidak" to 85, "dari" to 82, "dalam" to 80, "akan" to 78, "pada" to 76,
        "juga" to 75, "saya" to 74, "ke" to 72, "karena" to 70, "ada" to 70, "bisa" to 68,
        "mereka" to 68, "lebih" to 66, "kami" to 65, "sudah" to 64, "atau" to 63, "oleh" to 62,
        "saat" to 61, "harus" to 60, "ia" to 59, "kita" to 58, "hanya" to 57, "sangat" to 56,
        "seperti" to 55, "anda" to 54, "banyak" to 53, "secara" to 52, "antara" to 51, "telah" to 50,
        "lain" to 49, "menjadi" to 48, "tentang" to 47, "sampai" to 46, "lagi" to 45, "kamu" to 44,
        "mau" to 43, "lagi" to 42, "hari" to 41, "semua" to 40, "bukan" to 39, "mana" to 38,
        "apa" to 37, "siapa" to 36, "kenapa" to 35, "bagaimana" to 34, "terima" to 33, "kasih" to 33,
        "selamat" to 32, "pagi" to 31, "malam" to 30, "siang" to 29, "sore" to 28, "baik" to 27,
        "saja" to 26, "terus" to 25, "nanti" to 24, "kemarin" to 23, "besok" to 22, "tahu" to 21,
        "mau" to 20, "punya" to 19, "buat" to 18, "kerja" to 17, "rumah" to 16, "teman" to 15,
        "keyboard" to 14, "kaca" to 13, "bagus" to 12, "cepat" to 11, "keren" to 10, "mantap" to 10
    )

    // Indonesian Bigrams
    val INDONESIAN_BIGRAMS: Map<String, List<Pair<String, Int>>> = mapOf(
        "terima" to listOf("kasih" to 100),
        "terima kasih" to listOf("banyak" to 95, "kembali" to 70),
        "selamat" to listOf("pagi" to 95, "malam" to 90, "siang" to 85, "sore" to 80, "datang" to 75),
        "apa" to listOf("kabar" to 95, "yang" to 80, "itu" to 70, "ini" to 65),
        "apa kabar" to listOf("kamu" to 90, "hari" to 80),
        "saya" to listOf("sedang" to 90, "mau" to 85, "bisa" to 80, "sudah" to 75, "tidak" to 70),
        "kamu" to listOf("sudah" to 90, "lagi" to 85, "mau" to 80, "bisa" to 75),
        "tidak" to listOf("bisa" to 90, "ada" to 85, "mau" to 80, "apa" to 75),
        "tidak apa" to listOf("apa" to 100),
        "sampai" to listOf("jumpa" to 95, "nanti" to 85, "besok" to 80),
        "bisa" to listOf("bantu" to 85, "minta" to 80, "tolong" to 80),
        "tolong" to listOf("bantu" to 90),
        "semoga" to listOf("sukses" to 90, "hari" to 85, "baik" to 80)
    )
}
