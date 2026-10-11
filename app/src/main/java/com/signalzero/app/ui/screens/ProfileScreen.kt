package com.signalzero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.repository.AuthRepository
import com.signalzero.app.data.repository.IncidentRepository
import com.signalzero.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    incidentRepository: IncidentRepository,
    language: AppLanguage,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser = authRepository.currentUser
    val allIncidents by incidentRepository.allIncidents.collectAsState(initial = emptyList())
    var showLogoutDialog by remember { mutableStateOf(false) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "उपयोगकर्ता प्रोफ़ाइल एवं सेटिंग्स" else "Profile & Quick Settings",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Avatar Card
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MeshCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MeshCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.displayName?.ifBlank { null } ?: (if (currentUser != null) "Verified Rescuer" else "Guest User (Offline)"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = currentUser?.email ?: "No cloud account connected (Local Mode)",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = if (currentUser != null) SuccessEmerald.copy(alpha = 0.15f) else AlertAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (currentUser != null) "FIREBASE AUTH ACTIVE" else "LOCAL OFFLINE PROFILE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentUser != null) SuccessEmerald else AlertAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (actionStatusMessage != null) {
                Surface(
                    color = MeshCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = actionStatusMessage ?: "",
                        color = MeshCyan,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Quick Navigation Hub (Settings, Permissions, Help)
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    TextButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MeshCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "ऐप सेटिंग्स (भाषा एवं थीम)" else "App Settings (Theme & Language)",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    TextButton(
                        onClick = onNavigateToPermissions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AlertAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "सिस्टम अनुमतियां" else "System Permissions & Sensors",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    HorizontalDivider(color = BorderDark, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    TextButton(
                        onClick = onNavigateToHelp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MeshCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "सहायता एवं आपातकालीन गाइड" else "Help & User Guide",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Local Data Integrity Card
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = MeshCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "स्थानीय डेटा सुरक्षा" else "Local Data Storage & Persistence",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "सहेजी गई स्थानीय आपातकालीन घटनाएं: ${allIncidents.size}\n• साइन आउट करने पर भी आपका स्थानीय डेटा कभी नष्ट नहीं होता।"
                        else
                            "Saved local incidents: ${allIncidents.size}\n• Logging out never deletes your locally persisted SOS incidents or mesh queue.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        lineHeight = 18.sp
                    )
                }
            }

            // Account Actions
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    if (currentUser != null) {
                        TextButton(
                            onClick = {
                                currentUser.email?.let { em ->
                                    coroutineScope.launch {
                                        val res = authRepository.sendPasswordReset(em)
                                        actionStatusMessage = if (res.isSuccess) "Password reset email sent to $em" else res.exceptionOrNull()?.message
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = MeshCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "पासवर्ड रीसेट लिंक भेजें" else "Send Password Reset Link",
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        TextButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = EmergencyRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "साइन आउट करें" else "Sign Out",
                                    color = EmergencyRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MeshCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "साइन इन या नया खाता बनाएं" else "Sign In / Create Account",
                                    color = MeshCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "साइन आउट की पुष्टि करें" else "Confirm Sign Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.HINDI)
                        "क्या आप वाकई साइन आउट करना चाहते हैं? आपका स्थानीय SOS इतिहास और ब्लूटूथ मेश सुरक्षित रहेगा।"
                    else
                        "Are you sure you want to sign out? Your local SOS history and offline Bluetooth mesh will remain completely intact.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authRepository.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text(if (language == AppLanguage.HINDI) "साइन आउट" else "Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}
