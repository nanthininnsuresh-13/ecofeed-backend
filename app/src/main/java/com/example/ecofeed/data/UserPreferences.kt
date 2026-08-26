package com.example.ecofeed.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFS_NAME = "ecofeed_user_prefs"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_JWT = stringPreferencesKey("key_jwt")
        val KEY_USER_ID = stringPreferencesKey("key_user_id")
        val KEY_ROLE = stringPreferencesKey("key_role")
        val KEY_DARK_MODE = booleanPreferencesKey("key_dark_mode")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("key_notifications_enabled")
        val KEY_PHONE_NUMBER = stringPreferencesKey("key_phone_number")
        val KEY_ORG_NAME = stringPreferencesKey("key_org_name")
        val KEY_ADDRESS = stringPreferencesKey("key_address")
        val KEY_PROFILE_IMAGE = stringPreferencesKey("key_profile_image")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_JWT] }
    val userIdFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_USER_ID] }
    val roleFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_ROLE] }
    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[KEY_DARK_MODE] ?: false }
    val notificationsFlow: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[KEY_NOTIFICATIONS_ENABLED] ?: true }
    val profileImageFlow: Flow<String?> = context.dataStore.data.map { it[KEY_PROFILE_IMAGE] }
    val phoneFlow: Flow<String?> = context.dataStore.data.map { it[KEY_PHONE_NUMBER] }
    val orgNameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ORG_NAME] }
    val addressFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ADDRESS] }

    suspend fun saveSession(token: String, userId: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_JWT] = token
            prefs[KEY_USER_ID] = userId
            prefs[KEY_ROLE] = role
        }
    }

    suspend fun updateProfile(
        image: String? = null,
        phone: String? = null,
        org: String? = null,
        address: String? = null
    ) {
        context.dataStore.edit { prefs ->
            image?.let { prefs[KEY_PROFILE_IMAGE] = it }
            phone?.let { prefs[KEY_PHONE_NUMBER] = it }
            org?.let { prefs[KEY_ORG_NAME] = it }
            address?.let { prefs[KEY_ADDRESS] = it }
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
