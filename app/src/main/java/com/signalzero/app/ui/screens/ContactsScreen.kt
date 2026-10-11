package com.signalzero.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization
import com.signalzero.app.data.model.EmergencyServiceInfo
import com.signalzero.app.data.model.TrustedContact
import com.signalzero.app.ui.theme.AlertAmber
import com.signalzero.app.ui.theme.EmergencyRed
import com.signalzero.app.ui.theme.MeshCyan
import com.signalzero.app.ui.theme.SuccessEmerald
import java.util.UUID

@Composable
fun ContactsScreen(
    contacts: List<TrustedContact>,
    language: AppLanguage,
    onAddContact: (TrustedContact) -> Unit,
    onDeleteContact: (TrustedContact) -> Unit,
    onSetPrimary: (String) -> Unit,
    onCallNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var isPrimaryInput by remember { mutableStateOf(false) }

    // Native Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            val cursor = context.contentResolver.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val name = if (nameIndex >= 0) it.getString(nameIndex) else "Contact"
                    val id = if (idIndex >= 0) it.getString(idIndex) else ""

                    // Query phone number
                    val phonesCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    var phone = ""
                    phonesCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            val pIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (pIndex >= 0) phone = pc.getString(pIndex)
                        }
                    }

                    if (phone.isNotBlank()) {
                        onAddContact(
                            TrustedContact(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                phoneNumber = phone,
                                isPrimary = contacts.isEmpty()
                            )
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.get("contacts_title", language),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MeshCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text = "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Native Picker Button
        OutlinedButton(
            onClick = { contactPickerLauncher.launch(null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Contacts, contentDescription = null, tint = MeshCyan, modifier = Modifier.size(16.dp))
            Text(
                text = Localization.get("pick_contact", language),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Services Direct Call Grid
        Text(
            text = "Official Emergency Numbers",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmergencyServiceInfo.defaultIndiaServices().take(2).forEach { s ->
                EmergencyServicePill(
                    name = if (language == AppLanguage.HINDI) s.nameHi else s.nameEn,
                    number = s.number,
                    onCall = { onCallNumber(s.number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmergencyServiceInfo.defaultIndiaServices().drop(2).forEach { s ->
                EmergencyServicePill(
                    name = if (language == AppLanguage.HINDI) s.nameHi else s.nameEn,
                    number = s.number,
                    onCall = { onCallNumber(s.number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Trusted Contacts List
        Text(
            text = "My SOS Recipients (${contacts.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No emergency contacts added yet.\nPick from your phone or add manually.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (contact.isPrimary) SuccessEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (contact.isPrimary) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.padding(start = 10.dp)) {
                                    Text(
                                        text = contact.name + if (contact.isPrimary) " (Primary)" else "",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = contact.phoneNumber,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                if (!contact.isPrimary) {
                                    IconButton(onClick = { onSetPrimary(contact.id) }) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(onClick = { onDeleteContact(contact) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Trusted Contact") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            onAddContact(
                                TrustedContact(
                                    id = UUID.randomUUID().toString(),
                                    name = nameInput.trim(),
                                    phoneNumber = phoneInput.trim(),
                                    isPrimary = contacts.isEmpty()
                                )
                            )
                            nameInput = ""
                            phoneInput = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmergencyServicePill(
    name: String,
    number: String,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(EmergencyRed.copy(alpha = 0.15f))
            .border(1.dp, EmergencyRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable { onCall() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(text = number, fontSize = 13.sp, fontWeight = FontWeight.Black, color = EmergencyRed)
            }
            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
        }
    }
}
