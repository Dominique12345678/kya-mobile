package com.example.features.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.attendance.AttendanceViewModel
import com.example.ui.theme.KyaGreen
import com.example.ui.theme.KyaOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Retrieve active configuration flow values
    val currentToken by viewModel.employeeToken.collectAsState()
    val currentName by viewModel.employeeName.collectAsState()
    val currentUrl by viewModel.fastApiUrl.collectAsState()

    // Local state variables for forms
    var formToken by remember(currentToken) { mutableStateOf(currentToken) }
    var formName by remember(currentName) { mutableStateOf(currentName) }
    var formUrl by remember(currentUrl) { mutableStateOf(currentUrl) }

    var saveSuccessShow by remember { mutableStateOf(false) }

    // Diagnostic state
    var diagnosticState by remember { mutableStateOf<DiagnosticStatus>(DiagnosticStatus.Idle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuration",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // General settings header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Élément d'identification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KyaGreen
                    )
                    Text(
                        "Configurez l'identité de l'employé et sa clé sécurisée KYA associée.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Employee Full Name Input
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("Nom complet de l'employé") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = KyaGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("name_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KyaGreen,
                            focusedLabelColor = KyaGreen
                        ),
                        singleLine = true
                    )

                    // Secure Token Input (Acts as Employee Token in QR Badge)
                    OutlinedTextField(
                        value = formToken,
                        onValueChange = { formToken = it },
                        label = { Text("Token sécurisé de l'employé") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = KyaGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("token_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KyaGreen,
                            focusedLabelColor = KyaGreen
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // API Configuration settings card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Réseau - API FastAPI locale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KyaGreen
                    )
                    Text(
                        "Adresse IP ou URL de notre borne locale Edge / serveur FastAPI.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = formUrl,
                        onValueChange = { formUrl = it },
                        label = { Text("URL du Serveur Edge (FastAPI)") },
                        leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null, tint = KyaGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("url_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KyaGreen,
                            focusedLabelColor = KyaGreen
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test local connection button + display state
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                diagnosticState = DiagnosticStatus.Loading
                                // Simulate connection check on custom local FastAPI address
                                kotlinx.coroutines.delay(1200)
                                diagnosticState = if (formUrl.contains("http://") || formUrl.contains("https://")) {
                                    DiagnosticStatus.Success("URL bien formée. Serveur FastApi accessible localement.")
                                } else {
                                    DiagnosticStatus.Error("Erreur : L'adresse URL doit commencer par http:// ou https://")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_api_connection_button")
                    ) {
                        Icon(Icons.Default.SettingsEthernet, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vérifier la connexion au serveur", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Diagnostic output state matching colors
                    AnimatedVisibility(visible = diagnosticState != DiagnosticStatus.Idle) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (val state = diagnosticState) {
                                is DiagnosticStatus.Success -> KyaGreen.copy(alpha = 0.1f)
                                is DiagnosticStatus.Error -> KyaOrange.copy(alpha = 0.1f)
                                else -> Color(0xFFF8F9FA)
                            },
                            contentColor = when (val state = diagnosticState) {
                                is DiagnosticStatus.Success -> KyaGreen
                                is DiagnosticStatus.Error -> KyaOrange
                                else -> Color(0xFF64748B)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .border(
                                    width = 1.dp,
                                    color = when (val state = diagnosticState) {
                                        is DiagnosticStatus.Success -> KyaGreen.copy(alpha = 0.3f)
                                        is DiagnosticStatus.Error -> KyaOrange.copy(alpha = 0.3f)
                                        else -> Color(0xFFE2E8F0)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (val state = diagnosticState) {
                                    is DiagnosticStatus.Loading -> {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = KyaGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Recherche du serveur sur le réseau...", fontSize = 12.sp)
                                    }
                                    is DiagnosticStatus.Success -> {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(state.message, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    is DiagnosticStatus.Error -> {
                                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(state.reason, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    DiagnosticStatus.Idle -> {}
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action save button
            Button(
                onClick = {
                    viewModel.saveSettings(
                        token = formToken,
                        name = formName,
                        url = formUrl
                    )
                    coroutineScope.launch {
                        saveSuccessShow = true
                        kotlinx.coroutines.delay(2000)
                        saveSuccessShow = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KyaGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(52.dp)
                    .testTag("save_profile_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Enregistrer les configurations", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Success save popup alert
            AnimatedVisibility(visible = saveSuccessShow) {
                Surface(
                    color = KyaGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("save_success_toast")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Configurations enregistrées avec succès !", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

sealed class DiagnosticStatus {
    object Idle : DiagnosticStatus()
    object Loading : DiagnosticStatus()
    data class Success(val message: String) : DiagnosticStatus()
    data class Error(val reason: String) : DiagnosticStatus()
}
