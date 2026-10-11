package com.signalzero.app.core

object Constants {
    const val TAG = "SignalZero"
    const val APP_NAME = "SignalZero"
    const val TAGLINE_EN = "When the network goes silent, help still gets through."
    const val TAGLINE_HI = "जब नेटवर्क मौन हो जाता है, तब भी मदद पहुँचती है।"

    // Emergency Numbers (Official India ERSS Verified)
    const val DEFAULT_POLICE_NUMBER = "112"
    const val DEFAULT_AMBULANCE_NUMBER = "108"
    const val DEFAULT_FIRE_NUMBER = "101"
    const val DEFAULT_WOMEN_HELPLINE_NUMBER = "1091"

    // Mesh & Protocol Constants
    const val MAX_HOP_COUNT = 5
    const val DEFAULT_PACKET_TTL_MS = 3600000L // 1 hour
    const val RETRY_BACKOFF_BASE_MS = 2000L
    const val MAX_RETRIES = 4

    // Location
    const val LOCATION_TIMEOUT_MS = 8000L
    const val FRESH_LOCATION_MAX_AGE_MS = 120000L // 2 minutes
    const val FRESH_LOCATION_MAX_ACCURACY_M = 200.0f
}
