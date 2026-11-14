package com.example.adaptive_offline_sync_data_ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkClient
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val token: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun performLogin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            uiState = LoginUiState.Error("Please enter both email and password.")
            return
        }

        viewModelScope.launch {
            uiState = LoginUiState.Loading
            try {
                // Network call targeting POST /auth/login with form-urlencoded fields
                val response = NetworkClient.authService.login(email.trim(), pass)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.accessToken
                    uiState = LoginUiState.Success(token)
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Incorrect email or password."
                        422 -> "Invalid input format."
                        else -> "Login failed (${response.code()})"
                    }
                    uiState = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error(e.localizedMessage ?: "Unable to connect to server.")
            }
        }
    }

    fun resetState() {
        uiState = LoginUiState.Idle
    }
}