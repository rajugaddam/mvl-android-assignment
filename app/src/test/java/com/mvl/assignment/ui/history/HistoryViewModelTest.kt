package com.mvl.assignment.ui.history

import app.cash.turbine.test
import com.mvl.assignment.R
import com.mvl.assignment.domain.FakeAirQualityRepository
import com.mvl.assignment.domain.FakeBookRepository
import com.mvl.assignment.domain.FakeSelectionRepository
import com.mvl.assignment.domain.FixedMonthProvider
import com.mvl.assignment.domain.GANGNAM
import com.mvl.assignment.domain.SEOUL
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.place
import com.mvl.assignment.domain.usecase.GetMonthlyHistoryUseCase
import com.mvl.assignment.domain.usecase.RestoreBookingUseCase
import com.mvl.assignment.ui.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val month = BookingMonth(2026, 9)
    private val bookings = listOf(
        Book("1", place("A1", SEOUL), place("B1", GANGNAM), price = 4_000.0),
        Book("2", place("A2", GANGNAM), place("B2", SEOUL), price = 6_500.5),
    )
    private val books = FakeBookRepository().apply { monthlyResult = Result.success(bookings) }
    private val airQuality = FakeAirQualityRepository(Result.success(33))
    private val selection = FakeSelectionRepository()

    private fun viewModel() = HistoryViewModel(
        getMonthlyHistory = GetMonthlyHistoryUseCase(books, FixedMonthProvider(month)),
        restoreBooking = RestoreBookingUseCase(airQuality, selection),
    )

    @Test
    fun `loads the current month with totals`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val history = (viewModel.uiState.value as HistoryUiState.Loaded).history
        assertEquals(listOf(month), books.monthlyRequests)
        assertEquals(2, history.totalCount)
        assertEquals(10_500.5, history.totalPrice, 0.0)
        assertEquals(bookings, history.books)
    }

    @Test
    fun `error then retry succeeds`() = runTest {
        books.monthlyResult = Result.failure(IOException("offline"))
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(R.string.error_network, (viewModel.uiState.value as HistoryUiState.Error).message.resId)

        books.monthlyResult = Result.success(bookings)
        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(2, (viewModel.uiState.value as HistoryUiState.Loaded).history.totalCount)
    }

    @Test
    fun `tapping a booking restores A and B with fresh aqi and returns to the map`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onBookClick(bookings.first())
            assertEquals(HistoryEvent.BackToMap, awaitItem())
        }
        val restored = selection.selection.value
        assertEquals(SelectionStep.BOOK, restored.step)
        assertEquals(33, restored.a?.aqi)
        assertEquals("A1", restored.a?.name)
    }

    @Test
    fun `restore failure shows a message and keeps the list`() = runTest {
        airQuality.result = Result.failure(IOException("offline"))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onBookClick(bookings.first())
        advanceUntilIdle()

        val state = viewModel.uiState.value as HistoryUiState.Loaded
        assertFalse(state.isRestoring)
        assertEquals(R.string.error_network, state.restoreError?.resId)
        assertEquals(SelectionStep.SET_A, selection.selection.value.step)
    }
}
