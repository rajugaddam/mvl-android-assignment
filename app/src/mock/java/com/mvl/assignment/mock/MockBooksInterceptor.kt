package com.mvl.assignment.mock

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

/** Short-circuits books requests to [MockBooksServer]; nothing reaches the network. */
class MockBooksInterceptor(
    private val server: MockBooksServer,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val body = request.body?.let { requestBody ->
            Buffer().also(requestBody::writeTo).readUtf8()
        }

        if (chain.call().isCanceled()) throw IOException("Canceled")

        val url = request.url
        val response = server.handle(
            MockHttpRequest(
                method = request.method,
                pathSegments = url.pathSegments.filter { it.isNotEmpty() },
                query = url.queryParameterNames.associateWith { url.queryParameter(it) },
                body = body,
            ),
        )

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(response.code)
            .message(response.message)
            .body(response.body.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
