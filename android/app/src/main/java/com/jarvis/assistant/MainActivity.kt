package com.jarvis.assistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jarvis.assistant.ui.components.ActionConfirmationDialog
import com.jarvis.assistant.ui.screens.*
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.ui.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JarvisTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: "dashboard"

                // Runtime Permissions Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    val allGranted = results.values.all { it }
                    if (allGranted) {
                        viewModel.handleUserPrompt("Hello Jarvis", isVoice = false)
                    }
                }

                fun requestRequiredPermissions() {
                    val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        perms.add(Manifest.permission.READ_MEDIA_IMAGES)
                        perms.add(Manifest.permission.READ_MEDIA_AUDIO)
                        perms.add(Manifest.permission.READ_MEDIA_VIDEO)
                    } else {
                        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    permissionLauncher.launch(perms.toTypedArray())
                }

                LaunchedEffect(Unit) {
                    requestRequiredPermissions()
                }

                // Global Confirmation Dialog for Sensitive Actions
                val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
                if (pendingConfirmation != null) {
                    val conf = pendingConfirmation!!
                    ActionConfirmationDialog(
                        title = conf.title,
                        message = conf.message,
                        isDestructive = conf.isDestructive,
                        onConfirm = { conf.onConfirm() },
                        onDismiss = { viewModel.dismissConfirmation() }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = StarkBg,
                    bottomBar = {
                        NavigationBar(
                            containerColor = StarkSurface,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                Triple("dashboard", "HUD", Icons.Default.Adjust),
                                Triple("chat", "Logs", Icons.Default.Chat),
                                Triple("files", "Files", Icons.Default.Folder),
                                Triple("apps", "Apps", Icons.Default.Apps),
                                Triple("productivity", "Tasks", Icons.Default.Alarm),
                                Triple("permissions", "Audit", Icons.Default.Security),
                                Triple("settings", "Setup", Icons.Default.Settings)
                            )

                            items.forEach { (route, label, icon) ->
                                val selected = currentRoute == route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (selected) StarkCyan else StarkTextMuted
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (selected) StarkCyan else StarkTextMuted
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = StarkCyan.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { navController.navigate("chat") }
                            )
                        }
                        composable("chat") {
                            ChatScreen(viewModel = viewModel)
                        }
                        composable("files") {
                            FileManagerScreen(viewModel = viewModel)
                        }
                        composable("apps") {
                            AppsScreen(viewModel = viewModel)
                        }
                        composable("productivity") {
                            ProductivityScreen(viewModel = viewModel)
                        }
                        composable("permissions") {
                            PermissionsDashboardScreen(
                                viewModel = viewModel,
                                onRequestRuntimePermissions = { requestRequiredPermissions() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
