package com.signalzero.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.TrustedContact
import com.signalzero.app.data.preferences.ThemeMode
import com.signalzero.app.mesh.MeshForegroundService
import com.signalzero.app.ui.navigation.SignalZeroNavGraph
import com.signalzero.app.ui.theme.SignalZeroTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val app by lazy { application as SignalZeroApplication }

    // Multi-permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allBtGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.BLUETOOTH_ADVERTISE] == true &&
                    permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        } else true

        val locGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        if (allBtGranted && locGranted) {
            startMeshService()
        }
    }

    // Native Contact Picker
    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        contactUri?.let { uri ->
            extractAndSaveContact(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request initial critical permissions
        requestRequiredPermissions()

        // Setup Shake Detector
        lifecycleScope.launch {
            app.userPreferences.shakeEnabledFlow.collect { enabled ->
                if (enabled) {
                    app.shakeDetector.startListening {
                        lifecycleScope.launch {
                            val activeLang = app.userPreferences.languageFlow.first()
                            Toast.makeText(
                                this@MainActivity,
                                if (activeLang == AppLanguage.HINDI) "शेक का पता चला! SOS ट्रिगर हो रहा है..." else "Shake Detected! Triggering Emergency SOS...",
                                Toast.LENGTH_SHORT
                            ).show()
                            app.emergencyOrchestrator.triggerEmergency(
                                category = EmergencyCategory.OTHER,
                                triggerSource = "SHAKE_MOTION"
                            )
                        }
                    }
                } else {
                    app.shakeDetector.stopListening()
                }
            }
        }

        // Setup Voice Detector
        lifecycleScope.launch {
            app.userPreferences.voiceEnabledFlow.collect { enabled ->
                if (enabled) {
                    app.voiceSosDetector.startListening { phrase ->
                        lifecycleScope.launch {
                            val activeLang = app.userPreferences.languageFlow.first()
                            Toast.makeText(
                                this@MainActivity,
                                if (activeLang == AppLanguage.HINDI) "वॉयस आपातकाल पहचाना गया! ($phrase)" else "Voice Distress Detected! ($phrase)",
                                Toast.LENGTH_SHORT
                            ).show()
                            app.emergencyOrchestrator.triggerEmergency(
                                category = EmergencyCategory.OTHER,
                                triggerSource = "VOICE_TRIGGER"
                            )
                        }
                    }
                } else {
                    app.voiceSosDetector.stopListening()
                }
            }
        }

        setContent {
            val themeMode by app.userPreferences.themeFlow.collectAsState(initial = ThemeMode.DARK)

            SignalZeroTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                SignalZeroNavGraph(
                    navController = navController,
                    orchestrator = app.emergencyOrchestrator,
                    authRepository = app.authRepository,
                    incidentRepository = app.incidentRepository,
                    contactRepository = app.contactRepository,
                    chatRepository = app.chatRepository,
                    peerRegistry = app.peerRegistry,
                    bleScanner = app.bleScanner,
                    bleAdvertiser = app.bleAdvertiser,
                    meshRouter = app.meshRouter,
                    locationService = app.locationService,
                    contactNotificationService = app.contactNotificationService,
                    firebaseSyncService = app.firebaseSyncService,
                    userPreferences = app.userPreferences,
                    simulator = app.grevixSimulator,
                    onPickContactClick = { contactPickerLauncher.launch(null) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasBluetoothPermissions() && hasLocationPermissions()) {
            startMeshService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        app.shakeDetector.stopListening()
        app.voiceSosDetector.stopListening()
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun startMeshService() {
        try {
            val serviceIntent = Intent(this, MeshForegroundService::class.java).apply {
                action = MeshForegroundService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start MeshForegroundService: ${e.message}")
        }
    }

    private fun extractAndSaveContact(contactUri: Uri) {
        lifecycleScope.launch {
            try {
                var name = ""
                var phoneNumber = ""

                val cursor: Cursor? = contentResolver.query(contactUri, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                        val contactId = if (idIndex != -1) c.getString(idIndex) else ""
                        name = if (nameIndex != -1) c.getString(nameIndex) ?: "" else "Contact"
                        val hasPhone = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) > 0 else false

                        if (hasPhone && contactId.isNotEmpty()) {
                            val phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numberIndex != -1) {
                                        phoneNumber = pc.getString(numberIndex) ?: ""
                                    }
                                }
                            }
                        }
                    }
                }

                if (phoneNumber.isNotBlank()) {
                    val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
                    app.contactRepository.insertContact(
                        TrustedContact(
                            name = name.ifBlank { "Emergency Contact" },
                            phoneNumber = cleanPhone,
                            isPrimary = false,
                            notifyBySms = true
                        )
                    )
                    Toast.makeText(this@MainActivity, "Added: $name ($cleanPhone)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "No phone number found for this contact", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error picking contact: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Failed to load contact", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
