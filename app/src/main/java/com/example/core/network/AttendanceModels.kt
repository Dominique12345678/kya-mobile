package com.example.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttendancePunchRequest(
    val employee_token: String,
    val employee_name: String,
    val timestamp: Long,
    val punch_type: String // "IN" or "OUT"
)

@JsonClass(generateAdapter = true)
data class AttendancePunchResponse(
    val success: Boolean,
    val message: String,
    val id: String? = null
)
