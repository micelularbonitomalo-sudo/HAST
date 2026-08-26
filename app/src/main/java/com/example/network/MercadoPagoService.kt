package com.example.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MercadoPagoService {
    @POST("checkout/preferences")
    suspend fun createPreference(
        @Header("Authorization") authorization: String,
        @Body request: MPPreferenceRequest
    ): MPPreferenceResponse
}
