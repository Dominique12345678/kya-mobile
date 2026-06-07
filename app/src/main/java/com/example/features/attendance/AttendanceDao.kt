package com.example.features.attendance

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedRecords(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Query("UPDATE attendance_records SET isSynced = 1, syncError = null WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("UPDATE attendance_records SET syncError = :error WHERE id = :id")
    suspend fun setSyncError(id: Int, error: String)

    @Query("DELETE FROM attendance_records")
    suspend fun clearHistory()
}
