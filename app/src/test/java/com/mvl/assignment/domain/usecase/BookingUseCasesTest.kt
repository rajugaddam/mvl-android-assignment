package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.FakeBookRepository
import com.mvl.assignment.domain.FakeSelectionRepository
import com.mvl.assignment.domain.FixedMonthProvider
import com.mvl.assignment.domain.GANGNAM
import com.mvl.assignment.domain.SEOUL
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.model.IncompleteSelectionException
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.place
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingUseCasesTest {

    private val a = place("A", coordinate = SEOUL)
    private val b = place("B", coordinate = GANGNAM)
    private val bookRepository = FakeBookRepository()

    @Test
    fun `book sends current A and B and returns the response`() = runTest {
        val selection = FakeSelectionRepository(Selection(a = a, b = b))
        bookRepository.bookResult = { from, to -> Result.success(Book("42", from, to, price = 3300.0)) }

        val book = BookTripUseCase(selection, bookRepository)().getOrThrow()

        assertEquals(listOf(a to b), bookRepository.bookRequests)
        assertEquals(3300.0, book.price, 0.0)
        assertEquals(book, GetBookUseCase(bookRepository)("42").getOrThrow())
    }

    @Test
    fun `book without B fails without calling the API`() = runTest {
        val selection = FakeSelectionRepository(Selection(a = a))

        val error = BookTripUseCase(selection, bookRepository)().exceptionOrNull()

        assertTrue(error is IncompleteSelectionException)
        assertTrue(bookRepository.bookRequests.isEmpty())
    }

    @Test
    fun `history queries the current month and computes totals`() = runTest {
        val month = BookingMonth(2026, 9)
        bookRepository.monthlyResult = Result.success(
            listOf(
                Book("1", a, b, price = 1200.0),
                Book("2", b, a, price = 3400.25),
                Book("3", a, a, price = 500.25),
            ),
        )

        val history = GetMonthlyHistoryUseCase(bookRepository, FixedMonthProvider(month))().getOrThrow()

        assertEquals(listOf(month), bookRepository.monthlyRequests)
        assertEquals(3, history.totalCount)
        assertEquals(5100.5, history.totalPrice, 0.0)
    }

    @Test
    fun `empty history has zero totals`() = runTest {
        val history = GetMonthlyHistoryUseCase(bookRepository, FixedMonthProvider(BookingMonth(2026, 1)))()
            .getOrThrow()

        assertEquals(0, history.totalCount)
        assertEquals(0.0, history.totalPrice, 0.0)
    }
}
