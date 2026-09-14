package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.vpn.VpnServer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionSheet(
    servers: List<VpnServer>,
    selectedServer: VpnServer,
    isTestingPing: Boolean,
    onSelectServer: (VpnServer) -> Unit,
    onRefreshPings: () -> Unit,
    onOpenAddCustomServer: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredServers = remember(servers, searchQuery) {
        if (searchQuery.isBlank()) {
            servers
        } else {
            servers.filter {
                it.countryName.contains(searchQuery, ignoreCase = true) ||
                        it.countryNameAr.contains(searchQuery, ignoreCase = true) ||
                        it.city.contains(searchQuery, ignoreCase = true) ||
                        it.cityAr.contains(searchQuery, ignoreCase = true) ||
                        it.protocol.contains(searchQuery, ignoreCase = true) ||
                        it.host.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Grouping by category
    val gcpServers = filteredServers.filter { it.category == "GCP" }
    val sshServers = filteredServers.filter { it.category == "SSH" }
    val v2rayServers = filteredServers.filter { it.category == "V2RAY" }
    val customServers = filteredServers.filter { it.category == "CUSTOM" || (it.category != "GCP" && it.category != "SSH" && it.category != "V2RAY") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VpnNavyBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("server_selection_sheet")
        ) {
            // Header Row (Screenshot 2 style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = VpnTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "قائمة السيرفرات / Operadoras",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VpnTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshPings,
                        enabled = !isTestingPing,
                        modifier = Modifier.testTag("refresh_pings_button")
                    ) {
                        if (isTestingPing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VpnTealAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث البينج",
                                tint = VpnTealAccent
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenAddCustomServer,
                        modifier = Modifier.testTag("add_custom_server_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة سيرفر",
                            tint = VpnTealAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar (Screenshot 2 style)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_search_field"),
                placeholder = { Text("Buscar operadora ou servidor...", color = VpnTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = VpnTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VpnCardBackground,
                    unfocusedContainerColor = VpnCardBackground,
                    focusedBorderColor = VpnTealAccent,
                    unfocusedBorderColor = VpnCardBorder,
                    focusedTextColor = VpnTextPrimary,
                    unfocusedTextColor = VpnTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Big Full-width Action: SELECIONAR AUTOMATICAMENTE (Screenshot 2 style)
            Button(
                onClick = {
                    // Pick the fastest server with lowest positive ping
                    val fastest = servers.filter { it.pingMs > 0 }.minByOrNull { it.pingMs }
                        ?: servers.firstOrNull()
                    if (fastest != null) {
                        onSelectServer(fastest)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auto_select_fastest_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VpnTealAccent,
                    contentColor = VpnNavyBackground
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SELECIONAR AUTOMATICAMENTE (الأسرع تلقائياً)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Categorized Server List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (gcpServers.isNotEmpty()) {
                    item {
                        CategoryHeader("🌐 GOOGLE CLOUD PLATFORM (سيرفرات قوقل كلاود)")
                    }
                    items(gcpServers, key = { it.id }) { server ->
                        ServerListItem(
                            server = server,
                            isSelected = server.id == selectedServer.id,
                            onClick = { onSelectServer(server) }
                        )
                    }
                }

                if (sshServers.isNotEmpty()) {
                    item {
                        CategoryHeader("⚡ SSH / BHTTP DIRECT TUNNEL")
                    }
                    items(sshServers, key = { it.id }) { server ->
                        ServerListItem(
                            server = server,
                            isSelected = server.id == selectedServer.id,
                            onClick = { onSelectServer(server) }
                        )
                    }
                }

                if (v2rayServers.isNotEmpty()) {
                    item {
                        CategoryHeader("🟣 V2RAY / XRAY - VLESS & VMESS")
                    }
                    items(v2rayServers, key = { it.id }) { server ->
                        ServerListItem(
                            server = server,
                            isSelected = server.id == selectedServer.id,
                            onClick = { onSelectServer(server) }
                        )
                    }
                }

                if (customServers.isNotEmpty()) {
                    item {
                        CategoryHeader("➕ سيرفرات Google Cloud المضافة (CUSTOM)")
                    }
                    items(customServers, key = { it.id }) { server ->
                        ServerListItem(
                            server = server,
                            isSelected = server.id == selectedServer.id,
                            onClick = { onSelectServer(server) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = VpnTextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ServerListItem(
    server: VpnServer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) VpnTealAccent else VpnCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("server_item_${server.id}"),
        color = if (isSelected) VpnCardElevated else VpnCardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Radio-like Selection indicator + Server Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Radio indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VpnTealAccent else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) VpnTealAccent else VpnTextMuted,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = VpnNavyBackground,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = server.cityAr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VpnTextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${server.protocol} • ${server.host}:${server.port}",
                        fontSize = 11.sp,
                        color = VpnTextSecondary
                    )
                }
            }

            // Latency / Status Badge (Screenshot 2 style: "38 ms" or "Offline")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (server.pingMs > 0) VpnConnectedGreen.copy(alpha = 0.15f)
                        else VpnDisconnectedRed.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (server.pingMs > 0) VpnConnectedGreen.copy(alpha = 0.4f)
                        else VpnDisconnectedRed.copy(alpha = 0.4f),
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
