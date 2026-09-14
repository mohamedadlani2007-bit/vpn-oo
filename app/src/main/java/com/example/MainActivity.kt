package com.example

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AddServerDialog
import com.example.ui.AdminDashboardDialog
import com.example.ui.AdminPasscodeDialog
import com.example.ui.ServerSelectionSheet
import com.example.ui.VpnLogsDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VpnCardBackground
import com.example.ui.theme.VpnCardBorder
import com.example.ui.theme.VpnCardElevated
import com.example.ui.theme.VpnConnectedGreen
import com.example.ui.theme.VpnConnectedGreenGlow
import com.example.ui.theme.VpnConnectingAmber
import com.example.ui.theme.VpnConnectingAmberGlow
import com.example.ui.theme.VpnCyanGlow
import com.example.ui.theme.VpnCyanPrimary
import com.example.ui.theme.VpnDisconnectedRed
import com.example.ui.theme.VpnNavyBackground
import com.example.ui.theme.VpnTealAccent
import com.example.ui.theme.VpnTealGlow
import com.example.ui.theme.VpnTextMuted
import com.example.ui.theme.VpnTextPrimary
import com.example.ui.theme.VpnTextSecondary
import com.example.vpn.ConnectionStatus
import com.example.vpn.VpnServer
import com.example.vpn.VpnStatistics
import com.example.vpn.VpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VpnAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnAppScreen(viewModel: VpnViewModel = viewModel()) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val status by viewModel.status.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val publicIp by viewModel.publicIp.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
    val protocol by viewModel.protocol.collectAsState()
    val killSwitch by viewModel.killSwitch.collectAsState()
    val dnsLeakProtection by viewModel.dnsLeakProtection.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val isTestingPing by viewModel.isTestingPing.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    val showServerSheet by viewModel.showServerSheet.collectAsState()
    val showLogsDialog by viewModel.showLogsDialog.collectAsState()
    val showAddServerDialog by viewModel.showAddServerDialog.collectAsState()
    val showAdminPasscodeDialog by viewModel.showAdminPasscodeDialog.collectAsState()
    val showAdminDashboard by viewModel.showAdminDashboard.collectAsState()
    val editingServer by viewModel.editingServer.collectAsState()
    val adminTestResult by viewModel.adminTestResult.collectAsState()
    val isTestingAdminServer by viewModel.isTestingAdminServer.collectAsState()

    var showPasswordText by remember { mutableStateOf(false) }

    // Android VPN Permission Launcher
    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.connect()
        } else {
            Toast.makeText(context, "تم إلغاء إذن الاتصال من قبل المستخدم", Toast.LENGTH_SHORT).show()
        }
    }

    // Android 13+ Notification Launcher
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val onConnectToggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        when (status) {
            ConnectionStatus.CONNECTED -> {
                viewModel.disconnect()
            }
            ConnectionStatus.CONNECTING -> {
                viewModel.disconnect()
            }
            else -> {
                viewModel.clearErrorMessage()
                val prepareIntent = viewModel.prepareVpnIntent()
                if (prepareIntent != null) {
                    vpnLauncher.launch(prepareIntent)
                } else {
                    viewModel.connect()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_screen"),
        containerColor = VpnNavyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(VpnTealAccent.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, VpnTealAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = VpnTealAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "VPN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = VpnTextPrimary
                        )

                        // Status Badge (Screenshot 1: "DESCONECTADO" / "CONECTADO")
                        val (statusBadgeText, statusBadgeColor) = when (status) {
                            ConnectionStatus.CONNECTED -> Pair("CONECTADO", VpnConnectedGreen)
                            ConnectionStatus.CONNECTING -> Pair("CONECTANDO", VpnConnectingAmber)
                            ConnectionStatus.ERROR -> Pair("ERRO", VpnDisconnectedRed)
                            else -> Pair("DESCONECTADO", Color(0xFFEF5350))
                        }

                        Box(
                            modifier = Modifier
                                .background(statusBadgeColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .border(1.dp, statusBadgeColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusBadgeText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusBadgeColor
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowAdminPasscodeDialog(true) },
                        modifier = Modifier.testTag("admin_panel_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "لوحة تحكم المدير (mooh2026)",
                            tint = VpnTealAccent
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setShowAddServerDialog(true) },
                        modifier = Modifier.testTag("quick_import_link_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة سيرفر Google Cloud",
                            tint = VpnTealAccent
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setShowLogsDialog(true) },
                        modifier = Modifier.testTag("open_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "سجل الاتصال",
                            tint = VpnTealAccent
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.refreshNetworkInfo()
                            viewModel.testServerPings()
                            Toast.makeText(context, "تم فحص حالة الشبكة والسيرفرات", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("refresh_network_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = VpnTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VpnNavyBackground,
                    titleContentColor = VpnTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VpnCardBackground,
                contentColor = VpnTealAccent,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, VpnCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Conectar") },
                    label = { Text("Conectar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VpnNavyBackground,
                        selectedTextColor = VpnTealAccent,
                        indicatorColor = VpnTealAccent,
                        unselectedIconColor = VpnTextMuted,
                        unselectedTextColor = VpnTextMuted
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Velocidade") },
                    label = { Text("Velocidade", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VpnNavyBackground,
                        selectedTextColor = VpnTealAccent,
                        indicatorColor = VpnTealAccent,
                        unselectedIconColor = VpnTextMuted,
                        unselectedTextColor = VpnTextMuted
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.setShowAddServerDialog(true) },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = "Google Cloud") },
                    label = { Text("Atualizar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VpnNavyBackground,
                        selectedTextColor = VpnTealAccent,
                        indicatorColor = VpnTealAccent,
                        unselectedIconColor = VpnTextMuted,
                        unselectedTextColor = VpnTextMuted
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuração") },
                    label = { Text("Configuração", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VpnNavyBackground,
                        selectedTextColor = VpnTealAccent,
                        indicatorColor = VpnTealAccent,
                        unselectedIconColor = VpnTextMuted,
                        unselectedTextColor = VpnTextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 0: CONECTAR (Matches Screenshot 1)

                    // Error Banner: Shown when server connection fails (Strict Requirement!)
                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, VpnDisconnectedRed, RoundedCornerShape(14.dp))
                                .testTag("connection_error_banner"),
                            color = VpnDisconnectedRed.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = VpnDisconnectedRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "فشل الاتصال - السيرفر غير شغال!",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VpnDisconnectedRed
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            fontSize = 11.5.sp,
                                            color = VpnTextPrimary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.clearErrorMessage() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = VpnTextSecondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Central Circular Connect Dial (Screenshot 1 style)
                    ScreenshotConnectDial(
                        status = status,
                        onClick = onConnectToggle
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // 2. Configuration Card (Screenshot 1 style: Server / Operadora, Usuário, Senha)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, VpnCardBorder, RoundedCornerShape(16.dp))
                            .testTag("server_configuration_card"),
                        color = VpnCardBackground
                    ) {
                        Column {
                            // Row 1: Operadora / Server Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setShowServerSheet(true) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .testTag("operadora_selector_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(VpnTealAccent.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = VpnTealAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedServer.cityAr,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VpnTextPrimary
                                            )
                                            if (selectedServer.sniHost.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(VpnTealAccent.copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "📺 ${selectedServer.sniHost}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VpnTealAccent
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${selectedServer.protocol} • ${selectedServer.host}:${selectedServer.port} ${if (selectedServer.pingMs > 0) "(${selectedServer.pingMs}ms)" else ""}",
                                            fontSize = 11.5.sp,
                                            color = VpnTextSecondary
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "فتح القائمة",
                                    tint = VpnTealAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            HorizontalDivider(color = VpnCardBorder, thickness = 1.dp)

                            // Row 2: Usuário (Username)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = VpnTealAccent,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Usuário (اسم المستخدم)",
                                        fontSize = 10.5.sp,
                                        color = VpnTextMuted
                                    )
                                    BasicTextField(
                                        value = username,
                                        onValueChange = { viewModel.setCredentials(it, password) },
                                        textStyle = TextStyle(color = VpnTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                        cursorBrush = SolidColor(VpnTealAccent),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("username_input_field"),
                                        decorationBox = { innerTextField ->
                                            if (username.isEmpty()) {
                                                Text("Digite seu usuário", color = VpnTextMuted, fontSize = 13.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(color = VpnCardBorder, thickness = 1.dp)

                            // Row 3: Senha (Password)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = VpnTealAccent,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Senha (كلمة المرور)",
                                        fontSize = 10.5.sp,
                                        color = VpnTextMuted
                                    )
                                    BasicTextField(
                                        value = password,
                                        onValueChange = { viewModel.setCredentials(username, it) },
                                        textStyle = TextStyle(color = VpnTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                        cursorBrush = SolidColor(VpnTealAccent),
                                        visualTransformation = if (showPasswordText) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("password_input_field"),
                                        decorationBox = { innerTextField ->
                                            if (password.isEmpty()) {
                                                Text("Digite sua senha", color = VpnTextMuted, fontSize = 13.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }

                                IconButton(
                                    onClick = { showPasswordText = !showPasswordText },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showPasswordText) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = VpnTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Host Bug / Plan description badge
                            if (selectedServer.sniHost.isNotBlank()) {
                                HorizontalDivider(color = VpnCardBorder, thickness = 1.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(VpnTealAccent.copy(alpha = 0.08f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = VpnTealAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "عرض الشريحة المطبق: ${selectedServer.sniHost} (تصفح عبر باقة اليوتيوب)",
                                        fontSize = 11.sp,
                                        color = VpnTealAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Admin Control Panel Card (كود الدخول: mooh2026)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, VpnTealAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { viewModel.setShowAdminPasscodeDialog(true) }
                            .testTag("admin_panel_info_card"),
                        color = VpnCardBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(VpnTealAccent.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = VpnTealAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "لوحة تحكم السيرفرات 🔐",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VpnTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(VpnTealAccent.copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("الكود: mooh2026", fontSize = 9.sp, color = VpnTealAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "إدارة سيرفرات Google Cloud VPS، إضافة وحذف وتعديل هوست يوتيوب ↗",
                                        fontSize = 11.sp,
                                        color = VpnTextSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = VpnTealAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick diagnostics actions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setShowLogsDialog(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("registros_quick_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VpnCardBorder)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = VpnTealAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registros (السجلات)", fontSize = 11.sp, color = VpnTextPrimary)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.testServerPings()
                                Toast.makeText(context, "جاري فحص استجابة السيرفرات...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("ping_test_quick_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VpnCardBorder)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = VpnTealAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فحص السيرفر", fontSize = 11.sp, color = VpnTextPrimary)
                        }
                    }

                    // 4. Live Connection Metrics Dashboard (Visible when connected)
                    AnimatedVisibility(
                        visible = status == ConnectionStatus.CONNECTED,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            MetricsDashboard(statistics = statistics)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                1 -> {
                    // TAB 1: VELOCIDADE (Speed & Latency Live Dashboard)
                    Text(
                        text = "فحص السرعة والاستجابة (Velocidade)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VpnTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.testServerPings() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VpnTealAccent)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = VpnTealAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة فحص استجابة جميع السيرفرات", fontSize = 12.sp, color = VpnTealAccent, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    servers.forEach { server ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, if (server.id == selectedServer.id) VpnTealAccent else VpnCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.selectServer(server)
                                    viewModel.setActiveTab(0)
                                },
                            color = VpnCardBackground
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = server.cityAr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VpnTextPrimary)
                                    Text(text = "${server.protocol} • ${server.host}:${server.port}", fontSize = 11.sp, color = VpnTextSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (server.pingMs > 0) VpnConnectedGreen.copy(alpha = 0.15f)
                                            else VpnDisconnectedRed.copy(alpha = 0.15f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (server.pingMs > 0) "🟢 ${server.pingMs} ms" else "🔴 غير متاح",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (server.pingMs > 0) VpnConnectedGreen else VpnDisconnectedRed
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                3 -> {
                    // TAB 3: CONFIGURAÇÃO (Settings)
                    // Admin Panel Entry in Settings
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, VpnTealAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { viewModel.setShowAdminPasscodeDialog(true) }
                            .testTag("admin_panel_settings_card"),
                        color = VpnCardBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(VpnTealAccent.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = VpnTealAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "لوحة تحكم المدير 🔐 (mooh2026)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VpnTextPrimary
                                    )
                                    Text(
                                        text = "إضافة وتعديل وحذف سيرفرات Google Cloud VPS وضبط ثغرات الشريحة (Host / SNI)",
                                        fontSize = 11.5.sp,
                                        color = VpnTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = VpnTealAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SecurityFeaturesCard(
                        protocol = protocol,
                        onProtocolChange = { viewModel.setProtocol(it) },
                        killSwitch = killSwitch,
                        onToggleKillSwitch = { viewModel.toggleKillSwitch() },
                        dnsLeakProtection = dnsLeakProtection,
                        onToggleDnsLeakProtection = { viewModel.toggleDnsLeakProtection() }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Bottom Sheets & Dialogs
    if (showServerSheet) {
        ServerSelectionSheet(
            servers = servers,
            selectedServer = selectedServer,
            isTestingPing = isTestingPing,
            onSelectServer = { viewModel.selectServer(it) },
            onRefreshPings = { viewModel.testServerPings() },
            onOpenAddCustomServer = {
                viewModel.setShowServerSheet(false)
                viewModel.setShowAddServerDialog(true)
            },
            onOpenAdminPanel = {
                viewModel.setShowServerSheet(false)
                viewModel.setShowAdminPasscodeDialog(true)
            },
            onDismiss = { viewModel.setShowServerSheet(false) }
        )
    }

    if (showAddServerDialog) {
        AddServerDialog(
            onImportUri = { uri, autoConnect ->
                val res = viewModel.importConfigUri(uri, autoConnect)
                if (res.isSuccess) {
                    Toast.makeText(context, "تم استيراد السيرفر بنجاح!", Toast.LENGTH_SHORT).show()
                }
                res
            },
            onAddManualServer = { name, host, port, dns, proto ->
                viewModel.addCustomServer(name, host, port, dns, proto)
                Toast.makeText(context, "تم حفظ سيرفر $name", Toast.LENGTH_SHORT).show()
            },
            onAddGcpServer = { name, host, port, user, pass, proto ->
                viewModel.addGoogleCloudServer(name, host, port, user, pass, proto)
                Toast.makeText(context, "تمت إضافة سيرفر Google Cloud $name", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { viewModel.setShowAddServerDialog(false) }
        )
    }

    if (showLogsDialog) {
        VpnLogsDialog(
            logs = logs,
            onClearLogs = { viewModel.clearLogs() },
            onDismiss = { viewModel.setShowLogsDialog(false) }
        )
    }

    if (showAdminPasscodeDialog) {
        AdminPasscodeDialog(
            onDismiss = { viewModel.setShowAdminPasscodeDialog(false) },
            onVerify = { code -> viewModel.verifyAdminPasscode(code) }
        )
    }

    if (showAdminDashboard) {
        AdminDashboardDialog(
            servers = servers,
            editingServer = editingServer,
            testResult = adminTestResult,
            isTesting = isTestingAdminServer,
            onSaveServer = { server ->
                viewModel.saveOrUpdateServer(server)
                Toast.makeText(context, "تم حفظ سيرفر ${server.cityAr} بنجاح!", Toast.LENGTH_SHORT).show()
            },
            onDeleteServer = { id ->
                viewModel.deleteServer(id)
                Toast.makeText(context, "تم حذف السيرفر", Toast.LENGTH_SHORT).show()
            },
            onTestServer = { server ->
                viewModel.testAdminServer(server)
            },
            onResetDefaults = {
                viewModel.resetServersToDefault()
                Toast.makeText(context, "تمت استعادة كافة السيرفرات الافتراضية", Toast.LENGTH_SHORT).show()
            },
            onSelectServerToEdit = { server ->
                viewModel.setEditingServer(server)
            },
            onDismiss = {
                viewModel.setShowAdminDashboard(false)
                viewModel.setEditingServer(null)
            }
        )
    }
}

@Composable
fun ScreenshotConnectDial(
    status: ConnectionStatus,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == ConnectionStatus.CONNECTING) 1.10f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val (buttonLabel, statusLabel, statusColor, statusDot) = when (status) {
        ConnectionStatus.CONNECTED -> Quadruple("DESCONECTAR", "Conectado", VpnConnectedGreen, "● Conectado")
        ConnectionStatus.CONNECTING -> Quadruple("CONECTANDO", "Conectando...", VpnConnectingAmber, "● Conectando...")
        ConnectionStatus.ERROR -> Quadruple("RECONECTAR", "Erro de Conexão", VpnDisconnectedRed, "● Erro de Conexão")
        else -> Quadruple("CONECTAR", "Desconectado", Color(0xFFEF5350), "● Desconectado")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .testTag("connect_power_button_container"),
            contentAlignment = Alignment.Center
        ) {
            // Glowing border ring
            Box(
                modifier = Modifier
                    .size(185.dp)
                    .scale(if (status == ConnectionStatus.CONNECTING || status == ConnectionStatus.CONNECTED) pulseScale else 1f)
                    .border(
                        width = 4.dp,
                        brush = if (status == ConnectionStatus.CONNECTED) {
                            Brush.sweepGradient(listOf(VpnConnectedGreen, VpnTealAccent, VpnConnectedGreen))
                        } else if (status == ConnectionStatus.CONNECTING) {
                            Brush.sweepGradient(listOf(VpnConnectingAmber, Color.White, VpnConnectingAmber))
                        } else {
                            Brush.sweepGradient(listOf(VpnTealAccent, Color(0xFF1E293B), VpnTealAccent))
                        },
                        shape = CircleShape
                    )
            )

            // Inner dark circular core
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF131A26))
                    .clickable { onClick() }
                    .testTag("vpn_connect_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (status == ConnectionStatus.CONNECTING) {
                        CircularProgressIndicator(
                            color = VpnConnectingAmber,
                            modifier = Modifier.size(38.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = buttonLabel,
                            tint = if (status == ConnectionStatus.CONNECTED) VpnConnectedGreen else VpnTealAccent,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = buttonLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status text below dial (Screenshot 1: "● Desconectado / Sua conexão começa aqui.")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = statusLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = if (status == ConnectionStatus.CONNECTED) "اتصالك مشفر ونشط الآن" else "Sua conexão começa aqui.",
            fontSize = 12.sp,
            color = VpnTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun MetricsDashboard(statistics: VpnStatistics) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VpnCardBorder, RoundedCornerShape(16.dp))
            .testTag("metrics_dashboard"),
        color = VpnCardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "بيانات الاتصال الفورية",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = VpnTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "سرعة التنزيل",
                    value = statistics.formattedDownloadSpeed(),
                    icon = Icons.Default.ArrowDownward,
                    color = VpnConnectedGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    title = "سرعة الرفع",
                    value = statistics.formattedUploadSpeed(),
                    icon = Icons.Default.ArrowUpward,
                    color = VpnTealAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "مدة الاتصال",
                    value = statistics.formattedDuration(),
                    icon = Icons.Default.Timer,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    title = "إجمالي البيانات",
                    value = statistics.formattedDownloadedTotal(),
                    icon = Icons.Default.Speed,
                    color = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, VpnCardBorder, RoundedCornerShape(12.dp)),
        color = VpnCardElevated
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = VpnTextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VpnTextPrimary
            )
        }
    }
}

@Composable
fun SecurityFeaturesCard(
    protocol: String,
    onProtocolChange: (String) -> Unit,
    killSwitch: Boolean,
    onToggleKillSwitch: () -> Unit,
    dnsLeakProtection: Boolean,
    onToggleDnsLeakProtection: () -> Unit
) {
    val protocols = listOf("SSH", "VLESS", "VMess", "WireGuard", "DNS Tunnel")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VpnCardBorder, RoundedCornerShape(16.dp))
            .testTag("security_features_card"),
        color = VpnCardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إعدادات الأمان والتشفير",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VpnTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "البروتوكول الافتراضي للاتصال:",
                fontSize = 12.sp,
                color = VpnTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                protocols.forEach { proto ->
                    val isSelected = proto.equals(protocol, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) VpnTealAccent.copy(alpha = 0.2f) else VpnCardElevated)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) VpnTealAccent else VpnCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onProtocolChange(proto) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = proto,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) VpnTealAccent else VpnTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kill Switch Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مفتاح القفل (Kill Switch)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VpnTextPrimary
                    )
                    Text(
                        text = "حظر الاتصال في حال انقطاع VPN لحماية الخصوصية",
                        fontSize = 11.sp,
                        color = VpnTextMuted
                    )
                }

                Switch(
                    checked = killSwitch,
                    onCheckedChange = { onToggleKillSwitch() },
                    modifier = Modifier.testTag("kill_switch_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VpnNavyBackground,
                        checkedTrackColor = VpnTealAccent,
                        uncheckedTrackColor = VpnCardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DNS Leak Protection Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حماية تسرب DNS (8.8.8.8 / 1.1.1.1)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VpnTextPrimary
                    )
                    Text(
                        text = "توجيه طلبات العناوين عبر قوقل كلاود المشفرة تمنع التعقب",
                        fontSize = 11.sp,
                        color = VpnTextMuted
                    )
                }

                Switch(
                    checked = dnsLeakProtection,
                    onCheckedChange = { onToggleDnsLeakProtection() },
                    modifier = Modifier.testTag("dns_leak_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VpnNavyBackground,
                        checkedTrackColor = VpnConnectedGreen,
                        uncheckedTrackColor = VpnCardElevated
                    )
                )
            }
        }
    }
}
