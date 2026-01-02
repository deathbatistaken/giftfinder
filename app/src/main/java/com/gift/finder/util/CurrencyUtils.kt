package com.gift.finder.util

/**
 * Utility for currency conversion and formatting.
 */
object CurrencyUtils {
    const val CURRENCY_TRY = "TRY"
    const val CURRENCY_USD = "USD"
    const val CURRENCY_EUR = "EUR"

    // Approximate exchange rates for a local-first experience
    private const val RATE_USD_TO_TRY = 30.0
    private const val RATE_USD_TO_EUR = 0.92

    /**
     * Formats a USD value to the selected currency.
     */
    /**
     * Formats a USD value to the selected currency.
     */
    fun formatPrice(usdValue: Double, currency: String): String {
        return when (currency) {
            CURRENCY_TRY -> "₺${(usdValue * RATE_USD_TO_TRY).toInt()}"
            CURRENCY_EUR -> "€${(usdValue * RATE_USD_TO_EUR).toInt()}"
            else -> "$${usdValue.toInt()}"
        }
    }

    /**
     * Formats a USD value (Int) to the selected currency.
     */
    fun formatPrice(usdValue: Int, currency: String): String = formatPrice(usdValue.toDouble(), currency)

    /**
     * Formats a budget range (min-max) to the selected currency.
     */
    fun formatRange(minUsd: Int, maxUsd: Int, currency: String): String {
        val min = formatPrice(minUsd, currency)
        val max = if (maxUsd == Int.MAX_VALUE) "+" else formatPrice(maxUsd, currency)
        
        return if (maxUsd == Int.MAX_VALUE) "$min$max" else "$min - $max"
    }

    /**
     * Gets the currency symbol.
     */
    fun getSymbol(currency: String): String {
        return when (currency) {
            CURRENCY_TRY -> "₺"
            CURRENCY_EUR -> "€"
            else -> "$"
        }
    }

    /**
     * Converts a value from a specific currency to USD.
     */
    fun convertToUsd(value: Double, fromCurrency: String): Double {
        return when (fromCurrency) {
            CURRENCY_TRY -> value / RATE_USD_TO_TRY
            CURRENCY_EUR -> value / RATE_USD_TO_EUR
            else -> value
        }
    }
}
