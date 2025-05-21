package com.example.focuslock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslock.ui.animations.FadeInAnimation
import com.example.focuslock.ui.animations.SlideInAnimation
import com.example.focuslock.ui.components.TimerDisplay
import com.example.focuslock.util.BlockListManager
import com.example.focuslock.util.LockManager
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusLockApp(
    lockManager: LockManager,
    onRequestDeviceAdmin: () -> Unit,
    onStartVpn: () -> Unit,
    isDeviceAdminActive: () -> Boolean,
    onOpenSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    var selectedDuration by remember { mutableStateOf(7) } // Default 7 days
    var durationText by remember { mutableStateOf("7") } // Text field value
    var selectedTimeUnit by remember { mutableStateOf(TimeUnit.DAYS) }
    var isLockActive by remember { mutableStateOf(lockManager.isLockActive()) }
    var remainingTimeMillis by remember { mutableStateOf(lockManager.getRemainingTimeMillis()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Get BlockListManager
    val context = LocalContext.current
    val blockListManager = remember { BlockListManager(context) }
    
    // YouTube Shorts and Instagram Reels blocking options
    var blockYouTubeShorts by remember { mutableStateOf(blockListManager.isYoutubeShortsBlocked()) }
    var blockInstagramReels by remember { mutableStateOf(blockListManager.isInstagramReelsBlocked()) }

    LaunchedEffect(isLockActive) {
        if (isLockActive) {
            while (isActive && lockManager.isLockActive()) {
                remainingTimeMillis = lockManager.getRemainingTimeMillis()
                kotlinx.coroutines.delay(1000)
            }
            isLockActive = lockManager.isLockActive()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "FocusLock",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        onNavigateToCalendar()
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Streak Calendar") },
                    label = { Text("Streak") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = isLockActive,
                enter = fadeIn(animationSpec = tween(500)) + 
                        slideInVertically(animationSpec = tween(500)) { it },
                exit = fadeOut(animationSpec = tween(500)) + 
                        slideOutVertically(animationSpec = tween(500)) { it }
            ) {
                LockActiveScreen(
                    remainingTimeMillis = remainingTimeMillis,
                    onOpenSettings = onOpenSettings,
                    blockYouTubeShorts = blockYouTubeShorts,
                    blockInstagramReels = blockInstagramReels
                )
            }
            
            AnimatedVisibility(
                visible = !isLockActive,
                enter = fadeIn(animationSpec = tween(500)) + 
                        slideInVertically(animationSpec = tween(500)) { -it },
                exit = fadeOut(animationSpec = tween(500)) + 
                        slideOutVertically(animationSpec = tween(500)) { -it }
            ) {
                LockSetupScreen(
                    durationText = durationText,
                    onDurationTextChange = { 
                        durationText = it
                        // Convert to int if not empty
                        if (it.isNotEmpty()) {
                            selectedDuration = it.toIntOrNull()?.coerceIn(1, 365) ?: 1
                        } else {
                            // Keep the text field empty but use 1 as the actual value
                            selectedDuration = 1
                        }
                    },
                    selectedTimeUnit = selectedTimeUnit,
                    onTimeUnitChange = { selectedTimeUnit = it },
                    onActivateLock = {
                        if (!isDeviceAdminActive()) {
                            onRequestDeviceAdmin()
                        } else {
                            // Ensure we have a valid duration (minimum 1)
                            val duration = if (durationText.isEmpty()) 1 else selectedDuration
                            lockManager.activateLock(duration, selectedTimeUnit)
                            onStartVpn()
                            isLockActive = true
                        }
                    },
                    blockYouTubeShorts = blockYouTubeShorts,
                    onBlockYouTubeShortsChange = { 
                        if (!isLockActive) {
                            blockYouTubeShorts = it
                            blockListManager.setYoutubeShortsBlocked(it)
                        }
                    },
                    blockInstagramReels = blockInstagramReels,
                    onBlockInstagramReelsChange = { 
                        if (!isLockActive) {
                            blockInstagramReels = it
                            blockListManager.setInstagramReelsBlocked(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LockActiveScreen(
    remainingTimeMillis: Long,
    onOpenSettings: () -> Unit,
    blockYouTubeShorts: Boolean,
    blockInstagramReels: Boolean
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FadeInAnimation {
            TimerDisplay(
                remainingTimeMillis = remainingTimeMillis
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Content blocking status
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Content Blocking Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "YouTube Shorts",
                        color = Color.White
                    )
                    
                    Text(
                        if (blockYouTubeShorts) "Blocked" else "Allowed",
                        color = if (blockYouTubeShorts) Color(0xFF4CAF50) else Color.Red
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Instagram Reels",
                        color = Color.White
                    )
                    
                    Text(
                        if (blockInstagramReels) "Blocked" else "Allowed",
                        color = if (blockInstagramReels) Color(0xFF4CAF50) else Color.Red
                    )
                }
                
                Text(
                    "To change these settings, you need to access the settings page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text("Settings")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockSetupScreen(
    durationText: String,
    onDurationTextChange: (String) -> Unit,
    selectedTimeUnit: TimeUnit,
    onTimeUnitChange: (TimeUnit) -> Unit,
    onActivateLock: () -> Unit,
    blockYouTubeShorts: Boolean,
    onBlockYouTubeShortsChange: (Boolean) -> Unit,
    blockInstagramReels: Boolean,
    onBlockInstagramReelsChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SlideInAnimation {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set Focus Duration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { 
                                // Only allow digits
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    onDurationTextChange(it)
                                }
                            },
                            label = { Text("Duration") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50),
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTimeUnit == TimeUnit.DAYS,
                                    onClick = { onTimeUnitChange(TimeUnit.DAYS) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF4CAF50)
                                    )
                                )
                                Text(
                                    "Days",
                                    color = Color.White
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTimeUnit == TimeUnit.HOURS,
                                    onClick = { onTimeUnitChange(TimeUnit.HOURS) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF4CAF50)
                                    )
                                )
                                Text(
                                    "Hours",
                                    color = Color.White
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTimeUnit == TimeUnit.MINUTES,
                                    onClick = { onTimeUnitChange(TimeUnit.MINUTES) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF4CAF50)
                                    )
                                )
                                Text(
                                    "Minutes",
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Content blocking options
                    Text(
                        text = "Content Blocking Options",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
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
                            onCheckedChange = onBlockYouTubeShortsChange,
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
                            onCheckedChange = onBlockInstagramReelsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }
                    
                    Text(
                        text = "This will activate content filtering for the specified duration. The app will require a 24-hour cooldown period if you decide to deactivate early.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    
                    Button(
                        onClick = onActivateLock,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Activate Focus Mode")
                    }
                }
            }
        }
        
        FadeInAnimation(durationMillis = 800) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How It Works",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "1. Set your desired focus duration",
                        color = Color.White
                    )
                    Text(
                        "2. Grant required permissions",
                        color = Color.White
                    )
                    Text(
                        "3. FocusLock will filter distracting content",
                        color = Color.White
                    )
                    Text(
                        "4. Early deactivation requires a 24-hour cooldown",
                        color = Color.White
                    )
                }
            }
        }
    }
}
