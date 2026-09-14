package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.VpnCardBackground
import com.example.ui.theme.VpnCardBorder
import com.example.ui.theme.VpnCardElevated
import com.example.ui.theme.VpnConnectedGreen
import com.example.ui.theme.VpnDisconnectedRed
import com.example.ui.theme.VpnNavyBackground
import com.example.ui.theme.VpnTealAccent
import com.example.ui.theme.VpnTextMuted
import com.example.ui.theme.VpnTextPrimary
import com.example.ui.theme.VpnTextSecondary
import com.example.vpn.ServerHealthResult
import com.example.vpn.VpnServer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardDialog(
    servers: List<VpnServer>,
    editingServer: VpnServer?,
    testResult: ServerHealthResult?,
    isTesting: Boolean,
    onSaveServer: (VpnServer) -> Unit,
    onDeleteServer: (String) -> Unit,
    onTestServer: (VpnServer) -> Unit,
    onResetDefaults: () -> Unit,
    onSelectServerToEdit: (VpnServer?) -> Unit,
    onDismiss: () -> Unit
) {
    // True when the user is in "Add / Edit Server" mode
    var isFormVisible by remember { mutableStateOf(editingServer != null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, VpnTealAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .testTag("admin_dashboard_dialog"),
            color = VpnNavyBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VpnTealAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = VpnTealAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "لوحة تحكم السيرفرات ⚙️",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = VpnTextPrimary
                            )
                            Text(
                                text = "Google Cloud VPS & ثغرات الشريحة (mooh2026)",
                                fontSize = 11.sp,
                                color = VpnTealAccent
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_admin_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = VpnTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isFormVisible) {
                    // Edit or Add Server Form
                    ServerFormView(
                        initialServer = editingServer,
                        testResult = testResult,
                        isTesting = isTesting,
                        onTestServer = onTestServer,
                        onSave = { server ->
                            onSaveServer(server)
                            isFormVisible = false
                            onSelectServerToEdit(null)
                        },
                        onCancel = {
                            isFormVisible = false
                            onSelectServerToEdit(null)
                        }
                    )
                } else {
                    // Server Management List
                    ServerListView(
                        servers = servers,
                        testResult = testResult,
                        isTesting = isTesting,
                        onAddNew = {
                            onSelectServerToEdit(null)
                            isFormVisible = true
                        },
                        onEdit = { server ->
                            onSelectServerToEdit(server)
                            isFormVisible = true
                        },
                        onDelete = onDeleteServer,
                        onTestServer = onTestServer,
                        onResetDefaults = onResetDefaults
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerListView(
    servers: List<VpnServer>,
    testResult: ServerHealthResult?,
    isTesting: Boolean,
    onAddNew: () -> Unit,
    onEdit: (VpnServer) -> Unit,
    onDelete: (String) -> Unit,
    onTestServer: (VpnServer) -> Unit,
    onResetDefaults: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddNew,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("add_new_server_admin_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VpnTealAccent,
                    contentColor = VpnNavyBackground
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة سيرفر VPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onResetDefaults,
                modifier = Modifier
                    .height(44.dp)
                    .testTag("reset_defaults_admin_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VpnTextSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("استعادة الافتراضي", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(VpnCardBackground)
                .border(1.dp, VpnCardBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = VpnTealAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "عند وضع هوست youtube.com، يتعرف الهاتف على عرض يوتيوب في الشريحة ويتصل بالسيرفر بنجاح!",
                    fontSize = 11.sp,
                    color = VpnTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        // Live Health Test Result Banner (if tested recently)
        if (testResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (testResult.isReachable) VpnConnectedGreen.copy(alpha = 0.15f) else VpnDisconnectedRed.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (testResult.isReachable) VpnConnectedGreen.copy(alpha = 0.4f) else VpnDisconnectedRed.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (testResult.isReachable) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (testResult.isReachable) VpnConnectedGreen else VpnDisconnectedRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = testResult.statusMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (testResult.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                        )
                        Text(
                            text = testResult.details,
                            fontSize = 10.sp,
                            color = VpnTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Servers List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(servers, key = { it.id }) { server ->
                AdminServerCard(
                    server = server,
                    onEdit = { onEdit(server) },
                    onDelete = { onDelete(server.id) },
                    onTest = { onTestServer(server) },
                    isTesting = isTesting
                )
            }
        }
    }
}

@Composable
private fun AdminServerCard(
    server: VpnServer,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    isTesting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, VpnCardBorder, RoundedCornerShape(14.dp))
            .testTag("admin_server_card_${server.id}"),
        colors = CardDefaults.cardColors(containerColor = VpnCardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Name and Category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = server.flagEmoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = server.cityAr,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VpnTextPrimary
                        )
                        Text(
                            text = "${server.countryNameAr} • ${server.protocol}",
                            fontSize = 11.sp,
                            color = VpnTextSecondary
                        )
                    }
                }

                // Ping indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (server.pingMs > 0) VpnConnectedGreen.copy(alpha = 0.15f)
                            else VpnDisconnectedRed.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (server.pingMs > 0) "${server.pingMs} ms" else "Offline",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (server.pingMs > 0) VpnConnectedGreen else VpnDisconnectedRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Host, Port and SNI (Bug Host)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Host / IP
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VpnNavyBackground)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(text = "HOST / IP", fontSize = 9.sp, color = VpnTextMuted)
                        Text(
                            text = "${server.host}:${server.port}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = VpnTextPrimary
                        )
                    }
                }

                // SNI / Bug Host
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (server.sniHost.isNotBlank()) VpnTealAccent.copy(alpha = 0.12f) else VpnNavyBackground)
                        .border(
                            1.dp,
                            if (server.sniHost.isNotBlank()) VpnTealAccent.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "SNI / ثغرة الشريحة",
                            fontSize = 9.sp,
                            color = if (server.sniHost.isNotBlank()) VpnTealAccent else VpnTextMuted
                        )
                        Text(
                            text = if (server.sniHost.isNotBlank()) "📺 ${server.sniHost}" else "مباشر (بدون SNI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (server.sniHost.isNotBlank()) VpnTealAccent else VpnTextSecondary
                        )
                    }
                }
            }

            // Optional Proxy & Payload info banner
            if (server.proxyHost.isNotBlank() || server.payload.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (server.proxyHost.isNotBlank()) {
                        Text(
                            text = "🔌 بروكسي: ${server.proxyHost}:${server.proxyPort}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFB74D)
                        )
                    }
                    if (server.proxyHost.isNotBlank() && server.payload.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "•", fontSize = 10.sp, color = VpnTextMuted)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (server.payload.isNotBlank()) {
                        Text(
                            text = "📦 بايلود: ${server.payload.replace("[crlf]", " ").take(24)}...",
                            fontSize = 10.sp,
                            color = VpnTealAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Test button
                OutlinedButton(
                    onClick = onTest,
                    enabled = !isTesting,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("test_server_${server.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VpnTealAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فحص السيرفر ⚡", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VpnNavyBackground)
                        .testTag("edit_server_${server.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = VpnTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VpnDisconnectedRed.copy(alpha = 0.15f))
                        .testTag("delete_server_${server.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = VpnDisconnectedRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private data class SshOceanParsed(
    val host: String,
    val port: String,
    val user: String,
    val pass: String,
    val proxyHost: String?,
    val proxyPort: String?
)

private fun parseSshOceanInfo(text: String): SshOceanParsed? {
    val lines = text.lines()
    var host: String? = null
    var port: String? = null
    var user: String? = null
    var pass: String? = null
    var proxyHost: String? = null
    var proxyPort: String? = null

    for (line in lines) {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()
        when {
            (lower.contains("host") || lower.contains("server") || lower.contains("ip :") || lower.contains("ip:")) && 
            !lower.contains("squid") && !lower.contains("proxy") -> {
                val parts = trimmed.split(":", "=")
                if (parts.size >= 2) {
                    val candidate = parts.drop(1).joinToString(":").trim()
                    val cleanCandidate = candidate.split(" ", "\t").firstOrNull { it.isNotBlank() }?.trim()
                    if (!cleanCandidate.isNullOrBlank()) host = cleanCandidate
                }
            }
            lower.contains("port") && !lower.contains("squid") && !lower.contains("proxy") -> {
                val parts = trimmed.split(":", "=")
                if (parts.size >= 2) {
                    val nums = parts[1].filter { it.isDigit() || it == ',' || it == ' ' }
                    val cleanPort = nums.split(",", " ").firstOrNull { it.isNotBlank() }?.trim()
                    if (!cleanPort.isNullOrBlank()) port = cleanPort
                }
            }
            lower.contains("user") -> {
                val parts = trimmed.split(":", "=")
                if (parts.size >= 2) user = parts[1].trim()
            }
            lower.contains("pass") -> {
                val parts = trimmed.split(":", "=")
                if (parts.size >= 2) pass = parts[1].trim()
            }
            lower.contains("squid") || lower.contains("proxy") -> {
                val parts = trimmed.split(":", "=")
                if (parts.size >= 2) {
                    val candidate = parts.drop(1).joinToString(":").trim()
                    if (candidate.contains(":")) {
                        val subParts = candidate.split(":")
                        proxyHost = subParts[0].trim()
                        proxyPort = subParts[1].filter { it.isDigit() }.trim()
                    } else if (candidate.any { it.isDigit() }) {
                        val digits = candidate.filter { it.isDigit() }.trim()
                        if (digits.length in 2..5) proxyPort = digits
                    }
                }
            }
        }
    }
    return if (!host.isNullOrBlank()) {
        SshOceanParsed(host, port ?: "22", user ?: "", pass ?: "", proxyHost, proxyPort ?: "8080")
    } else null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServerFormView(
    initialServer: VpnServer?,
    testResult: ServerHealthResult?,
    isTesting: Boolean,
    onTestServer: (VpnServer) -> Unit,
    onSave: (VpnServer) -> Unit,
    onCancel: () -> Unit
) {
    val isEdit = initialServer != null

    var serverName by remember { mutableStateOf(initialServer?.cityAr ?: "") }
    var host by remember { mutableStateOf(initialServer?.host ?: "") }
    var portString by remember { mutableStateOf(initialServer?.port?.toString() ?: "22") }
    var sniHost by remember { mutableStateOf(initialServer?.sniHost ?: "youtube.com") }
    var protocol by remember { mutableStateOf(initialServer?.protocol ?: "SSH + PROXY") }
    var proxyHost by remember { mutableStateOf(initialServer?.proxyHost ?: "") }
    var proxyPortString by remember { mutableStateOf(if ((initialServer?.proxyPort ?: 0) > 0) initialServer!!.proxyPort.toString() else "8080") }
    var payload by remember { mutableStateOf(initialServer?.payload ?: "GET / HTTP/1.1[crlf]Host: youtube.com[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]") }
    var defaultUsername by remember { mutableStateOf(initialServer?.username ?: "") }
    var defaultPassword by remember { mutableStateOf(initialServer?.password ?: "") }
    var showSshOceanPasteBox by remember { mutableStateOf(false) }
    var sshOceanPastedText by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("server_form_view")
    ) {
        // Form Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = VpnTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isEdit) "تعديل سيرفر VPS" else "إضافة سيرفر SSH / Google Cloud VPS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VpnTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Smart SSHOcean / FastSSH Import Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = VpnCardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, VpnTealAccent.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSshOceanPasteBox = !showSshOceanPasteBox },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📋", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "لصق سريع لمعلومات حساب SSHOcean / FastSSH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VpnTealAccent
                        )
                    }
                    Text(
                        text = if (showSshOceanPasteBox) "إخفاء ▲" else "فتح ▼",
                        fontSize = 11.sp,
                        color = VpnTextMuted
                    )
                }

                if (showSshOceanPasteBox) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "انسخ النص الكامل الذي يعطيه موقع SSHOcean (Host, Port, User, Pass, Squid Proxy) والصقه هنا ليتم استخراجه فوراً:",
                        fontSize = 11.sp,
                        color = VpnTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = sshOceanPastedText,
                        onValueChange = { sshOceanPastedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = {
                            Text(
                                "مثال:\nHost : 128.199.50.20\nPort : 22\nUser : sshocean-user\nPass : 1234\nSquid Proxy : 128.199.50.20:8080",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val parsed = parseSshOceanInfo(sshOceanPastedText)
                            if (parsed != null) {
                                host = parsed.host
                                portString = parsed.port
                                defaultUsername = parsed.user
                                defaultPassword = parsed.pass
                                if (!parsed.proxyHost.isNullOrBlank()) {
                                    proxyHost = parsed.proxyHost
                                } else {
                                    proxyHost = parsed.host
                                }
                                if (!parsed.proxyPort.isNullOrBlank()) {
                                    proxyPortString = parsed.proxyPort
                                }
                                protocol = "SSH + PROXY"
                                sniHost = "youtube.com"
                                serverName = "خادم SSHOcean (${parsed.host})"
                                payload = "GET / HTTP/1.1[crlf]Host: youtube.com[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]"
                                showSshOceanPasteBox = false
                                formError = null
                            } else {
                                formError = "تعذر العثور على Host أو IP في النص الملصوق"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VpnTealAccent, contentColor = VpnNavyBackground)
                    ) {
                        Text("⚡ استخراج وتعبئة الخانات تلقائياً", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Protocol Selector
        Text(
            text = "نوع البروتوكول (Protocol):",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = VpnTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("SSH + PROXY", "SSH", "SSL", "VLESS", "VMESS").forEach { proto ->
                val chipLabel = when (proto) {
                    "SSH + PROXY" -> "⚡ SSH + بروكسي وبايلود (SSHOcean)"
                    "SSH" -> "SSH مباشر"
                    "SSL" -> "SSH + SSL (SNI)"
                    "VLESS" -> "VLESS WS"
                    "VMESS" -> "VMess CDN"
                    else -> proto
                }
                FilterChip(
                    selected = protocol == proto,
                    onClick = { protocol = proto },
                    label = { Text(chipLabel, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VpnTealAccent.copy(alpha = 0.2f),
                        selectedLabelColor = VpnTealAccent,
                        containerColor = VpnCardBackground,
                        labelColor = VpnTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Server Name field
        OutlinedTextField(
            value = serverName,
            onValueChange = { serverName = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_server_name"),
            label = { Text("اسم السيرفر الذي يظهر للناس في التطبيق") },
            placeholder = { Text("مثال: خادم SSHOcean يوتيوب شريحة جيزي") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = formFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Host IP and Port
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                modifier = Modifier
                    .weight(1.8f)
                    .testTag("form_server_host"),
                label = { Text("عنوان خادم SSH (Host / IP)") },
                placeholder = { Text("مثال: sg-1.sshocean.net أو 128.199.x.x") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )

            OutlinedTextField(
                value = portString,
                onValueChange = { portString = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("form_server_port"),
                label = { Text("المنفذ (Port)") },
                placeholder = { Text("22 / 80 / 443") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dedicated SSH + Proxy & Payload Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131F33)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🔌 إعدادات البروكسي (Squid Proxy) والبايلود (Payload):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB74D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Proxy Host & Port
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = proxyHost,
                        onValueChange = { input ->
                            // Auto split host:port if user pastes with colon
                            if (input.contains(":")) {
                                val split = input.split(":")
                                proxyHost = split[0].trim()
                                if (split.size > 1 && split[1].all { it.isDigit() }) {
                                    proxyPortString = split[1].trim()
                                }
                            } else {
                                proxyHost = input
                            }
                        },
                        modifier = Modifier
                            .weight(1.8f)
                            .testTag("form_proxy_host"),
                        label = { Text("بروكسي السيرفر (Proxy IP)") },
                        placeholder = { Text("مثال: 128.199.50.20 أو fastssh.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = proxyPortString,
                        onValueChange = { proxyPortString = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_proxy_port"),
                        label = { Text("بورت البروكسي") },
                        placeholder = { Text("8080 / 3128") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick button to copy server host to proxy
                if (host.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            proxyHost = host.trim()
                            if (proxyPortString.isBlank()) proxyPortString = "8080"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D))
                    ) {
                        Text("⚡ استخدام نفس IP السيرفر كبروكسي Squid (بورت 8080)", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Payload field
                Text(
                    text = "بايلود الحقن (Custom HTTP/WS Payload):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VpnTealAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = payload,
                    onValueChange = { payload = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("form_server_payload"),
                    placeholder = { Text("GET / HTTP/1.1[crlf]Host: youtube.com[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "قوالب بايلود جاهزة بضغطة واحدة:", fontSize = 10.sp, color = VpnTextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "📺 بايلود يوتيوب WS" to "GET / HTTP/1.1[crlf]Host: youtube.com[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        "📺 بايلود يوتيوب CONNECT" to "CONNECT [host_port] HTTP/1.1[crlf]Host: youtube.com[crlf]X-Online-Host: youtube.com[crlf]Connection: Keep-Alive[crlf][crlf]",
                        "🌐 بايلود Cloudflare CDN" to "GET / HTTP/1.1[crlf]Host: [host][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        "⚡ Direct CONNECT" to "CONNECT [host_port] HTTP/1.1[crlf]Connection: Keep-Alive[crlf][crlf]"
                    )
                    presets.forEach { (label, code) ->
                        FilterChip(
                            selected = payload == code,
                            onClick = { payload = code },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VpnTealAccent.copy(alpha = 0.2f),
                                selectedLabelColor = VpnTealAccent,
                                containerColor = VpnNavyBackground,
                                labelColor = VpnTextSecondary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SNI / Bug Host (مضيف الثغرة للشريحة)
        Text(
            text = "مضيف الثغرة / SNI (لتجاوز وتصفح عرض يوتيوب والشريحة):",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = VpnTealAccent
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = sniHost,
            onValueChange = { sniHost = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_server_sni"),
            label = { Text("SNI / Bug Host") },
            placeholder = { Text("مثال: youtube.com") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = formFieldColors()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick suggestions for Bug Host
        Text(text = "اقتراحات سريعة للشريحة:", fontSize = 10.sp, color = VpnTextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("youtube.com", "m.youtube.com", "googlevideo.com", "fast.com", "").forEach { suggestion ->
                val label = if (suggestion.isEmpty()) "بدون SNI (مباشر)" else suggestion
                FilterChip(
                    selected = sniHost == suggestion,
                    onClick = { sniHost = suggestion },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VpnTealAccent.copy(alpha = 0.2f),
                        selectedLabelColor = VpnTealAccent,
                        containerColor = VpnCardBackground,
                        labelColor = VpnTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // User & Password (Optional default, user can also enter on home screen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = defaultUsername,
                onValueChange = { defaultUsername = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("form_server_user"),
                label = { Text("اسم المستخدم (Username)") },
                placeholder = { Text("يوزر SSH") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )

            OutlinedTextField(
                value = defaultPassword,
                onValueChange = { defaultPassword = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("form_server_pass"),
                label = { Text("كلمة المرور (Password)") },
                placeholder = { Text("كلمة سر SSH") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )
        }

        // Live Health Test Result in Form
        if (testResult != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (testResult.isReachable) VpnConnectedGreen.copy(alpha = 0.15f) else VpnDisconnectedRed.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (testResult.isReachable) VpnConnectedGreen.copy(alpha = 0.4f) else VpnDisconnectedRed.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (testResult.isReachable) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (testResult.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = testResult.statusMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (testResult.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                        )
                        Text(
                            text = testResult.details,
                            fontSize = 11.sp,
                            color = VpnTextSecondary
                        )
                    }
                }
            }
        }

        // Error message if any
        if (formError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formError!!,
                color = VpnDisconnectedRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pre-save Test Server Button
        OutlinedButton(
            onClick = {
                val parsedPort = portString.toIntOrNull() ?: 443
                val parsedProxyPort = proxyPortString.toIntOrNull() ?: 0
                val tempServer = VpnServer(
                    id = initialServer?.id ?: "temp_probe",
                    countryName = "SSH VPS",
                    countryNameAr = serverName.ifBlank { "سيرفر تجريبي" },
                    city = serverName.ifBlank { "VPS" },
                    cityAr = serverName.ifBlank { "VPS" },
                    flagEmoji = if (sniHost.contains("youtube", ignoreCase = true)) "📺" else "🌐",
                    host = host.trim(),
                    port = parsedPort,
                    protocol = protocol,
                    sniHost = sniHost.trim(),
                    proxyHost = proxyHost.trim(),
                    proxyPort = parsedProxyPort,
                    payload = payload.trim(),
                    username = defaultUsername.trim(),
                    password = defaultPassword,
                    category = "GCP"
                )
                onTestServer(tempServer)
            },
            enabled = !isTesting && host.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("pre_test_server_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = VpnTealAccent
            )
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = VpnTealAccent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري فحص استجابة السيرفر والبروكسي...", fontSize = 12.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚡ فحص السيرفر والبروكسي الآن للتأكد من أنه شغال", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Save & Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("cancel_form_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VpnTextSecondary
                )
            ) {
                Text("إلغاء", fontSize = 14.sp)
            }

            Button(
                onClick = {
                    if (host.isBlank()) {
                        formError = "❌ يرجى إدخال عنوان السيرفر (Host/IP)"
                        return@Button
                    }
                    val parsedPort = portString.toIntOrNull() ?: 443
                    val parsedProxyPort = proxyPortString.toIntOrNull() ?: 0

                    val finalName = if (serverName.isNotBlank()) serverName else "سيرفر SSH ($host)"
                    val finalServer = VpnServer(
                        id = initialServer?.id ?: "gcp_${System.currentTimeMillis()}",
                        countryName = "SSH VPS",
                        countryNameAr = finalName,
                        city = finalName,
                        cityAr = finalName,
                        flagEmoji = if (sniHost.contains("youtube", ignoreCase = true)) "📺" else "🌐",
                        host = host.trim(),
                        port = parsedPort,
                        dnsServer = "8.8.8.8",
                        secondaryDns = "8.8.4.4",
                        pingMs = testResult?.latencyMs ?: -1,
                        loadPercent = 20,
                        protocol = protocol,
                        isCustom = true,
                        username = defaultUsername.trim(),
                        password = defaultPassword,
                        category = "GCP",
                        sniHost = sniHost.trim(),
                        proxyHost = proxyHost.trim(),
                        proxyPort = parsedProxyPort,
                        payload = payload.trim(),
                        notes = "تمت الإضافة عبر لوحة تحكم المدير"
                    )
                    onSave(finalServer)
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("save_server_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VpnTealAccent,
                    contentColor = VpnNavyBackground
                )
            ) {
                Text(
                    text = if (isEdit) "حفظ التعديلات 💾" else "إضافة السيرفر 💾",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = VpnCardBackground,
    unfocusedContainerColor = VpnCardBackground,
    focusedBorderColor = VpnTealAccent,
    unfocusedBorderColor = VpnCardBorder,
    focusedTextColor = VpnTextPrimary,
    unfocusedTextColor = VpnTextPrimary,
    focusedLabelColor = VpnTealAccent,
    unfocusedLabelColor = VpnTextSecondary
)
