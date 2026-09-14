package com.example

import com.example.vpn.VpnConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VpnConfigParserTest {

    @Test
    fun testParseVlessDirectLink() {
        val vlessLink = "vless://auto-uuid-12345@1.1.1.1:443?type=ws&security=tls&sni=example.com&path=/vless#MyVlessNode"
        val result = VpnConfigParser.parse(vlessLink)

        assertTrue(result.isSuccess)
        val server = result.getOrNull()
        assertNotNull(server)
        assertEquals("1.1.1.1", server?.host)
        assertEquals(443, server?.port)
        assertEquals("VLESS", server?.protocol)
        assertEquals("MyVlessNode", server?.cityAr)
        assertEquals("tls", server?.extraParams?.get("security"))
        assertEquals("example.com", server?.extraParams?.get("sni"))
    }

    @Test
    fun testParseSshDirectLink() {
        val sshLink = "ssh://root:secretpass@192.168.1.1:22#SSH-Fast-Tunnel"
        val result = VpnConfigParser.parse(sshLink)

        assertTrue(result.isSuccess)
        val server = result.getOrNull()
        assertNotNull(server)
        assertEquals("192.168.1.1", server?.host)
        assertEquals(22, server?.port)
        assertEquals("SSH", server?.protocol)
        assertEquals("SSH-Fast-Tunnel", server?.cityAr)
        assertEquals("root", server?.extraParams?.get("user"))
    }

    @Test
    fun testParseDnsDirectLink() {
        val dnsLink = "dns://1.1.1.1:53#CloudflareDns"
        val result = VpnConfigParser.parse(dnsLink)

        assertTrue(result.isSuccess)
        val server = result.getOrNull()
        assertNotNull(server)
        assertEquals("1.1.1.1", server?.host)
        assertEquals("DNS Tunnel", server?.protocol)
    }

    @Test
    fun testParseInvalidSchemeReturnsFailure() {
        val invalidLink = "unknown://some-link"
        val result = VpnConfigParser.parse(invalidLink)

        assertTrue(result.isFailure)
    }
}
