package com.signalzero.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization
import com.signalzero.app.data.model.Incident
import com.signalzero.app.ui.components.TimelineCard
import com.signalzero.app.ui.navigation.NavRoutes
import com.signalzero.app.ui.theme.EmergencyRed
import com.signalzero.app.ui.theme.MeshCyan
import com.signalzero.app.ui.theme.SuccessEmerald

@Composable
fun SosStatusScreen(
    incident: Incident?,
    language: AppLanguage,
    navController: NavController,
    onCancelIncident: (String) -> Unit,
    onResolveIncident: (String) -> Unit,
    onShareIncident: (String) -> Unit,
    onCallEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (incident == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Localization.get("no_incidents", language),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = Localization.get("incident_details", language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Incident Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = incident.category.getDisplayName(language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = EmergencyRed
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmergencyRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = incident.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = incident.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                if (incident.mapsUrl != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MeshCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "GPS: ${String.format("%.4f", incident.latitude)}, ${String.format("%.4f", incident.longitude)} (±${incident.accuracyMeters?.toInt()}m)",
                            fontSize = 11.sp,
                            color = MeshCyan,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                // AI Triage Rationale
                if (!incident.aiTriageRationale.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Triage AI: ${incident.aiTriageRationale}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verified Delivery Timeline Card
        TimelineCard(
            currentStatus = incident.deliveryStatus,
            language = language
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions (Share, View on Map, Call 112)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onShareIncident(incident.message) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = "Share", fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
            }

            Button(
                onClick = { navController.navigate(NavRoutes.Map.route) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = "Map", fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
            }

            Button(
                onClick = onCallEmergency,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = "112", fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel / Resolve Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onCancelIncident(incident.id) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                Text(text = Localization.get("cancel_sos", language), color = EmergencyRed, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
            }

            Button(
                onClick = { onResolveIncident(incident.id) },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = Localization.get("resolve_sos", language), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}
