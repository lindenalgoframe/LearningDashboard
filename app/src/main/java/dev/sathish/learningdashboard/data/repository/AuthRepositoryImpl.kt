package dev.sathish.learningdashboard.data.repository

import dev.sathish.learningdashboard.data.local.SessionStore
import dev.sathish.learningdashboard.data.remote.AuthApi
import dev.sathish.learningdashboard.data.remote.dto.LoginRequest
import dev.sathish.learningdashboard.data.remote.safeApiCall
import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.repository.AuthRepository
import dev.sathish.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
    private val courseRepository: CourseRepository,
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> =
        sessionStore.token.map { !it.isNullOrBlank() }.distinctUntilChanged()

    override suspend fun login(email: String, password: String): DataResult<Unit> = safeApiCall {
        val response = api.login(LoginRequest(email = email, password = password))
        sessionStore.saveToken(response.token)
    }

    /**
     * Clears the user's cached data so nothing leaks to the next account. The session is cleared
     * last: that emission triggers navigation, which destroys the calling ViewModel's scope.
     */
    override suspend fun logout() {
        courseRepository.clearCache()
        sessionStore.clear()
    }
}
