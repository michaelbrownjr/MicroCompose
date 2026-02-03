package com.example.microcompose.ui.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifiedUser(
    val username: String? = null,
    @SerialName("name") // Maps JSON key "full_name" to this variable
    @com.google.gson.annotations.SerializedName("name")
    val fullName: String? = null,
    val avatar: String? = null,
    val token: String? = null, // This is the permanent auth token
    @SerialName("has_site")
    @com.google.gson.annotations.SerializedName("has_site")
    val hasSite: Boolean? = null, // Optional fields based on React Native code
    @SerialName("default_site")
    @com.google.gson.annotations.SerializedName("default_site")
    val defaultSite: String? = null,
    @SerialName("is_premium")
    @com.google.gson.annotations.SerializedName("is_premium")
    val isPremium: Boolean? = null,
    @SerialName("is_using_ai")
    @com.google.gson.annotations.SerializedName("is_using_ai")
    val isUsingAi: Boolean? = null
    // Add other fields if needed based on the actual API response
)