package com.example.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FastApiEdgeService {
    @POST("attendance/punch")
    suspend fun postPunch(
        @Body request: AttendancePunchRequest
    ): Response<AttendancePunchResponse>
}
