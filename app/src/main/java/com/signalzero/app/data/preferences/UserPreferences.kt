package com.signalzero.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.signalzero.app.core.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "signalzero_prefs")

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

class UserPreferences(private val context: Context) {

    private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_SHAKE_ENABLED = booleanPreferencesKey("shake_sos_enabled")
    private val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_sos_enabled")
    private val KEY_NODE_PSEUDONYM = stringPreferencesKey("node_pseudonym")

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        val code = prefs[KEY_LANGUAGE] ?: AppLanguage.ENGLISH.code
        if (code == AppLanguage.HINDI.code) AppLanguage.HINDI else AppLanguage.ENGLISH
    }

    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_THEME] ?: ThemeMode.DARK.name
        try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    val shakeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHAKE_ENABLED] ?: true
    }

    val voiceEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_ENABLED] ?: false
    }

    val nodePseudonymFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_NODE_PSEUDONYM] ?: "Node-${UUID.randomUUID().toString().take(4).uppercase()}"
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
    }

    suspend fun setTheme(theme: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun setShakeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHAKE_ENABLED] = enabled
        }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_ENABLED] = enabled
        }
    }

    suspend fun setNodePseudonym(pseudonym: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NODE_PSEUDONYM] = pseudonym
        }
    }
}
