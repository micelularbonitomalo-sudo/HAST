package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MPPreferenceRequest(
    val items: List<MPItem>,
    val back_urls: MPBackUrls,
    val auto_return: String = "approved",
    val external_reference: String? = null
)

@JsonClass(generateAdapter = true)
data class MPItem(
    val title: String,
    val quantity: Int,
    val currency_id: String = "MXN",
    val unit_price: Double
)

@JsonClass(generateAdapter = true)
data class MPBackUrls(
    val success: String,
    val failure: String,
    val pending: String
)

@JsonClass(generateAdapter = true)
data class MPPreferenceResponse(
    val id: String,
    val init_point: String,
    val sandbox_init_point: String
)
