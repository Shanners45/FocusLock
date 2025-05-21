package com.example.focuslock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focuslock.util.BlockListManager
import com.example.focuslock.util.LockManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    lockManager: LockManager,
    onStopVpn: () -> Unit
) {
    val context = LocalContext.current
    val isLockActive = remember { mutableStateOf(lockManager.isLockActive()) }
    val blockListManager = remember { BlockListManager(context) }
    val focusManager = LocalFocusManager.current
    
    // State for blacklist management
    var newDomain by remember { mutableStateOf("") }
    var blockedDomains by remember { mutableStateOf(blockListManager.getBlockedDomains()) }
    var blockYouTubeShorts by remember { mutableStateOf(blockListManager.isYoutubeShortsBlocked()) }
    var blockInstagramReels by remember { mutableStateOf(blockListManager.isInstagramReelsBlocked()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
            // Focus Mode Control
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Focus Mode",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        
                        if (isLockActive.value) {
                            Text(
                                "Focus mode is currently active. Remaining time: ${lockManager.getRemainingTimeFormatted()}",
                                color = Color.White
                            )
                            
                            Button(
                                onClick = {
                                    lockManager.deactivateLock()
                                    onStopVpn()
                                    isLockActive.value = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                ),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Text("Deactivate Focus Mode")
                            }
                            
                            Text(
                                "Warning: Deactivating focus mode will stop content filtering immediately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                "Focus mode is currently inactive.",
                                color = Color.White
                            )
                            
                            Text(
                                "Return to the home screen to activate focus mode.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Content Blocking Options
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Content Blocking Options",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Block YouTube Shorts",
                                color = Color.White
                            )
                            Switch(
                                checked = blockYouTubeShorts,
                                onCheckedChange = { 
                                    blockYouTubeShorts = it
                                    blockListManager.setYoutubeShortsBlocked(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Block Instagram Reels",
                                color = Color.White
                            )
                            Switch(
                                checked = blockInstagramReels,
                                onCheckedChange = { 
                                    blockInstagramReels = it
                                    blockListManager.setInstagramReelsBlocked(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
            
            // Custom Domain Blocking
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Custom Domain Blocking",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
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
                                placeholder = { Text("e.g., example.com") },
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
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50),
                                    unfocusedLabelColor = Color.Gray
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
                                Icon(
                                    Icons.Default.Add, 
                                    contentDescription = "Add domain",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                        
                        Text(
                            "Blocked Domains:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        
                        Text(
                            "Note: The app uses CleanBrowsing Adult Filter DNS servers which block most adult content automatically. The domains below are additional blocks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        // Show custom blocked domains (excluding default ones)
                        val customDomains = blockedDomains.take(10)
                        
                        if (customDomains.isEmpty()) {
                            Text(
                                "No custom domains added yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            customDomains.forEach { domain ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        domain,
                                        color = Color.White
                                    )
                                    IconButton(
                                        onClick = {
                                            blockListManager.removeBlockedDomain(domain)
                                            blockedDomains = blockListManager.getBlockedDomains()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete, 
                                            contentDescription = "Remove domain",
                                            tint = Color.Red
                                        )
                                    }
                                }
                                Divider(color = Color(0xFF333333))
                            }
                            
                            if (blockedDomains.size > 10) {
                                Text(
                                    "... and ${blockedDomains.size - 10} more domains",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            
            // About Content Filtering
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "About Content Filtering",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        
                        Text(
                            "FocusLock uses Cloudflare Family DNS and other secure DNS servers to block adult content at the network level. These servers block access to adult content sites, proxies, and mixed content sites.",
                            color = Color.White
                        )
                        
                        Text(
                            "The app also monitors browser activity to detect and block inappropriate content in real-time.",
                            color = Color.White
                        )
                    }
                }
            }
            
            // Credits
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Credits",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Made by Nishant Nayak",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            "© 2025 All Rights Reserved",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
