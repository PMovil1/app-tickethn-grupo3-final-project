package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AccessPreferences
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.DemoPaymentCard
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ScannedTicket
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
internal fun ProfileDestinationScreen(
    destination: ProfileDestination,
    email: String,
    onBack: () -> Unit,
    onLookupTicket: suspend (String) -> ScannedTicket,
    onCheckIn: suspend (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    painterResource(R.drawable.ic_verification_back),
                    contentDescription = stringResource(R.string.profile_back)
                )
            }
            Text(
                stringResource(destination.title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
        }
        ScreenHeaderDivider()
        when (destination) {
            ProfileDestination.ScanTicket -> TicketScanner(onLookupTicket, onCheckIn)
            ProfileDestination.Payment -> PaymentMethods(email)
            ProfileDestination.Purchases -> PurchaseHistory()
            ProfileDestination.Help -> HelpAndSupport()
            ProfileDestination.About -> AboutTicketHN()
            ProfileDestination.OrganizerEvents, ProfileDestination.CreateEvent -> Unit
        }
    }
}

@Composable
private fun PaymentMethods(email: String) {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { AccessPreferences(context) }
    val scope = rememberCoroutineScope()
    var savedCards by remember(email) { mutableStateOf<Set<String>?>(null) }
    var readFailed by remember(email) { mutableStateOf(false) }
    var retry by remember(email) { mutableStateOf(0) }
    var saving by remember(email) { mutableStateOf(false) }
    var saveFailed by remember(email) { mutableStateOf(false) }

    fun updateCard(card: DemoPaymentCard, saved: Boolean) {
        if (saving) return
        saving = true
        saveFailed = false
        scope.launch {
            try {
                savedCards = preferences.setPaymentCardSaved(email, card, saved)
            } catch (_: IOException) {
                saveFailed = true
            } finally {
                saving = false
            }
        }
    }

    LaunchedEffect(email, retry) {
        readFailed = false
        try {
            savedCards = preferences.readPaymentCards(email)
        } catch (_: IOException) {
            readFailed = true
        }
    }

    Text(stringResource(R.string.profile_payment_demo), color = MaterialTheme.colorScheme.onSurfaceVariant)
    val saved = savedCards
    if (saved == null) {
        if (readFailed) {
            ProfileFeedback(R.string.profile_payment_load_error)
            TextButton(onClick = { retry++ }) { Text(stringResource(R.string.onboarding_retry)) }
        } else {
            Text(stringResource(R.string.profile_payment_loading))
        }
        return
    }

    val savedOptions = DemoPaymentCard.entries.filter { it.name in saved }
    val availableOptions = DemoPaymentCard.entries.filterNot { it.name in saved }
    ProfileSection(R.string.profile_payment_saved) {
        if (savedOptions.isEmpty()) Text(stringResource(R.string.profile_payment_empty))
        savedOptions.forEach { card ->
            PaymentCardRow(card, saved = true, enabled = !saving) {
                updateCard(card, saved = false)
            }
        }
    }
    if (availableOptions.isNotEmpty()) {
        ProfileSection(R.string.profile_payment_available) {
            availableOptions.forEach { card ->
                PaymentCardRow(card, saved = false, enabled = !saving) {
                    updateCard(card, saved = true)
                }
            }
        }
    }
    if (saveFailed) ProfileFeedback(R.string.profile_payment_save_error)
}

@Composable
internal fun PaymentCardRow(card: DemoPaymentCard, saved: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(card.brand, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.profile_payment_last_four, card.lastFour),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(if (saved) R.string.profile_payment_remove else R.string.profile_payment_add))
        }
    }
}

@Composable
private fun PurchaseHistory() {
    var receipt by rememberSaveable { mutableStateOf<DemoPurchase?>(null) }
    Text(stringResource(R.string.profile_purchases_demo), color = MaterialTheme.colorScheme.onSurfaceVariant)
    DemoPurchase.entries.sortedByDescending { it.purchasedOn }.forEach { purchase ->
        PurchaseCard(purchase) { receipt = purchase }
    }
    receipt?.let { purchase ->
        PurchaseReceiptSheet(purchase, onDismiss = { receipt = null })
    }
}

private enum class DemoPurchase(
    val eventTitle: Int,
    val image: Int,
    val admission: Int,
    val orderId: String,
    val purchasedOn: Long,
    val subtotal: Int,
    val serviceFee: Int
) {
    Concert(R.string.home_event_concert, R.drawable.home_concert, R.string.home_ticket_general,
        "THN-10482", eventDay(2026, 11, 10), 850, 50),
    Festival(R.string.home_event_festival, R.drawable.home_festival, R.string.home_ticket_vip,
        "THN-10367", eventDay(2026, 11, 4), 1200, 75);

    val total: Int get() = subtotal + serviceFee
}

