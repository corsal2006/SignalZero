package com.signalzero.app.core

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी")
}

object Localization {

    private val stringsEn = mapOf(
        // General & Brand
        "app_name" to "SignalZero",
        "tagline" to "When the network goes silent, help still gets through.",
        "emergency" to "EMERGENCY",
        "sos" to "SOS",
        "hold_to_sos" to "HOLD FOR SOS",
        "press_and_hold_hint" to "Press and hold for 1.5s to trigger emergency",
        "offline_ready" to "Offline Mesh Ready",
        "online_cloud" to "Cloud Sync Active",
        "no_internet" to "No Internet — Mesh Only",
        "bluetooth_off" to "Bluetooth Disabled",
        "bluetooth_on" to "Bluetooth Active",
        "peers_nearby" to "Nearby Rescue Peers",

        // Navigation
        "nav_home" to "Home",
        "nav_map" to "Map",
        "nav_network" to "Network",
        "nav_contacts" to "Contacts",
        "nav_more" to "More",
        "nav_chat" to "Peer Chat",
        "nav_challenge" to "Grevix Challenge",
        "nav_history" to "Incident History",
        "nav_settings" to "Settings",
        "nav_help" to "Offline Guide",

        // Categories
        "cat_medical" to "Medical Emergency",
        "cat_fire" to "Fire Hazard",
        "cat_flood" to "Flood / Water",
        "cat_earthquake" to "Earthquake",
        "cat_trapped" to "Trapped",
        "cat_personal_safety" to "Personal Safety",
        "cat_other" to "Other Emergency",

        // Prewritten Templates
        "template_medical" to "MEDICAL EMERGENCY. I need urgent medical assistance. Please help me.",
        "template_fire" to "FIRE EMERGENCY. There may be immediate danger. Please send help.",
        "template_flood" to "FLOOD EMERGENCY. I need help because of flooding or rising water.",
        "template_earthquake" to "EARTHQUAKE EMERGENCY. I need assistance. Please help me reach safety.",
        "template_trapped" to "I AM TRAPPED. Please help me reach safety.",
        "template_personal_safety" to "I NEED PERSONAL SAFETY ASSISTANCE. Please contact me or send help.",
        "template_other" to "I NEED EMERGENCY ASSISTANCE. Please help me.",

        // Delivery Statuses
        "status_saved_locally" to "Saved Locally",
        "status_queued_relay" to "Queued for Relay",
        "status_searching_peers" to "Searching for Nearby Devices",
        "status_accepted_peer" to "Accepted by Peer",
        "status_relayed_peer" to "Relayed by Peer",
        "status_received_server" to "Received by Server",
        "status_acknowledged" to "Acknowledged by Responder",
        "status_cancelled" to "Cancelled",
        "status_resolved" to "Resolved",
        "status_failed_expired" to "Delivery Failed or Expired",

        // Location Freshness
        "loc_fresh" to "Fresh GPS Fix",
        "loc_stale" to "Stale GPS Fix (>2m)",
        "loc_last_known" to "Last Known Location",
        "loc_unavailable" to "Location Unavailable",
        "loc_denied" to "Location Permission Denied",

        // Contacts & Actions
        "contacts_title" to "Trusted Emergency Contacts",
        "add_contact" to "Add Contact",
        "pick_contact" to "Pick from Phone Contacts",
        "name" to "Name",
        "phone_number" to "Phone Number",
        "is_primary" to "Primary Contact",
        "send_sms" to "Send Emergency SMS",
        "open_sms_composer" to "Open in SMS App",
        "share_sheet" to "Share Emergency Details",
        "call_service" to "Call Emergency Services",
        "call_confirm" to "Are you sure you want to call this emergency service?",
        "police" to "Police (112)",
        "ambulance" to "Ambulance (108)",
        "fire_service" to "Fire Brigade (101)",
        "women_helpline" to "Women's Helpline (1091)",

        // Grevix Challenge
        "challenge_title" to "Grevix Resilience Challenge",
        "challenge_objective" to "Maintaining emergency communication when 20%+ of relay phones disconnect for 30 seconds.",
        "challenge_run_sim" to "Run 20% / 30s Resilience Simulation",
        "challenge_status_idle" to "Ready to Execute Test",
        "challenge_status_running" to "Simulating 20% Node Disconnection for 30s...",
        "challenge_status_passed" to "CHALLENGE PASSED: Zero Packet Loss & Flawless Queue Recovery",
        "total_nodes" to "Total Participating Nodes",
        "disconnected_nodes" to "Disconnected Nodes",
        "disconnection_duration" to "Disconnection Duration",
        "packets_queued" to "Packets Safely Queued",
        "packets_delivered" to "Packets Delivered",
        "packets_recovered" to "Packets Recovered After Reconnect",
        "duplicates_prevented" to "Duplicate Packets Prevented",
        "avg_latency" to "Delivery Latency P50 / P95",
        "recovery_time" to "Queue Draining Recovery Time",

        // Triggers & Settings
        "shake_to_sos" to "Shake-to-SOS (3 Deliberate Shakes)",
        "voice_to_sos" to "Voice SOS ('Help me' / 'मदद करो')",
        "test_shake" to "Test Shake Sensitivity",
        "test_voice" to "Test Voice Phrase",
        "language_select" to "Language / भाषा",
        "theme_select" to "Visual Theme",
        "theme_dark" to "Obsidian Dark",
        "theme_light" to "Warm Light",
        "theme_system" to "System Default",
        "cancel_sos" to "Cancel SOS",
        "resolve_sos" to "Mark Resolved",
        "confirm_cancel" to "Are you sure you want to cancel this emergency incident?",
        "incident_details" to "Incident Details",
        "view_on_map" to "View on Interactive Map",
        "no_incidents" to "No emergency incidents recorded."
    )

