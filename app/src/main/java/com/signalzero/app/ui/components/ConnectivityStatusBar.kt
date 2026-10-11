package com.signalzero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.ui.theme.AlertAmber
import com.signalzero.app.ui.theme.BorderDark
import com.signalzero.app.ui.theme.MeshCyan
import com.signalzero.app.ui.theme.SuccessEmerald

@Composable
fun ConnectivityStatusBar(
    isBluetoothEnabled: Boolean,
    isOnline: Boolean,
    peerCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bluetooth Status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isBluetoothEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = if (isBluetoothEnabled) MeshCyan else AlertAmber,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isBluetoothEnabled) "BLE MESH" else "BLE OFF",
                fontSize = 11.sp,
                color = if (isBluetoothEnabled) MaterialTheme.colorScheme.onSurface else AlertAmber,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Internet Status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (isOnline) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isOnline) "CLOUD SYNC" else "OFFLINE",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Nearby Rescue Peers
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (peerCount > 0) SuccessEmerald else Color.Gray)
            )
            Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = null,
                tint = if (peerCount > 0) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 2.dp)
            )
            Text(
                text = "$peerCount PEERS",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
