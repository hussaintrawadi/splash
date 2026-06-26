package com.splash.water.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.splash.water.ui.history.HistoryScreen
import com.splash.water.ui.home.HomeScreen
import com.splash.water.ui.rewards.RewardsScreen
import com.splash.water.ui.settings.SettingsScreen

@Composable
private fun RequestNotificationPermission() {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) {}
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }
}

enum class TopDest(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Filled.WaterDrop),
    HISTORY("history", "History", Icons.Filled.BarChart),
    REWARDS("rewards", "Rewards", Icons.Filled.EmojiEvents),
    SETTINGS("settings", "Settings", Icons.Filled.Settings),
}

@Composable
fun SplashRoot() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    RequestNotificationPermission()

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val current = backStack?.destination
            NavigationBar {
                TopDest.entries.forEach { dest ->
                    val selected = current?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopDest.HOME.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(TopDest.HOME.route) { HomeScreen(snackbarHostState) }
            composable(TopDest.HISTORY.route) { HistoryScreen() }
            composable(TopDest.REWARDS.route) { RewardsScreen() }
            composable(TopDest.SETTINGS.route) { SettingsScreen() }
        }
    }
}
