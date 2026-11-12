package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.TicketHNWordmark
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider

private enum class ProfileAction { Edit, Payment, Notifications, Help, City, Language, Organizer }

@Composable
internal fun ProfileScreen(
    email: String,
    details: ProfileDetails?,
    readFailed: Boolean,
    saveFailed: Boolean,
    saving: Boolean,
    onRetry: () -> Unit,
    onUpdate: (ProfileDetails) -> Unit,
    onSignOut: () -> Unit,
    signOutFailed: Boolean
) {
    var action by rememberSaveable { mutableStateOf<ProfileAction?>(null) }
    val colors = MaterialTheme.colorScheme
    val current = details ?: ProfileDetails()
    val canEdit = details != null && !saving
    val name = current.displayName ?: profileNameFromEmail(email) ?: stringResource(R.string.profile_default_name)
    val city = HomeCity.entries.firstOrNull { it.name == current.cityCode } ?: HomeCity.Tegucigalpa

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TicketHNWordmark()
            IconButton(onClick = { action = ProfileAction.Notifications }) {
                Icon(painterResource(R.drawable.ic_profile_bell), stringResource(R.string.profile_notifications))
            }
        }
        ScreenHeaderDivider()

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                stringResource(R.string.home_profile),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 34.sp),
                modifier = Modifier.semantics { heading() }
            )
            Text(stringResource(R.string.profile_subtitle), color = colors.onSurfaceVariant)
        }

        ProfilePanel {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfilePortrait(Modifier.size(60.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.background(colors.secondaryContainer, CircleShape)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_access_person),
                            contentDescription = null,
                            tint = colors.onSecondaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            stringResource(R.string.profile_buyer),
                            color = colors.onSecondaryContainer,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                OutlinedButton(
                    onClick = { action = ProfileAction.Edit },
                    enabled = canEdit,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_profile_edit), contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.profile_edit), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        OrganizerBanner(
            requested = current.organizerRequested,
            enabled = canEdit,
            onRequest = { onUpdate(current.copy(organizerRequested = true)) },
            onLearnMore = { action = ProfileAction.Organizer }
        )

        ProfilePanel {
            ProfileRow(R.drawable.ic_access_person, R.string.profile_data, onClick = { action = ProfileAction.Edit })
            ProfileDivider()
            ProfileRow(R.drawable.ic_profile_card, R.string.profile_payment, onClick = { action = ProfileAction.Payment })
            ProfileDivider()
            ProfileRow(R.drawable.ic_profile_bell, R.string.profile_notifications, onClick = { action = ProfileAction.Notifications })
            ProfileDivider()
            ProfileRow(R.drawable.ic_profile_help, R.string.profile_help, onClick = { action = ProfileAction.Help })
            ProfileDivider()
            ProfileRow(R.drawable.ic_profile_logout, R.string.home_sign_out, enabled = !saving, onClick = onSignOut)
        }

        ProfilePanel {
            ProfileRow(
                R.drawable.ic_home_pin,
                R.string.profile_city,
                value = stringResource(city.label),
                enabled = canEdit,
                onClick = { action = ProfileAction.City }
            )
            ProfileDivider()
            ProfileRow(
                R.drawable.ic_profile_language,
                R.string.profile_language,
                value = stringResource(R.string.profile_spanish),
                onClick = { action = ProfileAction.Language }
            )
        }
        if (readFailed) {
            ProfileFeedback(R.string.profile_load_error)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.onboarding_retry)) }
        }
        if (saveFailed) ProfileFeedback(R.string.profile_save_error)
        if (signOutFailed) ProfileFeedback(R.string.home_sign_out_error)
    }

    val close = { action = null }
    when (action) {
        ProfileAction.Edit -> EditProfileDialog(
            name = current.displayName ?: profileNameFromEmail(email).orEmpty(),
            email = email,
            canEdit = canEdit,
            onDismiss = close,
            onSave = {
                onUpdate(current.copy(displayName = it))
                close()
            }
        )
        ProfileAction.City -> ProfileCityDialog(city, onDismiss = close, onSelect = {
            onUpdate(current.copy(cityCode = it.name))
            close()
        })
        ProfileAction.Notifications -> AlertDialog(
            onDismissRequest = close,
            title = { Text(stringResource(R.string.profile_notifications)) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth().toggleable(
                        value = current.notifications,
                        enabled = canEdit,
                        role = Role.Switch,
                        onValueChange = { onUpdate(current.copy(notifications = it)) }
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.profile_notifications_preference), Modifier.weight(1f))
                    Switch(
                        checked = current.notifications,
                        onCheckedChange = null,
                        enabled = canEdit
                    )
                }
            },
            confirmButton = { ProfileCloseButton(close) }
        )
        ProfileAction.Payment -> ProfileInfoDialog(R.string.profile_payment, R.string.profile_payment_empty, close)
        ProfileAction.Help -> ProfileInfoDialog(R.string.profile_help, R.string.profile_help_text, close)
        ProfileAction.Language -> ProfileInfoDialog(R.string.profile_language, R.string.profile_spanish, close)
        ProfileAction.Organizer -> ProfileInfoDialog(R.string.profile_organizer_title, R.string.profile_organizer_info, close)
        null -> Unit
    }
}

