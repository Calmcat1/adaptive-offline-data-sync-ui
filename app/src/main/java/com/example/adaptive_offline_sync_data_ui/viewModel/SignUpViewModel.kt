package com.example.adaptive_offline_sync_data_ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adaptive_offline_sync_data_ui.data.remote.NetworkClient
import com.example.adaptive_offline_sync_data_ui.data.remote.UserCreate
import kotlinx.coroutines.launch

sealed interface SignUpUiState {
    object Idle : SignUpUiState
    object Loading : SignUpUiState
    object Success : SignUpUiState
    data class Error(val message: String) : SignUpUiState
}

class SignUpViewModel : ViewModel() {

    var uiState by mutableStateOf<SignUpUiState>(SignUpUiState.Idle)
        private set

    fun registerUser(email: String, fullName: String, pass: String) {
        if (email.isBlank() || fullName.isBlank() || pass.isBlank()) {
            uiState = SignUpUiState.Error("Please fill in all fields.")
            return
        }

        viewModelScope.launch {
            uiState = SignUpUiState.Loading
            try {
                val payload = UserCreate(
                    email = email.trim(),
                    fullName = fullName.trim(),
                    password = pass
                )
                val response = NetworkClient.authService.register(payload)

                if (response.isSuccessful && response.body() != null) {
                    uiState = SignUpUiState.Success
                } else if (response.code() == 409) {
                    uiState = SignUpUiState.Error("An account with this email already exists.")
                } else if (response.code() == 422) {
                    // Extract the exact field error returned by FastAPI/Pydantic or Django
                    val errorDetail = response.errorBody()?.string()
                    uiState = SignUpUiState.Error(
                        if (!errorDetail.isNullOrBlank()) "Invalid data: $errorDetail"
                        else "Invalid request data. Check password length or required fields."
                    )
                } else {
                    uiState = SignUpUiState.Error("Registration failed (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = SignUpUiState.Error(e.localizedMessage ?: "Unable to connect to server.")
            }
        }
    }

    fun resetState() {
        uiState = SignUpUiState.Idle
    }
}