package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.MonthlyHistory
import com.mvl.assignment.domain.repository.BookRepository
import com.mvl.assignment.domain.repository.CurrentMonthProvider
import javax.inject.Inject

/** Bookings for the current month (`GET /books?year=&month=`) with totals. */
class GetMonthlyHistoryUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val currentMonthProvider: CurrentMonthProvider,
) {
    suspend operator fun invoke(): Result<MonthlyHistory> {
        val month = currentMonthProvider.currentMonth()
        return bookRepository.getMonthlyBooks(month).map { books ->
            MonthlyHistory(
                month = month,
                books = books,
                totalCount = books.size,
                totalPrice = books.sumOf { it.price },
            )
        }
    }
}
