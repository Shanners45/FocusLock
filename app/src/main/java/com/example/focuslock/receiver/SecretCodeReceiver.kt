package com.example.focuslock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.focuslock.MainActivity
import com.example.focuslock.util.HiddenModeManager

class SecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            val hiddenModeManager = HiddenModeManager(context)
            val secretCode = intent.data?.host ?: return
        
            if (secretCode == hiddenModeManager.getUnlockCode()) {
                Log.d("SecretCodeReceiver", "Secret code matched, unhiding app")
            
                // Unhide the app launcher icon
                hiddenModeManager.showApp(context)
            
                // Launch the app
                val launchIntent = Intent(context, MainActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }
}
