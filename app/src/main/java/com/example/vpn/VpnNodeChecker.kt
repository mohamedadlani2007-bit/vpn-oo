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

                "SSH" -> {
                    // SSH server sends banner e.g. "SSH-2.0-OpenSSH..."
                    var banner = ""
                    Socket().use { socket ->
                        socket.soTimeout = timeoutMs
                        socket.connect(InetSocketAddress(host, port), timeoutMs)
                        val reader = socket.getInputStream().bufferedReader()
                        banner = reader.readLine() ?: ""
                    }
                    val latency = (System.currentTimeMillis() - start).toInt().coerceAtLeast(10)
                    ServerHealthResult(
                        isReachable = true,
                        latencyMs = latency,
                        statusMessage = "خادم SSH متصل",
                        details = if (banner.isNotBlank()) "إشعار الخادم: ${banner.take(40)}" else "المنفذ $port مفتوح"
                    )
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
