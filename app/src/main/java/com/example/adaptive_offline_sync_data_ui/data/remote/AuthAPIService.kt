package com.example.adaptive_offline_sync_data_ui.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


// UserCreate
data class UserCreate(
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("password") val password: String
)


// UserData
data class UserData(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("created_at") val createdAt: String
)


// registerResponse
data class RegisterResponse(
    val user: UserData
)




// TokenOut dataclass
data class TokenOut(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
    @SerializedName("expires_in_minutes") val expiresInMinutes: Int
)

interface AuthApiService {
    // Remote registration function
    @POST("api/v1/auth/register")
    suspend fun register(
        @Body payload: UserCreate
    ): Response<RegisterResponse>

    // Remote login function (OAuth2 form)
    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") pass: String
    ): Response<TokenOut>
}