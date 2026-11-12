package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access

import androidx.activity.compose.BackHandler
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
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home.HomeScreen
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.onboarding.OnboardingScreen
import java.io.IOException
import kotlinx.coroutines.launch

private enum class AccessRoute { Onboarding, Welcome, Login, Register, VerifyEmail, Verified }

@Composable
internal fun AccessFlow(
    accessStatus: Result<AccessState>?,
    onRetry: () -> Unit,
    onCompleteOnboarding: suspend () -> Unit,
    onSignIn: suspend (String) -> Unit,
    onSignOut: suspend () -> Unit,
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
        var sessionEmail by remember(access) { mutableStateOf(access.email) }
        var route by rememberSaveable {
            mutableStateOf(if (completed) AccessRoute.Login else AccessRoute.Onboarding)
        }
        val currentRoute = if (completed && route == AccessRoute.Onboarding) AccessRoute.Login else route
        val scope = rememberCoroutineScope()
        var canReturnToWelcome by rememberSaveable { mutableStateOf(false) }
        var verificationEmail by rememberSaveable { mutableStateOf("") }
        var saving by remember { mutableStateOf(false) }
        var saveFailed by remember { mutableStateOf(false) }
        var sessionError by remember { mutableStateOf(false) }

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
            verificationEmail = ""
            sessionError = false
            route = AccessRoute.Login
        }

        fun changeSession(email: String?) {
            if (saving) return
            saving = true
            sessionError = false
            scope.launch {
                try {
                    if (email == null) onSignOut() else onSignIn(email)
                    canReturnToWelcome = false
                    goToLogin()
                    sessionEmail = email
                } catch (_: IOException) {
                    sessionError = true
                } finally {
                    saving = false
                }
            }
        }

        val signedInEmail = sessionEmail
        if (signedInEmail != null) {
            HomeScreen(
                email = signedInEmail,
                onSignOut = { changeSession(null) },
                signingOut = saving,
                signOutFailed = sessionError,
                onReadProfile = { onReadProfile(signedInEmail) },
                onSaveProfile = { onSaveProfile(signedInEmail, it) }
            )
            return@Surface
        }

        BackHandler(enabled = currentRoute == AccessRoute.Register || currentRoute == AccessRoute.Verified) { goToLogin() }
        BackHandler(enabled = currentRoute == AccessRoute.VerifyEmail) { route = AccessRoute.Register }
        BackHandler(enabled = currentRoute == AccessRoute.Login && canReturnToWelcome) { route = AccessRoute.Welcome }
        when (currentRoute) {
            AccessRoute.Onboarding, AccessRoute.Welcome -> OnboardingScreen(
                onRegister = { openAccess(AccessRoute.Register) },
                onLogin = { openAccess(AccessRoute.Login) },
                saving = saving,
                saveFailed = saveFailed,
                startAtWelcome = currentRoute == AccessRoute.Welcome
            )
            AccessRoute.Login -> AuthScreen(
                registering = false,
                onSwitch = {
                    if (!saving) {
                        sessionError = false
                        route = AccessRoute.Register
                    }
                },
                onLoginValidated = { changeSession(it) },
                loginFailed = sessionError,
                loginPending = saving
            )
            AccessRoute.Register -> AuthScreen(
                registering = true,
                onSwitch = ::goToLogin,
                onRegistrationValidated = { email ->
                    verificationEmail = email
                    route = AccessRoute.VerifyEmail
                }
            )
            AccessRoute.VerifyEmail -> VerifyEmailScreen(
                email = verificationEmail,
                onBack = { route = AccessRoute.Register },
                onVerified = { route = AccessRoute.Verified }
            )
            AccessRoute.Verified -> VerifiedScreen(onLogin = ::goToLogin)
        }
    }
}
