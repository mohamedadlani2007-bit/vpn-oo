package com.example.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

data class VpnStatistics(
    val durationSeconds: Long = 0L,
    val downloadSpeedBps: Long = 0L,
    val uploadSpeedBps: Long = 0L,
    val totalBytesDownloaded: Long = 0L,
    val totalBytesUploaded: Long = 0L
) {
    fun formattedDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formattedDownloadSpeed(): String = formatSpeed(downloadSpeedBps)
    fun formattedUploadSpeed(): String = formatSpeed(uploadSpeedBps)
    fun formattedDownloadedTotal(): String = formatBytes(totalBytesDownloaded)
    fun formattedUploadedTotal(): String = formatBytes(totalBytesUploaded)

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format(Locale.US, "%.1f KB/s", bytesPerSec / 1024.0)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

data class VpnLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isError: Boolean = false
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(Date(timestamp))
    }
}

object VpnController {
    private val _status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val _selectedServer = MutableStateFlow(VpnServer.DEFAULT_SERVERS.first())
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _statistics = MutableStateFlow(VpnStatistics())
    val statistics: StateFlow<VpnStatistics> = _statistics.asStateFlow()

    private val _logs = MutableStateFlow<List<VpnLogEntry>>(emptyList())
    val logs: StateFlow<List<VpnLogEntry>> = _logs.asStateFlow()

    private val _publicIp = MutableStateFlow("جاري الفحص...")
    val publicIp: StateFlow<String> = _publicIp.asStateFlow()

    private val _protocol = MutableStateFlow("WireGuard")
    val protocol: StateFlow<String> = _protocol.asStateFlow()

    private val _killSwitch = MutableStateFlow(false)
    val killSwitch: StateFlow<Boolean> = _killSwitch.asStateFlow()

    private val _dnsLeakProtection = MutableStateFlow(true)
    val dnsLeakProtection: StateFlow<Boolean> = _dnsLeakProtection.asStateFlow()

    private val _username = MutableStateFlow("admin")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun updateStatus(newStatus: ConnectionStatus) {
        _status.value = newStatus
        if (newStatus == ConnectionStatus.CONNECTED) {
            _errorMessage.value = null
        }
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    fun setCredentials(user: String, pass: String) {
        _username.value = user
        _password.value = pass
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
    }

    fun updateStatistics(stats: VpnStatistics) {
        _statistics.value = stats
    }

    fun updatePublicIp(ip: String) {
        _publicIp.value = ip
    }

    fun setProtocol(proto: String) {
        _protocol.value = proto
    }

    fun setKillSwitch(enabled: Boolean) {
        _killSwitch.value = enabled
    }

    fun setDnsLeakProtection(enabled: Boolean) {
        _dnsLeakProtection.value = enabled
    }

    fun log(tag: String, message: String, isError: Boolean = false) {
        val entry = VpnLogEntry(tag = tag, message = message, isError = isError)
        val current = _logs.value.toMutableList()
        if (current.size > 200) {
            current.removeAt(0)
        }
        current.add(entry)
        _logs.value = current
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
