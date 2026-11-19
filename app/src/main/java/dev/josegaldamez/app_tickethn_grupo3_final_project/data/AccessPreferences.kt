package dev.josegaldamez.app_tickethn_grupo3_final_project.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Locale

private val Context.ticketPreferences by preferencesDataStore(name = "tickethn_preferences")
private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
private val sessionEmail = stringPreferencesKey("session_email")
private val sessionFullName = stringPreferencesKey("session_full_name")
private val sessionOrganizer = booleanPreferencesKey("session_is_organizer")
private val sessionRoles = stringSetPreferencesKey("session_roles")
private val accessToken = stringPreferencesKey("access_token")
private val refreshToken = stringPreferencesKey("refresh_token")

internal data class AccessState(val onboardingCompleted: Boolean, val session: AuthSession?)

internal data class ProfileDetails(
    val displayName: String? = null,
    val cityCode: String? = null,
    val notifications: Boolean = true,
    val organizerRequested: Boolean = false
)

internal enum class DemoPaymentCard(val brand: String, val lastFour: String) {
    Visa("Visa", "4242"),
    Mastercard("Mastercard", "4444"),
    Amex("American Express", "0005")
}

internal class AccessPreferences(context: Context) {
    private val dataStore = context.applicationContext.ticketPreferences
    private val sessionStore = sessionStore(context.applicationContext)

    suspend fun readState(): AccessState {
        val preferences = dataStore.data.first()
        val storedSession = sessionStore.data.first()
        val email = storedSession[sessionEmail]
        val access = storedSession[accessToken]
        val refresh = storedSession[refreshToken]
        val session = if (email != null && access != null && refresh != null) {
            val roles = storedSession[sessionRoles] ?: storedSession[sessionOrganizer]?.let { isOrganizer ->
                if (isOrganizer) setOf(ORGANIZER_ROLE) else emptySet()
            }
            AuthSession(email, storedSession[sessionFullName], access, refresh, roles)
        } else {
            null
        }
        return AccessState(preferences[onboardingCompleted] == true, session)
    }

    suspend fun markCompleted() {
        dataStore.edit { it[onboardingCompleted] = true }
    }

    suspend fun signIn(session: AuthSession) {
        sessionStore.edit {
            it[sessionEmail] = session.email
            if (session.fullName != null) {
                it[sessionFullName] = session.fullName
            } else {
                it.remove(sessionFullName)
            }
            it[accessToken] = session.accessToken
            it[refreshToken] = session.refreshToken
            session.roles?.let { roles -> it[sessionRoles] = roles } ?: it.remove(sessionRoles)
            it.remove(sessionOrganizer)
        }
    }

    suspend fun saveRoles(session: AuthSession, roles: Set<String>) {
        sessionStore.edit {
            if (it[accessToken] == session.accessToken) {
                it[sessionRoles] = roles
                it.remove(sessionOrganizer)
            }
        }
    }

    suspend fun signOut() {
        sessionStore.edit { it.clear() }
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

    suspend fun readPaymentCards(email: String): Set<String> =
        dataStore.data.first()[ProfileKeys(email).paymentCards].orEmpty()

    suspend fun setPaymentCardSaved(email: String, card: DemoPaymentCard, saved: Boolean): Set<String> {
        val key = ProfileKeys(email).paymentCards
        return dataStore.edit { preferences ->
            val current = preferences[key].orEmpty()
            preferences[key] = if (saved) current + card.name else current - card.name
        }[key].orEmpty()
    }

    private class ProfileKeys(email: String) {
        private val prefix = "profile:" + email.lowercase(Locale.ROOT)
        val name = stringPreferencesKey("$prefix:name")
        val city = stringPreferencesKey("$prefix:city")
        val notifications = booleanPreferencesKey("$prefix:notifications")
        val organizerRequested = booleanPreferencesKey("$prefix:organizer_requested")
        val paymentCards = stringSetPreferencesKey("$prefix:demo_payment_cards")
    }

    private companion object {
        private var store: DataStore<Preferences>? = null

        @Synchronized
        fun sessionStore(context: Context): DataStore<Preferences> =
            store ?: PreferenceDataStoreFactory.create {
                context.noBackupFilesDir.resolve("auth_session.preferences_pb")
            }.also { store = it }
    }
}
