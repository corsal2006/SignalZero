package com.signalzero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tsunami
import androidx.compose.material.icons.filled.Warning
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
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.ui.theme.AlertAmber
import com.signalzero.app.ui.theme.EmergencyRed
import com.signalzero.app.ui.theme.MeshCyan

@Composable
fun CategoryCard(
    category: EmergencyCategory,
    isSelected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (category) {
        EmergencyCategory.MEDICAL -> Icons.Default.LocalHospital
        EmergencyCategory.FIRE -> Icons.Default.Fireplace
        EmergencyCategory.FLOOD -> Icons.Default.Tsunami
        EmergencyCategory.EARTHQUAKE -> Icons.Default.Warning
        EmergencyCategory.TRAPPED -> Icons.Default.Lock
        EmergencyCategory.PERSONAL_SAFETY -> Icons.Default.Shield
        EmergencyCategory.OTHER -> Icons.Default.Emergency
    }

    val iconColor = when (category) {
        EmergencyCategory.MEDICAL, EmergencyCategory.FIRE -> EmergencyRed
        EmergencyCategory.FLOOD, EmergencyCategory.EARTHQUAKE -> MeshCyan
        EmergencyCategory.TRAPPED, EmergencyCategory.PERSONAL_SAFETY -> AlertAmber
        EmergencyCategory.OTHER -> Color.LightGray
    }

    val borderColor = if (isSelected) EmergencyRed else MaterialTheme.colorScheme.outline
    val bgColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.getDisplayName(language),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
