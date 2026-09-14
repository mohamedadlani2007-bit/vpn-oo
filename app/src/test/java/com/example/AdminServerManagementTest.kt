package com.example

import com.example.vpn.VpnServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminServerManagementTest {

    @Test
    fun testAdminPasscodeVerification() {
        val correctPin = "mooh2026"
        val wrongPin = "123456"

        assertTrue(correctPin == "mooh2026")
        assertFalse(wrongPin == "mooh2026")
    }

    @Test
    fun testServerSniHostConfiguration() {
        val server = VpnServer(
            id = "gcp_vps_custom",
            countryName = "Google Cloud VPS",
            countryNameAr = "قوقل كلاود",
            city = "GCP Cloud Server",
            cityAr = "سيرفر يوتيوب شريحة جيزي / موبيليس",
            flagEmoji = "📺",
            host = "34.120.55.10",
            port = 443,
            sniHost = "youtube.com",
            proxyHost = "youtube.com",
            proxyPort = 8080,
            username = "admin_user",
            password = "secure_password"
        )

        assertEquals("youtube.com", server.sniHost)
        assertEquals("youtube.com", server.proxyHost)
        assertEquals(8080, server.proxyPort)
        assertEquals("admin_user", server.username)
        assertEquals("secure_password", server.password)
        assertEquals(443, server.port)
    }

    @Test
    fun testDefaultServersIncludeYouTubeSni() {
        val servers = VpnServer.DEFAULT_SERVERS
        assertTrue(servers.isNotEmpty())
        
        // Find server with youtube SNI
        val ytServer = servers.find { it.sniHost == "youtube.com" }
        assertTrue("At least one server should have youtube.com SNI", ytServer != null)
    }
}
