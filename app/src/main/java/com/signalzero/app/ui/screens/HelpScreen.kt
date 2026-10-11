package com.signalzero.app.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    selectedLang: String,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedLang == "hi") "सहायता एवं मार्गदर्शन" else "Offline Help & Guides",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
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
            // How Mesh Works Section
            HelpSectionCard(
                title = if (selectedLang == "hi") "ऑफ़लाइन मेश कैसे काम करता है?" else "How Offline Mesh Works",
                icon = Icons.Default.Share,
                items = listOf(
                    if (selectedLang == "hi")
                        "1. जब मोबाइल नेटवर्क बंद हो जाता है, तो SignalZero ब्लूटूथ लो एनर्जी (BLE) का उपयोग करके सीधे आसपास के अन्य फ़ोनों से जुड़ता है।"
                    else
                        "1. When cellular towers fail, SignalZero uses Bluetooth Low Energy (BLE) to link directly to nearby smartphones without internet.",

                    if (selectedLang == "hi")
                        "2. स्टोर-एंड-फ़ॉरवर्ड: आपका SOS संदेश सुरक्षित रूप से आस-पास के प्रत्येक विश्वसनीय रिले फ़ोन पर भेजा जाता है, जो इसे आगे बढ़ाते हैं।"
                    else
                        "2. Store-and-Forward: Your encrypted alert hops from phone to phone across the disaster perimeter until a responder or connected node is reached.",

                    if (selectedLang == "hi")
                        "3. कोई इंटरनेट या सिम कार्ड आवश्यक नहीं है। संदेश पहले आपके फ़ोन के स्थानीय डेटाबेस में सुरक्षित होता है।"
                    else
                        "3. Zero internet or SIM card required. Your SOS is saved to local storage instantly before any wireless transmission begins."
                )
            )

            // Delivery States Explained
            HelpSectionCard(
                title = if (selectedLang == "hi") "वितरण स्थिति का क्या अर्थ है?" else "Delivery States Explained",
                icon = Icons.Default.CheckCircle,
                items = listOf(
                    if (selectedLang == "hi")
                        "• स्थानीय रूप से सहेजा गया (Saved Locally): आपका SOS डिवाइस में सुरक्षित है, भले ही कोई नेटवर्क या फ़ोन पास न हो।"
                    else
                        "• Saved Locally: Durably written to SQLite Room database. Completely safe from app crashes or battery death.",

                    if (selectedLang == "hi")
                        "• मेश कतार में (Mesh Queued): संदेश प्रसारण के लिए तैयार है और नजदीकी रिले डिवाइस खोज रहा है।"
                    else
                        "• Mesh Queued: Outgoing packet created, awaiting a nearby Bluetooth peer or relay.",

                    if (selectedLang == "hi")
                        "• सहकर्मी द्वारा स्वीकृत (Peer Accepted): आस-पास के किसी फ़ोन ने संदेश प्राप्त कर लिया है और उसे आगे बढ़ा रहा है।"
                    else
                        "• Peer Accepted: A nearby smartphone received the packet and verified its CRC integrity.",

                    if (selectedLang == "hi")
                        "• प्रतिक्रियाकर्ता द्वारा पावती (Acknowledged): एक बचाव दल या केंद्रीय नोड ने पुष्टि की है कि सहायता रास्ते में है।"
                    else
                        "• Responder Acknowledged: An official responder node has acknowledged receipt of your distress call."
                )
            )

            // Emergency Triggers Guide
            HelpSectionCard(
                title = if (selectedLang == "hi") "त्वरित ट्रिगर विधियां" else "Emergency Triggers",
                icon = Icons.Default.Notifications,
                items = listOf(
                    if (selectedLang == "hi")
                        "• शेक-टू-SOS (Shake-to-SOS): यदि सक्षम हो, तो फ़ोन को 3 सेकंड में 3 बार ज़ोर से हिलाने पर SOS शुरू हो जाता है (5 सेकंड का कूलडाउन)।"
                    else
                        "• Shake-to-SOS: When enabled, 3 deliberate shakes within 3 seconds triggers emergency dispatch with vibration feedback.",

                    if (selectedLang == "hi")
                        "• वॉयस SOS (Voice SOS): 'Help me' या 'मदद करो' बोलने पर आपातकालीन चेतावनी सक्रिय हो जाती है।"
                    else
                        "• Voice SOS: Speak 'Help me' or 'मदद करो' to automatically initiate emergency persistence and mesh broadcast."
                )
            )

            // Offline First-Aid & CPR Guidelines
            HelpSectionCard(
                title = if (selectedLang == "hi") "ऑफ़लाइन प्राथमिक उपचार एवं सीपीआर" else "Emergency First Aid & CPR Protocol",
                icon = Icons.Default.Favorite,
                items = listOf(
                    if (selectedLang == "hi")
                        "• वयस्क सीपीआर (CPR): 30 बार छाती को 100-120 बीपीएम की गति से दबाएं (Stayin' Alive गीत की गति), फिर 2 बचाव सांसें दें।"
                    else
                        "• Adult CPR: Push hard & fast in the center of the chest at 100-120 BPM (to the beat of 'Stayin' Alive'). 30 compressions followed by 2 rescue breaths.",

                    if (selectedLang == "hi")
                        "• गंभीर रक्तस्राव: साफ़ कपड़े या पट्टी से घाव पर लगातार सीधा दबाव बनाए रखें। दबाव कभी न हटाएं।"
                    else
                        "• Severe Bleeding: Apply firm, continuous direct pressure with a clean cloth. Do not remove saturated bandages; layer more on top.",

                    if (selectedLang == "hi")
                        "• दम घुटना: 5 बार पीठ के बीच में थपकी दें, फिर 5 बार पेट में ऊपर की ओर दबाव (Heimlich Maneuver) डालें।"
                    else
                        "• Choking: Alternate 5 back blows between shoulder blades with 5 upward abdominal thrusts (Heimlich maneuver)."
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HelpSectionCard(
    title: String,
    icon: ImageVector,
    items: List<String>
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Text(
                        text = item,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
