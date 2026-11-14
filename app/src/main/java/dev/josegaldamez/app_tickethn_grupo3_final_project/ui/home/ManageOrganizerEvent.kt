package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.LocalityInput
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEvent
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEvents
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerLocality
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import java.io.IOException
import kotlinx.coroutines.launch

private enum class EventEditor { Summary, Details, Locality }

@Composable
internal fun ManageOrganizerEvent(
    id: String,
    api: OrganizerEvents,
    saving: Boolean,
    onSaving: (Boolean) -> Unit,
    onPublished: () -> Unit
) {
    var event by remember { mutableStateOf<OrganizerEvent?>(null) }
    var retry by remember { mutableStateOf(0) }
    var loadError by remember { mutableStateOf<Int?>(null) }
    var editor by rememberSaveable { mutableStateOf(EventEditor.Summary) }
    var editingLocalityId by rememberSaveable { mutableStateOf<String?>(null) }
    var showPublish by rememberSaveable { mutableStateOf(false) }
    var publishError by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id, retry) {
        loadError = null
        try {
            event = api.detail(id)
        } catch (error: AuthApiException) {
            loadError = organizerError(error, R.string.organizer_load_error)
        } catch (_: IOException) {
            loadError = R.string.organizer_load_error
        }
    }

    val current = event
    if (current == null) {
        val error = loadError
        if (error == null) {
            EventLoading(EventLoadingLayout.OrganizerDetail)
        } else {
            ProfileFeedback(error)
            TextButton(onClick = { retry++ }) { Text(stringResource(R.string.onboarding_retry)) }
        }
        return
    }
    val localities = current.localities.orEmpty()
    if (editor == EventEditor.Details) {
        BackHandler { if (!saving) editor = EventEditor.Summary }
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { editor = EventEditor.Summary }, enabled = !saving) {
                Text(stringResource(R.string.organizer_back_to_event))
            }
            EventDetailsForm(current, saving, onSaving, scroll = false) { draft, cover, _ ->
                val updated = api.update(id, draft)
                event = updated.copy(localities = localities)
                if (cover != null) {
                    event = api.replaceCover(id, cover).copy(localities = localities)
                }
                editor = EventEditor.Summary
            }
        }
        return
    }
    if (editor == EventEditor.Locality) {
        BackHandler { if (!saving) editor = EventEditor.Summary }
        key(editingLocalityId) {
            LocalityEditor(
                locality = localities.firstOrNull { it.id == editingLocalityId },
                saving = saving,
                onSaving = onSaving,
                onBack = { editor = EventEditor.Summary },
                canDeactivate = current.status != "published" ||
                    localities.any { it.id != editingLocalityId && it.active },
                onSave = { input ->
                    val saved = api.saveLocality(id, editingLocalityId, input)
                    val updated = if (editingLocalityId == null) localities + saved else {
                        localities.map { if (it.id == saved.id) saved else it }
                    }
                    event = current.copy(localities = updated)
                    editor = EventEditor.Summary
                }
            )
        }
        return
    }

    val missing = buildList {
        if (current.description.isNullOrBlank()) add(R.string.organizer_need_description)
        if (current.address.isNullOrBlank()) add(R.string.organizer_need_address)
        if ((parseOrganizerDate(current.startsAt) ?: 0L) <= System.currentTimeMillis()) add(R.string.organizer_need_date)
        if (localities.none { it.active }) add(R.string.organizer_need_locality)
    }

    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(current.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(
            stringResource(when (current.status) {
                "draft" -> R.string.organizer_status_draft
                "published" -> R.string.organizer_status_published
                "cancelled" -> R.string.organizer_status_cancelled
                else -> R.string.organizer_status_unknown
            }),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = { editor = EventEditor.Details },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = !saving
        ) { Text(stringResource(R.string.organizer_edit_details)) }

        HorizontalDivider()
        Text(
            stringResource(R.string.organizer_localities),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
        if (localities.isEmpty()) {
            Text(stringResource(R.string.organizer_localities_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        localities.forEach { locality ->
            key(locality.id) {
                ProfilePanel {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(locality.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.organizer_locality_summary, locality.price.toPlainString(), locality.capacity),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            stringResource(
                                if (locality.active) R.string.organizer_locality_active_summary else R.string.organizer_locality_inactive_summary,
                                locality.soldCount
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            enabled = !saving,
                            onClick = {
                                editingLocalityId = locality.id
                                editor = EventEditor.Locality
                            }
                        ) { Text(stringResource(R.string.organizer_edit_locality)) }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = {
                editingLocalityId = null
                editor = EventEditor.Locality
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = !saving
        ) { Text(stringResource(R.string.organizer_add_locality)) }

        if (current.status == "draft") {
            HorizontalDivider()
            if (missing.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.organizer_publish_requirements), style = MaterialTheme.typography.titleMedium)
                    missing.forEach { requirement ->
                        Text(
                            stringResource(requirement),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            publishError?.let { ProfileFeedback(it) }
            AuthPrimaryButton(
                label = stringResource(if (saving) R.string.organizer_publishing else R.string.organizer_publish),
                onClick = { showPublish = true },
                color = MaterialTheme.colorScheme.primary,
                enabled = !saving && missing.isEmpty()
            )
        }
    }

    if (showPublish) {
        AlertDialog(
            onDismissRequest = { showPublish = false },
            title = { Text(stringResource(R.string.organizer_publish)) },
            text = { Text(stringResource(R.string.organizer_publish_confirmation, current.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (saving || !showPublish) return@TextButton
                        showPublish = false
                        onSaving(true)
                        publishError = null
                        scope.launch {
                            try {
                                api.publish(id)
                                onPublished()
                            } catch (error: AuthApiException) {
                                publishError = organizerError(error, R.string.organizer_publish_error)
                                if (error.status == 409) retry++
                            } catch (_: IOException) {
                                publishError = R.string.organizer_publish_error
                                retry++
                            } finally {
                                onSaving(false)
                            }
                        }
                    }
                ) { Text(stringResource(R.string.organizer_publish)) }
            },
            dismissButton = {
                TextButton(onClick = { showPublish = false }) { Text(stringResource(R.string.organizer_cancel)) }
            }
        )
    }
}

private val localityPrice = Regex("[0-9]{1,10}([.,][0-9]{1,2})?")

@Composable
private fun LocalityEditor(
    locality: OrganizerLocality?,
    saving: Boolean,
    onSaving: (Boolean) -> Unit,
    onBack: () -> Unit,
    canDeactivate: Boolean,
    onSave: suspend (LocalityInput) -> Unit
) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var title by rememberSaveable { mutableStateOf(locality?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(locality?.description.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(locality?.price?.toPlainString().orEmpty()) }
    var capacity by rememberSaveable { mutableStateOf(locality?.capacity?.toString().orEmpty()) }
    var active by rememberSaveable { mutableStateOf(locality?.active ?: true) }
    var error by remember { mutableStateOf<Int?>(null) }
    val amount = price.trim().takeIf(localityPrice::matches)?.replace(',', '.')?.toBigDecimalOrNull()
    val quantity = capacity.trim().toIntOrNull()?.takeIf { it >= maxOf(1, locality?.soldCount ?: 0) }
    Column(
        Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack, enabled = !saving) { Text(stringResource(R.string.organizer_back_to_event)) }
        Text(
            stringResource(if (locality == null) R.string.organizer_add_locality else R.string.organizer_edit_locality),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
        DraftField(title, { title = it }, R.string.organizer_locality_title, saving)
        DraftField(description, { description = it }, R.string.organizer_description, saving, multiline = true)
        DraftField(price, { price = it }, R.string.organizer_price, saving, keyboardType = KeyboardType.Decimal)
        if (price.isNotBlank() && amount == null) ProfileFeedback(R.string.organizer_price_error)
        DraftField(capacity, { capacity = it }, R.string.organizer_capacity, saving, keyboardType = KeyboardType.Number)
        if (capacity.isNotBlank() && quantity == null) {
            ProfileFeedback(R.string.organizer_capacity_error)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val label = stringResource(R.string.organizer_locality_active)
            Text(label, modifier = Modifier.weight(1f))
            Switch(
                checked = active,
                onCheckedChange = { active = it },
                enabled = !saving && (!active || canDeactivate),
                modifier = Modifier.semantics { contentDescription = label }
            )
        }
        if (active && !canDeactivate) {
            Text(
                stringResource(R.string.organizer_keep_active),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        error?.let { ProfileFeedback(it) }
        AuthPrimaryButton(
            label = stringResource(if (saving) R.string.organizer_saving else R.string.organizer_save_locality),
            onClick = {
                if (saving || amount == null || quantity == null || title.isBlank()) return@AuthPrimaryButton
                focus.clearFocus()
                val input = LocalityInput(title.trim(), description.trim(), amount, quantity, active)
                onSaving(true)
                error = null
                scope.launch {
                    try {
                        onSave(input)
                    } catch (failure: AuthApiException) {
                        error = organizerError(failure, R.string.organizer_locality_error)
                    } catch (_: IOException) {
                        error = R.string.organizer_locality_error
                    } finally {
                        onSaving(false)
                    }
                }
            },
            color = MaterialTheme.colorScheme.primary,
            enabled = !saving && title.isNotBlank() && amount != null && quantity != null
        )
    }
}

