package com.mvl.assignment.data.remote.interceptor

import com.mvl.assignment.di.WaqiToken
import com.mvl.assignment.domain.model.MissingApiKeyException
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Appends the waqi `token` query parameter; the value comes from BuildConfig via DI, never from source. */
class WaqiTokenInterceptor @Inject constructor(
    @param:WaqiToken private val token: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (token.isBlank()) throw MissingApiKeyException(TOKEN_KEY_NAME)
        val request = chain.request()
        val url = request.url.newBuilder()
            .addQueryParameter(TOKEN_QUERY_PARAM, token)
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }

    private companion object {
        const val TOKEN_QUERY_PARAM = "token"
        const val TOKEN_KEY_NAME = "WAQI_TOKEN"
    }
}
