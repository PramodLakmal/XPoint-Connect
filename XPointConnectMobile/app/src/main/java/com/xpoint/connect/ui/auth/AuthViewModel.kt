/**
 * AuthViewModel.kt
 *
 * Purpose: Manages authentication operations and validation for login and registration Author:
 * XPoint Connect Development Team Date: September 27, 2025
 *
 * Description: This ViewModel handles user authentication logic including login and registration
 * processes. It provides validation for user input, manages API communication through the
 * AuthRepository, and exposes LiveData for UI observation. The ViewModel ensures proper separation
 * of concerns by handling business logic away from UI components.
 *
 * Key Features:
 * - EV Owner login with NIC and password validation
 * - EV Owner registration with comprehensive input validation
 * - Real-time validation error handling and reporting
 * - Asynchronous API communication management
 * - Reactive data binding through LiveData observables
 */
package com.xpoint.connect.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.EVOwnerLoginResponse
import com.xpoint.connect.data.model.RegisterEVOwnerRequest
import com.xpoint.connect.data.model.RegisterEVOwnerResponse
import com.xpoint.connect.data.repository.AuthRepository
import com.xpoint.connect.utils.Resource
import com.xpoint.connect.utils.ValidationUtils
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Resource<EVOwnerLoginResponse>>()
    val loginResult: LiveData<Resource<EVOwnerLoginResponse>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<RegisterEVOwnerResponse>>()
    val registerResult: LiveData<Resource<RegisterEVOwnerResponse>> = _registerResult

    private val _validationErrors = MutableLiveData<Map<String, String>>()
    val validationErrors: LiveData<Map<String, String>> = _validationErrors

    /**
     * Validates user credentials and initiates login process Performs client-side validation before
     * making API call to authenticate user
     * @param nic User's National Identity Card number
     * @param password User's password
     */
    fun login(nic: String, password: String) {
        val errors = mutableMapOf<String, String>()

        // Validate inputs
        if (nic.isBlank()) {
            errors["nic"] = "NIC is required"
        } else if (!ValidationUtils.isValidNIC(nic)) {
            errors["nic"] = "Please enter a valid NIC"
        }

        if (password.isBlank()) {
            errors["password"] = "Password is required"
        } else if (!ValidationUtils.isValidPassword(password)) {
            errors["password"] = "Password must be at least 6 characters"
        }

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }

        _validationErrors.value = emptyMap()
        _loginResult.value = Resource.Loading()

        viewModelScope.launch { _loginResult.value = authRepository.evOwnerLogin(nic, password) }
    }

    fun register(
            nic: String,
            firstName: String,
            lastName: String,
            email: String,
            phoneNumber: String,
            password: String,
            confirmPassword: String,
            address: String
    ) {
        val errors = mutableMapOf<String, String>()

        // Validate inputs
        if (nic.isBlank()) {
            errors["nic"] = "NIC is required"
        } else if (!ValidationUtils.isValidNIC(nic)) {
            errors["nic"] = "Please enter a valid NIC"
        }

        if (firstName.isBlank()) {
            errors["firstName"] = "First name is required"
        }

        if (lastName.isBlank()) {
            errors["lastName"] = "Last name is required"
        }

        if (email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!ValidationUtils.isValidEmail(email)) {
            errors["email"] = "Please enter a valid email address"
        }

        if (phoneNumber.isBlank()) {
            errors["phoneNumber"] = "Phone number is required"
        } else if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            errors["phoneNumber"] = "Please enter a valid phone number"
        }

        if (address.isBlank()) {
            errors["address"] = "Address is required"
        }

        if (password.isBlank()) {
            errors["password"] = "Password is required"
        } else if (!ValidationUtils.isValidPassword(password)) {
            errors["password"] = "Password must be at least 6 characters"
        }

        if (confirmPassword.isBlank()) {
            errors["confirmPassword"] = "Please confirm your password"
        } else if (password != confirmPassword) {
            errors["confirmPassword"] = "Passwords do not match"
        }

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            return
        }

        _validationErrors.value = emptyMap()
        _registerResult.value = Resource.Loading()

        val request =
                RegisterEVOwnerRequest(
                        nic = nic,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber,
                        password = password,
                        address = address
                )

        viewModelScope.launch { _registerResult.value = authRepository.registerEVOwner(request) }
    }

    fun clearErrors() {
        _validationErrors.value = emptyMap()
    }
}
