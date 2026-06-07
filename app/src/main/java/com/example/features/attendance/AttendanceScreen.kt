package com.example.features.attendance

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KyaGreen
import com.example.ui.theme.KyaOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.attendanceRecords.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display Toast / Snackbar messages when a sync finishes
    LaunchedEffect(syncMessage) {
        syncMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historique",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncLogs() },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("sync_action_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = KyaGreen
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Synchroniser avec l'API",
                                tint = KyaGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Summary count of offline records pending
            val unsyncedCount = records.count { !it.isSynced }
            if (unsyncedCount > 0) {
                Surface(
                    color = KyaOrange.copy(alpha = 0.12f),
                    contentColor = KyaOrange,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .border(1.dp, KyaOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Sync Info",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pointages hors ligne",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFC2410C)
                            )
                            Text(
                                "Il y a $unsyncedCount pointage(s) stockés localement en attente de synchronisation.",
                                fontSize = 12.sp,
                                color = Color(0xFFEA580C)
                            )
                        }
                        Button(
                            onClick = { viewModel.syncLogs() },
                            colors = ButtonDefaults.buttonColors(containerColor = KyaOrange, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.heightIn(max = 32.dp)
                        ) {
                            Text("Envoyer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // MAIN LIST
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "Empty",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucun pointage",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vos pointages d'entrées et de sorties s'afficheront ici en temps réel.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF94A3B8)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("attendance_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        AttendanceCard(record = record)
                    }
                    
                    item {
                        // Options menu to delete logs
                        TextButton(
                            onClick = { viewModel.clearAllHistory() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_history_button")
                        ) {
                            Text("Effacer le journal local", color = Color(0xFFE11D48), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceCard(record: AttendanceRecord) {
    val isPunchIn = record.type == "IN"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("attendance_item_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Punch Type Indicator Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isPunchIn) KyaGreen.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPunchIn) Icons.Default.Login else Icons.Default.Logout,
                    contentDescription = if (isPunchIn) "Arrivée" else "Départ",
                    tint = if (isPunchIn) KyaGreen else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Punch date & time column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isPunchIn) "ENTRÉE (Arrivée)" else "SORTIE (Départ)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPunchIn) KyaGreen else Color(0xFF475569)
                    )
                )
                
                Text(
                    text = formatDateTime(record.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B)
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Sync Status badge in graphics charter
            if (record.isSynced) {
                Surface(
                    color = KyaGreen.copy(alpha = 0.12f),
                    contentColor = KyaGreen,
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Synced",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("API Synced", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    color = KyaOrange.copy(alpha = 0.12f),
                    contentColor = KyaOrange,
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Local offline DB",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Local", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatDateTime(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("EEEE dd MMMM yyyy à HH:mm", Locale.FRENCH)
        val formatted = sdf.format(Date(millis))
        // Capitalize first letter (e.g. Dimanche)
        formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } catch (e: Exception) {
        val backupSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        backupSdf.format(Date(millis))
    }
}
