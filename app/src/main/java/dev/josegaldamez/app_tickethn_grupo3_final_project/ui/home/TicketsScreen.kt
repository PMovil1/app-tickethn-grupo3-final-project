package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.BuyerTicket
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEvent
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.TicketHNWordmark

internal enum class TicketStatus(val label: Int, val emptyLabel: Int, val icon: Int) {
    All(R.string.home_tickets_all, R.string.home_tickets_empty, R.drawable.ic_home_all_categories),
    Active(R.string.home_tickets_active, R.string.home_tickets_empty_active, R.drawable.ic_home_ticket),
    Past(R.string.home_tickets_past, R.string.home_tickets_empty_past, R.drawable.ic_home_calendar),
    Cancelled(R.string.home_tickets_cancelled, R.string.home_tickets_empty_cancelled, R.drawable.ic_ticket_cancelled)
}

internal fun BuyerTicket.status(now: Long = System.currentTimeMillis()): TicketStatus? {
    if (orderStatus == "cancelled" || eventStatus == "cancelled") return TicketStatus.Cancelled
    if (usedAt != null) return TicketStatus.Past
    val starts = parseOrganizerDate(eventStartsAt) ?: return null
    return if (starts <= now) TicketStatus.Past else TicketStatus.Active
}

internal fun LazyListScope.ticketItems(
    tickets: List<BuyerTicket>?,
    catalog: List<PublicEvent>,
    failed: Boolean,
    status: TicketStatus,
    onStatus: (TicketStatus) -> Unit,
    onRetry: () -> Unit,
    onTicket: (BuyerTicket) -> Unit
) {
    val events = catalog.associateBy { it.id }
    val allTickets = tickets.orEmpty().map { ticket ->
        val event = events[ticket.eventId]
        if (event == null) ticket else ticket.copy(
            eventTitle = ticket.eventTitle ?: event.title,
            eventAddress = ticket.eventAddress ?: event.address,
            eventStartsAt = ticket.eventStartsAt ?: event.startsAt,
            eventCoverUrl = ticket.eventCoverUrl ?: event.coverUrl,
            ticketTypeTitle = ticket.ticketTypeTitle ?: event.ticketTypes.orEmpty()
                .firstOrNull { it.id == ticket.ticketTypeId }?.title
        )
    }
    val now = System.currentTimeMillis()
    val visibleTickets = if (status == TicketStatus.All) allTickets else allTickets.filter { it.status(now) == status }
    item(key = "tickets_header") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            TicketHNWordmark(Modifier.padding(vertical = 6.dp))
            ScreenHeaderDivider()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.home_my_tickets),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 29.sp, lineHeight = 34.sp),
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    stringResource(R.string.home_tickets_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (tickets != null) {
                TicketFilters(status, allTickets.count { it.status(now) == TicketStatus.Active }, onStatus)
            }
        }
    }
    when {
        failed -> item {
            Column {
                ProfileFeedback(R.string.home_tickets_error)
                TextButton(onClick = onRetry) { Text(stringResource(R.string.onboarding_retry)) }
            }
        }
        tickets == null -> item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(stringResource(R.string.home_tickets_loading))
            }
        }
        visibleTickets.isEmpty() -> item {
            Text(
                stringResource(status.emptyLabel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
    itemsIndexed(visibleTickets, key = { _, ticket -> ticket.id }) { index, ticket ->
        Column {
            if (index > 0) {
                val differentEvent = visibleTickets[index - 1].eventId != ticket.eventId
                Box(
                    modifier = Modifier.fillMaxWidth().height(if (differentEvent) 48.dp else 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val colors = MaterialTheme.colorScheme
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        thickness = if (differentEvent) 2.dp else 1.dp,
                        color = if (differentEvent) colors.outline else colors.outlineVariant
                    )
                }
            }
            TicketPreviewCard(ticket, onClick = { onTicket(ticket) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketFilters(status: TicketStatus, activeCount: Int, onStatus: (TicketStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val labels = TicketStatus.entries.associateWith {
        if (it == TicketStatus.Active) stringResource(it.label, activeCount) else stringResource(it.label)
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = labels.getValue(status),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.home_tickets_filter)) },
            leadingIcon = { Icon(painterResource(status.icon), null, Modifier.size(20.dp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TicketStatus.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(labels.getValue(item)) },
                    leadingIcon = { Icon(painterResource(item.icon), null, Modifier.size(20.dp)) },
                    trailingIcon = { RadioButton(selected = item == status, onClick = null) },
                    onClick = {
                        expanded = false
                        onStatus(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun TicketPreviewCard(ticket: BuyerTicket, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val title = ticket.eventTitle ?: stringResource(R.string.home_ticket_detail)
    val locality = ticket.ticketTypeTitle?.takeIf { it.isNotBlank() }
    val hasCover = !ticket.eventCoverUrl.isNullOrBlank()
    val cancelled = ticket.status() == TicketStatus.Cancelled
    val statusLabel = when {
        cancelled -> stringResource(R.string.home_ticket_cancelled)
        ticket.usedAt != null -> stringResource(R.string.home_ticket_used)
        else -> null
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        if (hasCover) {
            TicketCardCover(rememberEventCover(ticket.eventCoverUrl), title, ticket) { navy ->
                TicketCardBadges(locality, statusLabel, cancelled, coverBackground = navy)
            }
        } else {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (locality != null || statusLabel != null) {
                    TicketCardBadges(locality, statusLabel, cancelled)
                }
                TicketCardMetadata(ticket)
            }
        }
        Box(Modifier.fillMaxWidth().background(colors.surfaceVariant)) {
            TicketPerforation(Modifier.fillMaxWidth().padding(horizontal = 7.dp).height(1.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.home_ticket_folio),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    Text(ticket.code, style = MaterialTheme.typography.titleSmall)
                }
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.home_ticket_view),
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary
                    )
                    Icon(painterResource(R.drawable.ic_home_next), null, Modifier.size(18.dp), tint = colors.primary)
                }
            }
            Box(Modifier.align(Alignment.TopStart).offset(x = (-7).dp, y = (-7).dp)
                .size(14.dp).background(colors.background, CircleShape))
            Box(Modifier.align(Alignment.TopEnd).offset(x = 7.dp, y = (-7).dp)
                .size(14.dp).background(colors.background, CircleShape))
        }
    }
}

@Composable
private fun TicketCardCover(
    bitmap: Bitmap?,
    title: String,
    ticket: BuyerTicket,
    badges: @Composable (Color) -> Unit
) {
    val navy = Color(0xFF0B1F4D)
    val titleGradient = Brush.verticalGradient(
        colors = listOf(navy.copy(alpha = 0f), navy.copy(alpha = 0.94f)),
        endY = with(LocalDensity.current) { 30.dp.toPx() }
    )
    BoxWithConstraints(Modifier.fillMaxWidth().background(navy)) {
        val coverHeight = maxWidth / bitmap.ticketCoverRatio
        TicketEventCover(bitmap, backgroundColor = navy)
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = coverHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 12.dp)) {
                badges(navy)
            }
            Column(
                modifier = Modifier.fillMaxWidth().background(titleGradient)
                    .padding(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                TicketCardMetadata(ticket, color = Color.White)
            }
        }
    }
}

@Composable
private fun TicketCardBadges(locality: String?, statusLabel: String?, cancelled: Boolean, coverBackground: Color? = null) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, if (coverBackground == null) Alignment.Start else Alignment.End),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (statusLabel != null) {
            Text(
                statusLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (cancelled) colors.onErrorContainer else colors.onSurfaceVariant,
                modifier = Modifier.background(
                    if (cancelled) colors.errorContainer else colors.surfaceVariant, CircleShape
                ).padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        if (locality != null) {
            TicketLocalityBadge(
                locality,
                coverBackground ?: colors.primaryContainer,
                if (coverBackground == null) colors.onPrimaryContainer else Color.White
            )
        }
    }
}

@Composable
private fun TicketLocalityBadge(locality: String, background: Color, foreground: Color) {
    Row(
        Modifier.background(background, CircleShape).padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_home_ticket), null, Modifier.size(15.dp), tint = foreground)
        Text(locality, color = foreground, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TicketCardMetadata(ticket: BuyerTicket, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    val details = listOfNotNull(
        parseOrganizerDate(ticket.eventStartsAt)?.let { R.drawable.ic_home_calendar to formatOrganizerDate(it) },
        ticket.eventAddress?.takeIf { it.isNotBlank() }?.let { R.drawable.ic_home_pin to it }
    )
    if (details.isEmpty()) return

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 300.dp && LocalDensity.current.fontScale <= 1.3f) 2 else 1
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            maxItemsInEachRow = columns
        ) {
            details.forEach { (icon, label) ->
                Box(Modifier.weight(1f)) {
                    EventMetadata(icon, label, color)
                }
            }
        }
    }
}

private val Bitmap?.ticketCoverRatio: Float
    get() = this?.let { it.width.toFloat() / it.height } ?: 2f

@Composable
internal fun TicketEventCover(
    bitmap: Bitmap?,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    EventCoverImage(
        bitmap,
        modifier = Modifier.fillMaxWidth().aspectRatio(bitmap.ticketCoverRatio),
        placeholderColor = backgroundColor,
        contentScale = ContentScale.Fit
    )
}

@Composable
internal fun TicketPerforation(modifier: Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        drawLine(
            color, Offset(0f, center.y), Offset(size.width, center.y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        )
    }
}
