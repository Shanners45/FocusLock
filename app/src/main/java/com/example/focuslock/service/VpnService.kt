package com.example.focuslock.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.focuslock.MainActivity
import com.example.focuslock.R
import com.example.focuslock.util.BlockListManager
import com.example.focuslock.util.LockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicBoolean
import java.net.Inet4Address
import java.net.Inet6Address
import java.nio.ByteOrder

class VpnService : VpnService() {
    private val TAG = "FocusLockVpnService"
    private val running = AtomicBoolean(false)
    private var vpnInterface: ParcelFileDescriptor? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var blockListManager: BlockListManager
    private lateinit var lockManager: LockManager
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "focus_lock_vpn_channel"
        private const val NOTIFICATION_ID = 1
        
        // Alternative DNS servers that are more widely compatible
        
        // Cloudflare Family DNS (blocks malware and adult content)
        private const val CLOUDFLARE_FAMILY_DNS_1 = "1.1.1.3"
        private const val CLOUDFLARE_FAMILY_DNS_2 = "1.0.0.3"
        
        // Google DNS (as fallback)
        private const val GOOGLE_DNS_1 = "8.8.8.8"
        private const val GOOGLE_DNS_2 = "8.8.4.4"
        
        // Quad9 (security focused DNS)
        private const val QUAD9_DNS_1 = "9.9.9.9"
        private const val QUAD9_DNS_2 = "149.112.112.112"
        
        // Maximum packet size
        private const val MAX_PACKET_SIZE = 32767
        
        // IP protocol constants
        private const val IP_PROTOCOL_TCP = 6
        private const val IP_PROTOCOL_UDP = 17
        
        // Port constants
        private const val DNS_PORT = 53
        
