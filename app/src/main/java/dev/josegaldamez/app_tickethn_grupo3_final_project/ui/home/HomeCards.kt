package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R

internal enum class HomeCategory(@param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    Concerts(R.string.home_concerts, R.drawable.ic_home_music),
    Festivals(R.string.home_festivals, R.drawable.ic_home_festival),
    Sports(R.string.home_sports, R.drawable.ic_home_ball),
    Theater(R.string.home_theater, R.drawable.ic_home_theater)
}

internal enum class HomeEvent(
    @param:StringRes val title: Int,
    val dateMillis: Long,
    val city: HomeCity,
    @param:StringRes val type: Int,
    @param:DrawableRes val image: Int,
    val category: HomeCategory,
    val price: Int,
    @param:StringRes val featuredTitle: Int = title
) {
    Concert(
        R.string.home_event_concert, eventDay(2027, 3, 13), HomeCity.Tegucigalpa,
        R.string.home_concert_category, R.drawable.home_concert, HomeCategory.Concerts, 850,
        featuredTitle = R.string.home_featured_concert_title
    ),
    Festival(
        R.string.home_event_festival, eventDay(2027, 4, 9), HomeCity.SanPedroSula,
        R.string.home_festival_category, R.drawable.home_festival, HomeCategory.Festivals, 500,
        featuredTitle = R.string.home_featured_festival_title
    ),
    Football(
        R.string.home_event_football, eventDay(2027, 3, 25), HomeCity.Tegucigalpa,
        R.string.home_sports_category, R.drawable.home_football, HomeCategory.Sports, 350
    ),
    AcousticNight(
        R.string.home_event_acoustic_night, eventDay(2027, 3, 13), HomeCity.LaCeiba,
        R.string.home_concert_category, R.drawable.home_concert, HomeCategory.Concerts, 1_000
    ),
    FoodFestival(
        R.string.home_event_food_festival, eventDay(2027, 3, 20), HomeCity.Comayagua,
        R.string.home_festival_category, R.drawable.home_festival, HomeCategory.Festivals, 0
    ),
    ValleyStories(
        R.string.home_event_valley_stories, eventDay(2027, 3, 20), HomeCity.Tegucigalpa,
        R.string.home_theater, R.drawable.home_theater_cover, HomeCategory.Theater, 450
    ),
    PortFootball(
        R.string.home_event_port_football, eventDay(2027, 3, 27), HomeCity.PuertoCortes,
        R.string.home_sports_category, R.drawable.home_football, HomeCategory.Sports, 300
    ),
    CortesNights(
        R.string.home_event_cortes_nights, eventDay(2027, 4, 9), HomeCity.SanPedroSula,
        R.string.home_concert_category, R.drawable.home_concert, HomeCategory.Concerts, 1_800
    ),
    CoastalFestival(
        R.string.home_event_coastal_festival, eventDay(2027, 4, 17), HomeCity.LaCeiba,
        R.string.home_festival_category, R.drawable.home_festival, HomeCategory.Festivals, 3_200
    ),
    ComedyNight(
        R.string.home_event_comedy_night, eventDay(2027, 4, 24), HomeCity.Danli,
        R.string.home_theater, R.drawable.home_theater_cover, HomeCategory.Theater, 650
    );

    val dateLabel = formatEventDay(dateMillis)
}

@Composable
internal fun HomeEvent.priceLabel(): String =
    if (price == 0) stringResource(R.string.home_filter_free) else stringResource(R.string.home_from_price, price)

@Composable
internal fun FeaturedEvents(onEvent: (HomeEvent) -> Unit) {
    val events = listOf(HomeEvent.Concert, HomeEvent.Festival, HomeEvent.Football)
    val pager = rememberPagerState(pageCount = { events.size })
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { index ->
            FeaturedEvent(events[index], onClick = { onEvent(events[index]) })
        }
        val pageDescription = stringResource(R.string.home_featured_position, pager.currentPage + 1, events.size)
        Row(
            modifier = Modifier.semantics { contentDescription = pageDescription },
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            events.indices.forEach { index ->
                Box(
                    Modifier.size(7.dp).background(
                        if (pager.currentPage == index) colors.primary else colors.outlineVariant,
                        CircleShape
                    )
                )
            }
        }
    }
}

