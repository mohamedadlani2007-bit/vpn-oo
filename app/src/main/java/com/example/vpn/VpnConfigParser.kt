package com.example.vpn

import android.net.Uri
import android.util.Base64
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class VpnConfigParserResult(
    val server: VpnServer,
    val rawConfig: String
)

object VpnConfigParser {

    /**
     * Parses standard V2Ray/Xray & SSH & Shadowsocks & Trojan & DNS URI schemes:
     * - vless://uuid@host:port?type=ws&security=tls&path=/path#Remark
     * - vmess://base64(json)
     * - trojan://password@host:port?security=tls#Remark
     * - ss://base64(method:password@host:port)#Remark or ss://method:password@host:port#Remark
     * - ssh://username:password@host:port#Remark
     * - dns://host:port#Remark or doh:// / dot://
     */
    fun parse(rawInput: String): Result<VpnServer> {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("الرابط فارغ، يرجى إدخال رابط تهيئة صالح"))
        }

        return try {
            when {
                trimmed.startsWith("vless://", ignoreCase = true) -> parseVless(trimmed)
                trimmed.startsWith("vmess://", ignoreCase = true) -> parseVmess(trimmed)
                trimmed.startsWith("trojan://", ignoreCase = true) -> parseTrojan(trimmed)
                trimmed.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(trimmed)
                trimmed.startsWith("ssh://", ignoreCase = true) -> parseSsh(trimmed)
                trimmed.startsWith("dns://", ignoreCase = true) ||
                trimmed.startsWith("doh://", ignoreCase = true) ||
                trimmed.startsWith("dot://", ignoreCase = true) -> parseDns(trimmed)
                else -> Result.failure(
                    IllegalArgumentException("البروتوكول غير مدعوم. يدعم التطبيق: vless://, vmess://, trojan://, ss://, ssh://, dns://")
                )
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("فشل قراءة وتفكيك الرابط: ${e.message}"))
        }
    }

    /**
     * Parse vless://uuid@host:port?security=reality&sni=...&path=...#Remark
     */
    private fun parseVless(uriStr: String): Result<VpnServer> {
        val uri = Uri.parse(uriStr)
        val uuid = uri.userInfo ?: ""
        val host = uri.host ?: throw IllegalArgumentException("عنوان المضيف (Host) مفقود في رابط VLESS")
        val port = if (uri.port > 0) uri.port else 443
        val rawRemark = uri.fragment ?: uri.getQueryParameter("remarks") ?: "VLESS Server"
        val remark = decodeUrl(rawRemark)
        val security = uri.getQueryParameter("security") ?: "tls"
        val transport = uri.getQueryParameter("type") ?: "tcp"
        val sni = uri.getQueryParameter("sni") ?: host
        val path = uri.getQueryParameter("path") ?: "/"

        val server = VpnServer(
            id = "vless_${System.currentTimeMillis()}_${(1000..9999).random()}",
            countryName = "VLESS Node ($transport/$security)",
            countryNameAr = "سيرفر VLESS مشفر",
            city = if (remark.isNotBlank()) remark else host,
            cityAr = if (remark.isNotBlank()) remark else host,
            flagEmoji = "⚡",
            host = host,
            port = port,
            dnsServer = "1.1.1.1",
            secondaryDns = "8.8.8.8",
            pingMs = -1,
            loadPercent = (15..45).random(),
            protocol = "VLESS",
            isCustom = true,
            rawConfig = uriStr,
            extraParams = mapOf(
                "uuid" to uuid,
                "security" to security,
                "type" to transport,
                "sni" to sni,
                "path" to path
            )
        )
        return Result.success(server)
    }

    /**
     * Parse vmess://base64(json)
     */
    private fun parseVmess(uriStr: String): Result<VpnServer> {
        val b64 = uriStr.removePrefix("vmess://").trim()
        val decodedJsonStr = decodeBase64Safe(b64)
            ?: throw IllegalArgumentException("تشفير Base64 لرابط VMess غير صالح")

        val json = JSONObject(decodedJsonStr)
        val host = json.optString("add").ifBlank {
            throw IllegalArgumentException("عنوان المضيف مفقود في إعدادات VMess (حقل add)")
        }
        val port = json.optInt("port", 443)
        val uuid = json.optString("id", "")
        val ps = json.optString("ps", "VMess Node").ifBlank { "VMess Server" }
        val net = json.optString("net", "ws")
        val tls = json.optString("tls", "none")
        val sni = json.optString("sni", host)
        val path = json.optString("path", "/")

        val server = VpnServer(
            id = "vmess_${System.currentTimeMillis()}_${(1000..9999).random()}",
            countryName = "VMess Node ($net/$tls)",
            countryNameAr = "سيرفر VMess مشفر",
            city = ps,
            cityAr = ps,
            flagEmoji = "🛡️",
            host = host,
            port = port,
            dnsServer = "1.1.1.1",
            secondaryDns = "8.8.8.8",
            pingMs = -1,
            loadPercent = (20..50).random(),
            protocol = "VMESS",
            isCustom = true,
            rawConfig = uriStr,
            extraParams = mapOf(
                "uuid" to uuid,
                "net" to net,
                "tls" to tls,
                "sni" to sni,
                "path" to path
            )
        )
        return Result.success(server)
    }

    /**
     * Parse trojan://password@host:port?security=tls&sni=...#Remark
     */
    private fun parseTrojan(uriStr: String): Result<VpnServer> {
        val uri = Uri.parse(uriStr)
        val password = uri.userInfo ?: ""
        val host = uri.host ?: throw IllegalArgumentException("المضيف مفقود في رابط Trojan")
        val port = if (uri.port > 0) uri.port else 443
        val rawRemark = uri.fragment ?: "Trojan Server"
        val remark = decodeUrl(rawRemark)
        val sni = uri.getQueryParameter("sni") ?: host

        val server = VpnServer(
            id = "trojan_${System.currentTimeMillis()}_${(1000..9999).random()}",
            countryName = "Trojan Tunnel",
            countryNameAr = "سيرفر Trojan مشفر",
            city = if (remark.isNotBlank()) remark else host,
            cityAr = if (remark.isNotBlank()) remark else host,
            flagEmoji = "🐎",
            host = host,
            port = port,
            dnsServer = "1.1.1.1",
            secondaryDns = "8.8.8.8",
            pingMs = -1,
            loadPercent = (10..40).random(),
            protocol = "Trojan",
            isCustom = true,
            rawConfig = uriStr,
            extraParams = mapOf(
                "password" to password,
                "sni" to sni
            )
        )
        return Result.success(server)
    }

    /**
     * Parse ss://base64(method:password@host:port)#Remark
     */
    private fun parseShadowsocks(uriStr: String): Result<VpnServer> {
        val withoutPrefix = uriStr.removePrefix("ss://")
        val hashIndex = withoutPrefix.indexOf('#')
        val mainPart = if (hashIndex != -1) withoutPrefix.substring(0, hashIndex) else withoutPrefix
        val remark = if (hashIndex != -1) decodeUrl(withoutPrefix.substring(hashIndex + 1)) else "Shadowsocks Server"

        // Format 1: ss://BASE64(method:password@host:port)
        // Format 2: ss://BASE64(method:password)@host:port
        val decodedPart = decodeBase64Safe(mainPart)
        val (methodPassword, hostPort) = if (decodedPart != null && decodedPart.contains("@")) {
            val parts = decodedPart.split("@", limit = 2)
            Pair(parts[0], parts[1])
        } else if (mainPart.contains("@")) {
            val parts = mainPart.split("@", limit = 2)
            val userInfo = decodeBase64Safe(parts[0]) ?: parts[0]
            Pair(userInfo, parts[1])
        } else {
            throw IllegalArgumentException("صيغة رابط Shadowsocks غير معروفة")
        }

        val hostAndPort = hostPort.split(":", limit = 2)
        val host = hostAndPort[0]
        val port = if (hostAndPort.size > 1) hostAndPort[1].toIntOrNull() ?: 8388 else 8388

        val server = VpnServer(
            id = "ss_${System.currentTimeMillis()}_${(1000..9999).random()}",
            countryName = "Shadowsocks",
            countryNameAr = "سيرفر Shadowsocks (SS)",
            city = if (remark.isNotBlank()) remark else host,
            cityAr = if (remark.isNotBlank()) remark else host,
            flagEmoji = "🚀",
            host = host,
            port = port,
            dnsServer = "1.1.1.1",
            secondaryDns = "8.8.8.8",
            pingMs = -1,
            loadPercent = 30,
            protocol = "Shadowsocks",
            isCustom = true,
            rawConfig = uriStr,
            extraParams = mapOf(
                "credentials" to methodPassword
            )
        )
        return Result.success(server)
    }

    /**
     * Parse ssh://username:password@host:port#Remark
     */
    private fun parseSsh(uriStr: String): Result<VpnServer> {
        val uri = Uri.parse(uriStr)
        val userInfo = uri.userInfo ?: ""
        val host = uri.host ?: throw IllegalArgumentException("عنوان الخادم مفقود في رابط SSH")
        val port = if (uri.port > 0) uri.port else 22
        val rawRemark = uri.fragment ?: "SSH Tunnel"
        val remark = decodeUrl(rawRemark)

        val parts = userInfo.split(":", limit = 2)
        val user = parts.getOrNull(0) ?: "root"
        val pass = parts.getOrNull(1) ?: ""

        val server = VpnServer(
            id = "ssh_${System.currentTimeMillis()}_${(1000..9999).random()}",
            countryName = "SSH Secure Tunnel",
            countryNameAr = "نفق SSH مشفر",
            city = if (remark.isNotBlank()) remark else "$user@$host",
            cityAr = if (remark.isNotBlank()) remark else "$user@$host",
            flagEmoji = "🔑",
            host = host,
            port = port,
            dnsServer = "1.1.1.1",
            secondaryDns = "8.8.8.8",
            pingMs = -1,
            loadPercent = 25,
            protocol = "SSH",
            isCustom = true,
            rawConfig = uriStr,
            extraParams = mapOf(
                "user" to user,
                "password" to pass
            )
        )
        return Result.success(server)
    }

    /**
     * Parse dns://8.8.8.8#Remark or doh://...
     */
    private fun parseDns(uriStr: String): Result<VpnServer> {
        val uri = Uri.parse(uriStr)
        val host = uri.host ?: uri.schemeSpecificPart.removePrefix("//").substringBefore('#').substringBefore(':')
        val port = if (uri.port > 0) uri.port else 53
        val rawRemark = uri.fragment ?: "Secure DNS"
        val remark = decodeUrl(rawRemark)

        val server = VpnServer(
            id = "dns_${System.currentTimeMillis()}_${(1000..9999).random()}",
            countryName = "Encrypted DNS Tunnel",
            countryNameAr = "نفق DNS مشفر ومانع حجب",
            city = if (remark.isNotBlank()) remark else host,
            cityAr = if (remark.isNotBlank()) remark else host,
            flagEmoji = "🌐",
            host = host,
            port = port,
            dnsServer = host,
            secondaryDns = "1.1.1.1",
            pingMs = -1,
            loadPercent = 10,
            protocol = "DNS Tunnel",
            isCustom = true,
            rawConfig = uriStr
        )
        return Result.success(server)
    }

    private fun decodeBase64Safe(input: String): String? {
        val clean = input.trim().replace("\n", "").replace("\r", "")
        return try {
            val decoded = Base64.decode(clean, Base64.DEFAULT or Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            String(decoded, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            try {
                // Pad if missing
                var padded = clean
                while (padded.length % 4 != 0) {
                    padded += "="
                }
                val decoded = Base64.decode(padded, Base64.DEFAULT or Base64.URL_SAFE or Base64.NO_WRAP)
                String(decoded, StandardCharsets.UTF_8)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun decodeUrl(str: String): String {
        return try {
            URLDecoder.decode(str, "UTF-8")
        } catch (_: Exception) {
            str
        }
    }
}
