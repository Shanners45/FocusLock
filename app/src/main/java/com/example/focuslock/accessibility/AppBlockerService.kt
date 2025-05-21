package com.example.focuslock.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.focuslock.MainActivity
import com.example.focuslock.util.BlockListManager
import com.example.focuslock.util.LockManager

class AppBlockerService : AccessibilityService() {
    private lateinit var blockListManager: BlockListManager
    private lateinit var lockManager: LockManager
    private var lastBackPressTime = 0L
    
    companion object {
        private const val TAG = "AppBlockerService"
        private const val BACK_PRESS_COOLDOWN = 2000L // 2 seconds
        
        // YouTube package and UI identifiers
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val SHORTS_TAB_TEXT = "Shorts"
        private const val SHORTS_PLAYER_ID = "shorts_player"
        private const val SHORTS_CONTAINER_ID = "shorts_container"
        
        // Instagram package and UI identifiers
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val REELS_TAB_TEXT = "Reels"
        private const val REELS_TAB_ID = "tab_reels"
        private const val REELS_PLAYER_ID = "reels_player"
        
        // Browser packages
        private val BROWSER_PACKAGES = listOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.duckduckgo.mobile.android",
            "com.sec.android.app.sbrowser",
            "com.huawei.browser"
        )
        
        // Inappropriate search terms
        private val INAPPROPRIATE_TERMS = listOf(
            "porn", "xxx", "sex", "nude", "naked", "boobs", "tits", "ass", 
            "pussy", "dick", "cock", "penis", "vagina", "fuck", "fucking", 
            "masturbate", "masturbation", "orgasm", "cum", "cumming", 
            "blowjob", "handjob", "anal", "hentai", "rule34", "nsfw", 
            "adult video", "adult content", "adult site", "adult website",
            "pornhub", "xvideos", "xnxx", "xhamster", "redtube", "youporn"
        )
        
        // Incognito mode indicators
        private val INCOGNITO_INDICATORS = listOf(
            "incognito", "private", "private browsing", "private tab", "incognito tab"
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        blockListManager = BlockListManager(this)
        lockManager = LockManager(this)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!lockManager.isLockActive()) {
            return
        }
        
        val packageName = event.packageName?.toString() ?: return
        val blockedApps = blockListManager.getBlockedApps()
        
