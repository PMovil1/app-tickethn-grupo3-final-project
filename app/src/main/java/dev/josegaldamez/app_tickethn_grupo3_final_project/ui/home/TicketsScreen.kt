package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.TicketHNWordmark
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import kotlin.random.Random

internal enum class TicketStatus(val label: Int, val emptyLabel: Int, val icon: Int) {
    Active(R.string.home_tickets_active, R.string.home_tickets_empty_active, R.drawable.ic_home_ticket),
    Past(R.string.home_tickets_past, R.string.home_tickets_empty_past, R.drawable.ic_home_calendar),
    Cancelled(R.string.home_tickets_cancelled, R.string.home_tickets_empty_cancelled, R.drawable.ic_ticket_cancelled)
}

internal enum class DemoTicketEvent(val title: Int, val city: HomeCity, val image: Int, day: Long) {
    Concert(R.string.home_event_concert, HomeCity.Tegucigalpa, R.drawable.home_concert, eventDay(2027, 3, 13)),
    Festival(R.string.home_event_festival, HomeCity.SanPedroSula, R.drawable.home_festival, eventDay(2027, 4, 9));

    val dateLabel = formatEventDay(day)
}

internal enum class DemoTicket(
    val event: DemoTicketEvent,
    @param:StringRes val venue: Int,
    @param:StringRes val time: Int,
    @param:StringRes val admission: Int,
    val section: String,
    val row: String,
    val seat: String,
    val accessCode: String,
    val status: TicketStatus
) {
    Concert(
        DemoTicketEvent.Concert, R.string.home_ticket_venue_concert, R.string.home_ticket_concert_time,
        R.string.home_ticket_general, "G-12", "18", "24", "DEMO-MOR-1824", TicketStatus.Active
    ),
    Festival(
        DemoTicketEvent.Festival, R.string.home_ticket_venue_festival, R.string.home_ticket_festival_time,
        R.string.home_ticket_vip, "VIP-A", "4", "12", "DEMO-VFH-0412", TicketStatus.Active
    )
}

@Composable
internal fun TicketsScreen(onTicket: (DemoTicket) -> Unit) {
    var status by rememberSaveable { mutableStateOf(TicketStatus.Active) }
    val colors = MaterialTheme.colorScheme
    val tickets = DemoTicket.entries.filter { it.status == status }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            TicketStatus.entries.forEach { item ->
                val selected = item == status
                FilterChip(
                    selected = selected,
                    onClick = { status = item },
                    label = {
                        val label = if (item == TicketStatus.Active) {
                            stringResource(item.label, DemoTicket.entries.count { it.status == item })
                        } else {
                            stringResource(item.label)
                        }
                        Text(label, style = MaterialTheme.typography.labelMedium)
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(item.icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.onPrimary,
                        selectedLeadingIconColor = colors.onPrimary
                    )
                )
            }
        }

        if (tickets.isEmpty()) {
            Text(
                stringResource(status.emptyLabel),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            tickets.forEach { ticket -> TicketPreviewCard(ticket, onClick = { onTicket(ticket) }) }
        }
    }
}

@Composable
private fun TicketPreviewCard(ticket: DemoTicket, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val vip = ticket.admission == R.string.home_ticket_vip
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        HomeCover(
            ticket.event.image,
            modifier = Modifier.fillMaxWidth().aspectRatio(3.1f)
        )
        Box(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(stringResource(ticket.event.title), style = MaterialTheme.typography.titleMedium)
                    EventMetadata(
                        R.drawable.ic_home_calendar,
                        stringResource(R.string.home_ticket_list_date, ticket.event.dateLabel, stringResource(ticket.time))
                    )
                    EventMetadata(R.drawable.ic_home_pin, ticketVenueLabel(ticket))
                    Text(
                        stringResource(ticket.admission),
                        color = if (vip) colors.onTertiaryContainer else colors.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.background(
                            if (vip) colors.tertiaryContainer else colors.primaryContainer,
                            CircleShape
                        ).padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                TicketPerforation(Modifier.width(1.dp).height(78.dp), vertical = true)
                TicketQr(ticket, size = 76.dp)
            }
            Box(Modifier.align(Alignment.TopStart).offset(x = (-7).dp, y = (-7).dp)
                .size(14.dp).background(colors.background, CircleShape))
            Box(Modifier.align(Alignment.TopEnd).offset(x = 7.dp, y = (-7).dp)
                .size(14.dp).background(colors.background, CircleShape))
        }
    }
}

@Composable
internal fun ticketVenueLabel(ticket: DemoTicket): String =
    stringResource(R.string.home_ticket_venue_label, stringResource(ticket.venue), stringResource(ticket.event.city.label))

@Composable
internal fun TicketPerforation(modifier: Modifier, vertical: Boolean = false) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        val start = if (vertical) Offset(center.x, 0f) else Offset(0f, center.y)
        val end = if (vertical) Offset(center.x, size.height) else Offset(size.width, center.y)
        drawLine(
            color, start, end,
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        )
    }
}

@Composable
internal fun TicketQr(ticket: DemoTicket, size: Dp) {
    val grid = 25
    // Decorative modules only. No payload or QR encoding.
    val modules = remember(ticket) {
        val random = Random(ticket.accessCode.hashCode())
        BooleanArray(grid * grid) { random.nextBoolean() }
    }
    val ink = Color(0xFF08132B)
    Canvas(Modifier.size(size).background(Color.White, RoundedCornerShape(6.dp)).padding(5.dp)) {
        val cell = this.size.width / grid
        for (y in 0 until grid) {
            for (x in 0 until grid) {
                val finderArea = (y < 8 && (x < 8 || x >= grid - 8)) || (x < 8 && y >= grid - 8)
                if (!finderArea && x != 8 && modules[y * grid + x]) {
                    drawRect(ink, Offset(x * cell, y * cell), Size(cell, cell))
                }
            }
        }
        for ((x, y) in listOf(0 to 0, (grid - 7) to 0, 0 to (grid - 7))) {
            drawRect(ink, Offset(x * cell, y * cell), Size(7 * cell, 7 * cell))
            drawRect(Color.White, Offset((x + 1) * cell, (y + 1) * cell), Size(5 * cell, 5 * cell))
            drawRect(ink, Offset((x + 2) * cell, (y + 2) * cell), Size(3 * cell, 3 * cell))
        }
    }
}
