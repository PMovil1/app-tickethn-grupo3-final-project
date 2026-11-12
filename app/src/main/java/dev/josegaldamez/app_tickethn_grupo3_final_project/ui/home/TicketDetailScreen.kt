package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val FallbackTicketHue = 222f

@Composable
internal fun TicketDetailScreen(ticket: DemoTicket, onClose: () -> Unit) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val venue = ticketVenueLabel(ticket)
    var mapFailed by rememberSaveable(ticket) { mutableStateOf(false) }
    val resources = context.resources
    val cover = ticket.event.image
    val hue by produceState(FallbackTicketHue, resources, cover) {
        value = withContext(Dispatchers.Default) { dominantTicketHue(resources, cover) }
    }
    val backgroundEdge by animateColorAsState(Color.hsv(hue, 0.70f, 0.23f), tween(220), label = "Ticket edge")
    val backgroundCenter by animateColorAsState(Color.hsv(hue, 0.57f, 0.40f), tween(220), label = "Ticket center")

    BackHandler(onBack = onClose)
    TicketSystemBars()

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(backgroundEdge, backgroundCenter, backgroundEdge)))
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp).fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        painterResource(R.drawable.ic_home_clear),
                        contentDescription = stringResource(R.string.access_close),
                        tint = Color.White
                    )
                }
                Text(
                    stringResource(R.string.home_ticket_detail),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 50.dp).semantics { heading() }
                )
            }
            ScreenHeaderDivider(color = Color.White.copy(alpha = 0.22f))
            Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surface,
                    contentColor = colors.onSurface,
                    shadowElevation = 5.dp
                ) {
                    Column {
                        TicketCover(ticket)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EventMetadata(R.drawable.ic_home_calendar, ticket.event.dateLabel, colors.onSurface)
                            EventMetadata(R.drawable.ic_verification_clock, stringResource(ticket.time), colors.onSurface)
                            EventMetadata(R.drawable.ic_home_pin, venue, colors.onSurface)
                        }
                        Box(Modifier.fillMaxWidth().height(16.dp), contentAlignment = Alignment.Center) {
                            TicketPerforation(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp))
                            Box(Modifier.align(Alignment.CenterStart).size(13.dp).background(backgroundCenter, CircleShape))
                            Box(Modifier.align(Alignment.CenterEnd).size(13.dp).background(backgroundCenter, CircleShape))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TicketSeatValue(R.string.home_ticket_admission, stringResource(ticket.admission), Modifier.weight(1.2f))
                            TicketSeatValue(R.string.home_ticket_section, ticket.section, Modifier.weight(1f))
                            TicketSeatValue(R.string.home_ticket_row, ticket.row, Modifier.weight(0.7f))
                            TicketSeatValue(R.string.home_ticket_seat, ticket.seat, Modifier.weight(0.9f))
                        }
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TicketWaves(Modifier.matchParentSize())
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TicketQr(ticket, size = 168.dp)
                                Text(
                                    stringResource(R.string.home_ticket_access_code),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(ticket.accessCode, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.home_mock_ticket),
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            if (mapFailed) {
                Text(
                    stringResource(R.string.home_ticket_map_error),
                    color = Color.White,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            Button(
                onClick = { mapFailed = !openVenueInMaps(context, venue) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = backgroundEdge)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_home_pin), contentDescription = null, modifier = Modifier.size(19.dp))
                    Text(stringResource(R.string.home_ticket_open_map), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun dominantTicketHue(resources: Resources, @DrawableRes cover: Int): Float {
    val options = BitmapFactory.Options().apply {
        inSampleSize = 8
        inScaled = false
    }
    val bitmap = BitmapFactory.decodeResource(resources, cover, options) ?: return FallbackTicketHue
    val weights = FloatArray(24)
    val hueSums = FloatArray(weights.size)
    val hsv = FloatArray(3)

    try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixel in pixels) {
            if (AndroidColor.alpha(pixel) < 128) continue
            AndroidColor.colorToHSV(pixel, hsv)
            val (hue, saturation, brightness) = hsv
            if (saturation < 0.25f || brightness < 0.20f) continue
            val bin = (hue / 15f).toInt().coerceAtMost(weights.lastIndex)
            val weight = saturation * brightness * brightness
            weights[bin] += weight
            hueSums[bin] += hue * weight
        }
    } finally {
        bitmap.recycle()
    }

    val best = weights.indices.maxBy { weights[it] }
    return if (weights[best] > 0f) hueSums[best] / weights[best] else FallbackTicketHue
}

@Composable
private fun TicketCover(ticket: DemoTicket) {
    Box(Modifier.fillMaxWidth().aspectRatio(2.1f)) {
        Image(
            painterResource(ticket.event.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = if (ticket == DemoTicket.Concert) Alignment.CenterStart else Alignment.Center,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xE9091429)), startY = 50f)
            )
        )
        Text(
            stringResource(ticket.event.title),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 25.sp, lineHeight = 28.sp),
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }
}

@Composable
private fun TicketSeatValue(label: Int, value: String, modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(value, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TicketWaves(modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val wave = Path().apply {
            moveTo(0f, height * 0.72f)
            cubicTo(width * 0.27f, height * 0.76f, width * 0.32f, height * 0.96f, width * 0.60f, height * 0.91f)
            cubicTo(width * 0.78f, height * 0.89f, width * 0.86f, height * 0.70f, width, height * 0.65f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(wave, color = colors.primaryContainer.copy(alpha = 0.5f))
    }
}

private fun openVenueInMaps(context: Context, venue: String): Boolean {
    val url = Uri.Builder().scheme("https").authority("www.google.com")
        .path("/maps/search/")
        .appendQueryParameter("api", "1").appendQueryParameter("query", "$venue, Honduras")
        .build()
    val mapsIntent = Intent(Intent.ACTION_VIEW, url).setPackage("com.google.android.apps.maps")
    return try {
        context.startActivity(mapsIntent)
        true
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}

@Composable
private fun TicketSystemBars() {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    DisposableEffect(window) {
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val lightStatus = controller?.isAppearanceLightStatusBars
        val lightNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (lightStatus != null) controller?.isAppearanceLightStatusBars = lightStatus
            if (lightNavigation != null) controller?.isAppearanceLightNavigationBars = lightNavigation
        }
    }
}
