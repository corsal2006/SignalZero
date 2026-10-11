package com.signalzero.app.data.repository

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val tag = "SignalZero_Auth"

    val currentUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Exception) { null }

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        try {
            auth.addAuthStateListener(listener)
        } catch (e: Exception) {
            Log.w(tag, "FirebaseAuth listener failed: ${e.message}")
            trySend(null)
        }
        awaitClose {
            try {
                auth.removeAuthStateListener(listener)
            } catch (_: Exception) {}
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Sign in failed. Please verify your credentials.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Sign in exception: ${e.message}")
            AuthResult.Error(friendlyErrorMessage(e))
        }
    }

    suspend fun createAccount(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            if (user != null) {
                if (displayName.isNotBlank()) {
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName.trim()
                    }
                    user.updateProfile(profileUpdates).await()
                }
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Account creation failed. Please try again.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Account creation exception: ${e.message}")
            AuthResult.Error(friendlyErrorMessage(e))
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Google Sign-In failed.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In exception: ${e.message}")
            AuthResult.Error(friendlyErrorMessage(e))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Password reset exception: ${e.message}")
            Result.failure(Exception(friendlyErrorMessage(e)))
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            Log.i(tag, "User successfully signed out from Firebase Auth.")
        } catch (e: Exception) {
            Log.e(tag, "Sign out exception: ${e.message}")
        }
    }

    private fun friendlyErrorMessage(e: Exception): String {
        val msg = e.message ?: return "An unexpected network or authentication error occurred."
        return when {
            msg.contains("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", ignoreCase = true) ||
            msg.contains("account-exists-with-different-credential", ignoreCase = true) ->
                "An account already exists with this email using another sign-in method. Please sign in with email/password."
            msg.contains("The email address is already in use", ignoreCase = true) ->
                "An account already exists with this email address."
            msg.contains("The email address is badly formatted", ignoreCase = true) ->
                "Please enter a valid email address."
            msg.contains("There is no user record", ignoreCase = true) || msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Invalid email or password. Please check your credentials."
            msg.contains("A network error", ignoreCase = true) || msg.contains("NETWORK_ERROR", ignoreCase = true) ->
                "Network unavailable. Offline SOS remains fully operational without signing in."
            msg.contains("Password should be at least", ignoreCase = true) ->
                "Password must be at least 6 characters long."
            msg.contains("12500") ->
                "Google Sign-In configuration error (12500). Please ensure Google Sign-In is enabled in Firebase Console."
            msg.contains("10") ->
                "Google Sign-In developer error (10). Verify that the debug SHA-1 fingerprint is added in Firebase Console."
            else -> msg
        }
    }
}
