package com.signalzero.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.challenge.ResilienceTestReport
import com.signalzero.app.challenge.SimulationState
import com.signalzero.app.challenge.VirtualNode
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization
import com.signalzero.app.ui.components.ChallengeCard
import com.signalzero.app.ui.theme.AlertAmber
import com.signalzero.app.ui.theme.EmergencyRed
import com.signalzero.app.ui.theme.MeshCyan
import com.signalzero.app.ui.theme.SuccessEmerald
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengeScreen(
    simState: SimulationState,
    progress: Float,
    elapsedSeconds: Int,
    report: ResilienceTestReport?,
    nodes: List<VirtualNode>,
    language: AppLanguage,
    onRunTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MeshCyan,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = Localization.get("challenge_title", language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Grevix Hackathon Official Mandatory Challenge",
                    fontSize = 11.sp,
                    color = MeshCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Challenge Execution Card
        ChallengeCard(
            simState = simState,
            progress = progress,
            elapsedSeconds = elapsedSeconds,
            report = report,
            language = language,
            onRunTest = onRunTest
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Simulated Virtual Nodes Topology
        Text(
            text = "Participating Simulated Relay Nodes (10 Virtual Phones)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nodes.forEach { node ->
                val isDown = !node.isConnected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDown) EmergencyRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isDown) EmergencyRed else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDown) EmergencyRed else SuccessEmerald)
                        )
                        Text(
                            text = "${node.name}: " + if (isDown) "30s OUTAGE" else "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDown) EmergencyRed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Export JSON Report for Judges
        if (report != null) {
            OutlinedButton(
                onClick = {
                    val json = JSONObject().apply {
                        put("challenge", "Grevix AfterCode Emergency Relay Resilience")
                        put("totalNodes", report.totalNodes)
                        put("disconnectedCount", report.disconnectedNodesCount)
                        put("disconnectedPercentage", report.disconnectedPercentage)
                        put("outageDurationSeconds", report.disconnectionDurationSeconds)
                        put("packetsDelivered", report.packetsDelivered)
                        put("packetsRecovered", report.packetsRecoveredAfterReconnect)
                        put("duplicatesPrevented", report.duplicatesPrevented)
                        put("latencyP50Ms", report.deliveryLatencyP50Ms)
                        put("latencyP95Ms", report.deliveryLatencyP95Ms)
                        put("recoveryTimeMs", report.recoveryTimeMs)
                        put("isPassed", report.isPassed)
                        put("summary", report.summary)
                    }.toString(2)

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Grevix Resilience Report", json))
                    Toast.makeText(context, "Resilience Report JSON copied to clipboard!", Toast.LENGTH_LONG).show()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = MeshCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Export Judge Report (JSON)",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
