package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrderItem
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEvent
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEventsApi
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicTicketType
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.TicketOrder
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.launch

private const val MaxTicketsPerOrder = 20

@Composable
internal fun EventDetailScreen(
    eventId: String,
    email: String,
    onBack: () -> Unit,
    onBuyTickets: suspend (String, List<OrderItem>) -> TicketOrder,
    onCatalogChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var event by remember(eventId) { mutableStateOf<PublicEvent?>(null) }
    var loading by remember(eventId) { mutableStateOf(true) }
    var loadError by remember(eventId) { mutableStateOf<Int?>(null) }
    var loadRequest by remember(eventId) { mutableStateOf(0) }
    var checkout by rememberSaveable(eventId) { mutableStateOf(false) }
    var choosingPayment by rememberSaveable(email, eventId) { mutableStateOf(false) }
    var quantities by rememberSaveable(eventId) { mutableStateOf(hashMapOf<String, Int>()) }
    var order by rememberSaveable(eventId) { mutableStateOf<TicketOrder?>(null) }
    var receiptTitle by rememberSaveable(eventId) { mutableStateOf<String?>(null) }
    var pendingOrder by rememberSaveable(eventId) { mutableStateOf(false) }
    var submitting by remember(eventId) { mutableStateOf(false) }
    var purchaseError by rememberSaveable(eventId) { mutableStateOf<Int?>(null) }

    fun reloadAvailability() {
        loading = true
        loadError = null
        loadRequest++
    }

    LaunchedEffect(eventId, loadRequest) {
        if (order != null) return@LaunchedEffect
        try {
            val current = PublicEventsApi.detail(eventId)
            quantities = current.ticketTypes.orEmpty().filter { it.active && it.available > 0 }
                .mapNotNull { locality ->
                    val quantity = minOf(quantities[locality.id] ?: 0, locality.available)
                    if (quantity > 0) locality.id to quantity else null
                }.toMap(hashMapOf())
            event = current
        } catch (error: AuthApiException) {
            if (error.status == 404) {
                loadError = R.string.event_detail_gone
                event = null
            } else {
                loadError = R.string.event_detail_load_error
            }
        } catch (_: IOException) {
            loadError = R.string.event_detail_load_error
        } finally {
            loading = false
        }
    }

    val currentEvent = event
    val receipt = order
    if (receipt != null) {
        PurchaseConfirmationScreen(
            order = receipt,
            eventTitle = receiptTitle ?: currentEvent?.title,
            onClose = onBack
        )
        return
    }
    val localities = currentEvent?.ticketTypes.orEmpty().filter { it.active }
    val hasStock = localities.any { it.available > 0 }
    val quantity = quantities.values.sum()
    val total = localities.fold(BigDecimal.ZERO) { sum, locality ->
        sum + locality.price.multiply(BigDecimal.valueOf((quantities[locality.id] ?: 0).toLong()))
    }
    val canSelect = !loading && loadError == null && !pendingOrder
    val canConfirm = canSelect && hasStock && quantity in 1..MaxTicketsPerOrder
    val orderFeedback = if (pendingOrder && !submitting) R.string.event_order_unknown else purchaseError
    val goBack: () -> Unit = {
        if (!submitting) {
            if (checkout && !pendingOrder) checkout = false else onBack()
        }
    }
    BackHandler(onBack = goBack)

    val submitOrder: () -> Unit = confirm@{
        if (!choosingPayment || !canConfirm || pendingOrder || loading) return@confirm
        val items = localities.mapNotNull { locality ->
            quantities[locality.id]?.let { OrderItem(locality.id, it) }
        }
        if (items.sumOf { it.quantity } !in 1..MaxTicketsPerOrder) return@confirm
        choosingPayment = false
        purchaseError = null
        pendingOrder = true
        submitting = true
        scope.launch {
            try {
                val confirmedOrder = onBuyTickets(eventId, items)
                receiptTitle = currentEvent?.title
                order = confirmedOrder
                pendingOrder = false
                onCatalogChanged()
            } catch (error: AuthApiException) {
                when (error.status) {
                    404, 409 -> {
                        pendingOrder = false
                        purchaseError = R.string.event_order_stock_changed
                        reloadAvailability()
                        onCatalogChanged()
                    }
                    in 400..499 -> {
                        pendingOrder = false
                        purchaseError = when (error.status) {
                            401 -> R.string.event_order_session
                            403 -> R.string.event_order_forbidden
                            else -> R.string.event_order_failed
                        }
                    }
                }
            } catch (_: IOException) {
                // A lost response may follow a committed order.
            } finally {
                submitting = false
            }
        }
    }

    if (choosingPayment) {
        CheckoutPaymentSheet(
            email = email,
            total = stringResource(R.string.event_order_total, formatEventPrice(total)),
            canConfirm = canConfirm,
            onDismiss = { choosingPayment = false },
            onConfirm = submitOrder
        )
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val contentInset = maxOf(20.dp, (maxWidth - 620.dp) / 2)
            val body = Modifier.fillMaxWidth().padding(horizontal = contentInset)
            val coverHeight = minOf(maxWidth * 0.75f, 440.dp)
            val showCover = !checkout && !currentEvent?.coverUrl.isNullOrBlank()
            val showSkeleton = currentEvent == null && loading
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                EventDetailHeader(
                    title = when {
                        checkout -> R.string.event_checkout_title
                        else -> R.string.event_detail_title
                    },
                    backEnabled = !submitting,
                    onBack = goBack
                )
                key(checkout) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(
                            top = if (showCover || showSkeleton) 0.dp else 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when {
                            currentEvent != null -> {
                                if (showCover) {
                                    item {
                                        PublicEventCover(
                                            currentEvent.coverUrl,
                                            Modifier.fillMaxWidth().height(coverHeight)
                                        )
                                    }
                                }
                                item {
                                    if (checkout) {
                                        Text(currentEvent.title, modifier = body, style = MaterialTheme.typography.headlineSmall)
                                    } else {
                                        EventOverview(currentEvent, body)
                                    }
                                }
                                loadError?.let { message ->
                                    item {
                                        Column(body) {
                                            EventFeedback(message)
                                            if (!pendingOrder) ReloadEventButton(::reloadAvailability)
                                        }
                                    }
                                }
                                item {
                                    Column(body, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            stringResource(R.string.organizer_localities),
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.semantics { heading() }
                                        )
                                        if (checkout) {
                                            Text(
                                                stringResource(R.string.event_order_limit, MaxTicketsPerOrder),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                if (localities.isEmpty()) {
                                    item { Text(stringResource(R.string.home_event_unavailable), modifier = body) }
                                }
                                items(localities, key = { it.id }) { locality ->
                                    val count = quantities[locality.id] ?: 0
                                    EventLocalityCard(
                                        modifier = body,
                                        locality = locality,
                                        quantity = count.takeIf { checkout },
                                        canAdd = canSelect && count < locality.available && quantity < MaxTicketsPerOrder,
                                        canRemove = canSelect && count > 0,
                                        onQuantityChanged = change@{ delta ->
                                            if (!canSelect || pendingOrder || loading) return@change
                                            val next = (quantities[locality.id] ?: 0) + delta
                                            if (next !in 0..locality.available ||
                                                (delta > 0 && quantities.values.sum() >= MaxTicketsPerOrder)
                                            ) return@change
                                            quantities = HashMap(quantities).apply {
                                                if (next == 0) remove(locality.id) else put(locality.id, next)
                                            }
                                        }
                                    )
                                }
                            }
                            loading -> item {
                                BuyerEventLoading(coverHeight, body)
                            }
                            else -> item {
                                Column(body) {
                                    EventFeedback(orderFeedback ?: loadError ?: R.string.event_detail_load_error)
                                    if (!pendingOrder && loadError != R.string.event_detail_gone) {
                                        ReloadEventButton(::reloadAvailability)
                                    }
                                }
                            }
                        }
                    }
                }

                when {
                    currentEvent != null && checkout -> EventAction(
                        label = if (submitting) R.string.event_order_submitting else R.string.event_order_submit,
                        enabled = canConfirm,
                        caption = if (hasStock) stringResource(R.string.event_order_simulated)
                        else stringResource(R.string.home_event_unavailable),
                        total = stringResource(R.string.event_order_total, formatEventPrice(total)),
                        feedback = orderFeedback,
                        onClick = { if (canConfirm) choosingPayment = true }
                    )
                    currentEvent != null -> EventAction(
                        label = R.string.event_buy,
                        enabled = canSelect && hasStock,
                        caption = stringResource(R.string.home_event_unavailable).takeUnless { hasStock },
                        onClick = { checkout = true }
                    )
                }
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().then(
                            if (currentEvent == null) Modifier.clearAndSetSemantics {} else Modifier
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailHeader(@StringRes title: Int, backEnabled: Boolean, onBack: () -> Unit) {
    Surface(modifier = Modifier.widthIn(max = 660.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 4.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, enabled = backEnabled) {
                    Icon(
                        painterResource(R.drawable.ic_verification_back),
                        contentDescription = stringResource(R.string.event_detail_back)
                    )
                }
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun EventOverview(event: PublicEvent, modifier: Modifier) {
    val context = LocalContext.current
    var mapFailed by remember(event.id) { mutableStateOf(false) }
    val address = event.address?.takeIf { it.isNotBlank() }
    val mapQuery = if (event.latitude != null && event.longitude != null) {
        "${event.latitude},${event.longitude}"
    } else {
        address
    }
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            event.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            parseOrganizerDate(event.startsAt)?.let {
                EventMetadata(R.drawable.ic_home_calendar, formatOrganizerDate(it))
            }
            address?.let { EventMetadata(R.drawable.ic_home_pin, it) }
            if (mapQuery != null) {
                OutlinedButton(
                    onClick = { mapFailed = !openVenueInMaps(context, mapQuery) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(painterResource(R.drawable.ic_google_maps), null, Modifier.size(24.dp))
                        Text(stringResource(R.string.home_ticket_open_map), textAlign = TextAlign.Center)
                    }
                }
            }
            if (mapFailed) EventFeedback(R.string.home_ticket_map_error)
        }
        event.description?.takeIf { it.isNotBlank() }?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = colors.surfaceVariant,
                contentColor = colors.onSurfaceVariant
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.event_about),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(it, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun EventLocalityCard(
    modifier: Modifier,
    locality: PublicTicketType,
    quantity: Int?,
    canAdd: Boolean,
    canRemove: Boolean,
    onQuantityChanged: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val inStock = locality.available > 0
    Box(modifier) {
        ProfilePanel {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer
                    ) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_home_ticket), null, Modifier.size(20.dp))
                        }
                    }
                    Text(locality.title, style = MaterialTheme.typography.titleMedium)
                }
                locality.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(color = colors.outlineVariant)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.event_locality_unit_price, formatEventPrice(locality.price)),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Surface(
                        modifier = Modifier.weight(1f, fill = false),
                        shape = MaterialTheme.shapes.small,
                        color = if (inStock) colors.primaryContainer else colors.surfaceVariant,
                        contentColor = if (inStock) colors.onPrimaryContainer else colors.onSurfaceVariant
                    ) {
                        Text(
                            if (inStock) {
                                pluralStringResource(R.plurals.event_locality_stock, locality.available, locality.available)
                            } else {
                                stringResource(R.string.event_locality_sold_out)
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if (quantity != null) {
                    Row(
                        Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuantityButton(
                            label = stringResource(R.string.event_quantity_remove, locality.title),
                            add = false,
                            enabled = canRemove,
                            onClick = { onQuantityChanged(-1) }
                        )
                        val countLabel = stringResource(R.string.event_quantity_selected, locality.title, quantity)
                        Text(
                            quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics {
                                contentDescription = countLabel
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                        QuantityButton(
                            label = stringResource(R.string.event_quantity_add, locality.title),
                            add = true,
                            enabled = canAdd,
                            onClick = { onQuantityChanged(1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityButton(label: String, add: Boolean, enabled: Boolean, onClick: () -> Unit) {
    OutlinedIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp).semantics { contentDescription = label }
    ) {
        val color = LocalContentColor.current
        Canvas(Modifier.size(16.dp)) {
            val stroke = 2.dp.toPx()
            drawLine(color, Offset(0f, center.y), Offset(size.width, center.y), stroke, StrokeCap.Round)
            if (add) drawLine(color, Offset(center.x, 0f), Offset(center.x, size.height), stroke, StrokeCap.Round)
        }
    }
}

@Composable
private fun EventFeedback(@StringRes message: Int) {
    Text(
        stringResource(message),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    )
}

@Composable
private fun ReloadEventButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(stringResource(R.string.home_catalog_refresh)) }
}

@Composable
private fun EventAction(
    @StringRes label: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
    caption: String? = null,
    total: String? = null,
    @StringRes feedback: Int? = null
) {
    Surface(modifier = Modifier.widthIn(max = 660.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider()
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (total != null) Text(total, style = MaterialTheme.typography.titleLarge)
                if (caption != null) Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (feedback != null) EventFeedback(feedback)
                Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(label))
                }
            }
        }
    }
}
