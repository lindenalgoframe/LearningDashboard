package dev.sathish.learningdashboard.domain

enum class ValidationError {
    EMAIL_BLANK,
    EMAIL_INVALID,
    PASSWORD_BLANK,
    PASSWORD_TOO_SHORT,
}

/** Pure Kotlin (no android.util.Patterns) so it is unit-testable on the JVM. */
object LoginValidator {
    private const val MIN_PASSWORD_LENGTH = 6
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): ValidationError? = when {
        email.isBlank() -> ValidationError.EMAIL_BLANK
        !EMAIL_REGEX.matches(email.trim()) -> ValidationError.EMAIL_INVALID
        else -> null
    }

    fun validatePassword(password: String): ValidationError? = when {
        password.isEmpty() -> ValidationError.PASSWORD_BLANK
        password.length < MIN_PASSWORD_LENGTH -> ValidationError.PASSWORD_TOO_SHORT
        else -> null
    }
}