        // Check if the current app is in the blocked list
        if (blockedApps.contains(packageName)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(this, "App blocked by FocusLock", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check for YouTube Shorts
        if (packageName == YOUTUBE_PACKAGE && blockListManager.isYoutubeShortsBlocked()) {
            val rootNode = rootInActiveWindow ?: return
            
            try {
                // Check if we're in the Shorts section
                val isInShorts = isInYouTubeShorts(rootNode)
                
                if (isInShorts) {
                    // Only press back if we haven't recently done so
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime > BACK_PRESS_COOLDOWN) {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        lastBackPressTime = currentTime
                        Toast.makeText(this, "YouTube Shorts blocked by FocusLock", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking YouTube: ${e.message}")
            } finally {
                rootNode.recycle()
            }
        }
        
        // Check for Instagram Reels
        if (packageName == INSTAGRAM_PACKAGE && blockListManager.isInstagramReelsBlocked()) {
            val rootNode = rootInActiveWindow ?: return
            
            try {
                // Check if we're in the Reels section
                val isInReels = isInInstagramReels(rootNode)
                
                if (isInReels) {
                    // Only press back if we haven't recently done so
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime > BACK_PRESS_COOLDOWN) {
                        performGlobalAction(GLOBAL_ACTION_BACK)
                        lastBackPressTime = currentTime
                        Toast.makeText(this, "Instagram Reels blocked by FocusLock", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking Instagram: ${e.message}")
            } finally {
                rootNode.recycle()
            }
        }
        
        // Check for inappropriate search terms in browsers
        if (BROWSER_PACKAGES.contains(packageName)) {
            val rootNode = rootInActiveWindow ?: return
            
            try {
                // Check if we're in incognito mode
                val isIncognito = isInIncognitoMode(rootNode)
                
                // Check for inappropriate content in text fields
                val hasInappropriateContent = checkForInappropriateContent(rootNode)
                
                // If in incognito mode and inappropriate content is detected, force close the app
                if (isIncognito && hasInappropriateContent) {
                    Log.d(TAG, "Detected inappropriate search in incognito mode, closing app")
                    
                    // Go to home screen
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    
                    // Show a toast message
                    Toast.makeText(
                        this, 
                        "Inappropriate content in incognito mode detected. App closed.", 
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Launch our app to show the user why it was closed
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    
                    return
                }
                
                // For non-incognito mode, just block the content if inappropriate
                if (hasInappropriateContent) {
                    // Go back to home screen
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    Toast.makeText(this, "Inappropriate content blocked by FocusLock", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking browser content: ${e.message}")
            } finally {
                rootNode.recycle()
            }
        }
    }
    
    private fun isInIncognitoMode(rootNode: AccessibilityNodeInfo): Boolean {
        // Look for incognito indicators in the UI
        for (indicator in INCOGNITO_INDICATORS) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(indicator)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return true
            }
        }
        
        // Check for specific UI elements that might indicate incognito mode
        // This is browser-specific and might need adjustments
        
        // Chrome incognito has a dark theme and specific UI elements
        val packageName = rootNode.packageName?.toString() ?: ""
        if (packageName == "com.android.chrome") {
            // Check for the incognito icon or mask icon
            val maskNodes = findNodesByViewIdContains(rootNode, "incognito_badge")
            if (maskNodes.isNotEmpty()) {
                maskNodes.forEach { it.recycle() }
                return true
            }
        }
        
        return false
    }
    
    private fun checkForInappropriateContent(rootNode: AccessibilityNodeInfo): Boolean {
        // Check all text fields for inappropriate content
        val editableNodes = findEditableNodes(rootNode)
        
        for (node in editableNodes) {
            val text = node.text?.toString()?.lowercase() ?: continue
            
            // Check if the text contains any inappropriate terms
            for (term in INAPPROPRIATE_TERMS) {
                if (text.contains(term)) {
                    Log.d(TAG, "Found inappropriate term: $term in text: $text")
                    return true
                }
            }
        }
        
        // Only check URL bar and search results for inappropriate content
        // Don't check all visible text to avoid false positives
        val urlBarNodes = findNodesByViewIdContains(rootNode, "url_bar")
        val searchBoxNodes = findNodesByViewIdContains(rootNode, "search_box")
        
        val nodesToCheck = urlBarNodes + searchBoxNodes
        
        for (node in nodesToCheck) {
            val text = node.text?.toString()?.lowercase() ?: continue
            
            // Check if the text contains any inappropriate terms
            for (term in INAPPROPRIATE_TERMS) {
                if (text.contains(term)) {
                    Log.d(TAG, "Found inappropriate term: $term in URL/search: $text")
                    return true
                }
            }
        }
        
        return false
    }
    
    private fun findEditableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findEditableNodesRecursive(rootNode, result)
        return result
    }
    
    private fun findEditableNodesRecursive(node: AccessibilityNodeInfo, result: MutableList<AccessibilityNodeInfo>) {
        if (node.isEditable) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findEditableNodesRecursive(child, result)
        }
    }
    
    private fun findTextNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findTextNodesRecursive(rootNode, result)
        return result
    }
    
    private fun findTextNodesRecursive(node: AccessibilityNodeInfo, result: MutableList<AccessibilityNodeInfo>) {
        if (node.text != null) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findTextNodesRecursive(child, result)
        }
    }
    
    private fun isInYouTubeShorts(rootNode: AccessibilityNodeInfo): Boolean {
        // Method 1: Check for the Shorts tab being selected
        val shortsTabNodes = rootNode.findAccessibilityNodeInfosByText(SHORTS_TAB_TEXT)
        for (node in shortsTabNodes) {
            if (node.isSelected) {
                node.recycle()
                return true
            }
            node.recycle()
        }
        
        // Method 2: Check for Shorts player or container by resource ID
        val shortsPlayerNodes = findNodesByViewIdContains(rootNode, SHORTS_PLAYER_ID)
        if (shortsPlayerNodes.isNotEmpty()) {
            shortsPlayerNodes.forEach { it.recycle() }
            return true
        }
        
        val shortsContainerNodes = findNodesByViewIdContains(rootNode, SHORTS_CONTAINER_ID)
        if (shortsContainerNodes.isNotEmpty()) {
            shortsContainerNodes.forEach { it.recycle() }
            return true
        }
        
        // Method 3: Check for vertical video player UI characteristics
        // This is a heuristic approach that looks for the typical Shorts UI layout
        val possibleShortsUI = findVerticalVideoPlayerUI(rootNode)
        
        return possibleShortsUI
    }
    
    private fun isInInstagramReels(rootNode: AccessibilityNodeInfo): Boolean {
        // Method 1: Check for the Reels tab being selected
        val reelsTabNodes = rootNode.findAccessibilityNodeInfosByText(REELS_TAB_TEXT)
        for (node in reelsTabNodes) {
            if (node.isSelected) {
                node.recycle()
                return true
            }
            node.recycle()
        }
        
        // Method 2: Check for Reels tab by ID
        val reelsTabIdNodes = findNodesByViewIdContains(rootNode, REELS_TAB_ID)
        for (node in reelsTabIdNodes) {
            if (node.isSelected) {
                node.recycle()
                return true
            }
            node.recycle()
        }
        
        // Method 3: Check for Reels player by ID
        val reelsPlayerNodes = findNodesByViewIdContains(rootNode, REELS_PLAYER_ID)
        if (reelsPlayerNodes.isNotEmpty()) {
            reelsPlayerNodes.forEach { it.recycle() }
            return true
        }
        
        // Method 4: Check for Reels UI characteristics
        // Only check for vertical video player if we're in the Reels section
        // This prevents false positives that could shut down the entire app
        val hasReelsText = findNodeWithText(rootNode, "Reels")
        if (hasReelsText) {
            val possibleReelsUI = findVerticalVideoPlayerUI(rootNode)
            return possibleReelsUI
        }
        
        return false
    }
    
    private fun findNodesByViewIdContains(rootNode: AccessibilityNodeInfo, idSubstring: String): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findNodesByViewIdContainsRecursive(rootNode, idSubstring, result)
        return result
    }
    
    private fun findNodesByViewIdContainsRecursive(node: AccessibilityNodeInfo, idSubstring: String, result: MutableList<AccessibilityNodeInfo>) {
        val viewId = node.viewIdResourceName
        if (viewId != null && viewId.contains(idSubstring)) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesByViewIdContainsRecursive(child, idSubstring, result)
        }
    }
    
