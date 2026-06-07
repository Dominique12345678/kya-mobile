package com.example.features.attendance

import com.example.core.network.AttendancePunchRequest
import com.example.core.network.RetrofitClientProvider
import kotlinx.coroutines.flow.Flow
import java.net.ConnectException
import java.net.SocketTimeoutException

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()

    suspend fun insertPunchRecord(
        type: String,
        token: String,
        name: String,
        baseUrl: String
    ): AttendanceRecord {
        // 1. Create offline-first local record
        var localRecord = AttendanceRecord(
            employeeToken = token,
            employeeName = name,
            type = type,
            isSynced = false
        )
        
        // Save to Room DB
        val insertedId = attendanceDao.insertRecord(localRecord)
        localRecord = localRecord.copy(id = insertedId.toInt())

        // 2. Try to sync immediately to FastAPI edge server
        val service = RetrofitClientProvider.createService(baseUrl)
        if (service != null) {
            try {
                val request = AttendancePunchRequest(
                    employee_token = token,
                    employee_name = name,
                    timestamp = localRecord.timestamp,
                    punch_type = type
                )
                val response = service.postPunch(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    attendanceDao.markAsSynced(localRecord.id)
                    return localRecord.copy(isSynced = true)
                } else {
                    val errMsg = response.errorBody()?.string() ?: response.message() ?: "API Error"
                    attendanceDao.setSyncError(localRecord.id, errMsg)
                    return localRecord.copy(syncError = errMsg)
                }
            } catch (e: SocketTimeoutException) {
                attendanceDao.setSyncError(localRecord.id, "Timeout server offline")
                return localRecord.copy(syncError = "Connexion expirée")
            } catch (e: ConnectException) {
                attendanceDao.setSyncError(localRecord.id, "Pas de connexion locale")
                return localRecord.copy(syncError = "Impossible de joindre le serveur FastAPI local")
            } catch (e: Exception) {
                attendanceDao.setSyncError(localRecord.id, e.localizedMessage ?: "Unknown network error")
                return localRecord.copy(syncError = e.localizedMessage ?: "Erreur réseau")
            }
        } else {
            attendanceDao.setSyncError(localRecord.id, "Configuration URL incorrecte")
            return localRecord.copy(syncError = "URL non valide")
        }
    }

    suspend fun syncUnsyncedRecords(baseUrl: String): SyncResult {
        val unsyncedList = attendanceDao.getUnsyncedRecords()
        if (unsyncedList.isEmpty()) {
            return SyncResult(successCount = 0, failCount = 0, message = "Tous les pointages sont déjà synchronisés.")
        }

        val service = RetrofitClientProvider.createService(baseUrl)
        if (service == null) {
            return SyncResult(successCount = 0, failCount = unsyncedList.size, message = "Impossible d'initialiser Retrofit avec l'URL fournie.")
        }

        var successCount = 0
        var failCount = 0
        var lastError = "Erreur"

        for (record in unsyncedList) {
            try {
                val request = AttendancePunchRequest(
                    employee_token = record.employeeToken,
                    employee_name = record.employeeName,
                    timestamp = record.timestamp,
                    punch_type = record.type
                )
                val response = service.postPunch(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    attendanceDao.markAsSynced(record.id)
                    successCount++
                } else {
                    val errMsg = response.body()?.message ?: "API error"
                    attendanceDao.setSyncError(record.id, errMsg)
                    failCount++
                    lastError = errMsg
                }
            } catch (e: Exception) {
                attendanceDao.setSyncError(record.id, e.localizedMessage ?: "Connexion échouée")
                failCount++
                lastError = e.localizedMessage ?: "Pas de connexion"
            }
        }

        return SyncResult(
            successCount = successCount,
            failCount = failCount,
            message = if (failCount == 0) {
                "Synchronisation réussie : $successCount pointages envoyés."
            } else {
                "Synchronisation partielle : $successCount OK, $failCount en attente. Dernière erreur : $lastError"
            }
        )
    }

    suspend fun clearHistory() {
        attendanceDao.clearHistory()
    }
}

data class SyncResult(
    val successCount: Int,
    val failCount: Int,
    val message: String
)
