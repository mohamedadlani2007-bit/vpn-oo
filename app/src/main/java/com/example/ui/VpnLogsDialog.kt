package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VpnCardBackground
import com.example.ui.theme.VpnCardBorder
import com.example.ui.theme.VpnCardElevated
import com.example.ui.theme.VpnConnectedGreen
import com.example.ui.theme.VpnCyanPrimary
import com.example.ui.theme.VpnDisconnectedRed
import com.example.ui.theme.VpnNavyBackground
import com.example.ui.theme.VpnTextMuted
import com.example.ui.theme.VpnTextPrimary
import com.example.ui.theme.VpnTextSecondary
import com.example.vpn.VpnLogEntry

@Composable
fun VpnLogsDialog(
    logs: List<VpnLogEntry>,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VpnNavyBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل العمليات والتشخيص",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VpnTextPrimary
                )
                if (logs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearLogs,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("clear_logs_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VpnDisconnectedRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VpnCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "مسح",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسح", fontSize = 12.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .testTag("vpn_logs_content")
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد سجلات اتصال حتى الآن.\nاضغط على زر الاتصال لبدء تشغيل VPN.",
                            fontSize = 13.sp,
                            color = VpnTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VpnCardBackground)
                            .border(1.dp, VpnCardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs.reversed()) { log ->
                            LogItemRow(log)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("close_logs_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VpnCyanPrimary,
                    contentColor = VpnNavyBackground
                )
            ) {
                Text("إغلاق", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun LogItemRow(log: VpnLogEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = log.formattedTime(),
            fontSize = 11.sp,
            color = VpnTextMuted,
            fontFamily = FontFamily.Monospace
        )

        Box(
            modifier = Modifier
                .background(
                    if (log.isError) VpnDisconnectedRed.copy(alpha = 0.2f)
                    else VpnCardElevated,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = log.tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (log.isError) VpnDisconnectedRed else VpnCyanPrimary
            )
        }

        Text(
            text = log.message,
            fontSize = 11.sp,
            color = if (log.isError) VpnDisconnectedRed else VpnTextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
