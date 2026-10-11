package com.signalzero.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    val hasBt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        checkPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                checkPermission(Manifest.permission.BLUETOOTH_ADVERTISE) &&
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT)
    } else true

    val hasLocation = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val hasSms = checkPermission(Manifest.permission.SEND_SMS)
    val hasContacts = checkPermission(Manifest.permission.READ_CONTACTS)
    val hasMic = checkPermission(Manifest.permission.RECORD_AUDIO)
    val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkPermission(Manifest.permission.POST_NOTIFICATIONS)
    } else true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "अनुमतियां स्थिति" else "System Permissions",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MeshCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "SignalZero केवल आपातकालीन कार्यों के लिए न्यूनतम आवश्यक अनुमतियों का अनुरोध करता है। डेटा कभी बेचा या ट्रैक नहीं किया जाता।"
                        else
                            "SignalZero requests only essential hardware permissions for disaster communication. All data remains private.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        lineHeight = 17.sp
                    )
                }
            }

            PermissionItemCard(
                icon = Icons.Default.Bluetooth,
                title = if (language == AppLanguage.HINDI) "ब्लूटूथ लो एनर्जी (BLE)" else "Bluetooth Low Energy",
                reason = if (language == AppLanguage.HINDI) "आसपास के फ़ोनों से ऑफ़लाइन जुड़ने के लिए आवश्यक" else "Required for nearby offline discovery, messaging, and mesh relaying",
                isGranted = hasBt
            )

            PermissionItemCard(
                icon = Icons.Default.LocationOn,
                title = if (language == AppLanguage.HINDI) "सटीक जीपीएस स्थान" else "Precise Location (GPS)",
                reason = if (language == AppLanguage.HINDI) "SOS अलर्ट में सटीक निर्देशांक संलग्न करने के लिए" else "Attached to emergency alerts so responders can locate you",
                isGranted = hasLocation
            )

            PermissionItemCard(
                icon = Icons.Default.Sms,
                title = if (language == AppLanguage.HINDI) "आपातकालीन SMS" else "SMS Dispatch",
                reason = if (language == AppLanguage.HINDI) "विश्वासपात्र संपर्कों को सीधे आपातकालीन संदेश भेजने हेतु" else "Sends direct automated distress SMS to trusted contacts if cellular is active",
                isGranted = hasSms
            )

            PermissionItemCard(
                icon = Icons.Default.Contacts,
                title = if (language == AppLanguage.HINDI) "संपर्क सूची" else "Emergency Contacts",
                reason = if (language == AppLanguage.HINDI) "फ़ोन से आपातकालीन संपर्क आयात करने हेतु" else "Allows picking trusted family members from your contact book",
                isGranted = hasContacts
            )

            PermissionItemCard(
                icon = Icons.Default.Mic,
                title = if (language == AppLanguage.HINDI) "माइक्रोफ़ोन (वॉयस SOS)" else "Microphone (Voice SOS)",
                reason = if (language == AppLanguage.HINDI) "'Help me' / 'मदद करो' की पहचान के लिए (वैकल्पिक)" else "Listens for 'Help me' or 'मदद करो' when Voice SOS is enabled (Audio is never stored)",
                isGranted = hasMic
            )

            PermissionItemCard(
                icon = Icons.Default.Notifications,
                title = if (language == AppLanguage.HINDI) "सिस्टम सूचनाएं" else "Notifications",
                reason = if (language == AppLanguage.HINDI) "पृष्ठभूमि मेश सेवा और आने वाले संदेशों के लिए" else "Keeps disaster mesh service alive in the background and delivers incoming alerts",
                isGranted = hasNotifications
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MeshCyan, contentColor = ObsidianBg)
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "अनुमतियों का अनुरोध करें" else "Request Required Permissions",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "फ़ोन सेटिंग्स में खोलें" else "Open App System Settings",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    reason: String,
    isGranted: Boolean
) {
    Surface(
        color = CardElevatedDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isGranted) SuccessEmerald.copy(alpha = 0.15f) else AlertAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) SuccessEmerald else AlertAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (isGranted) SuccessEmerald.copy(alpha = 0.15f) else EmergencyRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isGranted) "GRANTED" else "REQUIRED",
                            color = if (isGranted) SuccessEmerald else EmergencyRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reason,
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