@Composable
private fun FeaturedEvent(event: HomeEvent, onClick: () -> Unit) {
    val navy = Color(0xFF0B1F4D)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = navy,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 168.dp)) {
            Image(
                painterResource(event.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
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
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        stringResource(event.type),
                        modifier = Modifier.background(Color(0xFF1553D5), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Text(
                        stringResource(event.featuredTitle),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 19.sp, lineHeight = 22.sp),
                        color = Color.White
                    )
                }
                val metadataColor = Color(0xFFE1E9FA)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    EventMetadata(R.drawable.ic_home_calendar, formatEventDay(event.dateMillis, short = true), metadataColor)
                    EventMetadata(R.drawable.ic_home_pin, stringResource(event.city.label), metadataColor)
                }
                Text(
                    event.priceLabel(),
                    modifier = Modifier.background(Color(0xFF007F5E), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
internal fun EventRow(events: List<HomeEvent>, onEvent: (HomeEvent) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = 10.dp
        val coverHeight = (maxWidth - gap) / 2f / 2.55f
        Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.fillMaxWidth()) {
            events.forEach { event ->
                EventCard(event, coverHeight, onClick = { onEvent(event) }, modifier = Modifier.weight(1f))
            }
            if (events.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun EventCard(event: HomeEvent, coverHeight: Dp, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Image(
            painterResource(event.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(coverHeight).clip(RoundedCornerShape(15.dp))
        )
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                stringResource(event.title),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, lineHeight = 18.sp),
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            EventMetadata(R.drawable.ic_home_calendar, event.dateLabel, maxLines = 1)
            EventMetadata(R.drawable.ic_home_pin, stringResource(event.city.label), maxLines = 1)
        }
    }
}

@Composable
internal fun ExploreEventCard(event: HomeEvent, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(9.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(event.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = if (event.image == R.drawable.home_concert) Alignment.CenterStart else Alignment.Center,
                modifier = Modifier.width(108.dp).aspectRatio(1.08f).clip(RoundedCornerShape(11.dp))
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(event.title), style = MaterialTheme.typography.titleMedium)
                EventMetadata(R.drawable.ic_home_calendar, event.dateLabel)
                EventMetadata(R.drawable.ic_home_pin, stringResource(event.city.label))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(event.type),
                        modifier = Modifier.background(colors.secondaryContainer, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        color = colors.onSecondaryContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        event.priceLabel(),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
internal fun EventMetadata(
    @DrawableRes icon: Int,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = Int.MAX_VALUE
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(15.dp), tint = color)
        Text(label, color = color, style = MaterialTheme.typography.bodySmall, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun NextTicket(onClick: () -> Unit) {
    val ticket = DemoTicket.Concert
    val event = ticket.event
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.secondaryContainer.copy(alpha = 0.40f),
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(34.dp).background(colors.secondaryContainer, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_home_ticket),
                            contentDescription = null,
                            tint = colors.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        stringResource(event.title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                EventMetadata(
                    R.drawable.ic_home_calendar,
                    stringResource(R.string.home_ticket_list_date, event.dateLabel, stringResource(ticket.time))
                )
                EventMetadata(R.drawable.ic_home_pin, ticketVenueLabel(ticket))
            }
            TicketPerforation(Modifier.width(1.dp).height(70.dp), vertical = true)
            TicketQr(ticket, size = 54.dp)
            Icon(
                painterResource(R.drawable.ic_home_next),
                contentDescription = stringResource(R.string.home_open_ticket),
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
