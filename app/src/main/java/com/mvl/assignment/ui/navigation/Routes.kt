package com.mvl.assignment.ui.navigation

import com.mvl.assignment.domain.model.Slot
import kotlinx.serialization.Serializable

// Type-safe routes. Args carry only identifiers; screen data is loaded by each screen's ViewModel.

/** Screen 1: map with A/B selection. */
@Serializable
data object MapRoute

/** Screen 2: place detail / nickname for [slot]. */
@Serializable
data class PlaceDetailRoute(val slot: Slot)

/** Screen 3: booking result for the server-issued [bookId]. */
@Serializable
data class BookResultRoute(val bookId: String)

/** Screen 4: bookings of the current month. */
@Serializable
data object HistoryRoute

/** Screen 5: previously resolved places to fill the empty [slot]. */
@Serializable
data class CachedPickerRoute(val slot: Slot)
