package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.overview.OverviewScreen
import com.example.ui.screens.record.RecordScreen
import com.example.ui.screens.laboratory.LaboratoryScreen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.domain.repository.BioTrackRepository

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Overview : Screen("overview", "Přehled", Icons.Filled.ListAlt)
    object Record : Screen("record", "Záznam", Icons.Filled.AddCircle)
    object Laboratory : Screen("laboratory", "Laboratoř", Icons.Filled.Science)
    object Analytics : Screen("analytics", "Analytika", Icons.Filled.Analytics)
    object Settings : Screen("settings", "Nastavení", Icons.Filled.Settings)
}

val items = listOf(
    Screen.Overview,
    Screen.Record,
    Screen.Laboratory,
    Screen.Analytics,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(repository: BioTrackRepository) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Overview.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Overview.route) { OverviewScreen(repository) }
            composable(Screen.Record.route) { RecordScreen(repository) }
            composable(Screen.Laboratory.route) { LaboratoryScreen(repository) }
            composable(Screen.Analytics.route) { AnalyticsScreen(repository) }
            composable(Screen.Settings.route) { SettingsScreen(repository) }
        }
    }
}
