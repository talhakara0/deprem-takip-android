package com.talha.depremtakip.core

import java.util.Locale

object TurkishText {

    fun fold(value: String?): String = (value ?: "")
        .replace('İ', 'i')
        .replace('I', 'i')
        .replace('ı', 'i')
        .replace('Ş', 's')
        .replace('ş', 's')
        .replace('Ğ', 'g')
        .replace('ğ', 'g')
        .replace('Ü', 'u')
        .replace('ü', 'u')
        .replace('Ö', 'o')
        .replace('ö', 'o')
        .replace('Ç', 'c')
        .replace('ç', 'c')
        .lowercase(Locale.ROOT)
        .trim()

    fun contains(haystack: String?, needle: String): Boolean {
        val normalizedNeedle = fold(needle)
        if (normalizedNeedle.isEmpty()) return true
        return fold(haystack).contains(normalizedNeedle)
    }
}
