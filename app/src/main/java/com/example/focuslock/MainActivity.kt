package com.example.focuslock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.focuslock.admin.DeviceAdminReceiver
import com.example.focuslock.service.VpnService as FocusLockVpnService
import com.example.focuslock.ui.navigation.AppNavigation
import com.example.focuslock.ui.theme.FocusLockTheme
import com.example.focuslock.util.LockManager
import com.example.focuslock.util.PermissionManager

class MainActivity : ComponentActivity() {
    private lateinit var lockManager: LockManager
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminComponentName: ComponentName
    private lateinit var permissionManager: PermissionManager

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1
        private const val REQUEST_CODE_VPN = 2
        private const val REQUEST_CODE_ACCESSIBILITY = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lockManager = LockManager(this)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminComponentName = ComponentName(this, DeviceAdminReceiver::class.java)
        permissionManager = PermissionManager(this)

        setContent {
            FocusLockTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    AppNavigation(
                        navController = navController,
                        lockManager = lockManager,
                        onRequestDeviceAdmin = { requestDeviceAdmin() },
                        onStartVpn = { startVpnService() },
                        isDeviceAdminActive = { isDeviceAdminActive() },
                        onStopVpn = { stopVpnService() },
                        permissionManager = permissionManager
                    )
                }
            }
        }
        
        // Check permissions on startup
        checkRequiredPermissions()
        
        // Check if focus mode is active, if not, ensure VPN is stopped
        if (!lockManager.isLockActive()) {
            stopVpnService()
        }
    }
    
    private fun checkRequiredPermissions() {
        // Check if device admin is active
        if (!isDeviceAdminActive()) {
            requestDeviceAdmin()
        }
        
        // Check if accessibility service is enabled
        if (!permissionManager.isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY)
            Toast.makeText(
                this,
                "Please enable the FocusLock accessibility service",
                Toast.LENGTH_LONG
            ).show()
        }
        
        // Check if VPN permission is granted (only if focus mode is active)
        if (lockManager.isLockActive()) {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, REQUEST_CODE_VPN)
            }
        }
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Device admin is required to prevent accidental uninstallation during your focus period."
            )
        }
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    private fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(deviceAdminComponentName)
    }

    private fun startVpnService() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_VPN)
        } else {
            onVpnPermissionGranted()
        }
    }
    
    private fun stopVpnService() {
        val serviceIntent = Intent(this, FocusLockVpnService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "Content filtering deactivated", Toast.LENGTH_SHORT).show()
    }

    private fun onVpnPermissionGranted() {
        val serviceIntent = Intent(this, FocusLockVpnService::class.java)
        startService(serviceIntent)
        Toast.makeText(this, "Content filtering activated", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Device admin activated", Toast.LENGTH_SHORT).show()
                    
                    // Set device admin policies to prevent uninstallation
                    if (isDeviceAdminActive()) {
                        try {
                            devicePolicyManager.setPackagesSuspended(
                                deviceAdminComponentName,
                                arrayOf(packageName),
                                false
                            )
                        } catch (e: Exception) {
                            // Ignore if not supported on this device
                        }
                        
                        // Disable uninstallation
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            try {
                                devicePolicyManager.setUninstallBlocked(
                                    deviceAdminComponentName,
                                    packageName,
                                    true
                                )
                            } catch (e: Exception) {
                                // Ignore if not supported on this device
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Device admin is required for lock functionality", Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_CODE_VPN -> {
                if (resultCode == RESULT_OK) {
                    onVpnPermissionGranted()
                } else {
                    Toast.makeText(this, "VPN permission is required for content filtering", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // If focus mode is off, stop the VPN service
        if (!lockManager.isLockActive()) {
            val serviceIntent = Intent(this, FocusLockVpnService::class.java)
            stopService(serviceIntent)
        }
    }
}
