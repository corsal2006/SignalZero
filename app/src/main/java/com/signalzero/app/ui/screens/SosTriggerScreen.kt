package com.signalzero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.service.EmergencyOrchestrator
import com.signalzero.app.ui.components.CategoryCard
import com.signalzero.app.ui.components.SosButton
import com.signalzero.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosTriggerScreen(
    orchestrator: EmergencyOrchestrator,
    selectedLang: String,
    onNavigateBack: () -> Unit,
    onSosDispatched: (String) -> Unit
) {
    val language = if (selectedLang == "hi") AppLanguage.HINDI else AppLanguage.ENGLISH
    var selectedCategory by remember { mutableStateOf(EmergencyCategory.MEDICAL) }
    var userNote by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "आपातकालीन चेतावनी भेजें" else "Trigger SOS Alert",
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Banner
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "संदेश टाइप करने की आवश्यकता नहीं है। श्रेणी चुनें और SOS बटन दबाएं। संदेश पहले से तैयार है।"
                        else
                            "No typing required in danger. Select an emergency category and tap SOS. Prewritten alerts dispatch instantly.",
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Categories Selector
            Text(
                text = if (language == AppLanguage.HINDI) "आपातकाल की श्रेणी चुनें" else "Select Emergency Category",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EmergencyCategory.values().forEach { category ->
                    CategoryCard(
                        category = category,
                        isSelected = category == selectedCategory,
                        language = language,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            // Prewritten message preview
            Surface(
                color = CardElevatedDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "पूर्व-लिखित संदेश पूर्वावलोकन" else "Prewritten Message Preview",
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val previewMsg = selectedCategory.getTemplateMessage(language)
                    Text(
                        text = "“$previewMsg”",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "• सटीक जीपीएस निर्देशांक, सटीकता और समय स्वतः संलग्न होंगे।"
                        else
                            "• GPS coordinates, accuracy, and timestamp will be attached automatically.",
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                }
            }

            // Optional note
            OutlinedTextField(
                value = userNote,
                onValueChange = { userNote = it },
                label = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "वैकल्पिक टिप्पणी (आवश्यक नहीं)" else "Optional Note (Not Required)"
                    )
                },
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "उदा. कमरा संख्या, लैंडमार्क..." else "e.g. Floor 2, room 14...",
                        color = MutedSlate
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeshCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prominent Hold / Press SOS Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                SosButton(
                    language = language,
                    onTrigger = {
                        val incidentId = orchestrator.triggerEmergency(
                            category = selectedCategory,
                            triggerSource = "MANUAL_CONFIRMED",
                            customNote = userNote.takeIf { it.isNotBlank() }
                        )
                        onSosDispatched(incidentId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
