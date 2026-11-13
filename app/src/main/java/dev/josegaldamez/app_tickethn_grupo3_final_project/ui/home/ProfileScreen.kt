package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.TicketHNWordmark

internal enum class ProfileDestination(@param:StringRes val title: Int, @param:DrawableRes val icon: Int) {
    Payment(R.string.profile_payment, R.drawable.ic_profile_card),
    Purchases(R.string.profile_purchases, R.drawable.ic_home_ticket),
    Help(R.string.profile_help, R.drawable.ic_profile_help),
    About(R.string.profile_about, R.drawable.ic_profile_about)
}

@Composable
internal fun ProfileScreen(
    email: String,
    accountName: String?,
    details: ProfileDetails?,
    readFailed: Boolean,
    saving: Boolean,
    onRetry: () -> Unit,
    onEdit: () -> Unit,
    onOpen: (ProfileDestination) -> Unit,
    onSignOut: () -> Unit,
    signOutFailed: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val canEdit = details != null && !saving
    val name = details?.displayName ?: accountName ?: stringResource(R.string.profile_default_name)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TicketHNWordmark()
            ScreenHeaderDivider()
            Text(
                stringResource(R.string.home_profile),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 34.sp),
                modifier = Modifier.semantics { heading() }
            )
        }

        ProfilePanel {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePortrait(Modifier.size(64.dp))
                    Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onEdit, enabled = canEdit) {
                        Icon(
                            painterResource(R.drawable.ic_profile_edit),
                            contentDescription = stringResource(R.string.profile_edit),
                            tint = if (canEdit) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
                Text(
                    email,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = colors.outlineVariant)
            ProfileActivitySummary()
        }

        ProfilePanel {
            ProfileDestination.entries.forEachIndexed { index, destination ->
                if (index > 0) {
                    HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = colors.outlineVariant)
                }
                ProfileRow(destination.icon, destination.title) { onOpen(destination) }
            }
        }
        if (readFailed) {
            ProfileFeedback(R.string.profile_load_error)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.onboarding_retry)) }
        }
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.profile_version),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            if (signOutFailed) ProfileFeedback(R.string.home_sign_out_error)
            AuthPrimaryButton(
                label = stringResource(R.string.home_sign_out),
                onClick = onSignOut,
                color = colors.error,
                enabled = !saving,
                contentColor = colors.onError
            )
        }
    }
}

@Composable
private fun ProfileActivitySummary() {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileActivityCount(R.string.profile_favorites, 12, Modifier.weight(1f))
        VerticalDivider(Modifier.height(28.dp), color = dividerColor)
        ProfileActivityCount(R.string.profile_events, 8, Modifier.weight(1f))
        VerticalDivider(Modifier.height(28.dp), color = dividerColor)
        ProfileActivityCount(R.string.profile_rated, 5, Modifier.weight(1f))
    }
}

@Composable
private fun ProfileActivityCount(@StringRes label: Int, count: Int, modifier: Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp).semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun ProfilePanel(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { Column { content() } }
}

@Composable
private fun ProfileRow(@DrawableRes icon: Int, @StringRes label: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(21.dp))
        Text(
            stringResource(label),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            painterResource(R.drawable.ic_home_next),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun ProfileFeedback(@StringRes message: Int) {
    Text(
        stringResource(message),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    )
}
