package com.example.vpn

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    val status: StateFlow<ConnectionStatus> = VpnController.status
    val selectedServer: StateFlow<VpnServer> = VpnController.selectedServer
    val statistics: StateFlow<VpnStatistics> = VpnController.statistics
    val logs: StateFlow<List<VpnLogEntry>> = VpnController.logs
    val publicIp: StateFlow<String> = VpnController.publicIp
    val protocol: StateFlow<String> = VpnController.protocol
    val killSwitch: StateFlow<Boolean> = VpnController.killSwitch
    val dnsLeakProtection: StateFlow<Boolean> = VpnController.dnsLeakProtection
    val username: StateFlow<String> = VpnController.username
    val password: StateFlow<String> = VpnController.password
    val errorMessage: StateFlow<String?> = VpnController.errorMessage
    val activeTab: StateFlow<Int> = VpnController.activeTab

    private val _servers = MutableStateFlow(ServerStorageManager.loadServers(application))
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    private val _networkType = MutableStateFlow("جاري الفحص...")
    val networkType: StateFlow<String> = _networkType.asStateFlow()

    private val _isTestingPing = MutableStateFlow(false)
    val isTestingPing: StateFlow<Boolean> = _isTestingPing.asStateFlow()

    private val _showServerSheet = MutableStateFlow(false)
    val showServerSheet: StateFlow<Boolean> = _showServerSheet.asStateFlow()

    private val _showLogsDialog = MutableStateFlow(false)
    val showLogsDialog: StateFlow<Boolean> = _showLogsDialog.asStateFlow()

    private val _showAddServerDialog = MutableStateFlow(false)
    val showAddServerDialog: StateFlow<Boolean> = _showAddServerDialog.asStateFlow()

    // Admin Panel (Code: mooh2026)
    private val _showAdminPasscodeDialog = MutableStateFlow(false)
    val showAdminPasscodeDialog: StateFlow<Boolean> = _showAdminPasscodeDialog.asStateFlow()

    private val _showAdminDashboard = MutableStateFlow(false)
    val showAdminDashboard: StateFlow<Boolean> = _showAdminDashboard.asStateFlow()

    private val _editingServer = MutableStateFlow<VpnServer?>(null)
    val editingServer: StateFlow<VpnServer?> = _editingServer.asStateFlow()

    private val _adminTestResult = MutableStateFlow<ServerHealthResult?>(null)
    val adminTestResult: StateFlow<ServerHealthResult?> = _adminTestResult.asStateFlow()

    private val _isTestingAdminServer = MutableStateFlow(false)
    val isTestingAdminServer: StateFlow<Boolean> = _isTestingAdminServer.asStateFlow()

    init {
        // Ensure default selected server is valid
        if (_servers.value.isNotEmpty()) {
            VpnController.selectServer(_servers.value.first())
        }
        refreshNetworkInfo()
        testServerPings()
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch {
            _networkType.value = NetworkHelper.getNetworkType(getApplication())
            val (ip, _) = NetworkHelper.fetchPublicIp()
            VpnController.updatePublicIp(ip)
        }
    }

    fun testServerPings() {
        if (_isTestingPing.value) return
        viewModelScope.launch {
            _isTestingPing.value = true
            val updated = _servers.value.map { server ->
                val health = VpnNodeChecker.checkServerHealth(server)
                val ping = if (health.isReachable) health.latencyMs else -1
                server.copy(pingMs = ping)
            }
            _servers.value = updated

            // If selected server updated, sync it
            val currentSelected = selectedServer.value
            updated.find { it.id == currentSelected.id }?.let {
                VpnController.selectServer(it)
            }
            _isTestingPing.value = false
        }
    }

    suspend fun checkServerLiveStatus(server: VpnServer): ServerHealthResult {
        return VpnNodeChecker.checkServerHealth(server)
    }

    fun importConfigUri(rawUri: String, autoConnect: Boolean = false): Result<VpnServer> {
        val result = VpnConfigParser.parse(rawUri)
        if (result.isSuccess) {
            val server = result.getOrThrow()
            _servers.value = listOf(server) + _servers.value
            selectServer(server)
            VpnController.log("IMPORT", "تم استيراد ${server.protocol}: ${server.cityAr} (${server.host}:${server.port})")
            viewModelScope.launch {
                val health = VpnNodeChecker.checkServerHealth(server)
                val updatedServer = server.copy(pingMs = if (health.isReachable) health.latencyMs else -1)
                _servers.value = _servers.value.map { if (it.id == server.id) updatedServer else it }
                if (selectedServer.value.id == server.id) {
                    VpnController.selectServer(updatedServer)
                }
                VpnController.log("HEALTH", "فحص السيرفر المستورد: ${health.statusMessage} - زمن الاستجابة: ${health.latencyMs}ms")
            }
            if (autoConnect) {
                connect()
            }
        } else {
            VpnController.log("IMPORT", "خطأ في استيراد الرابط: ${result.exceptionOrNull()?.message}", isError = true)
        }
        return result
    }

    fun prepareVpnIntent(): Intent? {
        return VpnService.prepare(getApplication())
    }

    fun connect() {
        val server = selectedServer.value
        VpnController.log("USER", "المستخدم طلب الاتصال بالسيرفر: ${server.countryNameAr} (${server.cityAr})")
        RealVpnService.startVpn(getApplication(), server)
    }

    fun disconnect() {
        VpnController.log("USER", "المستخدم طلب قطع الاتصال")
        RealVpnService.stopVpn(getApplication())
    }

    fun selectServer(server: VpnServer) {
        val wasConnected = status.value == ConnectionStatus.CONNECTED
        VpnController.selectServer(server)
        setShowServerSheet(false)
        if (wasConnected) {
            // Reconnect to new server
            disconnect()
            viewModelScope.launch {
                kotlinx.coroutines.delay(600)
                connect()
            }
        }
    }

    fun addCustomServer(name: String, host: String, port: Int, dns: String, protocol: String) {
        val newServer = VpnServer(
            id = "custom_${System.currentTimeMillis()}",
            countryName = "Google Cloud VPS",
            countryNameAr = "سيرفر Google Cloud مخصص",
            city = name,
            cityAr = name,
            flagEmoji = "🌐",
            host = host.trim(),
            port = port,
            dnsServer = if (dns.isBlank()) "8.8.8.8" else dns,
            secondaryDns = "8.8.4.4",
            pingMs = -1,
            loadPercent = 10,
            protocol = protocol,
            isCustom = true,
            category = "CUSTOM"
        )
        _servers.value = listOf(newServer) + _servers.value
        selectServer(newServer)
        setShowAddServerDialog(false)
        testServerPings()
    }

    fun addGoogleCloudServer(
        vmName: String,
        externalIp: String,
        port: Int,
        user: String,
        pass: String,
        protocol: String = "SSH"
    ) {
        val server = VpnServer(
            id = "gcp_${System.currentTimeMillis()}",
            countryName = "Google Cloud VM",
            countryNameAr = "سيرفر قوقل كلاود: $vmName",
            city = vmName,
            cityAr = vmName,
            flagEmoji = "🌐",
            host = externalIp.trim(),
            port = port,
            dnsServer = "8.8.8.8",
            secondaryDns = "8.8.4.4",
            pingMs = -1,
            loadPercent = 15,
            protocol = protocol,
            isCustom = true,
            username = user.trim(),
            password = pass,
            category = "GCP",
            rawConfig = if (protocol == "SSH") "ssh://${user.trim()}@${externalIp.trim()}:$port#$vmName" else null
        )
        _servers.value = listOf(server) + _servers.value
        selectServer(server)
        VpnController.setCredentials(user, pass)
        setShowAddServerDialog(false)
        VpnController.log("GCP", "تمت إضافة سيرفر Google Cloud VM: $vmName ($externalIp:$port)")
        viewModelScope.launch {
            val health = VpnNodeChecker.checkServerHealth(server)
            val updated = server.copy(pingMs = if (health.isReachable) health.latencyMs else -1)
            _servers.value = _servers.value.map { if (it.id == server.id) updated else it }
            if (selectedServer.value.id == server.id) {
                VpnController.selectServer(updated)
            }
            VpnController.log("HEALTH", "فحص Google Cloud VM: ${health.statusMessage} (${health.details})")
        }
    }

    fun setCredentials(user: String, pass: String) {
        VpnController.setCredentials(user, pass)
    }

    fun setActiveTab(tab: Int) {
        VpnController.setActiveTab(tab)
    }

    fun clearErrorMessage() {
        VpnController.setErrorMessage(null)
    }

    fun removeCustomServer(server: VpnServer) {
        _servers.value = _servers.value.filter { it.id != server.id }
        if (selectedServer.value.id == server.id) {
            VpnController.selectServer(_servers.value.first())
        }
    }

    fun setProtocol(protocol: String) {
        VpnController.setProtocol(protocol)
        VpnController.log("SETTINGS", "تم تغيير بروتوكول التشفير إلى: $protocol")
    }

    fun toggleKillSwitch() {
        val current = killSwitch.value
        VpnController.setKillSwitch(!current)
        VpnController.log("SETTINGS", "مفتاح الإيقاف التلقائي (Kill Switch): ${if (!current) "مفعل" else "معطل"}")
    }

    fun toggleDnsLeakProtection() {
        val current = dnsLeakProtection.value
        VpnController.setDnsLeakProtection(!current)
        VpnController.log("SETTINGS", "حماية تسرب DNS: ${if (!current) "مفعل" else "معطل"}")
    }

    fun setShowServerSheet(show: Boolean) {
        _showServerSheet.value = show
    }

    fun setShowLogsDialog(show: Boolean) {
        _showLogsDialog.value = show
    }

    fun setShowAddServerDialog(show: Boolean) {
        _showAddServerDialog.value = show
    }

    // Admin Control Panel (Passcode: mooh2026)
    fun setShowAdminPasscodeDialog(show: Boolean) {
        _showAdminPasscodeDialog.value = show
    }

    fun setShowAdminDashboard(show: Boolean) {
        _showAdminDashboard.value = show
    }

    fun verifyAdminPasscode(enteredPin: String): Boolean {
        val valid = ServerStorageManager.verifyAdminPin(enteredPin)
        if (valid) {
            _showAdminPasscodeDialog.value = false
            _showAdminDashboard.value = true
            VpnController.log("ADMIN", "تم تسجيل دخول المدير بنجاح بكود mooh2026")
        } else {
            VpnController.log("ADMIN", "محاولة دخول خاطئة إلى لوحة تحكم السيرفرات", isError = true)
        }
        return valid
    }

    fun setEditingServer(server: VpnServer?) {
        _editingServer.value = server
        _adminTestResult.value = null
    }

    fun saveOrUpdateServer(server: VpnServer) {
        val current = _servers.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == server.id }
        if (existingIndex != -1) {
            current[existingIndex] = server
            VpnController.log("ADMIN", "تم تعديل السيرفر: ${server.cityAr} (${server.host}:${server.port})")
        } else {
            current.add(0, server)
            VpnController.log("ADMIN", "تمت إضافة سيرفر جديد: ${server.cityAr} (${server.host}:${server.port})")
        }
        _servers.value = current
        ServerStorageManager.saveServers(getApplication(), current)

        // If currently selected server was updated, refresh it
        if (selectedServer.value.id == server.id) {
            VpnController.selectServer(server)
        }

        // Test health of this server in background
        viewModelScope.launch {
            val health = VpnNodeChecker.checkServerHealth(server)
            val updated = server.copy(pingMs = if (health.isReachable) health.latencyMs else -1)
            val updatedList = _servers.value.map { if (it.id == server.id) updated else it }
            _servers.value = updatedList
            ServerStorageManager.saveServers(getApplication(), updatedList)
            if (selectedServer.value.id == server.id) {
                VpnController.selectServer(updated)
            }
        }
    }

    fun deleteServer(serverId: String) {
        val current = _servers.value.filter { it.id != serverId }
        val remaining = if (current.isEmpty()) VpnServer.DEFAULT_SERVERS else current
        _servers.value = remaining
        ServerStorageManager.saveServers(getApplication(), remaining)
        VpnController.log("ADMIN", "تم حذف السيرفر ذو المعرف: $serverId")

        if (selectedServer.value.id == serverId) {
            VpnController.selectServer(remaining.first())
        }
    }

    fun resetServersToDefault() {
        val defaults = ServerStorageManager.resetToDefaults(getApplication())
        _servers.value = defaults
        VpnController.selectServer(defaults.first())
        VpnController.log("ADMIN", "تمت استعادة كافة السيرفرات الافتراضية")
        testServerPings()
    }

    fun testAdminServer(server: VpnServer) {
        viewModelScope.launch {
            _isTestingAdminServer.value = true
            _adminTestResult.value = null
            val result = VpnNodeChecker.checkServerHealth(server, timeoutMs = 3500)
            _adminTestResult.value = result
            _isTestingAdminServer.value = false
            VpnController.log("ADMIN_PROBE", "فحص الخادم ${server.host}:${server.port} -> ${result.statusMessage} (${result.details})")
        }
    }

    fun clearLogs() {
        VpnController.clearLogs()
    }
}
