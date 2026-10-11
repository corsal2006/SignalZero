package com.signalzero.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.signalzero.app.data.model.Incident
import com.signalzero.app.data.model.TrustedContact

sealed class NotificationDispatchResult {
    data class DirectSmsSent(val recipientCount: Int) : NotificationDispatchResult()
    data class ComposerOpened(val phoneNumber: String) : NotificationDispatchResult()
    data class ShareSheetOpened(val text: String) : NotificationDispatchResult()
    data class Failed(val error: String) : NotificationDispatchResult()
}

class ContactNotificationService(private val context: Context) {

    private val tag = "SignalZero_Notifications"

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun dispatchEmergencyAlert(
        incident: Incident,
        contacts: List<TrustedContact>
    ): NotificationDispatchResult {
        if (contacts.isEmpty()) {
            return NotificationDispatchResult.Failed("No trusted contacts configured.")
        }

        val messageText = buildAlertMessage(incident)

        if (hasSmsPermission()) {
            var sentCount = 0
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            for (contact in contacts.filter { it.notifyBySms }) {
                try {
                    val parts = smsManager.divideMessage(messageText)
                    smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null)
                    sentCount++
                } catch (e: Exception) {
                    Log.e(tag, "Failed to send SMS to ${contact.phoneNumber}: ${e.message}")
                }
            }

            if (sentCount > 0) {
                return NotificationDispatchResult.DirectSmsSent(sentCount)
            }
        }

        // Fallback: Open SMS composer for primary contact or first contact
        val target = contacts.firstOrNull { it.isPrimary } ?: contacts.first()
        return openSmsComposer(target.phoneNumber, messageText)
    }

    fun openSmsComposer(phoneNumber: String, text: String): NotificationDispatchResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            NotificationDispatchResult.ComposerOpened(phoneNumber)
        } catch (e: Exception) {
            openShareSheet(text)
        }
    }

    fun openShareSheet(text: String): NotificationDispatchResult {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Emergency SOS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
            NotificationDispatchResult.ShareSheetOpened(text)
        } catch (e: Exception) {
            NotificationDispatchResult.Failed(e.message ?: "Failed to open share sheet")
        }
    }

    fun shareIncident(incident: Incident): NotificationDispatchResult {
        return openShareSheet(buildAlertMessage(incident))
    }

    @SuppressLint("MissingPermission")
    fun callEmergencyNumber(number: String) {
        val intent = if (hasCallPermission()) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun buildAlertMessage(incident: Incident): String {
        val mapsLink = incident.mapsUrl?.let { "\nLocation: $it" } ?: ""
        return "🚨 SIGNALZERO EMERGENCY ALERT [${incident.id}]\n" +
                "Category: ${incident.category.name}\n" +
                "${incident.message}" +
                mapsLink
    }
}
