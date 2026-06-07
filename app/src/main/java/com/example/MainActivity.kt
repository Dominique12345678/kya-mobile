package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.PreferencesHelper
import com.example.features.attendance.AttendanceDatabase
import com.example.features.attendance.AttendanceRepository
import com.example.features.attendance.AttendanceScreen
import com.example.features.attendance.AttendanceViewModel
import com.example.features.badge.BadgeScreen
import com.example.features.profile.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.KyaGreen

enum class MainTab {
    Badge, History, Profile
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Core Offline & Secure Dependencies
        val preferencesHelper = PreferencesHelper(applicationContext)
        val database = AttendanceDatabase.getDatabase(applicationContext)
        val repository = AttendanceRepository(database.attendanceDao())
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize View Model using our robust custom ViewModelFactory
                val attViewModel: AttendanceViewModel = viewModel(
                    factory = AttendanceViewModel.Factory(
                        application = this.application,
                        repository = repository,
                        preferencesHelper = preferencesHelper
                    )
                )

                var currentTab by remember { mutableStateOf(MainTab.Badge) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("kya_bottom_navigation")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == MainTab.Badge,
                                onClick = { currentTab = MainTab.Badge },
                                label = { Text("Badge") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "Badge"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = KyaGreen,
                                    indicatorColor = KyaGreen,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                ),
                                modifier = Modifier.testTag("nav_badge_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == MainTab.History,
                                onClick = { currentTab = MainTab.History },
                                label = { Text("Historique") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Historique"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = KyaGreen,
                                    indicatorColor = KyaGreen,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                ),
                                modifier = Modifier.testTag("nav_history_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == MainTab.Profile,
                                onClick = { currentTab = MainTab.Profile },
                                label = { Text("Configuration") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Configuration"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = KyaGreen,
                                    indicatorColor = KyaGreen,
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                ),
                                modifier = Modifier.testTag("nav_profile_tab")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        MainTab.Badge -> {
                            BadgeScreen(
                                viewModel = attViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        MainTab.History -> {
                            AttendanceScreen(
                                viewModel = attViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        MainTab.Profile -> {
                            ProfileScreen(
                                viewModel = attViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
