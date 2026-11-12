package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access

import android.os.SystemClock
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import kotlinx.coroutines.delay

private const val MockVerificationCode = "123456"
private const val CodeLength = 6
private const val ResendDelaySeconds = 42

@Composable
internal fun VerifyEmailScreen(email: String, onBack: () -> Unit, onVerified: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<Int?>(null) }
    var resent by rememberSaveable { mutableStateOf(false) }
    var resendAt by rememberSaveable { mutableLongStateOf(resendDeadline()) }
    var secondsLeft by remember { mutableIntStateOf(secondsUntil(resendAt)) }
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val action = verificationActionColor()

    LaunchedEffect(resendAt) {
        do {
            secondsLeft = secondsUntil(resendAt)
            if (secondsLeft > 0) delay(250)
        } while (secondsLeft > 0)
    }

    fun verify() {
        error = when {
            code.length < CodeLength -> R.string.verification_incomplete
            code != MockVerificationCode -> R.string.verification_invalid
            else -> null
        }
        if (error == null) {
            focusManager.clearFocus()
            onVerified()
        }
    }

    VerificationLayout(
        title = R.string.verification_title,
        description = R.string.verification_description,
        footer = R.drawable.verify_footer,
        onBack = onBack
    ) {
        Spacer(Modifier.height(20.dp))
        Surface(
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
            shape = RoundedCornerShape(50)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_access_email), contentDescription = null)
                Text(maskEmail(email), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(Modifier.height(24.dp))
        VerificationCodeField(
            code = code,
            onChange = {
                code = it
                error = null
                resent = false
            },
            onVerify = ::verify,
            action = action,
            hasError = error != null
        )
        Spacer(Modifier.height(8.dp))
        val message = when {
            error != null -> stringResource(requireNotNull(error))
            resent -> stringResource(R.string.verification_resent)
            else -> stringResource(R.string.verification_mock_code, MockVerificationCode)
        }
        Text(
            message,
            color = if (error != null) colors.error else colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )

        Spacer(Modifier.height(18.dp))
        AuthPrimaryButton(stringResource(R.string.verification_submit), ::verify, action)

        Spacer(Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.verification_resend_prompt), color = colors.onSurfaceVariant)
            TextButton(
                enabled = secondsLeft == 0,
                onClick = {
                    resendAt = resendDeadline()
                    secondsLeft = ResendDelaySeconds
                    code = ""
                    error = null
                    resent = true
                }
            ) {
                Text(
                    stringResource(R.string.verification_resend),
                    color = if (secondsLeft == 0) action else colors.onSurfaceVariant
                )
            }
        }
        if (secondsLeft > 0) {
            val time = "00:" + secondsLeft.toString().padStart(2, '0')
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, colors.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.ic_verification_clock),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.verification_countdown, time),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
internal fun VerifiedScreen(onLogin: () -> Unit) {
    VerificationLayout(
        title = R.string.verification_success_title,
        description = R.string.verification_success_description,
        footer = R.drawable.login_footer
    ) {
        Spacer(Modifier.height(20.dp))
        Image(
            painterResource(R.drawable.verify_success),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.74f).height(200.dp)
        )
        Spacer(Modifier.height(20.dp))
        AuthPrimaryButton(stringResource(R.string.verification_go_to_login), onLogin, verificationActionColor())
    }
}

@Composable
private fun VerificationLayout(
    @StringRes title: Int,
    @StringRes description: Int,
    @DrawableRes footer: Int,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val background = if (isSystemInDarkTheme()) {
        Brush.verticalGradient(listOf(colors.background, colors.surface))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFEEF2FD), Color(0xFFF7F8FD), Color(0xFFF0F4FE)))
    }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val scroll = rememberScrollState()
    AuthNavigationBar(useLightIcons = !keyboardVisible && !scroll.canScrollForward)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
    ) {
        val viewport = maxHeight
        val pageWidth = maxWidth.coerceAtMost(480.dp)
        val artHeight = if (keyboardVisible) 0.dp else minOf(pageWidth / 2f, viewport * 0.26f)
        val typeScale = (pageWidth / 420.dp).coerceIn(0.80f, 1.06f)
        Column(
            Modifier.fillMaxWidth().verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().heightIn(min = viewport - artHeight)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.fillMaxWidth()) {
                    TicketHNWordmark(
                        Modifier.align(Alignment.Center).padding(top = if (onBack == null) 10.dp else 20.dp)
                    )
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                            Icon(
                                painterResource(R.drawable.ic_verification_back),
                                contentDescription = stringResource(R.string.verification_back)
                            )
                        }
                    }
                }
                ScreenHeaderDivider(Modifier.padding(top = 8.dp, bottom = 12.dp))
                Text(
                    stringResource(title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 34.sp * typeScale,
                        lineHeight = 38.sp * typeScale
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                content()
            }
            if (!keyboardVisible) {
                Image(
                    painterResource(footer),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(artHeight),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

@Composable
private fun VerificationCodeField(
    code: String,
    onChange: (String) -> Unit,
    onVerify: () -> Unit,
    action: Color,
    hasError: Boolean
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val label = stringResource(R.string.verification_code_label)

    BasicTextField(
        value = code,
        onValueChange = { input -> onChange(input.filter { it in '0'..'9' }.take(CodeLength)) },
        modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = label },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineLarge.copy(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onVerify() }),
        decorationBox = { innerTextField ->
            Box {
                Row(
                    Modifier.fillMaxWidth().clearAndSetSemantics {},
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(CodeLength) { index ->
                        val active = focused && index == code.length.coerceAtMost(CodeLength - 1)
                        val border = when {
                            hasError -> colors.error
                            active -> action
                            else -> colors.outline
                        }
                        Box(
                            Modifier.weight(1f).heightIn(min = 56.dp)
                                .border(if (active) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                code.getOrNull(index)?.toString().orEmpty(),
                                color = colors.onBackground,
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp)
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterStart) { innerTextField() }
            }
        }
    )
}

@Composable
private fun verificationActionColor(): Color =
    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color(0xFF0752EC)

private fun maskEmail(email: String): String {
    val local = email.substringBefore('@')
    val visible = local.take((local.length - 1).coerceIn(0, 4))
    return visible + "****@" + email.substringAfter('@')
}

private fun resendDeadline(): Long = SystemClock.elapsedRealtime() + ResendDelaySeconds * 1_000L

private fun secondsUntil(deadline: Long): Int =
    ((deadline - SystemClock.elapsedRealtime() + 999) / 1_000L).toInt().coerceIn(0, ResendDelaySeconds)
