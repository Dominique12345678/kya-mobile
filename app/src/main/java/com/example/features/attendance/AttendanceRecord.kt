package com.example.features.attendance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeToken: String,
    val employeeName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "IN" (Entrée) or "OUT" (Sortie)
    val isSynced: Boolean = false,
    val syncError: String? = null
)
