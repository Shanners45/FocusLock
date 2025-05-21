package com.example.focuslock.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.focuslock.util.StreakManager
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val streakManager = remember { StreakManager(context) }
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    
    // Load streak data from SharedPreferences
    val streakData = remember { mutableStateMapOf<LocalDate, Boolean>() }
    var currentStreak by remember { mutableStateOf(0) }
    
    // Monthly statistics
    var successCount by remember { mutableStateOf(0) }
    var failCount by remember { mutableStateOf(0) }
    
    // Load streak data on first composition
    LaunchedEffect(Unit) {
        val savedStreakData = streakManager.getStreakData()
        streakData.clear()
        streakData.putAll(savedStreakData)
        currentStreak = streakManager.calculateCurrentStreak()
    }
    
    // Calculate monthly statistics
    LaunchedEffect(streakData, currentMonth) {
        // Calculate monthly statistics
        var monthSuccess = 0
        var monthFail = 0
        
        for (day in 1..currentMonth.lengthOfMonth()) {
            val monthDate = currentMonth.atDay(day)
            if (streakData[monthDate] == true) {
                monthSuccess++
            } else if (streakData[monthDate] == false) {
                monthFail++
            }
        }
        
        successCount = monthSuccess
        failCount = monthFail
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Calendar") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current streak display
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Current Streak",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        
                        Text(
                            "Keep going strong!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Text(
                            "$currentStreak days",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Monthly statistics
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Monthly Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$successCount",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "Success",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$failCount",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Red
                            )
                            Text(
                                "Failed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${successCount + failCount}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                "Total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth = currentMonth.minusMonths(1)
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowLeft,
                        contentDescription = "Previous Month",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                IconButton(
                    onClick = {
                        currentMonth = currentMonth.plusMonths(1)
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowRight,
                        contentDescription = "Next Month",
                        tint = Color.White
                    )
                }
            }
            
            // Days of week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Calendar grid
            val firstDayOfMonth = currentMonth.atDay(1)
            val lastDayOfMonth = currentMonth.atEndOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Adjust for Sunday start (0-indexed)
            
            val daysToShow = mutableListOf<LocalDate?>()
            
            // Add empty spaces for days before the first day of the month
            repeat(firstDayOfWeek) {
                daysToShow.add(null)
            }
            
            // Add all days of the month
            for (day in 1..lastDayOfMonth.dayOfMonth) {
                daysToShow.add(currentMonth.atDay(day))
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysToShow) { date ->
                    if (date == null) {
                        Box(modifier = Modifier.size(40.dp))
                    } else {
                        val isSuccess = streakData[date] == true
                        val isFailed = streakData[date] == false
                        val isToday = date.equals(today)
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSuccess -> Color(0xFF4CAF50)
                                        isFailed -> Color.Red
                                        isToday -> Color(0xFF1E1E1E)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isToday) 
                                        Modifier.border(2.dp, Color.White, CircleShape)
                                    else 
                                        Modifier
                                )
                                .clickable {
                                    selectedDate = date
                                    showDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = when {
                                    isSuccess || isFailed -> Color.White
                                    else -> Color.White
                                },
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            
            if (showDialog && selectedDate != null) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = selectedDate!!.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            
                            Text(
                                text = "How did you do today?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        // Mark as success
                                        streakData[selectedDate!!] = true
                                        
                                        // If this is today, increment streak
                                        if (selectedDate == today) {
                                            streakManager.incrementStreak()
                                            currentStreak = streakManager.getCurrentStreak()
                                        } else {
                                            // Recalculate streak based on consecutive days
                                            currentStreak = streakManager.calculateCurrentStreak()
                                            streakManager.saveCurrentStreak(currentStreak)
                                        }
                                        
                                        // Save streak data
                                        streakManager.saveStreakData(streakData)
                                        
                                        showDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Text("Success")
                                }
                                
                                Button(
                                    onClick = {
                                        // Mark as failed
                                        streakData[selectedDate!!] = false
                                        
                                        // If this is today, reset streak
                                        if (selectedDate == today) {
                                            streakManager.resetStreak()
                                            currentStreak = 0
                                        } else {
                                            // Recalculate streak based on consecutive days
                                            currentStreak = streakManager.calculateCurrentStreak()
                                            streakManager.saveCurrentStreak(currentStreak)
                                        }
                                        
                                        // Save streak data
                                        streakManager.saveStreakData(streakData)
                                        
                                        showDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    ),
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Text("Failed")
                                }
                            }
                            
                            TextButton(
                                onClick = {
                                    streakData.remove(selectedDate!!)
                                    
                                    // Recalculate streak
                                    currentStreak = streakManager.calculateCurrentStreak()
                                    streakManager.saveCurrentStreak(currentStreak)
                                    
                                    // Save streak data
                                    streakManager.saveStreakData(streakData)
                                    
                                    showDialog = false
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.Gray
                                )
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }
    }
}
