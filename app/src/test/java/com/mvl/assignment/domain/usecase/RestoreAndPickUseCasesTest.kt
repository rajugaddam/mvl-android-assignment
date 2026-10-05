package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.FakeAirQualityRepository
import com.mvl.assignment.domain.FakeSelectionRepository
import com.mvl.assignment.domain.GANGNAM
import com.mvl.assignment.domain.SEOUL
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.place
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RestoreAndPickUseCasesTest {

    private val selection = FakeSelectionRepository()
    private val airQuality = FakeAirQualityRepository(Result.success(99))

    private val book = Book(
        id = "bk-1",
        a = place("Home street", coordinate = SEOUL, aqi = 10, nickname = "home"),
        b = place("Office street", coordinate = GANGNAM, aqi = 20, nickname = "office"),
        price = 5_000.0,
    )

    @Test
    fun `restore re-fetches aqi and keeps names and nicknames`() = runTest {
        val restored = RestoreBookingUseCase(airQuality, selection)(book).getOrThrow()

        assertEquals(book.a.copy(aqi = 99), restored.a)
        assertEquals(book.b.copy(aqi = 99), restored.b)
        assertEquals(setOf(SEOUL, GANGNAM), airQuality.requests.toSet())
        assertEquals(SelectionStep.BOOK, selection.selection.value.step)
    }

    @Test
    fun `restore failure leaves the selection untouched`() = runTest {
        airQuality.result = Result.failure(IOException("offline"))

        val error = RestoreBookingUseCase(airQuality, selection)(book).exceptionOrNull()

        // Compared by type/message: coroutines may re-create exceptions crossing async boundaries.
        assertTrue(error is IOException)
        assertEquals("offline", error?.message)
        assertEquals(Selection(), selection.selection.value)
    }

    @Test
    fun `picking a cached place fills that exact slot`() {
        val cached = place("Cached place")

        val updated = SetSlotPlaceUseCase(selection)(Slot.B, cached)

        assertEquals(Selection(b = cached), updated)
        assertEquals(SelectionStep.SET_A, updated.step)
    }
}
