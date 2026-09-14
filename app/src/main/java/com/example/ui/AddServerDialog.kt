package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VpnCardBackground
import com.example.ui.theme.VpnCardBorder
import com.example.ui.theme.VpnCardElevated
import com.example.ui.theme.VpnConnectedGreen
import com.example.ui.theme.VpnCyanPrimary
import com.example.ui.theme.VpnDisconnectedRed
import com.example.ui.theme.VpnNavyBackground
import com.example.ui.theme.VpnTealAccent
import com.example.ui.theme.VpnTextMuted
import com.example.ui.theme.VpnTextPrimary
import com.example.ui.theme.VpnTextSecondary
import com.example.vpn.ServerHealthResult
import com.example.vpn.VpnConfigParser
import com.example.vpn.VpnNodeChecker
import com.example.vpn.VpnServer
import kotlinx.coroutines.launch

@Composable
fun AddServerDialog(
    onImportUri: (uri: String, autoConnect: Boolean) -> Result<VpnServer>,
    onAddManualServer: (name: String, host: String, port: Int, dns: String, protocol: String) -> Unit,
    onAddGcpServer: (name: String, host: String, port: Int, user: String, pass: String, protocol: String) -> Unit = { _, _, _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: GCP VM, 1: Direct Link, 2: Manual

    // 0: Google Cloud VM State
    var gcpVmName by remember { mutableStateOf("Google Cloud VM") }
    var gcpExternalIp by remember { mutableStateOf("") }
    var gcpPort by remember { mutableStateOf("22") }
    var gcpUser by remember { mutableStateOf("admin") }
    var gcpPassword by remember { mutableStateOf("") }
    var gcpShowPassword by remember { mutableStateOf(false) }
    var gcpProtocol by remember { mutableStateOf("SSH") }
    var isCheckingGcp by remember { mutableStateOf(false) }
    var gcpCheckResult by remember { mutableStateOf<ServerHealthResult?>(null) }
    var gcpError by remember { mutableStateOf<String?>(null) }

    // 1: Link Tab State
    var rawLinkText by remember { mutableStateOf("") }
    var linkError by remember { mutableStateOf<String?>(null) }
    var parsedPreview by remember { mutableStateOf<VpnServer?>(null) }
    var isCheckingNode by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ServerHealthResult?>(null) }

    // 2: Manual Tab State
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("443") }
    var dns by remember { mutableStateOf("8.8.8.8") }
    var selectedProtocol by remember { mutableStateOf("VLESS") }
    var manualError by remember { mutableStateOf<String?>(null) }

    val gcpProtocols = listOf("SSH", "VLESS", "VMess", "HTTP Proxy")
    val manualProtocols = listOf("VLESS", "VMess", "SSH", "DNS Tunnel", "WireGuard", "OpenVPN")

    fun pasteFromClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: ""
                if (text.isNotBlank()) {
                    rawLinkText = text.trim()
                    val parsed = VpnConfigParser.parse(rawLinkText)
                    if (parsed.isSuccess) {
                        parsedPreview = parsed.getOrNull()
                        linkError = null
                    } else {
                        linkError = parsed.exceptionOrNull()?.message
                        parsedPreview = null
                    }
                }
            }
        } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VpnNavyBackground,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.padding(vertical = 12.dp),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(VpnTealAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = VpnTealAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "إضافة سيرفر جديد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VpnTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = VpnCardBackground,
                    contentColor = VpnTealAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = VpnTealAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, VpnCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("Google Cloud", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("رابط مباشر", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text("يدوي", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("add_server_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // Google Cloud VM Form
                    Text(
                        text = "أدخل بيانات سيرفر Google Cloud (Compute Engine VM):",
                        fontSize = 12.sp,
                        color = VpnTextSecondary
                    )

                    OutlinedTextField(
                        value = gcpVmName,
                        onValueChange = { gcpVmName = it },
                        label = { Text("اسم السيرفر / المعرف", color = VpnTextMuted) },
                        placeholder = { Text("مثال: GCP-US-CENTRAL1", color = VpnTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("gcp_vm_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedTextField(
                        value = gcpExternalIp,
                        onValueChange = {
                            gcpExternalIp = it
                            gcpCheckResult = null
                            gcpError = null
                        },
                        label = { Text("عنوان IP الخارجي (External IP)", color = VpnTextMuted) },
                        placeholder = { Text("مثال: 34.120.45.67 أو نطاق السيرفر", color = VpnTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("gcp_ip_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = gcpPort,
                            onValueChange = {
                                gcpPort = it
                                gcpCheckResult = null
                            },
                            label = { Text("المنفذ (Port)", color = VpnTextMuted) },
                            placeholder = { Text("22 أو 443", color = VpnTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("gcp_port_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = customTextFieldColors()
                        )

                        OutlinedTextField(
                            value = gcpUser,
                            onValueChange = { gcpUser = it },
                            label = { Text("المستخدم (Usuário)", color = VpnTextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = VpnTealAccent, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f).testTag("gcp_user_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = customTextFieldColors()
                        )
                    }

                    OutlinedTextField(
                        value = gcpPassword,
                        onValueChange = { gcpPassword = it },
                        label = { Text("كلمة المرور (Senha)", color = VpnTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = VpnTealAccent, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { gcpShowPassword = !gcpShowPassword }) {
                                Icon(
                                    imageVector = if (gcpShowPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = VpnTextMuted
                                )
                            }
                        },
                        visualTransformation = if (gcpShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("gcp_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    Text(
                        text = "نوع الاتصال (Protocol):",
                        fontSize = 12.sp,
                        color = VpnTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gcpProtocols.forEach { proto ->
                            ProtocolChip(
                                proto = proto,
                                isSelected = gcpProtocol == proto,
                                onClick = {
                                    gcpProtocol = proto
                                    if (proto == "SSH" && (gcpPort == "443" || gcpPort.isBlank())) gcpPort = "22"
                                    if (proto == "VLESS" && (gcpPort == "22" || gcpPort.isBlank())) gcpPort = "443"
                                    gcpCheckResult = null
                                }
                            )
                        }
                    }

                    // Live Probe Button for GCP
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(VpnCardElevated)
                            .border(1.dp, VpnCardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val host = gcpExternalIp.trim()
                                        val portNum = gcpPort.toIntOrNull() ?: 22
                                        if (host.isBlank()) {
                                            gcpError = "أدخل IP السيرفر أولاً للفحص"
                                            return@Button
                                        }
                                        gcpError = null
                                        isCheckingGcp = true
                                        coroutineScope.launch {
                                            val dummy = VpnServer(
                                                id = "temp_gcp",
                                                countryName = "Google Cloud",
                                                countryNameAr = "سيرفر قوقل كلاود",
                                                city = gcpVmName,
                                                cityAr = gcpVmName,
                                                flagEmoji = "🌐",
                                                host = host,
                                                port = portNum,
                                                protocol = gcpProtocol
                                            )
                                            val res = VpnNodeChecker.checkServerHealth(dummy, timeoutMs = 3000)
                                            gcpCheckResult = res
                                            isCheckingGcp = false
                                        }
                                    },
                                    enabled = !isCheckingGcp,
                                    modifier = Modifier.testTag("test_gcp_live_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VpnTealAccent, contentColor = VpnNavyBackground)
                                ) {
                                    if (isCheckingGcp) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = VpnNavyBackground, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("جاري فحص السيرفر...", fontSize = 11.sp, color = VpnNavyBackground)
                                    } else {
                                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فحص السيرفر الآن (Live Test)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (gcpCheckResult != null) {
                                    val res = gcpCheckResult!!
                                    Text(
                                        text = if (res.isReachable) "🟢 ${res.latencyMs}ms" else "🔴 غير متاح",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                                    )
                                }
                            }

                            if (gcpCheckResult != null) {
                                Text(
                                    text = gcpCheckResult!!.details,
                                    fontSize = 11.sp,
                                    color = if (gcpCheckResult!!.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                                )
                            }
                        }
                    }

                    if (gcpError != null) {
                        Text(
                            text = gcpError ?: "",
                            fontSize = 12.sp,
                            color = VpnDisconnectedRed
                        )
                    }

                } else if (selectedTabIndex == 1) {
                    // Direct Link Mode
                    Text(
                        text = "الصق رابط السيرفر مباشرة (vless://, vmess://, ssh://, trojan://, ss://, dns://):",
                        fontSize = 12.sp,
                        color = VpnTextSecondary,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = rawLinkText,
                        onValueChange = { input ->
                            rawLinkText = input
                            val parsed = VpnConfigParser.parse(input)
                            if (parsed.isSuccess) {
                                parsedPreview = parsed.getOrNull()
                                linkError = null
                            } else if (input.isNotBlank()) {
                                linkError = parsed.exceptionOrNull()?.message
                                parsedPreview = null
                            } else {
                                linkError = null
                                parsedPreview = null
                            }
                            testResult = null
                        },
                        label = { Text("رابط السيرفر الصارم (Direct Link)", color = VpnTextMuted) },
                        placeholder = { Text("vless://uuid@host:443?security=tls...", color = VpnTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("direct_link_input_field"),
                        shape = RoundedCornerShape(14.dp),
                        colors = customTextFieldColors(),
                        trailingIcon = {
                            IconButton(
                                onClick = { pasteFromClipboard() },
                                modifier = Modifier.testTag("paste_link_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "لصق من الحافظة",
                                    tint = VpnTealAccent
                                )
                            }
                        }
                    )

                    // Quick Sample Presets
                    Text(
                        text = "سيرفرات جاهزة سريعة:",
                        fontSize = 11.sp,
                        color = VpnTextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip("Google Cloud US", "🌐") {
                            rawLinkText = "ssh://gcp-user@8.8.8.8:53#Google-Cloud-US"
                            parsedPreview = VpnConfigParser.parse(rawLinkText).getOrNull()
                            linkError = null
                            testResult = null
                        }
                        PresetChip("VLESS Reality", "⚡") {
                            rawLinkText = "vless://auto-cloud@1.1.1.1:443?type=ws&security=tls#VLESS-SuperFast"
                            parsedPreview = VpnConfigParser.parse(rawLinkText).getOrNull()
                            linkError = null
                            testResult = null
                        }
                        PresetChip("VMess Cloud", "🛡️") {
                            rawLinkText = "vmess://eyJaddIjoiOS45LjkuOSIsInBvcnQiOjQ0MywicHMiOiJDT1VELVZNRVNTIn0="
                            parsedPreview = VpnConfigParser.parse(rawLinkText).getOrNull()
                            linkError = null
                            testResult = null
                        }
                    }

                    // Parsed Card Preview
                    if (parsedPreview != null) {
                        val p = parsedPreview!!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VpnCardElevated)
                                .border(1.dp, VpnTealAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = VpnConnectedGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "تم التعرف على: ${p.protocol}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VpnConnectedGreen
                                    )
                                }
                                Text(
                                    text = "العنوان: ${p.host} : ${p.port}",
                                    fontSize = 12.sp,
                                    color = VpnTextPrimary
                                )
                                Text(
                                    text = "المعرف: ${p.cityAr}",
                                    fontSize = 12.sp,
                                    color = VpnTextSecondary
                                )

                                // Real check button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            isCheckingNode = true
                                            coroutineScope.launch {
                                                val res = VpnNodeChecker.checkServerHealth(p)
                                                testResult = res
                                                isCheckingNode = false
                                            }
                                        },
                                        enabled = !isCheckingNode,
                                        modifier = Modifier.testTag("test_node_health_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = VpnTealAccent, contentColor = VpnNavyBackground)
                                    ) {
                                        if (isCheckingNode) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = VpnNavyBackground,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("فحص مباشر...", fontSize = 11.sp, color = VpnNavyBackground)
                                        } else {
                                            Text("فحص استجابة الرابط", fontSize = 11.sp, color = VpnNavyBackground, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (testResult != null) {
                                        val res = testResult!!
                                        Text(
                                            text = if (res.isReachable) "🟢 ${res.latencyMs}ms" else "🔴 فشل",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (res.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                                        )
                                    }
                                }

                                if (testResult != null) {
                                    Text(
                                        text = testResult!!.details,
                                        fontSize = 11.sp,
                                        color = if (testResult!!.isReachable) VpnConnectedGreen else VpnDisconnectedRed
                                    )
                                }
                            }
                        }
                    }

                    if (linkError != null) {
                        Text(
                            text = linkError ?: "",
                            fontSize = 12.sp,
                            color = VpnDisconnectedRed,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                } else {
                    // Manual Mode
                    Text(
                        text = "أدخل بيانات الخادم يدوياً:",
                        fontSize = 12.sp,
                        color = VpnTextSecondary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم السيرفر", color = VpnTextMuted) },
                        placeholder = { Text("مثال: خادم VLESS مخصص", color = VpnTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_server_name_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("المضيف (Host أو IP)", color = VpnTextMuted) },
                        placeholder = { Text("مثال: 104.16.1.1", color = VpnTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_server_host_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = customTextFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("المنفذ", color = VpnTextMuted) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("manual_server_port_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = customTextFieldColors()
                        )

                        OutlinedTextField(
                            value = dns,
                            onValueChange = { dns = it },
                            label = { Text("DNS الآمن", color = VpnTextMuted) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("manual_server_dns_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = customTextFieldColors()
                        )
                    }

                    Text(
                        text = "اختر البروتوكول:",
                        fontSize = 12.sp,
                        color = VpnTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        manualProtocols.take(3).forEach { proto ->
                            ProtocolChip(proto, selectedProtocol == proto) { selectedProtocol = proto }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        manualProtocols.drop(3).forEach { proto ->
                            ProtocolChip(proto, selectedProtocol == proto) { selectedProtocol = proto }
                        }
                    }

                    if (manualError != null) {
                        Text(
                            text = manualError ?: "",
                            fontSize = 12.sp,
                            color = VpnDisconnectedRed,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTabIndex == 0) {
                // Confirm Google Cloud Server
                Button(
                    onClick = {
                        val host = gcpExternalIp.trim()
                        if (host.isBlank()) {
                            gcpError = "يرجى إدخال IP الخادم الخارجي في Google Cloud"
                            return@Button
                        }
                        val portNum = gcpPort.toIntOrNull() ?: 22
                        val vmLabel = if (gcpVmName.isBlank()) "GCP Server ($host)" else gcpVmName.trim()
                        onAddGcpServer(vmLabel, host, portNum, gcpUser.trim(), gcpPassword, gcpProtocol)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("save_gcp_server_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VpnTealAccent,
                        contentColor = VpnNavyBackground
                    )
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ واختيار السيرفر", fontWeight = FontWeight.Bold)
                }
            } else if (selectedTabIndex == 1) {
                // Connect or Import via Direct Link
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (rawLinkText.isBlank()) {
                                linkError = "يرجى لصق رابط السيرفر أولاً"
                                return@Button
                            }
                            val res = onImportUri(rawLinkText.trim(), true)
                            if (res.isFailure) {
                                linkError = res.exceptionOrNull()?.message
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("import_and_connect_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VpnTealAccent,
                            contentColor = VpnNavyBackground
                        )
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اتصال فوري", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (rawLinkText.isBlank()) {
                                linkError = "يرجى لصق رابط السيرفر أولاً"
                                return@OutlinedButton
                            }
                            val res = onImportUri(rawLinkText.trim(), false)
                            if (res.isFailure) {
                                linkError = res.exceptionOrNull()?.message
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_link_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VpnTealAccent.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VpnTealAccent)
                    ) {
                        Text("حفظ بالقائمة")
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            manualError = "يرجى كتابة اسم للخادم"
                            return@Button
                        }
                        if (host.isBlank()) {
                            manualError = "يرجى إدخال عنوان IP أو النطاق"
                            return@Button
                        }
                        val portNum = port.toIntOrNull() ?: 443
                        onAddManualServer(name.trim(), host.trim(), portNum, dns.trim(), selectedProtocol)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("save_manual_server_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VpnTealAccent,
                        contentColor = VpnNavyBackground
                    )
                ) {
                    Text("حفظ السيرفر", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_dialog_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VpnTextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, VpnCardBorder)
            ) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun PresetChip(label: String, icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(VpnCardElevated)
            .border(1.dp, VpnCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$icon $label",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = VpnTextPrimary
        )
    }
}

@Composable
private fun ProtocolChip(proto: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) VpnTealAccent else VpnCardElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) VpnTealAccent else VpnCardBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = proto,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) VpnNavyBackground else VpnTextPrimary
        )
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = VpnCardBackground,
    unfocusedContainerColor = VpnCardBackground,
    focusedBorderColor = VpnTealAccent,
    unfocusedBorderColor = VpnCardBorder,
    focusedTextColor = VpnTextPrimary,
    unfocusedTextColor = VpnTextPrimary,
    cursorColor = VpnTealAccent
)
