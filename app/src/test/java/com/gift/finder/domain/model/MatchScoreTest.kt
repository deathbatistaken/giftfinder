package com.gift.finder.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchScoreTest {

    @Test
    fun `calculateMatchScore with matching interests should return high score`() {
        val category = GiftCategory(
            id = "test",
            title = "Test Category",
            description = "Test Description",
            emoji = "🧪",
            interestTags = listOf("gaming", "technology"),
            styleTags = listOf(GiftStyle.TECH, GiftStyle.FUN),
            budgetRange = BudgetRange.MEDIUM,
            seasons = listOf(Season.ALL),
            storeType = StoreType.AMAZON,
            searchQuery = "test"
        )

        val score = category.calculateMatchScore(
            personInterests = listOf("Gaming", "Tech"),
            selectedStyle = GiftStyle.TECH,
            selectedBudget = BudgetRange.MEDIUM
        )

        // Base 50 + 20 (interests) + 20 (interests) + 10 (style) = 100
        assertEquals(100, score)
    }

    @Test
    fun `calculateMatchScore with no matches should return base score`() {
        val category = GiftCategory(
            id = "test",
            title = "Test Category",
            description = "Test Description",
            emoji = "🧪",
            interestTags = listOf("sports"),
            styleTags = listOf(GiftStyle.PRACTICAL),
            budgetRange = BudgetRange.LOW,
            seasons = listOf(Season.ALL),
            storeType = StoreType.AMAZON,
            searchQuery = "test"
        )

        val score = category.calculateMatchScore(
            personInterests = listOf("Gaming"),
            selectedStyle = GiftStyle.TECH,
            selectedBudget = BudgetRange.HIGH
        )

        // Base 50 - 5 (budget mismatch) = 45
        assertEquals(45, score)
    }

    @Test
    fun `calculateMatchScore with budget mismatch should penalize score`() {
        val category = GiftCategory(
            id = "test",
            title = "Test",
            description = "Test",
            emoji = "🧪",
            interestTags = listOf("gaming"),
            styleTags = listOf(GiftStyle.FUN),
            budgetRange = BudgetRange.HIGH,
            seasons = listOf(Season.ALL),
            storeType = StoreType.AMAZON,
            searchQuery = "test"
        )

        val score = category.calculateMatchScore(
            personInterests = listOf("gaming"),
            selectedStyle = GiftStyle.FUN,
            selectedBudget = BudgetRange.LOW
        )

        // Base 50 + 20 (interest) + 10 (style) - 5 (budget mismatch) = 75
        assertEquals(75, score)
    }
}