@Composable
private fun ProfilePanel(color: Color = MaterialTheme.colorScheme.surface, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { Column { content() } }
}

@Composable
private fun OrganizerBanner(
    requested: Boolean,
    enabled: Boolean,
    onRequest: () -> Unit,
    onLearnMore: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    ProfilePanel(color = colors.secondaryContainer.copy(alpha = 0.42f)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(46.dp).background(colors.secondaryContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_profile_organizer),
                        contentDescription = null,
                        tint = colors.onSecondaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.profile_organizer_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.profile_organizer_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onRequest, enabled = enabled && !requested) {
                    Text(stringResource(if (requested) R.string.profile_organizer_requested else R.string.profile_organizer_apply))
                }
                TextButton(onClick = onLearnMore) {
                    Text(stringResource(R.string.profile_organizer_more))
                    Icon(painterResource(R.drawable.ic_home_next), contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    value: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val title = stringResource(label)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(21.dp))
        Text(
            buildAnnotatedString {
                append(title)
                if (value != null) {
                    append(": ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value) }
                }
            },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) colors.onSurface else colors.onSurfaceVariant
        )
        Icon(
            painterResource(R.drawable.ic_home_next),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ProfileFeedback(@StringRes message: Int) {
    Text(
        stringResource(message),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    )
}

@Composable
private fun EditProfileDialog(
    name: String,
    email: String,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var draft by rememberSaveable(name) { mutableStateOf(name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_data)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text(stringResource(R.string.access_name)) },
                    singleLine = true,
                    enabled = canEdit
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.access_email)) },
                    singleLine = true,
                    readOnly = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft.trim()) }, enabled = canEdit && draft.isNotBlank() && draft.trim() != name) {
                Text(stringResource(R.string.profile_save))
            }
        },
        dismissButton = { ProfileCloseButton(onDismiss) }
    )
}

@Composable
private fun ProfileCityDialog(current: HomeCity, onDismiss: () -> Unit, onSelect: (HomeCity) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_city)) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                HomeCity.entries.forEach { city ->
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .clickable { onSelect(city) }.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(city.label))
                        RadioButton(selected = city == current, onClick = null)
                    }
                }
            }
        },
        confirmButton = { ProfileCloseButton(onDismiss) }
    )
}

@Composable
private fun ProfileInfoDialog(@StringRes title: Int, @StringRes description: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(description)) },
        confirmButton = { ProfileCloseButton(onDismiss) }
    )
}

@Composable
private fun ProfileCloseButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(stringResource(R.string.access_close)) }
}

internal fun profileNameFromEmail(email: String): String? =
    email.substringBefore('@').split('.', '_', '-', '+')
        .filter { it.any(Char::isLetter) }.take(3)
        .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
        .takeIf { it.isNotBlank() }
