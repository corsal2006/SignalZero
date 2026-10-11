package com.signalzero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    selectedLang: String,
    onNavigateToChallenge: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedLang == "hi") "अधिक विकल्प" else "More Options",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Grevix Hackathon Banner
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToChallenge() }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedLang == "hi") "ग्रेविक्स रेजिलिएंस चैलेंज" else "Grevix Resilience Challenge",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (selectedLang == "hi") "20% नोड विच्छेदन / 30s सिमुलेशन टेस्ट" else "20% Node Disconnection / 30s Test Harness",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Options List
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    MoreOptionRow(
                        title = if (selectedLang == "hi") "ऑफ़लाइन पीयर चैट" else "Offline Peer Chat",
                        subtitle = if (selectedLang == "hi") "ब्लूटूथ लो एनर्जी द्वारा प्रत्यक्ष संदेश" else "Direct device-to-device BLE messaging",
                        icon = Icons.Default.MailOutline,
                        iconTint = AccentBlue,
                        onClick = onNavigateToChat
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    MoreOptionRow(
                        title = if (selectedLang == "hi") "आपातकालीन इतिहास" else "Emergency History",
                        subtitle = if (selectedLang == "hi") "सहेजे गए SOS और प्राप्त किए गए अलर्ट" else "Archived SOS alerts and triage timeline",
                        icon = Icons.Default.List,
                        iconTint = AccentWarning,
                        onClick = onNavigateToHistory
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    MoreOptionRow(
                        title = if (selectedLang == "hi") "सेटिंग्स एवं प्राथमिकताएं" else "Settings & Preferences",
                        subtitle = if (selectedLang == "hi") "भाषा, थीम, शेक और वॉयस ट्रिगर" else "Language, theme, shake & voice triggers",
                        icon = Icons.Default.Settings,
                        iconTint = TextSecondary,
                        onClick = onNavigateToSettings
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    MoreOptionRow(
                        title = if (selectedLang == "hi") "ऑफ़लाइन सहायता एवं सीपीआर" else "Offline Help & First Aid",
                        subtitle = if (selectedLang == "hi") "मेश निर्देश एवं आपातकालीन प्रक्रियाएं" else "Disaster guidance & CPR instructions",
                        icon = Icons.Default.Info,
                        iconTint = AccentGreen,
                        onClick = onNavigateToHelp
                    )
                }
            }

            // Product Tagline & Hackathon Metadata
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SignalZero v1.0.0",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedLang == "hi")
                        "“जब नेटवर्क मौन हो जाता है, तब भी मदद पहुँचती है।”"
                    else
                        "“When the network goes silent, help still gets through.”",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Grevix Hackathon AfterCode • Offline BLE Mesh",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MoreOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
