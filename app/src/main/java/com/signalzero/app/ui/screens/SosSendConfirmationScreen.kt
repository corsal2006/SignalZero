package com.signalzero.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.LocationData
import com.signalzero.app.data.model.TrustedContact
import com.signalzero.app.data.repository.ContactRepository
import com.signalzero.app.service.EmergencyOrchestrator
import com.signalzero.app.ui.components.SosButton
import com.signalzero.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosSendConfirmationScreen(
    category: EmergencyCategory,
    orchestrator: EmergencyOrchestrator,
    contactRepository: ContactRepository,
    locationData: LocationData?,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onSosDispatched: (String) -> Unit,
    onPickNativeContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by contactRepository.allContacts.collectAsState(initial = emptyList())
    // Set of contact IDs selected for this specific emergency dispatch
    val selectedContactIds = remember { mutableStateMapOf<String, Boolean>() }
    // Per-contact channel toggles: Map<ContactId, Set<String>> (SMS, WHATSAPP, MESSENGER, SHARE)
    val contactChannels = remember { mutableStateMapOf<String, MutableSet<String>>() }

    var userCustomNote by remember { mutableStateOf("") }
    var isNoteExpanded by remember { mutableStateOf(false) }
    var isDispatching by remember { mutableStateOf(false) }

    // Initialize selection when contacts load
    LaunchedEffect(contacts) {
        contacts.forEach { contact ->
            if (!selectedContactIds.containsKey(contact.id)) {
                selectedContactIds[contact.id] = contact.isPrimary || contacts.size <= 3
            }
            if (!contactChannels.containsKey(contact.id)) {
                val set = mutableSetOf<String>()
                if (contact.notifyBySms) set.add("SMS")
                set.add("WHATSAPP")
                if (contact.notifyByShare) set.add("SHARE")
                contactChannels[contact.id] = set
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "SOS पुष्टि एवं संपर्क चयन" else "SOS Dispatch & Contacts",
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected Category & Prewritten Preview Card
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.getDisplayName(language),
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRed,
                            fontSize = 16.sp
                        )
                        Surface(
                            color = EmergencyRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "LEVEL ${category.severityLevel}",
                                color = EmergencyRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "“${category.getTemplateMessage(language)}”",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location status indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (locationData?.hasValidCoordinates == true) Icons.Default.LocationOn else Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = if (locationData?.hasValidCoordinates == true) SuccessEmerald else AlertAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (locationData?.hasValidCoordinates == true)
                                "GPS Fix: ±${locationData.accuracyMeters?.toInt() ?: 10}m (${locationData.freshness.getDisplayName(language)})"
                            else
                                (if (language == AppLanguage.HINDI) "जीपीएस अनुपलब्ध — ऑफ़लाइन सहेजा जाएगा" else "GPS Unavailable — Alert will save with timestamp only"),
                            fontSize = 11.sp,
                            color = if (locationData?.hasValidCoordinates == true) SuccessEmerald else AlertAmber
                        )
                    }
                }
            }

            // Contact Selection Header with Select All / Deselect All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "आपातकालीन संपर्क चयन" else "Select Contacts for Alert",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp
                )
                Row {
                    TextButton(onClick = { contacts.forEach { selectedContactIds[it.id] = true } }) {
                        Text(if (language == AppLanguage.HINDI) "सभी चुनें" else "Select All", fontSize = 12.sp, color = MeshCyan)
                    }
                    TextButton(onClick = { contacts.forEach { selectedContactIds[it.id] = false } }) {
                        Text(if (language == AppLanguage.HINDI) "हटाएं" else "Clear", fontSize = 12.sp, color = MutedSlate)
                    }
                }
            }

            // Contacts List
            if (contacts.isEmpty()) {
                Surface(
                    color = CardElevatedDark,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "कोई संपर्क नहीं जोड़ा गया।\nSOS सीधे स्थानीय रूप से और ब्लूटूथ मेश पर भेजा जाएगा।"
                            else
                                "No contacts configured.\nAlert will be saved locally & broadcast over Bluetooth mesh.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onPickNativeContact,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (language == AppLanguage.HINDI) "फ़ोन से संपर्क जोड़ें" else "Import Contact", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    contacts.forEach { contact ->
                        val isSelected = selectedContactIds[contact.id] ?: false
                        val channels = contactChannels[contact.id] ?: mutableSetOf()

                        Surface(
                            color = if (isSelected) CardElevatedDark else ObsidianBg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isSelected) MeshCyan.copy(alpha = 0.5f) else BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { selectedContactIds[contact.id] = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MeshCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = contact.name,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 14.sp
                                            )
                                            if (contact.isPrimary) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = AlertAmber.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "PRIMARY",
                                                        color = AlertAmber,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(text = contact.phoneNumber, fontSize = 12.sp, color = TextSecondaryDark)
                                    }
                                }

                                // Per-contact channel selector (visible if contact is selected)
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ChannelChip(
                                            label = "SMS",
                                            isSelected = channels.contains("SMS"),
                                            onToggle = {
                                                if (channels.contains("SMS")) channels.remove("SMS") else channels.add("SMS")
                                                contactChannels[contact.id] = channels
                                            }
                                        )
                                        ChannelChip(
                                            label = "WhatsApp",
                                            isSelected = channels.contains("WHATSAPP"),
                                            onToggle = {
                                                if (channels.contains("WHATSAPP")) channels.remove("WHATSAPP") else channels.add("WHATSAPP")
                                                contactChannels[contact.id] = channels
                                            }
                                        )
                                        ChannelChip(
                                            label = "Share",
                                            isSelected = channels.contains("SHARE"),
                                            onToggle = {
                                                if (channels.contains("SHARE")) channels.remove("SHARE") else channels.add("SHARE")
                                                contactChannels[contact.id] = channels
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Optional Note Accordion
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isNoteExpanded = !isNoteExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "वैकल्पिक टिप्पणी जोड़ें (आवश्यक नहीं)" else "Add Optional Note (Not Required)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Icon(
                            imageVector = if (isNoteExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MutedSlate
                        )
                    }

                    if (isNoteExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = userCustomNote,
                            onValueChange = { userCustomNote = it },
                            placeholder = { Text("e.g. 2nd floor, stairwell B...", color = MutedSlate, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeshCyan,
                                unfocusedBorderColor = BorderDark
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prominent Send SOS Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                SosButton(
                    language = language,
                    onTrigger = {
                        isDispatching = true
                        val incidentId = orchestrator.triggerEmergency(
                            category = category,
                            triggerSource = "CONFIRMED_WITH_CONTACTS",
                            customNote = userCustomNote.takeIf { it.isNotBlank() }
                        )
                        onSosDispatched(incidentId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ChannelChip(
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { Text(text = label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MeshCyan.copy(alpha = 0.2f),
            selectedLabelColor = MeshCyan
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (isSelected) MeshCyan else BorderDark,
            enabled = true,
            selected = isSelected
        )
    )
}
