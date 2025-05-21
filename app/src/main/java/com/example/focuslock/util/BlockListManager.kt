package com.example.focuslock.util

import android.content.Context
import android.content.SharedPreferences

class BlockListManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "focus_lock_blocklist_prefs"
        private const val KEY_BLOCKED_DOMAINS = "blocked_domains"
        private const val KEY_BLOCKED_APPS = "blocked_apps"
        private const val KEY_BLOCK_YOUTUBE_SHORTS = "block_youtube_shorts"
        private const val KEY_BLOCK_INSTAGRAM_REELS = "block_instagram_reels"
        
        // Default blocked domains - comprehensive list of adult content domains
        val DEFAULT_BLOCKED_DOMAINS = listOf(
            // Major adult content sites
            "pornhub.com",
            "xvideos.com",
            "xnxx.com",
            "xhamster.com",
            "redtube.com",
            "youporn.com",
            "pornhd.com",
            "spankbang.com",
            "tube8.com",
            "brazzers.com",
            "youjizz.com",
            "eporner.com",
            "beeg.com",
            "porntrex.com",
            "pornone.com",
            "porn.com",
            "pornmd.com",
            "pornhd.com",
            "porndig.com",
            "porngo.com",
            "pornhat.com",
            "pornkai.com",
            "pornburst.xxx",
            "pornoxo.com",
            "pornerbros.com",
            "pornheed.com",
            "pornhost.com",
            "pornktube.com",
            "pornlib.com",
            "pornmaki.com",
            "pornmaxim.com",
            "pornorips.com",
            "pornpros.com",
            "pornrabbit.com",
            "pornsocket.com",
            "pornsos.com",
            "porntube.com",
            "pornwatchers.com",
            "pornwhite.com",
            "pornzog.com",
            
            // Image hosting sites with adult content
            "imagefap.com",
            "erotic-pics.com",
            "pornpics.com",
            
            // Adult forums and communities
            "adultfriendfinder.com",
            "fetlife.com",
            
            // Adult webcam sites
            "chaturbate.com",
            "cam4.com",
            "myfreecams.com",
            "bongacams.com",
            "stripchat.com",
            "livejasmin.com",
            
            // Adult dating sites
            "ashleymadison.com",
            "onlyfans.com",
            "fansly.com",
            
            // Adult content aggregators
            "reddit.com/r/nsfw",
            "reddit.com/r/gonewild",
            "reddit.com/r/porn",
            
            // Adult manga/hentai
            "nhentai.net",
            "hentaihaven.xxx",
            "hanime.tv",
            "hentai2read.com",
            "hentaifox.com",
            
            // Adult game sites
            "nutaku.net",
            "f95zone.to",
            
            // Adult content networks
            "mindgeek.com",
            "adultempire.com",
            
            // Adult content search engines
            "pornmd.com",
            "nudevista.com",
            
            // Adult content link sites
            "theporndude.com",
            "porngeek.com",
            
            // Adult content torrent sites
            "pornleech.com",
            "pornorip.com",
            
            // Adult content tube sites
            "txxx.com",
            "upornia.com",
            "hclips.com",
            "hdzog.com",
            "drtuber.com",
            "tnaflix.com",
            "sunporno.com",
            "4tube.com",
            "porntube.com",
            "pornorip.com",
            "porntrex.com",
            "pornhub.org",
            "xvideos2.com",
            "xnxx.tv",
            "xhamster1.com",
            "xhamster2.com",
            "redtube.com.br",
            "youporn.sexy",
            
            // Adult content streaming sites
            "pornflip.com",
            "pornhd8k.com",
            "pornhd.com",
            
            // Adult content download sites
            "pornhub.download",
            "xvideos.download",
            
            // Adult content proxy sites
            "pornhub.proxy.com",
            "xvideos.proxy.com",
            
            // Adult content mirror sites
            "pornhub.mirror.com",
            "xvideos.mirror.com"
        )
    }
    
    fun getBlockedDomains(): List<String> {
        val domainsString = prefs.getString(KEY_BLOCKED_DOMAINS, null)
        return if (domainsString.isNullOrEmpty()) {
            DEFAULT_BLOCKED_DOMAINS
        } else {
            domainsString.split(",").toList() + DEFAULT_BLOCKED_DOMAINS
        }
    }
    
    fun addBlockedDomain(domain: String) {
        val normalizedDomain = domain.trim().lowercase()
        val currentDomains = getCustomBlockedDomains()
        
        if (normalizedDomain !in currentDomains && normalizedDomain !in DEFAULT_BLOCKED_DOMAINS) {
            val newDomainsString = if (currentDomains.isEmpty()) {
                normalizedDomain
            } else {
                "${currentDomains.joinToString(",")},${normalizedDomain}"
            }
            
            prefs.edit().putString(KEY_BLOCKED_DOMAINS, newDomainsString).apply()
        }
    }
    
    fun removeBlockedDomain(domain: String) {
        val normalizedDomain = domain.trim().lowercase()
        
        // Can't remove default domains
        if (normalizedDomain in DEFAULT_BLOCKED_DOMAINS) {
            return
        }
        
        val currentDomains = getCustomBlockedDomains().toMutableList()
        currentDomains.remove(normalizedDomain)
        
        prefs.edit().putString(KEY_BLOCKED_DOMAINS, currentDomains.joinToString(",")).apply()
    }
    
    private fun getCustomBlockedDomains(): List<String> {
        val domainsString = prefs.getString(KEY_BLOCKED_DOMAINS, "")
        return if (domainsString.isNullOrEmpty()) {
            emptyList()
        } else {
            domainsString.split(",")
        }
    }
    
    fun getBlockedApps(): List<String> {
        val appsString = prefs.getString(KEY_BLOCKED_APPS, "")
        return if (appsString.isNullOrEmpty()) {
            emptyList()
        } else {
            appsString.split(",")
        }
    }
    
    fun addBlockedApp(packageName: String) {
        val normalizedPackage = packageName.trim()
        val currentApps = getBlockedApps()
        
        if (normalizedPackage !in currentApps) {
            val newAppsString = if (currentApps.isEmpty()) {
                normalizedPackage
            } else {
                "${currentApps.joinToString(",")},${normalizedPackage}"
            }
            
            prefs.edit().putString(KEY_BLOCKED_APPS, newAppsString).apply()
        }
    }
    
    fun removeBlockedApp(packageName: String) {
        val normalizedPackage = packageName.trim()
        val currentApps = getBlockedApps().toMutableList()
        currentApps.remove(normalizedPackage)
        
        prefs.edit().putString(KEY_BLOCKED_APPS, currentApps.joinToString(",")).apply()
    }
    
    fun isYoutubeShortsBlocked(): Boolean {
        return prefs.getBoolean(KEY_BLOCK_YOUTUBE_SHORTS, false)
    }
    
    fun setYoutubeShortsBlocked(blocked: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_YOUTUBE_SHORTS, blocked).apply()
    }
    
    fun isInstagramReelsBlocked(): Boolean {
        return prefs.getBoolean(KEY_BLOCK_INSTAGRAM_REELS, false)
    }
    
    fun setInstagramReelsBlocked(blocked: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_INSTAGRAM_REELS, blocked).apply()
    }
}
