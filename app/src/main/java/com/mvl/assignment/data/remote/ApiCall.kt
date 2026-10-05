package com.mvl.assignment.data.remote

import com.mvl.assignment.domain.model.RemoteServiceException
import com.mvl.assignment.domain.util.runCatchingCancellable
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/** Runs a remote call, translating Retrofit/serialization failures into domain errors. */
inline fun <T> apiCall(block: () -> T): Result<T> = runCatchingCancellable {
    try {
        block()
    } catch (e: HttpException) {
        throw RemoteServiceException("HTTP ${e.code()}")
    } catch (e: SerializationException) {
        throw RemoteServiceException("Unreadable response: ${e.message}")
    }
}
