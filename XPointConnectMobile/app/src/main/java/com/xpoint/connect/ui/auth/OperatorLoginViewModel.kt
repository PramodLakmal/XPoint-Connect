package com.xpoint.connect.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpoint.connect.data.model.OperatorLoginResponse
import com.xpoint.connect.data.repository.AuthRepository
import com.xpoint.connect.utils.Resource
import kotlinx.coroutines.launch

class OperatorLoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Resource<OperatorLoginResponse>>()
    val loginResult: LiveData<Resource<OperatorLoginResponse>> = _loginResult

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginResult.value = Resource.Error("Username and password are required")
            return
        }
        _loginResult.value = Resource.Loading()
        viewModelScope.launch {
            _loginResult.value = authRepository.operatorLogin(username, password)
        }
    }
}


