package com.mvl.assignment.domain.model

/** A booking as returned by the books API (never built from local request data). */
data class Book(
    val id: String,
    val a: Place,
    val b: Place,
    val price: Double,
)

data class BookingMonth(
    val year: Int,
    val month: Int,
) {
    init {
        require(month in 1..12) { "month out of range: $month" }
    }
}

data class MonthlyHistory(
    val month: BookingMonth,
    val books: List<Book>,
    val totalCount: Int,
    val totalPrice: Double,
)
