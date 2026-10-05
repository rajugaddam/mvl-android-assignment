package com.mvl.assignment.ui.map

import com.mvl.assignment.domain.model.Coordinate

/**
 * Map screen settings, provided through DI (see ConfigModule) instead of being read from
 * BuildConfig or hardcoded in the screen.
 *
 * @param hasApiKey whether a Maps key was configured (only its presence, never the value).
 * @param fallbackTarget camera position until the device location is known.
 */
data class MapConfig(
    val hasApiKey: Boolean,
    val fallbackTarget: Coordinate,
    val zoom: Float,
)