    private val stringsHi = mapOf(
        // General & Brand
        "app_name" to "SignalZero",
        "tagline" to "जब नेटवर्क मौन हो जाता है, तब भी मदद पहुँचती है।",
        "emergency" to "आपातकाल",
        "sos" to "आपातकालीन SOS",
        "hold_to_sos" to "SOS दबाकर रखें",
        "press_and_hold_hint" to "आपातकाल ट्रिगर करने के लिए 1.5 सेकंड तक दबाए रखें",
        "offline_ready" to "ऑफ़लाइन मेश नेटवर्क सक्रिय",
        "online_cloud" to "क्लाउड सिंक सक्रिय",
        "no_internet" to "इंटरनेट नहीं — केवल मेश",
        "bluetooth_off" to "ब्लूटूथ बंद है",
        "bluetooth_on" to "ब्लूटूथ सक्रिय",
        "peers_nearby" to "आस-पास के रेस्क्यू उपकरण",

        // Navigation
        "nav_home" to "होम",
        "nav_map" to "मानचित्र",
        "nav_network" to "नेटवर्क",
        "nav_contacts" to "संपर्क",
        "nav_more" to "अधिक",
        "nav_chat" to "मेश चैट",
        "nav_challenge" to "ग्रेविक्स चैलेंज",
        "nav_history" to "घटना इतिहास",
        "nav_settings" to "सेटिंग्स",
        "nav_help" to "ऑफ़लाइन गाइड",

        // Categories
        "cat_medical" to "चिकित्सा आपातकाल",
        "cat_fire" to "आग लगने की आपात स्थिति",
        "cat_flood" to "बाढ़ / जलभराव",
        "cat_earthquake" to "भूकंप",
        "cat_trapped" to "फँसा हुआ/हुई",
        "cat_personal_safety" to "व्यक्तिगत सुरक्षा",
        "cat_other" to "अन्य आपातकाल",

        // Prewritten Templates
        "template_medical" to "चिकित्सा आपातकाल है। मुझे तुरंत चिकित्सा सहायता चाहिए। कृपया मेरी मदद करें।",
        "template_fire" to "आग लगने की आपात स्थिति है। तुरंत खतरा हो सकता है। कृपया मदद भेजें।",
        "template_flood" to "बाढ़ की आपात स्थिति है। मुझे मदद चाहिए। कृपया सहायता भेजें।",
        "template_earthquake" to "भूकंप की आपात स्थिति है। मुझे सहायता चाहिए। कृपया सुरक्षित स्थान तक पहुँचने में मदद करें।",
        "template_trapped" to "मैं फँसा हुआ/हुई हूँ। कृपया मुझे सुरक्षित निकालने में मदद करें।",
        "template_personal_safety" to "मुझे अपनी सुरक्षा के लिए सहायता चाहिए। कृपया मुझसे संपर्क करें या मदद भेजें।",
        "template_other" to "मुझे आपातकालीन सहायता चाहिए। कृपया मेरी मदद करें।",

        // Delivery Statuses
        "status_saved_locally" to "स्थानीय रूप से सहेजा गया",
        "status_queued_relay" to "रिले के लिए कतारबद्ध",
        "status_searching_peers" to "निकटवर्ती उपकरणों की खोज जारी",
        "status_accepted_peer" to "पीयर द्वारा स्वीकार किया गया",
        "status_relayed_peer" to "पीयर द्वारा अग्रेषित",
        "status_received_server" to "सर्वर द्वारा प्राप्त",
        "status_acknowledged" to "बचाव दल द्वारा पुष्टि की गई",
        "status_cancelled" to "रद्द किया गया",
        "status_resolved" to "सुलझाया गया",
        "status_failed_expired" to "वितरण विफल या समय समाप्त",

        // Location Freshness
        "loc_fresh" to "ताज़ा GPS स्थान",
        "loc_stale" to "पुराना GPS स्थान (>2 मिनट)",
        "loc_last_known" to "अंतिम ज्ञात स्थान",
        "loc_unavailable" to "स्थान अनुपलब्ध",
        "loc_denied" to "स्थान अनुमति अस्वीकृत",

        // Contacts & Actions
        "contacts_title" to "विश्वसनीय आपातकालीन संपर्क",
        "add_contact" to "संपर्क जोड़ें",
        "pick_contact" to "फ़ोन से संपर्क चुनें",
        "name" to "नाम",
        "phone_number" to "फ़ोन नंबर",
        "is_primary" to "प्राथमिक संपर्क",
        "send_sms" to "आपातकालीन SMS भेजें",
        "open_sms_composer" to "SMS ऐप में खोलें",
        "share_sheet" to "आपातकालीन विवरण साझा करें",
        "call_service" to "आपातकालीन सेवाओं को कॉल करें",
        "call_confirm" to "क्या आप वाकई इस आपातकालीन सेवा को कॉल करना चाहते हैं?",
        "police" to "पुलिस (112)",
        "ambulance" to "एम्बुलेंस (108)",
        "fire_service" to "अग्निशमन दल (101)",
        "women_helpline" to "महिला हेल्पलाइन (1091)",

        // Grevix Challenge
        "challenge_title" to "ग्रेविक्स लचीलापन चुनौती",
        "challenge_objective" to "जब 20%+ रिले फ़ोन 30 सेकंड के लिए डिस्कनेक्ट हो जाते हैं, तब भी आपातकालीन संचार बनाए रखना।",
        "challenge_run_sim" to "20% / 30 सेकंड लचीलापन परीक्षण चलाएँ",
        "challenge_status_idle" to "परीक्षण चलाने के लिए तैयार",
        "challenge_status_running" to "30 सेकंड के लिए 20% नोड्स का वियोग अनुकरण किया जा रहा है...",
        "challenge_status_passed" to "चुनौती उत्तीर्ण: शून्य पैकेट हानि और त्रुटिहीन कतार पुनर्प्राप्ति",
        "total_nodes" to "कुल सहभागी नोड्स",
        "disconnected_nodes" to "डिस्कनेक्ट किए गए नोड्स",
        "disconnection_duration" to "वियोग की अवधि",
        "packets_queued" to "सुरक्षित कतारबद्ध पैकेट",
        "packets_delivered" to "सफलतापूर्वक वितरित पैकेट",
        "packets_recovered" to "पुनः जुड़ने पर पुनर्प्राप्त पैकेट",
        "duplicates_prevented" to "रोके गए डुप्लिकेट पैकेट",
        "avg_latency" to "वितरण विलंबता P50 / P95",
        "recovery_time" to "कतार खाली करने का समय",

        // Triggers & Settings
        "shake_to_sos" to "शेक-टू-SOS (3 बार हिलाएँ)",
        "voice_to_sos" to "वॉइस SOS ('Help me' / 'मदद करो')",
        "test_shake" to "शेक संवेदनशीलता का परीक्षण करें",
        "test_voice" to "वॉइस वाक्यांश का परीक्षण करें",
        "language_select" to "भाषा / Language",
        "theme_select" to "थीम",
        "theme_dark" to "ऑब्सिडियन डार्क",
        "theme_light" to "वार्म लाइट",
        "theme_system" to "सिस्टम डिफ़ॉल्ट",
        "cancel_sos" to "SOS रद्द करें",
        "resolve_sos" to "सुलझा हुआ चिह्नित करें",
        "confirm_cancel" to "क्या आप वाकई इस आपातकालीन स्थिति को रद्द करना चाहते हैं?",
        "incident_details" to "घटना विवरण",
        "view_on_map" to "मानचित्र पर देखें",
        "no_incidents" to "कोई आपातकालीन घटना दर्ज नहीं है।"
    )

    fun get(key: String, language: AppLanguage = AppLanguage.ENGLISH): String {
        val dict = if (language == AppLanguage.HINDI) stringsHi else stringsEn
        return dict[key] ?: stringsEn[key] ?: key
    }
}
