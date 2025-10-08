package com.xpoint.connect.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.OperatorLoginResponse
import com.xpoint.connect.data.repository.AuthRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel for operator login with enhanced validation and error handling
 */
class OperatorLoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Resource<OperatorLoginResponse>>()
    val loginResult: LiveData<Resource<OperatorLoginResponse>> = _loginResult

    /**
     * Performs operator login with comprehensive validation
     */
    fun login(username: String, password: String) {
        // Clear any previous state
        _loginResult.value = Resource.Loading()

        // Validate inputs
        val validationError = validateInputs(username, password)
        if (validationError != null) {
            _loginResult.value = Resource.Error(validationError)
            return
        }

        // Perform login
        viewModelScope.launch {
            try {
                _loginResult.value = authRepository.operatorLogin(username, password)
            } catch (e: Exception) {
                _loginResult.value = Resource.Error(
                    e.message ?: "An unexpected error occurred during login"
                )
            }
        }
    }

    /**
     * Validates login inputs and returns error message if invalid
     */
    private fun validateInputs(username: String, password: String): String? {
        return when {
            username.isBlank() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            username.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(username).matches() -> 
                "Please enter a valid email address"
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    /**
     * Clears any current login state
     */
    fun clearState() {
        _loginResult.value = null
    }
}


