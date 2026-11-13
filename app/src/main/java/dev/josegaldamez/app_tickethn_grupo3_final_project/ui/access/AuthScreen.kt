package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.dismissKeyboardTarget

@Composable
internal fun AuthScreen(
    registering: Boolean,
    onSwitch: () -> Unit,
    onSubmit: (name: String, email: String, password: String) -> Unit,
    pending: Boolean,
    @StringRes requestError: Int?,
    duplicateEmail: String?,
    onVerifyExistingEmail: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val trimmedName = name.trim()
    val trimmedEmail = email.trim()
    val nameError = R.string.access_name_error.takeIf {
        registering && (trimmedName.isEmpty() || trimmedName.codePointCount(0, trimmedName.length) > 150 ||
            trimmedName.any { it.isISOControl() })
    }
    val emailError = R.string.access_email_error.takeUnless {
        trimmedEmail.length <= 255 && Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
    }
    val passwordError = when {
        password.isEmpty() -> R.string.access_password_required
        password.toByteArray(Charsets.UTF_8).size > 72 -> R.string.access_password_too_long
        registering && password.codePointCount(0, password.length) < 12 -> R.string.access_password_error
        else -> null
    }
    val confirmationError = R.string.access_confirm_error.takeIf {
        registering && (confirmation.isEmpty() || confirmation != password)
    }

    val submit = {
        attempted = true
        if (!pending && nameError == null && emailError == null && passwordError == null && confirmationError == null) {
            focusManager.clearFocus()
            onSubmit(trimmedName, trimmedEmail, password)
        }
    }
    val colors = MaterialTheme.colorScheme
    val fields: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (registering) {
                AuthField(
                    name, { name = it }, R.string.access_name,
                    error = nameError.takeIf { attempted },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    icon = R.drawable.ic_access_person,
                    enabled = !pending
                )
            }
            AuthField(
                email, { email = it }, R.string.access_email,
                error = emailError.takeIf { attempted },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                icon = R.drawable.ic_access_email,
                enabled = !pending
            )
            AuthField(
                password, { password = it }, R.string.access_password,
                error = passwordError.takeIf { attempted }, passwordField = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (registering) ImeAction.Next else ImeAction.Done
                ),
                onDone = submit,
                icon = R.drawable.ic_access_lock,
                enabled = !pending
            )
            if (registering) {
                AuthField(
                    confirmation, { confirmation = it }, R.string.access_confirm_password,
                    error = confirmationError.takeIf { attempted }, passwordField = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    onDone = submit,
                    icon = R.drawable.ic_access_lock,
                    enabled = !pending
                )
            }
        }
    }
    val status: @Composable () -> Unit = {
        if (requestError != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(requestError),
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        if (duplicateEmail != null && duplicateEmail == trimmedEmail) {
            TextButton(onClick = { onVerifyExistingEmail(duplicateEmail) }, enabled = !pending) {
                Text(stringResource(R.string.verification_title))
            }
        }
    }

    AuthLayout(registering, onSwitch, submit, fields, status, pending)
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    @StringRes error: Int?,
    keyboardOptions: KeyboardOptions,
    @DrawableRes icon: Int,
    enabled: Boolean,
    passwordField: Boolean = false,
    onDone: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        label = { Text(stringResource(label), style = typography.bodyLarge) },
        textStyle = typography.bodyLarge,
        leadingIcon = { Icon(painterResource(icon), contentDescription = null, tint = colors.onBackground) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { message -> { Text(stringResource(message), style = MaterialTheme.typography.bodySmall) } },
        shape = RoundedCornerShape(15.dp),
        colors = if (!isSystemInDarkTheme()) {
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF8294BA),
                focusedBorderColor = Color(0xFF0752EC),
                unfocusedLabelColor = colors.onSurfaceVariant
            )
        } else {
            OutlinedTextFieldDefaults.colors()
        },
        modifier = Modifier.fillMaxWidth().dismissKeyboardTarget(),
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = if (passwordField && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (passwordField) {
            { PasswordVisibility(visible, onClick = { visible = !visible }) }
        } else {
            null
        }
    )
}

@Composable
private fun PasswordVisibility(visible: Boolean, onClick: () -> Unit) {
    val description = stringResource(if (visible) R.string.access_hide_password else R.string.access_show_password)
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    IconButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = description }) {
        Canvas(Modifier.size(24.dp)) {
            val stroke = 1.6.dp.toPx()
            val eye = Path().apply {
                moveTo(size.width * 0.08f, center.y)
                quadraticTo(center.x, 0f, size.width * 0.92f, center.y)
                quadraticTo(center.x, size.height, size.width * 0.08f, center.y)
                close()
            }
            drawPath(eye, color, style = Stroke(stroke))
            drawCircle(color, radius = size.width * 0.11f, style = Stroke(stroke))
            if (!visible) {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.18f), Offset(size.width * 0.82f, size.height * 0.82f), stroke)
            }
        }
    }
}
