package com.example.adaptive_offline_sync_data_ui.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class FieldRecordIn(
    @SerializedName("client_record_id") val clientRecordId: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("data") val data: Map<String, Any> = emptyMap(),
    @SerializedName("client_updated_at") val clientUpdatedAt: String? = null,
    @SerializedName("version") val version: Int = 1,
    @SerializedName("is_deleted") val isDeleted: Boolean = false
)

interface RecordsAPI {

    @POST("api/v1/records")
    suspend fun upsertRecord(
        @Body payload: FieldRecordIn
    ): Response<FieldRecordIn>

    @GET("api/v1/records")
    suspend fun getRecords(
        @Query("since") since: String? = null
    ): Response<List<FieldRecordIn>>

    @DELETE("api/v1/records/{client_record_id}")
    suspend fun deleteRecord(
        @Path("client_record_id") clientRecordId: String
    ): Response<Unit>
}