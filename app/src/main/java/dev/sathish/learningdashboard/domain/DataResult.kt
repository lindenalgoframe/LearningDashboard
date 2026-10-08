package dev.sathish.learningdashboard.domain

/** Result type returned by repositories. UI never sees raw exceptions. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

enum class DataError {
    NETWORK,
    UNAUTHORIZED,
    NOT_FOUND,
    SERVER,
    UNKNOWN,
}
