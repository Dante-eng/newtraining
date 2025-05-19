package com.example.newtraining.util

fun convertArabicNumberToEnglish(input: String): String {
    val arabicNumbers = mapOf(
        '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
        '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
    )
    return input.map { arabicNumbers[it] ?: it }.joinToString("")
} 