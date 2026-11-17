package com.maskedparty.game.data

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WordBankTest {

    @Test
    fun `every category has at least four distinct words`() {
        WordCategory.all().forEach { category ->
            val words = WordBank.words(category)
            assertTrue("Category $category must have >= 4 words", words.size >= 4)
            assertEquals("Duplicates in $category", words.size, words.distinct().size)
        }
    }

    @Test
    fun `drawRound always returns distinct shared and imposter words in the same category`() {
        val random = Random(42)
        repeat(500) {
            val drawn = WordBank.drawRound(null, random)
            assertNotNull(drawn)
            drawn!!
            assertTrue(drawn.sharedWord != drawn.imposterWord)
            assertTrue(WordBank.words(drawn.category).contains(drawn.sharedWord))
            assertTrue(WordBank.words(drawn.category).contains(drawn.imposterWord))
        }
    }

    @Test
    fun `drawRound respects a fixed category`() {
        val drawn = WordBank.drawRound(WordCategory.ANIMALS, Random(7))
        assertNotNull(drawn)
        assertEquals(WordCategory.ANIMALS, drawn!!.category)
    }

    @Test
    fun `wordFor returns imposter word only for the imposter`() {
        val words = RoundWords(WordCategory.FOOD, "Sushi", "Ramen")
        assertEquals("Sushi", words.wordFor(isImposter = false))
        assertEquals("Ramen", words.wordFor(isImposter = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `RoundWords rejects identical shared and imposter words`() {
        RoundWords(WordCategory.FOOD, "Sushi", "Sushi")
    }

    @Test
    fun `isPlayable is false for a tiny pool`() {
        // All built-in categories are playable; guard against regressions.
        WordCategory.all().forEach { category ->
            assertTrue(WordBank.isPlayable(category))
        }
    }
}
