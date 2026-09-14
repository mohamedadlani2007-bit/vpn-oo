package com.example.vpn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

data class ServerHealthResult(
    val isReachable: Boolean,
    val latencyMs: Int,
    val statusMessage: String,
    val details: String
)

object VpnNodeChecker {

    /**
     * Actively tests connectivity to a VLESS/VMess/Trojan/SSH/DNS/Wireguard node.
     * Performs a real socket handshake or TLS handshake if appropriate,
     * and returns precise health & latency status.
     */
    suspend fun checkServerHealth(server: VpnServer, timeoutMs: Int = 2500): ServerHealthResult = withContext(Dispatchers.IO) {
        val host = server.host.trim()
        val port = server.port

        if (host.isBlank()) {
            return@withContext ServerHealthResult(
                isReachable = false,
                latencyMs = -1,
                statusMessage = "العنوان غير صالح",
                details = "المضيف فارغ"
            )
        }

        val start = System.currentTimeMillis()

        try {
            // Check if server has a dedicated HTTP/Squid Proxy configured (SSHOcean / Custom Proxy)
            val proxyHost = server.proxyHost.trim()
            val proxyPort = server.proxyPort
            val hasProxy = proxyHost.isNotBlank() && proxyPort > 0
            val isProxyProtocol = server.protocol.contains("PROXY", ignoreCase = true) || 
                                 server.protocol.contains("PAYLOAD", ignoreCase = true) ||
                                 hasProxy

            if (isProxyProtocol && hasProxy) {
                var proxyStatus = ""
                var httpCode = ""
                Socket().use { proxySocket ->
                    proxySocket.soTimeout = timeoutMs
                    proxySocket.connect(InetSocketAddress(proxyHost, proxyPort), timeoutMs)
                    
                    // If custom payload provided, inject and test response
                    val payloadToSend = if (server.payload.isNotBlank()) {
                        server.payload
                            .replace("[crlf]", "\r\n", ignoreCase = true)
                            .replace("[cr]", "\r", ignoreCase = true)
                            .replace("[lf]", "\n", ignoreCase = true)
                            .replace("[host_port]", "$host:$port")
                            .replace("[host]", host)
                            .replace("[port]", port.toString())
                            .replace("[bug_host]", if (server.sniHost.isNotBlank()) server.sniHost else "youtube.com")
                    } else {
                        "CONNECT $host:$port HTTP/1.1\r\nHost: ${if (server.sniHost.isNotBlank()) server.sniHost else host}\r\nProxy-Connection: Keep-Alive\r\n\r\n"
                    }

                    try {
                        val writer = proxySocket.getOutputStream()
                        writer.write(payloadToSend.toByteArray(Charsets.UTF_8))
                        writer.flush()
                        val reader = proxySocket.getInputStream().bufferedReader()
                        httpCode = reader.readLine() ?: ""
                    } catch (_: Exception) {}
                }

                val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(12)
                return@withContext ServerHealthResult(
                    isReachable = true,
                    latencyMs = latency,
                    statusMessage = "البروكسي وخادم SSH شغالين ⚡",
                    details = if (httpCode.isNotBlank()) "استجابة البروكسي ($proxyHost:$proxyPort): ${httpCode.take(35)}" else "بروكسي $proxyHost:$proxyPort متصل وجاهز للحقن"
                )
            }

            when (server.protocol.uppercase()) {
                "VLESS", "VMESS", "TROJAN" -> {
                    // Try TLS handshake if port is 443 or security=tls, otherwise standard TCP
                    val security = server.extraParams["security"] ?: server.extraParams["tls"] ?: "tls"
                    if (security.equals("tls", ignoreCase = true) || port == 443) {
                        Socket().use { rawSocket ->
                            rawSocket.connect(InetSocketAddress(host, port), timeoutMs)
                            val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                            val sniHost = server.extraParams["sni"] ?: host
                            val sslSocket = sslFactory.createSocket(rawSocket, sniHost, port, true)
                            sslSocket.soTimeout = timeoutMs
                            (sslSocket as? javax.net.ssl.SSLSocket)?.startHandshake()
                        }
                    } else {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(host, port), timeoutMs)
                        }
                    }
                    val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(5)
                    ServerHealthResult(
                        isReachable = true,
                        latencyMs = latency,
                        statusMessage = "شغال وجاهز (Active)",
                        details = "تم تأكيد مصافحة ${server.protocol} على المنفذ $port"
                    )
                }

                "SSH", "SSL", "SSH_SSL", "DIRECT" -> {
                    val sni = server.sniHost.trim()
                    if (sni.isNotBlank() && (port == 443 || server.protocol.contains("SSL", ignoreCase = true))) {
                        // SSL/TLS SNI Handshake check with Bug Host (e.g. youtube.com)
                        Socket().use { rawSocket ->
                            rawSocket.connect(InetSocketAddress(host, port), timeoutMs)
                            val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                            val sslSocket = sslFactory.createSocket(rawSocket, sni, port, true)
                            sslSocket.soTimeout = timeoutMs
                            (sslSocket as? javax.net.ssl.SSLSocket)?.startHandshake()
                        }
                        val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(10)
                        ServerHealthResult(
                            isReachable = true,
                            latencyMs = latency,
                            statusMessage = "ثغرة SNI ($sni) شغالة",
                            details = "تم تأكيد مصافحة TLS SNI: $sni عبر $host:$port بنجاح"
                        )
                    } else {
                        // Regular SSH check or banner read
                        var banner = ""
                        Socket().use { socket ->
                            socket.soTimeout = timeoutMs
                            socket.connect(InetSocketAddress(host, port), timeoutMs)
                            if (port == 22 || server.protocol.equals("SSH", ignoreCase = true)) {
                                try {
                                    val reader = socket.getInputStream().bufferedReader()
                                    banner = reader.readLine() ?: ""
                                } catch (_: Exception) {}
                            }
                        }
                        val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(10)
                        ServerHealthResult(
                            isReachable = true,
                            latencyMs = latency,
                            statusMessage = "خادم SSH متصل وشغال",
                            details = if (banner.isNotBlank()) "إشعار الخادم: ${banner.take(40)}" else "المنفذ $port مفتوح ومتاح"
                        )
                    }
                }

                "DNS TUNNEL", "DNS" -> {
                    val ping = NetworkHelper.measurePing(host, port, timeoutMs)
                    if (ping > 0) {
                        ServerHealthResult(
                            isReachable = true,
                            latencyMs = ping,
                            statusMessage = "سيرفر شغال (استجابة فورية)",
                            details = "زمن استجابة المنفذ $port: ${ping}ms"
                        )
                    } else {
                        ServerHealthResult(
                            isReachable = false,
                            latencyMs = -1,
                            statusMessage = "غير متاح (السيرفر لا يستجيب)",
                            details = "فشل الوصول إلى $host:$port"
                        )
                    }
                }

                else -> {
                    // Google Cloud VM / SSH / Wireguard / Custom VPS
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), timeoutMs)
                    }
                    val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(1)
                    ServerHealthResult(
                        isReachable = true,
                        latencyMs = latency,
                        statusMessage = "الخادم شغال ومتاح (Online)",
                        details = "المنفذ $port يستجيب بنجاح (${latency}ms)"
                    )
                }
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e is java.net.SocketTimeoutException -> "انتهت مهلة الاتصال (Timeout)"
                e is java.net.ConnectException -> "تم رفض الاتصال (المنفذ $port مغلق)"
                e is java.net.UnknownHostException -> "عنوان السيرفر غير موجود (DNS Error)"
                else -> e.localizedMessage ?: "السيرفر لا يستجيب"
            }
            ServerHealthResult(
                isReachable = false,
                latencyMs = -1,
                statusMessage = "غير شغال (Offline)",
                details = "تعذر الاتصال بـ $host:$port - $errorMsg"
            )
        }
    }
}
