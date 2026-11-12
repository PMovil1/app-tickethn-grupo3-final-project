package dev.josegaldamez.app_tickethn_grupo3_final_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AccessPreferences
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AccessState
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AccessFlow
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.theme.Apptickethngrupo3finalprojectTheme
import java.io.IOException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val preferences by lazy { AccessPreferences(applicationContext) }
    private var accessStatus by mutableStateOf<Result<AccessState>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splash.setKeepOnScreenCondition { accessStatus == null }
        loadAccessStatus()

        setContent {
            Apptickethngrupo3finalprojectTheme {
                AccessFlow(
                    accessStatus = accessStatus,
                    onRetry = ::loadAccessStatus,
                    onCompleteOnboarding = preferences::markCompleted,
                    onSignIn = preferences::signIn,
                    onSignOut = preferences::signOut,
                    onReadProfile = preferences::readProfile,
                    onSaveProfile = preferences::saveProfile
                )
            }
        }
    }

    private fun loadAccessStatus() {
        accessStatus = null
        lifecycleScope.launch {
            accessStatus = try {
                Result.success(preferences.readState())
            } catch (error: IOException) {
                Result.failure(error)
            }
        }
    }
}
