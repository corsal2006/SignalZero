package com.signalzero.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.signalzero.app.R
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.repository.AuthRepository
import com.signalzero.app.data.repository.AuthResult
import com.signalzero.app.ui.components.AuthDivider
import com.signalzero.app.ui.components.GoogleSignInButton
import com.signalzero.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    language: AppLanguage,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit,
    onContinueOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordSuccess by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Configure Google Sign-In Client
    val webClientId = remember(context) {
        try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            "127422800316-1da5l66t2o80nhuq8ojg1sfrm1u0gllo.apps.googleusercontent.com"
        }
    }

    val googleSignInClient = remember(context, webClientId) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    isGoogleLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = authRepository.signInWithCredential(credential)
                        isGoogleLoading = false
                        if (authResult is AuthResult.Success) {
                            onLoginSuccess()
                        } else if (authResult is AuthResult.Error) {
                            errorMessage = authResult.message
                        }
                    }
                } else {
                    isGoogleLoading = false
                    errorMessage = "Google authentication returned an empty ID token. Please verify OAuth client configuration."
                }
            } catch (e: ApiException) {
                isGoogleLoading = false
                errorMessage = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign-in cancelled."
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error during Google Sign-In. You can continue in Offline Mode."
                    12500 -> "Configuration error (12500): Check if Google Provider is enabled in Firebase Console and debug SHA-1 is added."
                    10 -> "Developer configuration error (10): Verify that debug SHA-1 (82:A4:CD:1A:26:D5:B5:35:BE:CC:F3:8A:15:7C:2F:E0:F9:40:9D:FB) is registered in Firebase Console."
                    else -> "Google Sign-In error (${e.statusCode}): ${e.localizedMessage ?: "Unknown error"}"
                }
            }
        } else {
            isGoogleLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_signalzero_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (language == AppLanguage.HINDI) "खाते में साइन इन करें" else "Sign In to SignalZero",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (language == AppLanguage.HINDI)
                    "क्लाउड सिंक और संपर्क बैकअप के लिए लॉग इन करें"
                else
                    "Optional cloud sync for emergency history and contacts",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Display with Retry / Dismiss
            if (errorMessage != null) {
                Surface(
                    color = EmergencyRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = EmergencyRed,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                            lineHeight = 17.sp
                        )
                        IconButton(
                            onClick = { errorMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = EmergencyRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text(if (language == AppLanguage.HINDI) "ईमेल पता" else "Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MeshCyan) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeshCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text(if (language == AppLanguage.HINDI) "पासवर्ड" else "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeshCyan) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MutedSlate
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (email.isNotBlank() && password.isNotBlank() && !isLoading && !isGoogleLoading) {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = authRepository.signInWithEmail(email, password)
                            isLoading = false
                            if (res is AuthResult.Success) {
                                onLoginSuccess()
                            } else if (res is AuthResult.Error) {
                                errorMessage = res.message
                            }
                        }
                    }
                }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeshCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            // Forgot password link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "पासवर्ड भूल गए?" else "Forgot password?",
                        color = MeshCyan,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sign In Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val res = authRepository.signInWithEmail(email, password)
                        isLoading = false
                        if (res is AuthResult.Success) {
                            onLoginSuccess()
                        } else if (res is AuthResult.Error) {
                            errorMessage = res.message
                        }
                    }
                },
                enabled = !isLoading && !isGoogleLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeshCyan,
                    contentColor = ObsidianBg
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = ObsidianBg, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (language == AppLanguage.HINDI) "साइन इन करें" else "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Divider: Subtle "OR"
            AuthDivider(language = language)

            // Mandatory: Official Google Sign-In Button
            GoogleSignInButton(
                isLoading = isGoogleLoading,
                onClick = {
                    if (!isGoogleLoading && !isLoading) {
                        isGoogleLoading = true
                        errorMessage = null
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    }
                },
                language = language
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Create account link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "खाता नहीं है? " else "Don't have an account? ",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "पंजीकरण करें" else "Create Account",
                        color = MeshCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Offline Mode Guarantee
            TextButton(onClick = onContinueOffline) {
                Text(
                    text = if (language == AppLanguage.HINDI)
                        "ऑफ़लाइन मोड (कोई लॉगिन आवश्यक नहीं) →"
                    else
                        "Offline Mode (No account required for SOS) →",
                    color = MutedSlate,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false; forgotPasswordSuccess = null },
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "पासवर्ड रीसेट करें" else "Reset Password",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "अपना पंजीकृत ईमेल दर्ज करें। हम रीसेट लिंक भेजेंगे।"
                        else
                            "Enter your registered email address to receive password reset instructions.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (forgotPasswordSuccess != null) {
                        Text(text = forgotPasswordSuccess ?: "", color = SuccessEmerald, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            coroutineScope.launch {
                                val res = authRepository.sendPasswordReset(forgotPasswordEmail)
                                if (res.isSuccess) {
                                    forgotPasswordSuccess = "Reset link sent! Please check your email."
                                } else {
                                    forgotPasswordSuccess = res.exceptionOrNull()?.message ?: "Failed to send reset link."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeshCyan, contentColor = ObsidianBg)
                ) {
                    Text(if (language == AppLanguage.HINDI) "भेजें" else "Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false; forgotPasswordSuccess = null }) {
                    Text(if (language == AppLanguage.HINDI) "बंद करें" else "Close")
                }
            }
        )
    }
}
