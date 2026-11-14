package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEvent
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.EventCoverCache
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay

internal val PublicEvent.availablePrice: BigDecimal?
    get() = ticketTypes.orEmpty().filter { it.active && it.available > 0 }.minOfOrNull { it.price }

internal val PublicEvent.calendarDay: Long?
    get() = parseOrganizerDate(startsAt)?.let { millis ->
        val date = Calendar.getInstance(TimeZone.getTimeZone("America/Tegucigalpa"))
        date.timeInMillis = millis
        eventDay(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH))
    }

internal fun formatEventPrice(price: BigDecimal): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("es-HN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(price)

@Composable
internal fun PublicEventCover(
    url: String?,
    modifier: Modifier = Modifier,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    placeholderAlignment: Alignment = Alignment.Center
) {
    val context = LocalContext.current.applicationContext
    var preview by remember(url) { mutableStateOf(url?.let(EventCoverCache::get)) }
    LaunchedEffect(context, url) {
        if (!url.isNullOrBlank() && preview == null) {
            preview = EventCoverCache.load(context, url)
        }
    }
    Box(modifier.background(placeholderColor), contentAlignment = Alignment.Center) {
        val bitmap = preview
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(
                painterResource(R.drawable.ic_home_calendar),
                contentDescription = null,
                modifier = Modifier.align(placeholderAlignment).padding(horizontal = 24.dp).size(26.dp)
            )
        }
    }
}

@Composable
internal fun FeaturedEvents(
    events: List<PublicEvent>,
    autoAdvance: Boolean,
    onEvent: (PublicEvent) -> Unit
) {
    val pager = rememberPagerState(pageCount = { events.size })
    val dragging by pager.interactionSource.collectIsDraggedAsState()
    LaunchedEffect(pager, events.size, pager.settledPage, dragging, autoAdvance) {
        if (events.size > 1 && autoAdvance && !dragging) {
            while (true) {
                delay(5_000L)
                if (!pager.isScrollInProgress) {
                    val nextPage = (pager.settledPage + 1) % events.size
                    pager.animateScrollToPage(nextPage, animationSpec = tween(350))
                }
            }
        }
    }
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth(), key = { events[it].id }) { index ->
            val event = events[index]
            FeaturedEventCard(event) { onEvent(event) }
        }
        if (events.size > 1) {
            val description = stringResource(R.string.home_featured_position, pager.currentPage + 1, events.size)
            Row(
                Modifier.semantics { contentDescription = description },
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                events.indices.forEach { index ->
                    Box(Modifier.size(7.dp).background(
                        if (pager.currentPage == index) colors.primary else colors.outlineVariant, CircleShape
                    ))
                }
            }
        }
    }
}

@Composable
private fun FeaturedEventCard(event: PublicEvent, onClick: () -> Unit) {
    val navy = Color(0xFF0B1F4D)
    val metadataColor = Color(0xFFE1E9FA)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = navy,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 168.dp)) {
            PublicEventCover(
                event.coverUrl,
                Modifier.matchParentSize(),
                placeholderColor = navy,
                placeholderAlignment = Alignment.CenterStart
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.40f to navy.copy(alpha = 0.10f),
                        0.55f to navy.copy(alpha = 0.94f),
                        1f to navy
                    )
                )
            )
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.49f)
                    .padding(start = 4.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 19.sp, lineHeight = 22.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                val day = remember(event.startsAt) { event.calendarDay }
                day?.let {
                    EventMetadata(R.drawable.ic_home_calendar, formatEventDay(it, short = true), metadataColor)
                }
                event.address?.takeIf { it.isNotBlank() }?.let {
                    EventMetadata(R.drawable.ic_home_pin, it, metadataColor, maxLines = 2)
                }
                Text(
                    eventPriceLabel(event),
                    modifier = Modifier.background(Color(0xFF007F5E), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
internal fun HomeEventRow(events: List<PublicEvent>, onEvent: (PublicEvent) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        events.forEach { event ->
            Card(
                onClick = { onEvent(event) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                PublicEventCover(event.coverUrl, Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PublicEventInfo(event, compact = true)
                }
            }
        }
        if (events.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
internal fun ExploreEventCard(event: PublicEvent, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PublicEventCover(event.coverUrl, Modifier.width(96.dp).aspectRatio(1.08f).clip(RoundedCornerShape(11.dp)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                PublicEventInfo(event)
            }
        }
    }
}

@Composable
private fun PublicEventInfo(event: PublicEvent, compact: Boolean = false) {
    Text(
        event.title,
        style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
        minLines = if (compact) 2 else 1,
        maxLines = if (compact) 2 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis
    )
    val day = remember(event.startsAt) { event.calendarDay }
    day?.let { EventMetadata(R.drawable.ic_home_calendar, formatEventDay(it, short = compact)) }
    event.address?.takeIf { it.isNotBlank() }?.let {
        EventMetadata(R.drawable.ic_home_pin, it, maxLines = if (compact) 1 else 2)
    }
    Text(
        eventPriceLabel(event),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
private fun eventPriceLabel(event: PublicEvent): String {
    val price = event.availablePrice
    return when {
        price == null -> stringResource(R.string.home_event_unavailable)
        price.signum() == 0 -> stringResource(R.string.home_filter_free)
        else -> stringResource(R.string.home_from_price, formatEventPrice(price))
    }
}

@Composable
internal fun EventMetadata(
    @DrawableRes icon: Int,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = Int.MAX_VALUE
) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Top) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(15.dp), tint = color)
        Text(label, color = color, style = MaterialTheme.typography.bodySmall, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

