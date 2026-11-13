package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.dismissKeyboardTarget
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider

@Composable
internal fun EditProfileScreen(
    details: ProfileDetails?,
    accountName: String?,
    email: String,
    readFailed: Boolean,
    saveFailed: Boolean,
    saving: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSave: (ProfileDetails) -> Unit,
    onRequestOrganizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !saving) {
                Icon(
                    painterResource(R.drawable.ic_verification_back),
                    contentDescription = stringResource(R.string.profile_back)
                )
            }
            Text(
                stringResource(R.string.profile_edit),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
        }
        ScreenHeaderDivider()

        when {
            details != null -> EditProfileForm(details, accountName, email, saveFailed, saving, onSave, onRequestOrganizer)
            readFailed -> {
                ProfileFeedback(R.string.profile_load_error)
                TextButton(onClick = onRetry) { Text(stringResource(R.string.onboarding_retry)) }
            }
            else -> Text(stringResource(R.string.profile_loading))
        }
    }
}

@Composable
private fun EditProfileForm(
    details: ProfileDetails,
    accountName: String?,
    email: String,
    saveFailed: Boolean,
    saving: Boolean,
    onSave: (ProfileDetails) -> Unit,
    onRequestOrganizer: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val focus = LocalFocusManager.current
    val originalName = details.displayName ?: accountName.orEmpty()
    val originalCity = HomeCity.entries.firstOrNull { it.name == details.cityCode } ?: HomeCity.Tegucigalpa
    var name by rememberSaveable(originalName) { mutableStateOf(originalName) }
    var city by rememberSaveable(originalCity) { mutableStateOf(originalCity) }
    var notifications by rememberSaveable(details.notifications) { mutableStateOf(details.notifications) }
    val trimmedName = name.trim()
    val nameChanged = trimmedName != originalName
    val nameInvalid = nameChanged && (trimmedName.isEmpty() ||
        trimmedName.codePointCount(0, trimmedName.length) > 150 || trimmedName.any { it.isISOControl() })
    val updated = details.copy(
        displayName = if (nameChanged) trimmedName else details.displayName,
        cityCode = if (city != originalCity) city.name else details.cityCode,
        notifications = notifications
    )

    ProfilePanel {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.profile_data),
                style = typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = colors.surfaceVariant,
                errorContainerColor = colors.surfaceVariant,
                unfocusedBorderColor = colors.outlineVariant
            )
            val fieldShape = RoundedCornerShape(12.dp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.access_name)) },
                singleLine = true,
                enabled = !saving,
                isError = nameInvalid,
                supportingText = if (nameInvalid) {
                    { Text(stringResource(R.string.access_name_error)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth().dismissKeyboardTarget(),
                shape = fieldShape,
                colors = fieldColors
            )
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text(stringResource(R.string.access_email)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().dismissKeyboardTarget(editable = false),
                shape = fieldShape,
                colors = fieldColors.copy(
                    focusedTextColor = colors.onSurfaceVariant,
                    unfocusedTextColor = colors.onSurfaceVariant,
                    focusedLabelColor = colors.onSurfaceVariant,
                    focusedIndicatorColor = colors.outlineVariant
                )
            )
        }
    }

    ProfilePanel {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.profile_preferences),
                style = typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            PreferredCityPicker(city, enabled = !saving, onSelect = { city = it })
            HorizontalDivider(color = colors.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.profile_language), style = typography.labelLarge)
                Text(stringResource(R.string.profile_spanish), style = typography.bodyLarge)
                Text(stringResource(R.string.profile_language_availability), style = typography.bodySmall, color = colors.onSurfaceVariant)
            }
            HorizontalDivider(color = colors.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                    value = notifications,
                    enabled = !saving,
                    role = Role.Switch,
                    onValueChange = { notifications = it }
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.profile_notifications), style = typography.labelLarge)
                    Text(stringResource(R.string.profile_notifications_preference), style = typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Switch(checked = notifications, onCheckedChange = null, enabled = !saving)
            }
        }
    }

    ProfilePanel {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.profile_organizer_title),
                style = typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text(stringResource(R.string.profile_organizer_info), style = typography.bodyMedium, color = colors.onSurfaceVariant)
            FilledTonalButton(
                onClick = {
                    focus.clearFocus()
                    onRequestOrganizer()
                },
                enabled = !saving && !details.organizerRequested,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                val label = if (details.organizerRequested) {
                    R.string.profile_organizer_requested
                } else {
                    R.string.profile_organizer_apply
                }
                Text(stringResource(label))
            }
        }
    }

    if (saveFailed) ProfileFeedback(R.string.profile_save_error)
    AuthPrimaryButton(
        label = stringResource(if (saving) R.string.profile_saving else R.string.profile_save),
        onClick = {
            focus.clearFocus()
            onSave(updated)
        },
        color = colors.primary,
        enabled = !saving && !nameInvalid && updated != details
    )
}

@Composable
private fun PreferredCityPicker(city: HomeCity, enabled: Boolean, onSelect: (HomeCity) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    Box {
        OutlinedButton(
            onClick = {
                focus.clearFocus()
                expanded = true
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            enabled = enabled
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.profile_city), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(city.label), style = MaterialTheme.typography.bodyLarge)
            }
            Icon(painterResource(R.drawable.ic_home_next), null, Modifier.size(16.dp).rotate(90f))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            HomeCity.entries.forEach { option ->
                val isCurrent = option == city
                DropdownMenuItem(
                    text = { Text(stringResource(option.label)) },
                    modifier = Modifier.semantics { selected = isCurrent },
                    trailingIcon = if (isCurrent) {
                        { RadioButton(selected = true, onClick = null) }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
