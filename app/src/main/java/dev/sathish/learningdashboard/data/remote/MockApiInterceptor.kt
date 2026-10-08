package dev.sathish.learningdashboard.data.remote

import android.content.res.AssetManager
import dev.sathish.learningdashboard.data.remote.dto.LoginRequest
import dev.sathish.learningdashboard.data.remote.dto.LoginResponse
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.FileNotFoundException
import java.net.UnknownHostException
import java.util.UUID

/**
 * Plays the role of the backend so the real Retrofit/OkHttp stack is exercised end-to-end.
 *
 * - Serves JSON from `assets/mock/`.
 * - Fails with an IOException when the device has no connectivity, exactly like a real
 *   request would, so the offline path is genuine (turn on airplane mode to see it).
 * - Adds latency so loading states are visible.
 *
 * Swapping to a real backend = remove this interceptor and change BASE_URL.
 */
class MockApiInterceptor(
    private val assets: AssetManager,
    private val networkMonitor: NetworkMonitor,
    private val json: Json,
    private val latencyMs: Long = 800,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!networkMonitor.isOnline()) throw UnknownHostException("No internet connection")
        Thread.sleep(latencyMs)

        val path = request.url.encodedPath
        val (code, body) = when {
            request.method == "POST" && path == "/auth/login" -> login(request)
            request.method == "GET" && path == "/courses" -> 200 to asset("mock/courses.json")
            request.method == "GET" && COURSE_DETAIL.matches(path) -> {
                val id = path.substringAfterLast('/')
                assetOrNull("mock/course_$id.json")?.let { 200 to it } ?: (404 to error("Course not found"))
            }
            else -> 404 to error("Not found")
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(body.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun login(request: Request): Pair<Int, String> {
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        val body = json.decodeFromString(LoginRequest.serializer(), buffer.readUtf8())
        return if (body.password == DEMO_PASSWORD) {
            200 to json.encodeToString(LoginResponse.serializer(), LoginResponse(token = "mock-${UUID.randomUUID()}"))
        } else {
            401 to error("Invalid credentials")
        }
    }

    private fun asset(name: String): String = assets.open(name).bufferedReader().use { it.readText() }

    private fun assetOrNull(name: String): String? = try {
        asset(name)
    } catch (e: FileNotFoundException) {
        null
    }

    private fun error(message: String) = """{"message":"$message"}"""

    companion object {
        const val BASE_URL = "https://api.learning.mock/"
        const val DEMO_PASSWORD = "password123"
        private val COURSE_DETAIL = Regex("^/courses/\\d+$")
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
