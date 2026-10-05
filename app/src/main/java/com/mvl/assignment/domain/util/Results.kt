package com.mvl.assignment.domain.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching] but never swallows coroutine cancellation, so `mapLatest`/scope
 * cancellation keeps working through code that wraps failures into [Result].
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
