package com.talha.depremtakip.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurkishTextTest {

    @Test
    fun `dotted capital i folds to lowercase i`() {
        assertEquals("izmir", TurkishText.fold("İzmir"))
    }

    @Test
    fun `dotless capital i folds to i for search`() {
        assertEquals("igdir", TurkishText.fold("IĞDIR"))
    }

    @Test
    fun `turkish special characters fold for matching`() {
        assertEquals("cankaya", TurkishText.fold("Çankaya"))
        assertEquals("sanliurfa", TurkishText.fold("Şanlıurfa"))
    }

    @Test
    fun `contains ignores case and accents`() {
        assertTrue(TurkishText.contains("İzmir Körfezi", "izmir"))
        assertTrue(TurkishText.contains("Çankaya", "cankaya"))
        assertFalse(TurkishText.contains("Ankara", "izmir"))
    }

    @Test
    fun `blank needle always matches`() {
        assertTrue(TurkishText.contains("Ankara", "   "))
        assertTrue(TurkishText.contains(null, ""))
    }
}