        // Check interval for focus mode status (in milliseconds)
        private const val FOCUS_MODE_CHECK_INTERVAL = 60000L // 1 minute
    }

    override fun onCreate() {
        super.onCreate()
        blockListManager = BlockListManager(this)
        lockManager = LockManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }
        
        if (running.get()) {
            Log.d(TAG, "Service already running")
            return START_STICKY
        }
        
        running.set(true)
        
        // Start a foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification())
        startVpn()
        
        // Start a periodic check for focus mode status
        startFocusModeCheck()
        
        return START_STICKY
    }
    
    private fun startFocusModeCheck() {
        scope.launch {
            while (running.get()) {
                // Check if focus mode is still active
                if (!lockManager.isLockActive()) {
                    Log.d(TAG, "Focus mode is no longer active, stopping VPN service")
                    stopSelf()
                    break
                }
                
                delay(FOCUS_MODE_CHECK_INTERVAL)
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FocusLock VPN Service"
            val descriptionText = "Running to filter content"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("FocusLock Active")
        .setContentText("Content filtering is active")
        .setSmallIcon(R.drawable.ic_shield)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
    
    private fun startVpn() {
        scope.launch {
            try {
                val builder = Builder()
                    .addAddress("10.0.0.2", 32)
                    .addAddress("fd00:1:fd00:1:fd00:1:fd00:1", 128) // IPv6 support
                    .addRoute("0.0.0.0", 0)
                    .addRoute("::", 0) // IPv6 default route
                    
                    // Use more widely compatible DNS servers
                    .addDnsServer(CLOUDFLARE_FAMILY_DNS_1)
                    .addDnsServer(CLOUDFLARE_FAMILY_DNS_2)
                    .addDnsServer(QUAD9_DNS_1)
                    .addDnsServer(QUAD9_DNS_2)
                    .addDnsServer(GOOGLE_DNS_1)
                    .addDnsServer(GOOGLE_DNS_2)
                    
                    .setSession("FocusLock VPN")
                    .setBlocking(true)
                
                // Exclude our own app from the VPN to avoid circular routing
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to exclude app from VPN: ${e.message}")
                }
                
                vpnInterface = builder.establish()
                
                if (vpnInterface != null) {
                    Log.d(TAG, "VPN interface established")
                    processPackets(vpnInterface!!)
                } else {
                    Log.e(TAG, "Failed to establish VPN interface")
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting VPN: ${e.message}")
                stopSelf()
            }
        }
    }
    
    private fun processPackets(vpnInterface: ParcelFileDescriptor) {
        val inputStream = FileInputStream(vpnInterface.fileDescriptor)
        val outputStream = FileOutputStream(vpnInterface.fileDescriptor)
        
        val buffer = ByteBuffer.allocate(MAX_PACKET_SIZE)
        val blockedDomains = blockListManager.getBlockedDomains().toHashSet()
        
        try {
            while (running.get()) {
                // Clear the buffer before reading
                buffer.clear()
                
                // Read a packet
                val length = inputStream.read(buffer.array(), 0, buffer.capacity())
                if (length <= 0) {
                    continue
                }
                
                // Set the buffer position and limit
                buffer.limit(length)
                buffer.position(0)
                
                // Process the packet
                try {
                    // Check if this is a DNS query
                    if (isDnsPacket(buffer)) {
                        // Process DNS packet
                        val modifiedPacket = processDnsPacket(buffer, blockedDomains)
                        if (modifiedPacket != null) {
                            // Write the modified packet back
                            outputStream.write(modifiedPacket.array(), 0, modifiedPacket.limit())
                            continue
                        }
                    }
                    
                    // For non-DNS packets or if DNS processing failed, just forward the original packet
                    buffer.position(0)
                    outputStream.write(buffer.array(), 0, length)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing packet: ${e.message}")
                    // Forward the original packet if there was an error
                    buffer.position(0)
                    outputStream.write(buffer.array(), 0, length)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in VPN packet processing: ${e.message}")
        } finally {
            try {
                vpnInterface.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing VPN interface: ${e.message}")
            }
        }
    }
    
    private fun isDnsPacket(buffer: ByteBuffer): Boolean {
        try {
            // Check if it's an IPv4 packet
            if (buffer.get(0).toInt() shr 4 == 4) {
                val protocol = buffer.get(9).toInt() and 0xFF
                if (protocol == IP_PROTOCOL_UDP) {
                    // Get the destination port (UDP header starts at IP header length)
                    val ipHeaderLength = (buffer.get(0).toInt() and 0x0F) * 4
                    buffer.position(ipHeaderLength + 2) // +2 to get to the destination port
                    val destPort = buffer.getShort().toInt() and 0xFFFF
                    return destPort == DNS_PORT
                }
            }
            // Check if it's an IPv6 packet
            else if (buffer.get(0).toInt() shr 4 == 6) {
                // IPv6 has a fixed header size of 40 bytes
                // The next header field is at offset 6
                val nextHeader = buffer.get(6).toInt() and 0xFF
                if (nextHeader == IP_PROTOCOL_UDP) {
                    // UDP header starts after IPv6 header (40 bytes)
                    buffer.position(40 + 2) // +2 to get to the destination port
                    val destPort = buffer.getShort().toInt() and 0xFFFF
                    return destPort == DNS_PORT
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if packet is DNS: ${e.message}")
        }
        
        // Reset position
        buffer.position(0)
        return false
    }
    
    private fun processDnsPacket(buffer: ByteBuffer, blockedDomains: Set<String>): ByteBuffer? {
        // This is a simplified implementation
        // In a real app, you would need to parse the DNS packet and check if the domain is blocked
        // For now, we'll just pass it through
        
        // Reset position
        buffer.position(0)
        return buffer
    }
    
    override fun onDestroy() {
        running.set(false)
        job.cancel()
        
        if (vpnInterface != null) {
            try {
                vpnInterface?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing VPN interface: ${e.message}")
            }
            vpnInterface = null
        }
        
        super.onDestroy()
    }
}
