package com.signalzero.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.R
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization
import com.signalzero.app.ui.theme.*

@Composable
fun WelcomeScreen(
    language: AppLanguage,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Original SignalZero Logo Mark
            Image(
                painter = painterResource(id = R.drawable.ic_signalzero_logo),
                contentDescription = "SignalZero Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SignalZero",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (language == AppLanguage.HINDI)
                    "“जब नेटवर्क मौन हो जाता है, तब भी मदद पहुँचती है।”"
                else
                    "“When the network goes silent, help still gets through.”",
                fontSize = 14.sp,
                color = MeshCyan,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Value pillars
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureHighlightRow(
                        icon = Icons.Default.WifiOff,
                        title = if (language == AppLanguage.HINDI) "100% ऑफ़लाइन कार्यक्षमता" else "100% Offline Resilience",
                        subtitle = if (language == AppLanguage.HINDI) "इंटरनेट या सिम के बिना भी SOS तुरंत सहेजा और रिले किया जाता है" else "Emergency alerts save locally & relay across peers without cellular data"
                    )

                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                    FeatureHighlightRow(
                        icon = Icons.Default.Hub,
                        title = if (language == AppLanguage.HINDI) "ब्लूटूथ पीयर-टू-पीयर मेश" else "Bluetooth Peer Mesh",
                        subtitle = if (language == AppLanguage.HINDI) "फ़ोन से फ़ोन सुरक्षित हॉप रिले" else "Store-and-forward hopping across nearby rescue smartphones"
                    )

                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                    FeatureHighlightRow(
                        icon = Icons.Default.Bolt,
                        title = if (language == AppLanguage.HINDI) "बिना टाइपिंग का SOS" else "Zero-Typing Dispatch",
                        subtitle = if (language == AppLanguage.HINDI) "पूर्व-लिखित द्विभाषी संदेश और सटीक जीपीएस" else "Prewritten English & Hindi templates with bounded GPS fix"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeshCyan,
                    contentColor = ObsidianBg
                )
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "साइन इन करें" else "Sign In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "नया खाता बनाएं" else "Create Account",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            TextButton(
                onClick = onContinueOffline,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI)
                        "अतिथि / ऑफ़लाइन मोड में जारी रखें →"
                    else
                        "Continue in Offline Emergency Mode →",
                    color = MutedSlate,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FeatureHighlightRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MeshCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MeshCyan,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondaryDark,
                lineHeight = 16.sp
            )
        }
    }
}
