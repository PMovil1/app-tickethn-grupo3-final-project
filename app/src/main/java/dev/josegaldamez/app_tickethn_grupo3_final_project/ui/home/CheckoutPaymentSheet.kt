package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AccessPreferences
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.DemoPaymentCard
import java.io.IOException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutPaymentSheet(
    email: String,
    total: String,
    canConfirm: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { AccessPreferences(context) }
    val scope = rememberCoroutineScope()
    var savedNames by remember(email) { mutableStateOf<Set<String>?>(null) }
    var selected by rememberSaveable(email) { mutableStateOf<DemoPaymentCard?>(null) }
    var adding by rememberSaveable(email) { mutableStateOf(false) }
    var retry by remember(email) { mutableStateOf(0) }
    var readFailed by remember(email) { mutableStateOf(false) }
    var saving by remember(email) { mutableStateOf(false) }
    var saveFailed by remember(email) { mutableStateOf(false) }

    LaunchedEffect(email, retry) {
        readFailed = false
        try {
            savedNames = preferences.readPaymentCards(email)
        } catch (_: IOException) {
            readFailed = true
        }
    }

    val savedCards = DemoPaymentCard.entries.filter { it.name in savedNames.orEmpty() }
    val availableCards = DemoPaymentCard.entries - savedCards.toSet()
    val paymentCard = selected?.takeIf { it in savedCards } ?: savedCards.firstOrNull()

    fun saveCard(card: DemoPaymentCard) {
        if (saving) return
        saving = true
        saveFailed = false
        scope.launch {
            try {
                savedNames = preferences.setPaymentCardSaved(email, card, saved = true)
                selected = card
                adding = false
            } catch (_: IOException) {
                saveFailed = true
            } finally {
                saving = false
            }
        }
    }

    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !saving }
    )
    ModalBottomSheet(
        onDismissRequest = { if (!saving) onDismiss() },
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = null
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.event_payment_title),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            if (!sheetState.isVisible) onDismiss()
                        }
                    },
                    enabled = !saving
                ) {
                    Icon(painterResource(R.drawable.ic_home_clear), stringResource(R.string.access_close))
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            Column(
                Modifier.fillMaxWidth().weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.event_payment_demo),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                when {
                    readFailed -> {
                        ProfileFeedback(R.string.profile_payment_load_error)
                        TextButton(onClick = { retry++ }) { Text(stringResource(R.string.onboarding_retry)) }
                    }
                    savedNames == null -> Text(stringResource(R.string.profile_payment_loading))
                    else -> {
                        if (paymentCard != null) {
                            SavedCardSelect(
                                selected = paymentCard,
                                cards = savedCards,
                                enabled = !saving,
                                onSelect = {
                                    selected = it
                                    adding = false
                                    saveFailed = false
                                },
                                onAdd = { adding = true },
                                canAdd = availableCards.isNotEmpty()
                            )
                        } else {
                            Text(
                                stringResource(R.string.profile_payment_empty),
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if ((paymentCard == null || adding) && availableCards.isNotEmpty()) {
                            Text(
                                stringResource(R.string.profile_payment_available),
                                style = MaterialTheme.typography.titleSmall
                            )
                            availableCards.forEach { card ->
                                ProfilePanel {
                                    Row(
                                        Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_profile_card),
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp), tint = colors.primary
                                        )
                                        Column(Modifier.weight(1f)) {
                                            PaymentCardRow(card, saved = false, enabled = !saving) { saveCard(card) }
                                        }
                                    }
                                }
                            }
                        }
                        if (saveFailed) ProfileFeedback(R.string.profile_payment_save_error)
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            Column(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canConfirm) Text(total, style = MaterialTheme.typography.titleLarge)
                Button(
                    onClick = { if (!saving && canConfirm && paymentCard != null) onConfirm() },
                    enabled = canConfirm && paymentCard != null && !saving,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.event_order_submit))
                }
            }
        }
    }
}

@Composable
private fun SavedCardSelect(
    selected: DemoPaymentCard,
    cards: List<DemoPaymentCard>,
    enabled: Boolean,
    canAdd: Boolean,
    onSelect: (DemoPaymentCard) -> Unit,
    onAdd: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val menuWidth = maxWidth
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_profile_card), null, Modifier.size(22.dp))
                Text(
                    stringResource(R.string.event_payment_card_label, selected.brand, selected.lastFour),
                    Modifier.weight(1f)
                )
                Icon(painterResource(R.drawable.ic_home_next), null, Modifier.size(16.dp).rotate(90f))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidth)
        ) {
            cards.forEach { card ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.event_payment_card_label, card.brand, card.lastFour)) },
                    trailingIcon = { RadioButton(selected = card == selected, onClick = null) },
                    onClick = {
                        expanded = false
                        onSelect(card)
                    }
                )
            }
            if (canAdd) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.event_payment_add_card)) },
                    onClick = {
                        expanded = false
                        onAdd()
                    }
                )
            }
        }
    }
}
