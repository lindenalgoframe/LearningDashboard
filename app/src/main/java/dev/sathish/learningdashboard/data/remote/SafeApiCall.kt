package dev.sathish.learningdashboard.data.remote

import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** Maps transport/HTTP/parsing failures to [DataError]. Cancellation is always rethrown. */
suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    DataResult.Failure(DataError.NETWORK)
} catch (e: HttpException) {
    DataResult.Failure(
        when (e.code()) {
            401, 403 -> DataError.UNAUTHORIZED
            404 -> DataError.NOT_FOUND
            else -> DataError.SERVER
        },
    )
} catch (e: SerializationException) {
    DataResult.Failure(DataError.SERVER)
} catch (e: Exception) {
    DataResult.Failure(DataError.UNKNOWN)
}
