package com.example.focuslock.settings

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.focuslock.MainActivity
import com.example.focuslock.accessibility.AppBlockerService
import com.example.focuslock.ui.theme.FocusLockTheme
import com.example.focuslock.util.BlockListManager
import com.example.focuslock.util.HiddenModeManager

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FocusLockTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val blockListManager = remember { BlockListManager(context) }
    val hiddenModeManager = remember { HiddenModeManager(context) }
    val settingsLockManager = remember { SettingsLockManager(context) }
    var isSettingsLocked by remember { mutableStateOf(settingsLockManager.isSettingsLocked()) }
    var settingsPassword by remember { mutableStateOf(settingsLockManager.getSettingsPassword()) }
    
    var isHiddenModeEnabled by remember { mutableStateOf(hiddenModeManager.isHiddenModeEnabled()) }
    var unlockCode by remember { mutableStateOf(hiddenModeManager.getUnlockCode()) }
    var newDomain by remember { mutableStateOf("") }
    var blockedDomains by remember { mutableStateOf(blockListManager.getBlockedDomains()) }
    var blockedApps by remember { mutableStateOf(blockListManager.getBlockedApps()) }
    var newApp by remember { mutableStateOf("") }
    var blockYouTubeShorts by remember { mutableStateOf(blockListManager.isYoutubeShortsBlocked()) }
    var blockInstagramReels by remember { mutableStateOf(blockListManager.isInstagramReelsBlocked()) }
    
    val focusManager = LocalFocusManager.current
    
    val isAccessibilityServiceEnabled = remember {
        mutableStateOf(
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )?.contains(context.packageName + "/" + AppBlockerService::class.java.name) == true
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hidden Mode Section
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Hidden Mode",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hide app from launcher")
                            Switch(
                                checked = isHiddenModeEnabled,
                                onCheckedChange = { checked ->
                                    if (checked && unlockCode.isBlank()) {
                                        Toast.makeText(context, "Please set an unlock code first", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isHiddenModeEnabled = checked
                                        
                                        if (checked) {
                                            hiddenModeManager.hideApp(context)
                                            Toast.makeText(
                                                context,
                                                "App hidden. Dial *#*#${unlockCode}#*#* to reopen",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            hiddenModeManager.showApp(context)
                                        }
                                    }
                                }
                            )
                        }
                        
                        OutlinedTextField(
                            value = unlockCode,
                            onValueChange = { 
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    unlockCode = it
                                    hiddenModeManager.setUnlockCode(it)
                                }
                            },
                            label = { Text("Unlock Code (digits only)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            "⚠️ Warning: If you enable hidden mode, you'll need to dial *#*#${unlockCode}#*#* to reopen the app.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Custom Domain Blocking
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Custom Domain Blocking",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newDomain,
                                onValueChange = { newDomain = it },
                                label = { Text("Domain to block") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newDomain.isNotBlank()) {
                                            blockListManager.addBlockedDomain(newDomain)
                                            blockedDomains = blockListManager.getBlockedDomains()
                                            newDomain = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                )
                            )
                            
                            IconButton(
                                onClick = {
                                    if (newDomain.isNotBlank()) {
                                        blockListManager.addBlockedDomain(newDomain)
                                        blockedDomains = blockListManager.getBlockedDomains()
                                        newDomain = ""
                                        focusManager.clearFocus()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add domain")
                            }
                        }
                        
                        Text(
                            "Blocked Domains:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "Note: The app uses CleanBrowsing Adult Filter DNS servers (185.228.168.10 and 185.228.169.11) which block most adult content automatically. The domains below are additional blocks.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        // Show only the first 10 domains to avoid overwhelming the UI
                        val displayDomains = blockedDomains.take(10)
                        displayDomains.forEach { domain ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(domain)
                                IconButton(
                                    onClick = {
                                        blockListManager.removeBlockedDomain(domain)
                                        blockedDomains = blockListManager.getBlockedDomains()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove domain")
                                }
                            }
                            Divider()
                        }
                        
                        if (blockedDomains.size > 10) {
                            Text(
                                "... and ${blockedDomains.size - 10} more domains",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // App Blocking
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "App Blocking",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        if (!isAccessibilityServiceEnabled.value) {
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enable Accessibility Service")
                            }
                            
                            Text(
                                "App blocking requires accessibility service. Please enable the FocusLock service in accessibility settings.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = newApp,
                                    onValueChange = { newApp = it },
                                    label = { Text("Package name (e.g., com.instagram.android)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (newApp.isNotBlank()) {
                                                blockListManager.addBlockedApp(newApp)
                                                blockedApps = blockListManager.getBlockedApps()
                                                newApp = ""
                                                focusManager.clearFocus()
                                            }
                                        }
                                    )
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (newApp.isNotBlank()) {
                                            blockListManager.addBlockedApp(newApp)
                                            blockedApps = blockListManager.getBlockedApps()
                                            newApp = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add app")
                                }
                            }
                            
                            Text(
                                "Common package names:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Instagram: com.instagram.android\nTikTok: com.zhiliaoapp.musically\nReddit: com.reddit.frontpage\nTwitter/X: com.twitter.android",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Divider()
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Block YouTube Shorts")
                                Switch(
                                    checked = blockYouTubeShorts,
                                    onCheckedChange = { checked ->
                                        blockYouTubeShorts = checked
                                        blockListManager.setYoutubeShortsBlocked(checked)
                                    }
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Block Instagram Reels")
                                Switch(
                                    checked = blockInstagramReels,
                                    onCheckedChange = { checked ->
                                        blockInstagramReels = checked
                                        blockListManager.setInstagramReelsBlocked(checked)
                                    }
                                )
                            }
                            
                            Divider()
                            
                            Text(
                                "Blocked Apps:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            blockedApps.forEach { app ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(app)
                                    IconButton(
                                        onClick = {
                                            blockListManager.removeBlockedApp(app)
                                            blockedApps = blockListManager.getBlockedApps()
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove app")
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
            
            // Settings Lock Section
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Settings Lock",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Lock settings during Focus Mode")
                            Switch(
                                checked = isSettingsLocked,
                                onCheckedChange = { checked ->
                                    isSettingsLocked = checked
                                    settingsLockManager.setSettingsLocked(checked)
                                }
                            )
                        }
                        
                        OutlinedTextField(
                            value = settingsPassword,
                            onValueChange = { 
                                settingsPassword = it
                                settingsLockManager.setSettingsPassword(it)
                            },
                            label = { Text("Settings Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            "This password will be required to access settings during Focus Mode.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // About DNS Filtering
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "About Content Filtering",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            "FocusLock uses CleanBrowsing's Adult Filter DNS servers to block adult content at the network level. These servers block access to all adult content sites, proxies, VPNs, and mixed content sites.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "DNS Servers Used:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "Primary: 185.228.168.10\nSecondary: 185.228.169.11",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "If you're still able to access adult content, please try restarting the app or your device. The VPN service needs to be running for content filtering to work.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
