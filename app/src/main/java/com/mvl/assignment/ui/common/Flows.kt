package com.mvl.assignment.ui.common

import kotlinx.coroutines.flow.SharingStarted

private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * Sharing policy for screen state: keeps upstream flows alive for 5 s after the UI stops collecting,
 * so a rotation doesn't restart them, while a screen left in the background stops working.
 */
val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS)
