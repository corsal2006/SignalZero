package com.signalzero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.DeliveryStatus
import com.signalzero.app.ui.theme.MeshCyan
import com.signalzero.app.ui.theme.SuccessEmerald

@Composable
fun TimelineCard(
    currentStatus: DeliveryStatus,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        DeliveryStatus.SAVED_LOCALLY,
        DeliveryStatus.QUEUED_FOR_RELAY,
        DeliveryStatus.ACCEPTED_BY_PEER,
        DeliveryStatus.RELAYED_BY_PEER,
        DeliveryStatus.RECEIVED_BY_SERVER,
        DeliveryStatus.ACKNOWLEDGED_BY_RESPONDER
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Verified Delivery Timeline",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(14.dp))

        steps.forEachIndexed { index, step ->
            val isCompleted = currentStatus.order >= step.order
            val isCurrent = currentStatus == step

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle Indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> SuccessEmerald
                                isCurrent -> MeshCyan
                                else -> Color(0x3394A3B8)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = step.getDisplayName(language),
                        fontSize = 13.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.onSurface
                            isCurrent -> MeshCyan
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 11.dp)
                        .width(2.dp)
                        .height(18.dp)
                        .background(if (isCompleted) SuccessEmerald else Color(0x2294A3B8))
                )
            }
        }
    }
}
