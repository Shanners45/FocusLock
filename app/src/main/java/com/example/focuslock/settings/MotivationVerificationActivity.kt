package com.example.focuslock.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.focuslock.ui.theme.FocusLockTheme

class MotivationVerificationActivity : ComponentActivity() {
    
    companion object {
        const val REQUIRED_WORD_COUNT = 100
        
        // Sample motivational paragraphs to show as examples
        val SAMPLE_PARAGRAPHS = listOf(
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
            
            "Believe in yourself and all that you are. Know that there is something inside you that is greater than any obstacle. " +
            "Your time is limited, so don't waste it living someone else's life. " +
            "Challenges are what make life interesting and overcoming them is what makes life meaningful. " +
            "If you want to achieve greatness stop asking for permission. " +
            "Things work out best for those who make the best of how things work out. " +
            "To live a creative life, we must lose our fear of being wrong. " +
            "If you are not willing to risk the usual you will have to settle for the ordinary. " +
            "Trust because you are willing to accept the risk, not because it's safe or certain. " +
            "All our dreams can come true if we have the courage to pursue them. " +
            "Good things come to people who wait, but better things come to those who go out and get them."
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FocusLockTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MotivationVerificationScreen(
                        onBackPressed = { finish() },
                        onVerificationSuccess = {
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotivationVerificationScreen(
    onBackPressed: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
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
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                "This helps reinforce your commitment to staying focused and avoiding distractions.",
                style = MaterialTheme.typography.bodyMedium
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
                        if (wordCount >= MotivationVerificationActivity.REQUIRED_WORD_COUNT) {
                            onVerificationSuccess()
                        } else {
                            showError = true
                        }
                        focusManager.clearFocus()
                    }
                ),
                isError = showError
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Word count: $wordCount/${MotivationVerificationActivity.REQUIRED_WORD_COUNT}",
                    color = if (wordCount >= MotivationVerificationActivity.REQUIRED_WORD_COUNT) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (wordCount >= MotivationVerificationActivity.REQUIRED_WORD_COUNT) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Sufficient word count",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (showError) {
                Text(
                    "Please write at least ${MotivationVerificationActivity.REQUIRED_WORD_COUNT} words to continue",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = {
                    if (wordCount >= MotivationVerificationActivity.REQUIRED_WORD_COUNT) {
                        onVerificationSuccess()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = wordCount >= MotivationVerificationActivity.REQUIRED_WORD_COUNT
            ) {
                Text("Access Settings")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Example motivational paragraph:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        MotivationVerificationActivity.SAMPLE_PARAGRAPHS.random(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