    private fun findVerticalVideoPlayerUI(rootNode: AccessibilityNodeInfo): Boolean {
        // This is a simplified heuristic to detect vertical video players like Shorts/Reels
        // In a real implementation, you would need more sophisticated detection
        
        // Check for vertical scroll view with video controls
        val scrollableNodes = findScrollableNodes(rootNode)
        for (node in scrollableNodes) {
            // Check if this scrollable container has video player characteristics
            if (hasVideoPlayerCharacteristics(node)) {
                return true
            }
        }
        
        return false
    }
    
    private fun findScrollableNodes(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        findScrollableNodesRecursive(rootNode, result)
        return result
    }
    
    private fun findScrollableNodesRecursive(node: AccessibilityNodeInfo, result: MutableList<AccessibilityNodeInfo>) {
        if (node.isScrollable) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findScrollableNodesRecursive(child, result)
        }
    }
    
    private fun hasVideoPlayerCharacteristics(node: AccessibilityNodeInfo): Boolean {
        // Check for play/pause buttons
        val playPauseNodes = findNodesByText(node, listOf("play", "pause"))
        if (playPauseNodes.isNotEmpty()) {
            return true
        }
        
        // Check for like/comment buttons (common in Shorts/Reels)
        val socialActionNodes = findNodesByText(node, listOf("like", "comment", "share"))
        if (socialActionNodes.size >= 2) { // If at least 2 of these are present
            return true
        }
        
        return false
    }
    
    private fun findNodesByText(node: AccessibilityNodeInfo, textList: List<String>): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        
        for (text in textList) {
            val nodes = node.findAccessibilityNodeInfosByText(text)
            result.addAll(nodes)
        }
        
        return result
    }
    
    private fun findNodeWithText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return true
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                if (findNodeWithText(child, text)) {
                    return true
                }
            } finally {
                child.recycle()
            }
        }
        
        return false
    }
    
    override fun onInterrupt() {
        // Not used
    }
}
