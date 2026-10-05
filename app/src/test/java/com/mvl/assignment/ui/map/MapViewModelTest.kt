package com.mvl.assignment.ui.map

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mvl.assignment.R
import com.mvl.assignment.domain.AddressFormatter
import com.mvl.assignment.domain.FakeAirQualityRepository
import com.mvl.assignment.domain.FakeBookRepository
import com.mvl.assignment.domain.FakeGeocodingRepository
import com.mvl.assignment.domain.FakeLocationRepository
import com.mvl.assignment.domain.FakePlaceCacheRepository
import com.mvl.assignment.domain.FakeSelectionRepository
import com.mvl.assignment.domain.GANGNAM
import com.mvl.assignment.domain.PlaceCacheKey
import com.mvl.assignment.domain.SEOUL
import com.mvl.assignment.domain.model.AdministrativeArea
import com.mvl.assignment.domain.model.GeocodedAddress
import com.mvl.assignment.domain.model.LocationUnavailableException
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.usecase.BookTripUseCase
import com.mvl.assignment.domain.usecase.GetAirQualityUseCase
import com.mvl.assignment.domain.usecase.GetCurrentLocationUseCase
import com.mvl.assignment.domain.usecase.ObserveSelectionUseCase
import com.mvl.assignment.domain.usecase.ResolvePlaceUseCase
import com.mvl.assignment.domain.usecase.SelectPlaceUseCase
import com.mvl.assignment.domain.usecase.SubmitSelectionStepUseCase
import com.mvl.assignment.ui.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val selection = FakeSelectionRepository()
    private val airQuality = FakeAirQualityRepository(Result.success(57))
    private val geocoding = FakeGeocodingRepository(
        Result.success(
            GeocodedAddress(
                administrative = listOf(
                    AdministrativeArea("Seocho District", order = 6),
                    AdministrativeArea("Yangjae 2(i)-dong", order = 8),
                ),
                city = null,
                locality = null,
            ),
        ),
    )
    private val books = FakeBookRepository()
    private val location = FakeLocationRepository()

    private fun viewModel(hasMapsKey: Boolean = true) = MapViewModel(
        savedStateHandle = SavedStateHandle(),
        config = MapConfig(hasApiKey = hasMapsKey, fallbackTarget = SEOUL, zoom = 16f),
        observeSelection = ObserveSelectionUseCase(selection),
        getAirQuality = GetAirQualityUseCase(airQuality),
        submitSelectionStep = SubmitSelectionStepUseCase(
            selectionRepository = selection,
            resolvePlace = ResolvePlaceUseCase(
                geocoding, airQuality, FakePlaceCacheRepository(), AddressFormatter(), PlaceCacheKey(),
            ),
            selectPlace = SelectPlaceUseCase(selection),
            bookTrip = BookTripUseCase(selection, books),
        ),
        getCurrentLocation = GetCurrentLocationUseCase(location),
    )

    /** uiState is WhileSubscribed, so keep a collector alive like the screen does. */
    private fun TestScope.subscribe(viewModel: MapViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `initial state asks to set A`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        val state = viewModel.uiState.value
        assertEquals(SelectionStep.SET_A, state.step)
        assertNull(state.aLabel)
        assertNull(state.bLabel)
        assertEquals(AqiUiState.Idle, state.aqi)
    }

    @Test
    fun `missing maps key is reported from the injected config`() = runTest {
        val viewModel = viewModel(hasMapsKey = false)
        subscribe(viewModel)

        assertTrue(viewModel.uiState.value.isMapsKeyMissing)
        assertFalse(viewModel().uiState.value.isMapsKeyMissing)
    }

    @Test
    fun `camera moves are debounced and only the latest position is queried`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onCameraIdle(SEOUL)
        advanceTimeBy(100)
        viewModel.onCameraIdle(GANGNAM)
        advanceUntilIdle()

        assertEquals(listOf(GANGNAM), airQuality.requests)
        assertEquals(AqiUiState.Value(57), viewModel.uiState.value.aqi)
    }

    @Test
    fun `same position is not re-queried but retry is`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onCameraIdle(SEOUL)
        advanceUntilIdle()
        viewModel.onCameraIdle(SEOUL)
        advanceUntilIdle()
        assertEquals(1, airQuality.requests.size)

        viewModel.onRetryAqi()
        advanceUntilIdle()
        assertEquals(2, airQuality.requests.size)
    }

    @Test
    fun `aqi failure shows the error state`() = runTest {
        airQuality.result = Result.failure(IOException("offline"))
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onCameraIdle(SEOUL)
        advanceUntilIdle()

        assertEquals(AqiUiState.Error, viewModel.uiState.value.aqi)
    }

    @Test
    fun `set A, set B, then book opens the result screen`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onPrimaryAction(SEOUL)
        advanceUntilIdle()
        assertEquals("Seocho District, Yangjae 2(i)-dong", viewModel.uiState.value.aLabel)
        assertEquals(SelectionStep.SET_B, viewModel.uiState.value.step)

        viewModel.onPrimaryAction(GANGNAM)
        advanceUntilIdle()
        assertEquals(SelectionStep.BOOK, viewModel.uiState.value.step)

        viewModel.events.test {
            viewModel.onPrimaryAction(GANGNAM)
            assertEquals(MapEvent.OpenBooking("1"), awaitItem())
        }
        assertEquals(1, books.bookRequests.size)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `double tap while resolving sends a single request`() = runTest {
        val viewModel = viewModel()
        subscribe(viewModel)

        // Second tap arrives before the first request has run.
        viewModel.onPrimaryAction(SEOUL)
        viewModel.onPrimaryAction(SEOUL)
        advanceUntilIdle()

        assertEquals(1, airQuality.requests.size)
        assertEquals(SelectionStep.SET_B, viewModel.uiState.value.step)
    }

    @Test
    fun `resolve failure shows a retryable message and keeps the step`() = runTest {
        geocoding.result = Result.failure(IOException("offline"))
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onPrimaryAction(SEOUL)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_network, state.message?.text?.resId)
        assertTrue(state.message?.retryable == true)
        assertFalse(state.isSubmitting)
        assertEquals(SelectionStep.SET_A, state.step)

        viewModel.onMessageDismissed()
        runCurrent()
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `granted permission centers the map once`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onLocationPermissionResult(true)
            assertEquals(MapEvent.MoveCamera(SEOUL), awaitItem())

            viewModel.onLocationPermissionResult(true) // e.g. after rotation
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `denied permission or no fix shows a non-retryable message`() = runTest {
        location.result = Result.failure(LocationUnavailableException())
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onLocationPermissionResult(false)
        runCurrent()

        val message = viewModel.uiState.value.message
        assertEquals(R.string.error_location_unavailable, message?.text?.resId)
        assertFalse(message?.retryable == true)
    }
}
