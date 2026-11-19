package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AccessState
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApi
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthSession
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEventsApi
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEvents
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.EventDraft
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.LocalityInput
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEventsApi
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home.HomeScreen
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.onboarding.OnboardingScreen
import java.io.IOException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private enum class AccessRoute { Onboarding, Welcome, Login, Register, VerifyEmail, Verified }

@Composable
internal fun AccessFlow(
    accessStatus: Result<AccessState>?,
    onRetry: () -> Unit,
    onCompleteOnboarding: suspend () -> Unit,
    onSignIn: suspend (String, String) -> AuthSession,
    onRefreshSession: suspend (AuthSession) -> AuthSession,
    onSaveRoles: suspend (AuthSession, Set<String>) -> Unit,
    onSignOut: suspend (AuthSession, (AuthSession) -> Unit) -> Unit,
    onReadProfile: suspend (String) -> ProfileDetails,
    onSaveProfile: suspend (String, ProfileDetails) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        if (accessStatus == null) return@Surface
        val access = accessStatus.getOrNull()
        if (access == null) {
            Column(
                modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.onboarding_read_error))
                Button(onClick = onRetry) { Text(stringResource(R.string.onboarding_retry)) }
            }
            return@Surface
        }

        val completed = access.onboardingCompleted
        var session by remember(access) { mutableStateOf(access.session) }
        val sessionLock = remember { Mutex() }
        var route by rememberSaveable {
            mutableStateOf(if (completed) AccessRoute.Login else AccessRoute.Onboarding)
        }
        val currentRoute = if (completed && route == AccessRoute.Onboarding) AccessRoute.Login else route
        val scope = rememberCoroutineScope()
        var canReturnToWelcome by rememberSaveable { mutableStateOf(false) }
        var verificationEmail by rememberSaveable { mutableStateOf("") }
        var canResendImmediately by rememberSaveable { mutableStateOf(false) }
        var saving by remember { mutableStateOf(false) }
        var saveFailed by remember { mutableStateOf(false) }
        var requestError by remember { mutableStateOf<Int?>(null) }
        var duplicateEmail by remember { mutableStateOf<String?>(null) }
        var resendCount by rememberSaveable { mutableStateOf(0) }

        fun openAccess(target: AccessRoute) {
            if (saving) return
            saving = true
            saveFailed = false
            scope.launch {
                try {
                    onCompleteOnboarding()
                    canReturnToWelcome = true
                    route = target
                } catch (_: IOException) {
                    saveFailed = true
                } finally {
                    saving = false
                }
            }
        }

        fun goToLogin() {
            if (saving) return
            verificationEmail = ""
            requestError = null
            duplicateEmail = null
            route = AccessRoute.Login
        }

        fun request(onFailure: (AuthApiException) -> Unit = {}, action: suspend () -> Unit) {
            if (saving) return
            saving = true
            requestError = null
            duplicateEmail = null
            scope.launch {
                try {
                    action()
                } catch (error: AuthApiException) {
                    requestError = error.messageResource()
                    onFailure(error)
                } catch (_: IOException) {
                    requestError = R.string.access_connection_error
                } finally {
                    saving = false
                }
            }
        }

        fun openVerification(email: String, allowResend: Boolean) {
            verificationEmail = email
            canResendImmediately = allowResend
            resendCount = 0
            requestError = null
            duplicateEmail = null
            route = AccessRoute.VerifyEmail
        }

        fun returnToRegister() {
            if (saving) return
            requestError = null
            route = AccessRoute.Register
        }

        val signedIn = session
        if (signedIn != null) {
            val signedInEmail = signedIn.email
            suspend fun <T> authorized(action: suspend (String) -> T): T = sessionLock.withLock {
                val current = session ?: throw AuthApiException(401, null)
                try {
                    action(current.accessToken)
                } catch (error: AuthApiException) {
                    if (error.status != 401) throw error
                    val refreshed = withContext(NonCancellable) {
                        onRefreshSession(current).also { session = it }
                    }
                    action(refreshed.accessToken)
                }
            }
            HomeScreen(
                email = signedInEmail,
                accountName = signedIn.fullName,
                roles = signedIn.roles,
                onSignOut = {
                    request {
                        sessionLock.withLock {
                            session?.let { current -> onSignOut(current) { session = it } }
                            session = null
                            route = AccessRoute.Login
                        }
                    }
                },
                onRefreshRoles = {
                    authorized { token ->
                        val roles = AuthApi.readRoles(token)
                        val current = session
                        if (current != null && current.roles != roles) {
                            session = current.copy(roles = roles)
                            onSaveRoles(current, roles)
                        }
                    }
                },
                organizerEvents = object : OrganizerEvents {
                    override suspend fun list(offset: Int) = authorized { OrganizerEventsApi.list(it, offset) }
                    override suspend fun create(draft: EventDraft, cover: ByteArray?) =
                        authorized { OrganizerEventsApi.create(it, draft, cover) }
                    override suspend fun detail(id: String) = authorized { OrganizerEventsApi.detail(it, id) }
                    override suspend fun update(id: String, draft: EventDraft) =
                        authorized { OrganizerEventsApi.update(it, id, draft) }
                    override suspend fun replaceCover(id: String, cover: ByteArray) =
                        authorized { OrganizerEventsApi.replaceCover(it, id, cover) }
                    override suspend fun saveLocality(id: String, localityId: String?, input: LocalityInput) =
                        authorized { OrganizerEventsApi.saveLocality(it, id, localityId, input) }
                    override suspend fun publish(id: String) = authorized { OrganizerEventsApi.publish(it, id) }
                },
                onBuyTickets = { eventId, items -> authorized { PublicEventsApi.buy(it, eventId, items) } },
                onReadTickets = { authorized { PublicEventsApi.tickets(it) } },
                onReadTicket = { id -> authorized { PublicEventsApi.ticket(it, id) } },
                onLookupTicket = { qrPayload -> authorized { PublicEventsApi.lookupTicket(it, qrPayload) } },
                onCheckIn = { qrPayload -> authorized { PublicEventsApi.checkIn(it, qrPayload) } },
                signingOut = saving,
                signOutFailed = requestError != null,
                onReadProfile = { onReadProfile(signedInEmail) },
                onSaveProfile = { onSaveProfile(signedInEmail, it) }
            )
            return@Surface
        }

        BackHandler(enabled = saving) {}
        BackHandler(enabled = !saving && (currentRoute == AccessRoute.Register || currentRoute == AccessRoute.Verified)) { goToLogin() }
        BackHandler(enabled = !saving && currentRoute == AccessRoute.VerifyEmail) { returnToRegister() }
        BackHandler(enabled = !saving && currentRoute == AccessRoute.Login && canReturnToWelcome) {
            requestError = null
            route = AccessRoute.Welcome
        }
        when (currentRoute) {
            AccessRoute.Onboarding, AccessRoute.Welcome -> OnboardingScreen(
                onRegister = { openAccess(AccessRoute.Register) },
                onLogin = { openAccess(AccessRoute.Login) },
                saving = saving,
                saveFailed = saveFailed,
                startAtWelcome = currentRoute == AccessRoute.Welcome
            )
            AccessRoute.Login, AccessRoute.Register -> key(currentRoute) {
                val registering = currentRoute == AccessRoute.Register
                AuthScreen(
                    registering = registering,
                    onSwitch = if (registering) ::goToLogin else ::returnToRegister,
                    onSubmit = { name, email, password ->
                        if (registering) {
                            request(onFailure = { error ->
                                if (error.code == "EMAIL_ALREADY_REGISTERED") duplicateEmail = email
                            }) {
                                AuthApi.register(name, email, password)
                                openVerification(email, allowResend = false)
                            }
                        } else {
                            request {
                                session = onSignIn(email, password)
                                canReturnToWelcome = false
                            }
                        }
                    },
                    pending = saving,
                    requestError = requestError,
                    duplicateEmail = duplicateEmail,
                    onVerifyExistingEmail = { openVerification(it, allowResend = true) }
                )
            }
            AccessRoute.VerifyEmail -> VerifyEmailScreen(
                email = verificationEmail,
                onBack = ::returnToRegister,
                onVerify = { code ->
                    request {
                        AuthApi.verifyEmail(verificationEmail, code)
                        route = AccessRoute.Verified
                    }
                },
                onResend = {
                    request {
                        AuthApi.resendVerification(verificationEmail)
                        resendCount++
                    }
                },
                pending = saving,
                requestError = requestError,
                onClearError = { requestError = null },
                canResendImmediately = canResendImmediately,
                resendCount = resendCount
            )
            AccessRoute.Verified -> VerifiedScreen(onLogin = ::goToLogin)
        }
    }
}

@StringRes
private fun AuthApiException.messageResource(): Int = when (code) {
    "INVALID_CREDENTIALS" -> R.string.access_invalid_credentials
    "EMAIL_ALREADY_REGISTERED" -> R.string.access_email_registered
    "INVALID_CODE" -> R.string.verification_invalid
    "INVALID_INPUT" -> R.string.access_invalid_input
    else -> if (status == 429) R.string.access_too_many_requests else R.string.access_service_error
}
