package com.mvl.assignment.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mvl.assignment.ui.book.BookResultScreen
import com.mvl.assignment.ui.detail.PlaceDetailScreen
import com.mvl.assignment.ui.history.HistoryScreen
import com.mvl.assignment.ui.map.MapScreen
import com.mvl.assignment.ui.picker.CachedPlacePickerScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = MapRoute,
        modifier = modifier,
    ) {
        composable<MapRoute> { entry ->
            MapScreen(
                onOpenPlace = { slot -> entry.ifResumed { navController.navigate(PlaceDetailRoute(slot)) } },
                onPickCachedPlace = { slot -> entry.ifResumed { navController.navigate(CachedPickerRoute(slot)) } },
                // Emitted once by the ViewModel after a successful booking (not a raw tap), so no resume guard.
                onOpenBooking = { bookId ->
                    navController.navigate(BookResultRoute(bookId)) { launchSingleTop = true }
                },
            )
        }

        composable<PlaceDetailRoute> { entry ->
            // The slot arg is read by PlaceDetailViewModel via SavedStateHandle.
            PlaceDetailScreen(
                onDone = { navController.popBackStackFrom(entry) },
            )
        }

        composable<BookResultRoute> { entry ->
            // The bookId arg is read by BookResultViewModel via SavedStateHandle.
            BookResultScreen(
                onNavigateBack = { navController.popBackStackFrom(entry) },
                // Result screen is replaced by History; back from History returns to the map.
                onOpenHistory = {
                    if (navController.currentBackStackEntry?.id == entry.id) {
                        navController.navigate(HistoryRoute) { popUpTo<MapRoute>() }
                    }
                },
            )
        }

        composable<HistoryRoute> { entry ->
            HistoryScreen(
                // History sits directly above the map, so popping returns there.
                onBackToMap = { navController.popBackStackFrom(entry) },
            )
        }

        composable<CachedPickerRoute> { entry ->
            // The slot arg is read by CachedPlacePickerViewModel via SavedStateHandle.
            CachedPlacePickerScreen(
                onDone = { navController.popBackStackFrom(entry) },
            )
        }
    }
}

/**
 * Ignores navigation requests from a screen that is no longer resumed (e.g. a double tap while
 * the transition runs), so the back stack can't be pushed twice or popped past the start.
 */
private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}

/** Pops [entry] only if it is still on top, so a repeated "done" event can't pop the screen below. */
private fun NavHostController.popBackStackFrom(entry: NavBackStackEntry) {
    if (currentBackStackEntry?.id == entry.id) popBackStack()
}
