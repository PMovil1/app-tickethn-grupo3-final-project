package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import dev.josegaldamez.app_tickethn_grupo3_final_project.R

@Composable
internal fun AuthLayout(
    registering: Boolean,
    onSwitch: () -> Unit,
    onSubmit: () -> Unit,
    fields: @Composable () -> Unit,
    status: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val colors = MaterialTheme.colorScheme
    val submitLabel = stringResource(if (registering) R.string.access_register else R.string.access_login)
    val action = if (dark) colors.primary else Color(0xFF0752EC)
    val background = if (dark) {
        Brush.verticalGradient(listOf(colors.background, colors.surface))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFEEF2FD), Color(0xFFF7F8FD), Color(0xFFF0F4FE)))
    }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val scrollState = rememberScrollState()
    var showRecovery by rememberSaveable { mutableStateOf(false) }
    AuthNavigationBar(useLightIcons = !keyboardVisible && !scrollState.canScrollForward)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val availableHeight = maxHeight
        val pageWidth = maxWidth.coerceAtMost(480.dp)
        val horizontalPadding = (pageWidth * 0.06f).coerceIn(20.dp, 28.dp)
        val scale = (pageWidth / 420.dp).coerceIn(0.80f, 1.06f)
        val artFraction = if (registering) 0.28f else 0.32f
        val artHeight = if (keyboardVisible) 0.dp else minOf(pageWidth / 1.8f, availableHeight * artFraction)

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    .heightIn(min = availableHeight - artHeight)
                    .padding(horizontal = horizontalPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TicketHNWordmark()
                ScreenHeaderDivider(Modifier.padding(top = 8.dp, bottom = 12.dp))
                Text(
                    text = if (registering) submitLabel else stringResource(R.string.access_login_heading),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 34.sp * scale,
                        lineHeight = 38.sp * scale
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (registering) R.string.access_register_description else R.string.access_login_description
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp * scale,
                        lineHeight = 22.sp * scale
                    ),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(26.dp))

                fields()
                if (!registering) {
                    TextButton(
                        onClick = { showRecovery = true },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.access_forgot_password), color = action)
                    }
                }
                status()
                Spacer(Modifier.height(if (registering) 18.dp else 9.dp))
                AuthPrimaryButton(submitLabel, onSubmit, action)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(if (registering) R.string.access_has_account else R.string.access_no_account),
                        color = colors.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onSwitch,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(if (registering) R.string.access_login else R.string.access_register),
                            color = action
                        )
                    }
                }
            }

            if (!keyboardVisible) {
                Image(
                    painter = painterResource(R.drawable.login_footer),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(artHeight),
                    alignment = Alignment.BottomCenter,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }

    if (showRecovery) {
        AlertDialog(
            onDismissRequest = { showRecovery = false },
            title = { Text(stringResource(R.string.access_forgot_password)) },
            text = { Text(stringResource(R.string.access_recovery_unavailable)) },
            confirmButton = {
                TextButton(onClick = { showRecovery = false }) {
                    Text(stringResource(R.string.access_close))
                }
            }
        )
    }
}

@Composable
internal fun AuthNavigationBar(useLightIcons: Boolean) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    DisposableEffect(window, useLightIcons) {
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val wasLight = controller?.isAppearanceLightNavigationBars
        if (useLightIcons) controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (useLightIcons && wasLight != null) controller?.isAppearanceLightNavigationBars = wasLight
        }
    }
}
