package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Locale

private val Context.ticketPreferences by preferencesDataStore(name = "tickethn_preferences")
private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
private val sessionEmail = stringPreferencesKey("mock_session_email")

internal data class AccessState(val onboardingCompleted: Boolean, val email: String?)

internal data class ProfileDetails(
    val displayName: String? = null,
    val cityCode: String? = null,
    val notifications: Boolean = true,
    val organizerRequested: Boolean = false
)

internal class AccessPreferences(context: Context) {
    private val dataStore = context.applicationContext.ticketPreferences

    suspend fun readState(): AccessState {
        val preferences = dataStore.data.first()
        return AccessState(preferences[onboardingCompleted] == true, preferences[sessionEmail])
    }

    suspend fun markCompleted() {
        dataStore.edit { it[onboardingCompleted] = true }
    }

    suspend fun signIn(email: String) {
        dataStore.edit {
            it[onboardingCompleted] = true
            it[sessionEmail] = email
        }
    }

    suspend fun signOut() {
        dataStore.edit { it.remove(sessionEmail) }
    }

    suspend fun readProfile(email: String): ProfileDetails {
        val keys = ProfileKeys(email)
        val preferences = dataStore.data.first()
        return ProfileDetails(
            displayName = preferences[keys.name],
            cityCode = preferences[keys.city],
            notifications = preferences[keys.notifications] ?: true,
            organizerRequested = preferences[keys.organizerRequested] ?: false
        )
    }

    suspend fun saveProfile(email: String, profile: ProfileDetails) {
        val keys = ProfileKeys(email)
        dataStore.edit { preferences ->
            profile.displayName?.let { preferences[keys.name] = it } ?: preferences.remove(keys.name)
            profile.cityCode?.let { preferences[keys.city] = it } ?: preferences.remove(keys.city)
            preferences[keys.notifications] = profile.notifications
            preferences[keys.organizerRequested] = profile.organizerRequested
        }
    }

    private class ProfileKeys(email: String) {
        private val prefix = "profile:" + email.lowercase(Locale.ROOT)
        val name = stringPreferencesKey("$prefix:name")
        val city = stringPreferencesKey("$prefix:city")
        val notifications = booleanPreferencesKey("$prefix:notifications")
        val organizerRequested = booleanPreferencesKey("$prefix:organizer_requested")
    }
}
