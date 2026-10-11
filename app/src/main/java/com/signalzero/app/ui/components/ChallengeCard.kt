package com.signalzero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.challenge.ResilienceTestReport
import com.signalzero.app.challenge.SimulationState
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization
import com.signalzero.app.ui.theme.BorderDark
import com.signalzero.app.ui.theme.MeshCyan
import com.signalzero.app.ui.theme.SuccessEmerald

@Composable
fun ChallengeCard(
    simState: SimulationState,
    progress: Float,
    elapsedSeconds: Int,
    report: ResilienceTestReport?,
    language: AppLanguage,
    onRunTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MeshCyan,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = Localization.get("challenge_title", language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (report?.isPassed == true) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PASSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessEmerald
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = Localization.get("challenge_objective", language),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar during test execution
        if (simState != SimulationState.IDLE && simState != SimulationState.COMPLETED) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (simState) {
                            SimulationState.BASELINE_STABLE -> "Step 1: Baseline Network Stable"
                            SimulationState.DISCONNECTING_20_PERCENT -> "Step 2: Disconnecting 20% Nodes..."
                            SimulationState.OUTAGE_ADAPTING_30S -> "Step 3: 30s Outage ($elapsedSeconds / 30s)"
                            SimulationState.RECONNECTING_DRAINING -> "Step 4: Reconnecting & Draining Queues..."
                            else -> "Running..."
                        },
                        fontSize = 12.sp,
                        color = MeshCyan,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = MeshCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MeshCyan,
                    trackColor = Color(0x2200E5FF)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Test Report Telemetry
        if (report != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                TelemetryRow(label = Localization.get("total_nodes", language), value = "${report.totalNodes} Nodes")
                TelemetryRow(label = Localization.get("disconnected_nodes", language), value = "${report.disconnectedNodesCount} (${report.disconnectedPercentage}%)")
                TelemetryRow(label = Localization.get("disconnection_duration", language), value = "${report.disconnectionDurationSeconds}s")
                TelemetryRow(label = Localization.get("packets_queued", language), value = "${report.packetsQueuedDuringOutage} Packets")
                TelemetryRow(label = Localization.get("packets_delivered", language), value = "${report.packetsDelivered} Delivered (100%)")
                TelemetryRow(label = Localization.get("duplicates_prevented", language), value = "${report.duplicatesPrevented} Filtered")
                TelemetryRow(label = Localization.get("avg_latency", language), value = "${report.deliveryLatencyP50Ms}ms / ${report.deliveryLatencyP95Ms}ms")
                TelemetryRow(label = Localization.get("recovery_time", language), value = "${report.recoveryTimeMs}ms")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Trigger Button
        Button(
            onClick = onRunTest,
            enabled = simState == SimulationState.IDLE || simState == SimulationState.COMPLETED,
            colors = ButtonDefaults.buttonColors(containerColor = MeshCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = Localization.get("challenge_run_sim", language),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
