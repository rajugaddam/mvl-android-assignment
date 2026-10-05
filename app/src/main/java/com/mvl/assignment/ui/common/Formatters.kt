package com.mvl.assignment.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat

private const val MAX_PRICE_FRACTION_DIGITS = 2

/**
 * Price (a Double in the API) with the current locale's grouping: whole values show no decimals
 * (10000.0 → "10,000"), fractional ones up to two (12.5 → "12.5"). Recomposes if the locale changes.
 */
@Composable
fun formatPrice(price: Double): String {
    val locale = LocalConfiguration.current.locales[0]
    val format = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = MAX_PRICE_FRACTION_DIGITS
        }
    }
    return format.format(price)
}
