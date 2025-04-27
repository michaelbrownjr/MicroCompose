package com.example.microcompose.ui.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifiedUser(
    val username: String,
    @SerialName("full_name") // Maps JSON key "full_name" to this variable
    val fullName: String,
    val avatar: String,
    val token: String, // This is the permanent auth token
    @SerialName("has_site")
    val hasSite: Boolean? = null, // Optional fields based on React Native code
    @SerialName("default_site")
    val defaultSite: String? = null,
    @SerialName("is_premium")
    val isPremium: Boolean? = null,
    @SerialName("is_using_ai")
    val isUsingAi: Boolean? = null
    // Add other fields if needed based on the actual API response
)