@Composable
private fun PurchaseCard(purchase: DemoPurchase, onReceipt: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    ProfilePanel {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.profile_purchase_order, purchase.orderId),
                        style = typography.labelLarge
                    )
                    Text(
                        stringResource(R.string.profile_purchase_date, formatEventDay(purchase.purchasedOn)),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.profile_purchase_total),
                        style = typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    Text(purchaseAmount(purchase.total), style = typography.titleMedium)
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(purchase.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(purchase.eventTitle), style = typography.titleMedium)
                    Text(stringResource(purchase.admission), style = typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            OutlinedButton(
                onClick = onReceipt,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.profile_purchase_view_receipt))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseReceiptSheet(purchase: DemoPurchase, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        // Size the content so the sheet still calculates its anchors from the full window height.
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.8f).navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    stringResource(R.string.profile_purchase_receipt),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() }
                )
                PurchaseReceipt(purchase)
            }
            Column(Modifier.padding(bottom = 16.dp)) {
                AuthPrimaryButton(
                    label = stringResource(R.string.access_close),
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            if (!sheetState.isVisible) onDismiss()
                        }
                    },
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun PurchaseReceipt(purchase: DemoPurchase) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    Text(stringResource(R.string.profile_purchase_receipt_demo), color = colors.onSurfaceVariant)
    ProfilePanel {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.profile_purchase_order, purchase.orderId), style = typography.titleLarge)
            Text(
                stringResource(R.string.profile_purchase_date, formatEventDay(purchase.purchasedOn)),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            HorizontalDivider(color = colors.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(purchase.eventTitle), style = typography.titleMedium)
                Text(
                    stringResource(R.string.profile_purchase_entry, stringResource(purchase.admission)),
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            ReceiptAmount(R.string.profile_purchase_subtotal, purchase.subtotal)
            ReceiptAmount(R.string.profile_purchase_fee, purchase.serviceFee)
            HorizontalDivider(color = colors.outlineVariant)
            ReceiptAmount(R.string.profile_purchase_total, purchase.total, emphasized = true)
        }
    }
}

@Composable
private fun ReceiptAmount(@StringRes label: Int, amount: Int, emphasized: Boolean = false) {
    val typography = MaterialTheme.typography
    val style = if (emphasized) typography.titleMedium else typography.bodyMedium
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(label), modifier = Modifier.weight(1f), style = style)
        Text(purchaseAmount(amount), style = style)
    }
}

@Composable
private fun purchaseAmount(amount: Int): String =
    stringResource(
        R.string.home_filter_price_value,
        NumberFormat.getNumberInstance(Locale.forLanguageTag("es-HN")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
    )

@Composable
private fun HelpAndSupport() {
    ProfileSection(R.string.profile_help_tickets_title) {
        Text(stringResource(R.string.profile_help_tickets))
    }
    ProfileSection(R.string.profile_help_events_title) {
        Text(stringResource(R.string.profile_help_events))
    }
    ProfileSection(R.string.profile_help_account_title) {
        Text(stringResource(R.string.profile_help_account))
    }
}

@Composable
private fun AboutTicketHN() {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val iconShape = RoundedCornerShape(22.dp)

    ProfilePanel {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.tickethn_logo),
                contentDescription = null,
                modifier = Modifier.size(88.dp).clip(iconShape)
                    .background(colorResource(R.color.tickethn_launcher_background))
                    .border(1.dp, colors.outlineVariant, iconShape)
                    .padding(16.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    stringResource(R.string.app_name),
                    style = typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    stringResource(R.string.profile_version),
                    style = typography.labelMedium,
                    color = colors.onSurfaceVariant
                )
            }
            Text(
                stringResource(R.string.profile_about_description),
                modifier = Modifier.widthIn(max = 360.dp),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
    ProfileSection(R.string.profile_about_project) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.uth_logo),
                contentDescription = null,
                modifier = Modifier.size(width = 80.dp, height = 52.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                stringResource(R.string.profile_about_course),
                modifier = Modifier.weight(1f),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
    ProfileSection(R.string.profile_about_team) {
        Column(Modifier.fillMaxWidth()) {
            stringArrayResource(R.array.profile_about_members).forEachIndexed { index, member ->
                if (index > 0) HorizontalDivider(color = colors.outlineVariant)
                Text(
                    member,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    style = typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(@StringRes title: Int, content: @Composable ColumnScope.() -> Unit) {
    ProfilePanel {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            content()
        }
    }
}
