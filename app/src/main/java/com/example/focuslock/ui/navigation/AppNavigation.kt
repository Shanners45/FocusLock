package com.example.focuslock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focuslock.ui.screens.CalendarScreen
import com.example.focuslock.ui.screens.FocusLockApp
import com.example.focuslock.ui.screens.MotivationVerificationScreen
import com.example.focuslock.ui.screens.SettingsScreen
import com.example.focuslock.util.LockManager
import com.example.focuslock.util.PermissionManager

@Composable
fun AppNavigation(
    navController: NavHostController,
    lockManager: LockManager,
    onRequestDeviceAdmin: () -> Unit,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit,
    isDeviceAdminActive: () -> Boolean,
    permissionManager: PermissionManager
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            FocusLockApp(
                lockManager = lockManager,
                onRequestDeviceAdmin = onRequestDeviceAdmin,
                onStartVpn = onStartVpn,
                isDeviceAdminActive = isDeviceAdminActive,
                onOpenSettings = { 
                    if (lockManager.isLockActive()) {
                        navController.navigate("motivation_verification")
                    } else {
                        navController.navigate("settings")
                    }
                },
                onNavigateToCalendar = {
                    navController.navigate("calendar")
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() },
                lockManager = lockManager,
                onStopVpn = onStopVpn
            )
        }
        
        composable("motivation_verification") {
            MotivationVerificationScreen(
                onBackPressed = { navController.popBackStack() },
                onVerificationSuccess = { navController.navigate("settings") }
            )
        }
        
        composable("calendar") {
            CalendarScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}
