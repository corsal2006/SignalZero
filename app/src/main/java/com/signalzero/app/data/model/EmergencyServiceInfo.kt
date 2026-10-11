package com.signalzero.app.data.model

data class EmergencyServiceInfo(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val number: String,
    val isEditable: Boolean = true,
    val isEnabled: Boolean = true
) {
    companion object {
        fun defaultIndiaServices(): List<EmergencyServiceInfo> = listOf(
            EmergencyServiceInfo("police", "Police Control Room", "पुलिस नियंत्रण कक्ष", "112"),
            EmergencyServiceInfo("ambulance", "National Ambulance Service", "राष्ट्रीय एम्बुलेंस सेवा", "108"),
            EmergencyServiceInfo("fire", "Fire & Rescue Brigade", "अग्निशमन एवं बचाव दल", "101"),
            EmergencyServiceInfo("women", "Women's Safety Helpline", "महिला सुरक्षा हेल्पलाइन", "1091")
        )
    }
}
