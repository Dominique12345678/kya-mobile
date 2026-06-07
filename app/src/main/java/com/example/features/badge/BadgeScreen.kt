package com.example.features.badge

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.features.attendance.AttendanceViewModel
import com.example.features.attendance.AttendanceRecord
import com.example.ui.theme.KyaGreen
import com.example.ui.theme.KyaOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 1. MAXIMIZE BRIGHTNESS ON OPEN, REVERT ON EXIT (as requested by layout mechanics)
    DisposableEffect(Unit) {
        val window = activity?.window
        val layoutParams = window?.attributes
        val originalBrightness = layoutParams?.screenBrightness ?: -1f
        
        layoutParams?.screenBrightness = 1.0f // Force 100% brightness
        window?.attributes = layoutParams

        onDispose {
            layoutParams?.screenBrightness = originalBrightness
            window?.attributes = layoutParams
        }
    }

    // Collect view model state
    val employeeToken by viewModel.employeeToken.collectAsState()
    val employeeName by viewModel.employeeName.collectAsState()
    val isEnPoste by viewModel.isEnPoste.collectAsState()
    val records by viewModel.attendanceRecords.collectAsState()

    // Get the latest attendance record to display in the "Recent Activity Preview"
    val latestRecord = remember(records) { records.firstOrNull() }

    // Dynamically regenerate QR Code bitmap when token changes
    val qrBitmap = remember(employeeToken) {
        QrCodeGenerator.generateQrCode(employeeToken, size = 512)
    }

    // Green pulsating active dot animation for "EN POSTE"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Natural Tones Header Box: w-24 h-12 bg-white rounded-lg shadow-sm border border-slate-100 mb-2
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .width(110.dp)
                .height(48.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KYA",
                    fontWeight = FontWeight.Black,
                    color = KyaGreen,
                    fontSize = 18.sp,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = " |",
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF94A3B8),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "EDGE",
                    fontWeight = FontWeight.Bold,
                    color = KyaOrange,
                    fontSize = 10.sp
                )
            }
        }

        // Title and welcome message from Natural Tones
        Text(
            text = "Badge de Présence",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Présentez ce QR Code devant la borne locale Edge",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF64748B)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // DUAL CONTAINER FOR CORNER ACCENTS - relative absolute layering
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(8.dp) // Leave negative space for corner accents to stick out (-top-2, etc)
        ) {
            // Main white container Card matching "bg-white p-8 rounded-[40px] shadow-xl border border-slate-100"
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .testTag("qr_badge_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(40.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Outer clean frame for QR
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "Code QR Badge Employé",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("qr_code_image")
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Erreur QR",
                                    tint = KyaOrange,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "Configurez votre Token",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Employee Name
                    Text(
                        text = employeeName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )

                    // Employee unique badge ID tag
                    Text(
                        text = "ID: $employeeToken",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Top-Left corner accent link: border-t-4 border-l-4 border-[#019a88]
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(28.dp)
                    .drawBehind {
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset.Zero,
                            size = Size(width = 28.dp.toPx(), height = 4.dp.toPx())
                        )
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset.Zero,
                            size = Size(width = 4.dp.toPx(), height = 28.dp.toPx())
                        )
                    }
            )

            // Top-Right corner accent link: border-t-4 border-r-4 border-[#019a88]
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(28.dp)
                    .drawBehind {
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset.Zero,
                            size = Size(width = 28.dp.toPx(), height = 4.dp.toPx())
                        )
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset(x = size.width - 4.dp.toPx(), y = 0f),
                            size = Size(width = 4.dp.toPx(), height = 28.dp.toPx())
                        )
                    }
            )

            // Bottom-Left corner accent link: border-b-4 border-l-4 border-[#019a88]
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(28.dp)
                    .drawBehind {
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset(x = 0f, y = size.height - 4.dp.toPx()),
                            size = Size(width = 28.dp.toPx(), height = 4.dp.toPx())
                        )
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset.Zero,
                            size = Size(width = 4.dp.toPx(), height = 28.dp.toPx())
                        )
                    }
            )

            // Bottom-Right corner accent link: border-b-4 border-r-4 border-[#019a88]
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(28.dp)
                    .drawBehind {
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset(x = 0f, y = size.height - 4.dp.toPx()),
                            size = Size(width = 28.dp.toPx(), height = 4.dp.toPx())
                        )
                        drawRect(
                            color = KyaGreen,
                            topLeft = Offset(x = size.width - 4.dp.toPx(), y = 0f),
                            size = Size(width = 4.dp.toPx(), height = 28.dp.toPx())
                        )
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DYNAMIC STATUS BADGE WITH PULSATING DOT and helper text from design
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = if (isEnPoste) KyaGreen.copy(alpha = 0.10f) else Color(0x1AE11D48),
                contentColor = if (isEnPoste) KyaGreen else Color(0xFFE11D48),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isEnPoste) KyaGreen.copy(alpha = 0.20f) else Color(0x33E11D48),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .testTag("status_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = (if (isEnPoste) KyaGreen else Color(0xFFE11D48)).copy(alpha = pulseAlpha),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnPoste) "EN POSTE" else "NON EN POSTE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Text(
                text = "Scannez pour pointer",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // RECENT ACTIVITY PREVIEW PANEL (Matches Today section in Design HTML)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUJOURD'HUI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Dernière action",
                    fontSize = 10.sp,
                    color = KyaGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // White rounded activity preview card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge circular icon indicator
                    val isPunchIn = latestRecord?.type == "IN" || latestRecord == null // Mock to IN if empty
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isPunchIn) KyaGreen.copy(alpha = 0.1f) else KyaOrange.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPunchIn) Icons.Default.Login else Icons.Default.Logout,
                            contentDescription = null,
                            tint = if (isPunchIn) KyaGreen else KyaOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (latestRecord == null) "Entrée Bureau" else if (isPunchIn) "Entrée En Registre" else "Sortie En Registre",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (latestRecord == null) "Siège Social, Lomé" else if (latestRecord.isSynced) "Code uniqueAPI Synced" else "Stocké localement (Hors ligne)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Display Time
                    val displayTime = remember(latestRecord) {
                        if (latestRecord != null) {
                            try {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(latestRecord.timestamp))
                            } catch (e: Exception) {
                                "08:15"
                            }
                        } else {
                            "08:15"
                        }
                    }

                    Text(
                        text = displayTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KyaGreen,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        // PUNCH/ACTION BUTTON IN PREMIUM CORPORATE GRAPICS
        Button(
            onClick = { viewModel.togglePoste() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("toggle_poste_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnPoste) Color(0xFFE11D48) else KyaGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isEnPoste) Icons.Default.Cached else Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnPoste) "Se dépointer (Sortir)" else "Pointer (Entrer)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

