package com.signalzero.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    authRepository: AuthRepository,
    language: AppLanguage,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onContinueOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                            onRegisterSuccess()
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
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (language == AppLanguage.HINDI) "नया खाता बनाएं" else "Create SignalZero Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (language == AppLanguage.HINDI)
                    "मेश नेटवर्क और क्लाउड सिंक में शामिल हों"
                else
                    "Join the resilient emergency mesh network",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(20.dp))

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

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = null },
                label = { Text(if (language == AppLanguage.HINDI) "पूरा नाम" else "Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MeshCyan) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeshCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text(if (language == AppLanguage.HINDI) "पासवर्ड (न्यूनतम 6 वर्ण)" else "Password (min 6 characters)") },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeshCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text(if (language == AppLanguage.HINDI) "पासवर्ड की पुष्टि करें" else "Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeshCyan) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeshCyan,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Register Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                        errorMessage = "Please fill in all required fields."
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters long."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val res = authRepository.createAccount(email, password, fullName)
                        isLoading = false
                        if (res is AuthResult.Success) {
                            onRegisterSuccess()
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
                        text = if (language == AppLanguage.HINDI) "खाता बनाएं" else "Create Account",
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

            // Already have account
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "पहले से खाता है? " else "Already have an account? ",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "साइन इन करें" else "Sign In",
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
}
