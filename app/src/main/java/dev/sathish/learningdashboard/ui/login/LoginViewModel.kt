package dev.sathish.learningdashboard.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.LoginValidator
import dev.sathish.learningdashboard.domain.ValidationError
import dev.sathish.learningdashboard.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val loginError: DataError? = null,
)

/**
 * Navigation after a successful login is not driven from here: the session store emits
 * "logged in" and the nav host reacts (see AppNavHost). One source of truth for auth state.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, loginError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, loginError = null) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = LoginValidator.validateEmail(state.email)
        val passwordError = LoginValidator.validatePassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, loginError = null) }
        viewModelScope.launch {
            val result = authRepository.login(state.email.trim(), state.password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loginError = (result as? DataResult.Failure)?.error,
                )
            }
        }
    }
}
