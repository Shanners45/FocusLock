package com.example.focuslock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationVerificationScreen(
    onBackPressed: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    val requiredWordCount = 100
    var motivationText by remember { mutableStateOf("") }
    var wordCount by remember { mutableStateOf(0) }
    var showError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // Calculate word count
    LaunchedEffect(motivationText) {
        wordCount = motivationText.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        showError = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motivation Verification") },
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "To access settings, please type a 100-word motivational paragraph",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Text(
                "This helps reinforce your commitment to staying focused and avoiding distractions.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            OutlinedTextField(
                value = motivationText,
                onValueChange = { motivationText = it },
                label = { Text("Your motivational paragraph") },
                placeholder = { Text("Start typing...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (wordCount >= requiredWordCount) {
                            onVerificationSuccess()
                        } else {
                            showError = true
                        }
                        focusManager.clearFocus()
                    }
                ),
                isError = showError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red,
                    cursorColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Word count: $wordCount/$requiredWordCount",
                    color = if (wordCount >= requiredWordCount) 
                        Color(0xFF4CAF50)
                    else 
                        Color.White
                )
                
                if (wordCount >= requiredWordCount) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Sufficient word count",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
            
            if (showError) {
                Text(
                    "Please write at least $requiredWordCount words to continue",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = {
                    if (wordCount >= requiredWordCount) {
                        onVerificationSuccess()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = wordCount >= requiredWordCount,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color.Gray
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text("Access Settings")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Example motivational paragraph:",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Success is not final, failure is not fatal: It is the courage to continue that counts. " +
                        "The road to success and the road to failure are almost exactly the same. " +
                        "It is better to fail in originality than to succeed in imitation. " +
                        "The pessimist sees difficulty in every opportunity. The optimist sees opportunity in every difficulty. " +
                        "Don't let yesterday take up too much of today. " +
                        "You learn more from failure than from success. Don't let it stop you. Failure builds character. " +
                        "If you are working on something that you really care about, you don't have to be pushed. The vision pulls you. " +
                        "Experience is a hard teacher because she gives the test first, the lesson afterward. " +
                        "To know how much there is to know is the beginning of learning to live. " +
                        "Goal setting is the secret to a compelling future.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
