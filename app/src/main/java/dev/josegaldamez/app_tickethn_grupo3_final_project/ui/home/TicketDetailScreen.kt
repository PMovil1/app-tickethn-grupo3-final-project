package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.SystemClock
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.BuyerTicket
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val TicketQrSize = 184.dp
private const val TicketCacheFreshnessMillis = 60_000L

internal class LoadedTicket(val ticket: BuyerTicket, val qrImage: ImageBitmap?, val loadedAt: Long)

@Composable
internal fun TicketDetailScreen(
    ticket: BuyerTicket,
    onClose: () -> Unit,
    onReadTicket: suspend (String) -> BuyerTicket,
    ticketCache: LruCache<String, LoadedTicket>
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    var detail by remember(ticket.id, ticketCache) {
        mutableStateOf(ticketCache.get(ticket.id)?.takeIf {
            SystemClock.elapsedRealtime() - it.loadedAt < TicketCacheFreshnessMillis
        })
    }
    var refreshed by remember(ticket.id) { mutableStateOf(false) }
    var loadFailed by remember(ticket.id) { mutableStateOf(false) }
    var retry by remember(ticket.id) { mutableStateOf(0) }
    var mapFailed by rememberSaveable(ticket.id) { mutableStateOf(false) }
    LaunchedEffect(ticket.id, retry) {
        loadFailed = false
        try {
            val latest = onReadTicket(ticket.id)
            val previous = ticketCache.get(ticket.id)
            val payload = latest.qrPayload
            val qrImage = when {
                latest.status() == TicketStatus.Cancelled || latest.usedAt != null || payload.isNullOrBlank() -> null
                previous != null && previous.ticket.qrPayload == payload && previous.qrImage != null -> previous.qrImage
                else -> withContext(Dispatchers.Default) { encodeTicketQr(payload) }
            }
            val loaded = LoadedTicket(latest, qrImage, SystemClock.elapsedRealtime())
            detail = loaded
            refreshed = true
            ticketCache.put(ticket.id, loaded)
        } catch (_: IOException) {
            loadFailed = true
        } catch (error: AuthApiException) {
            if (error.status == 401 || error.status == 403 || error.status == 404) {
                ticketCache.remove(ticket.id)
                detail = null
            }
            loadFailed = true
        }
    }
    val current = detail?.ticket ?: ticket
    val qrImage = detail?.qrImage
    val cancelled = current.status() == TicketStatus.Cancelled ||
        (!refreshed && ticket.status() == TicketStatus.Cancelled)
    val used = current.usedAt != null || (!refreshed && ticket.usedAt != null)
    val venue = current.eventAddress ?: ticket.eventAddress
    val cover = current.eventCoverUrl ?: ticket.eventCoverUrl
    val coverBitmap = rememberEventCover(cover)
    val gradient by produceState(DefaultTicketGradient, cover, coverBitmap) {
        value = if (coverBitmap == null) DefaultTicketGradient else {
            withContext(Dispatchers.Default) { ticketGradientFrom(coverBitmap) }
        }
    }
    val backgroundEdge by animateColorAsState(gradient.edge, tween(220), label = "Ticket gradient edge")
    val backgroundCenter by animateColorAsState(gradient.center, tween(220), label = "Ticket gradient center")

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
                        if (!cover.isNullOrBlank()) {
                            TicketEventCover(coverBitmap, backgroundColor = backgroundEdge)
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                current.eventTitle ?: ticket.eventTitle ?: stringResource(R.string.home_ticket_detail),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.semantics { heading() }
                            )
                            parseOrganizerDate(current.eventStartsAt ?: ticket.eventStartsAt)?.let {
                                EventMetadata(R.drawable.ic_home_calendar, formatOrganizerDate(it))
                            }
                            venue?.let { EventMetadata(R.drawable.ic_home_pin, it) }
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
                            val locality = current.ticketTypeTitle ?: ticket.ticketTypeTitle
                            if (locality != null) {
                                TicketField(R.string.home_ticket_admission, locality, Modifier.weight(1f))
                            }
                            TicketField(
                                R.string.home_ticket_price,
                                stringResource(R.string.home_ticket_price_value, formatEventPrice(current.unitPrice)),
                                Modifier.weight(1f)
                            )
                        }
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TicketWaves(Modifier.matchParentSize())
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                when {
                                    cancelled ->
                                        Text(stringResource(R.string.home_tickets_cancelled))
                                    used -> Text(stringResource(R.string.home_ticket_used))
                                    qrImage != null -> TicketQr(qrImage)
                                    loadFailed -> {
                                        ProfileFeedback(R.string.home_ticket_error)
                                        TextButton(onClick = { retry++ }) { Text(stringResource(R.string.onboarding_retry)) }
                                    }
                                    detail == null -> TicketQrSkeleton()
                                    else -> Text(stringResource(R.string.home_ticket_qr_unavailable))
                                }
                                Text(
                                    stringResource(R.string.home_ticket_access_code),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(current.code, style = MaterialTheme.typography.titleMedium)
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
            if (!venue.isNullOrBlank()) Button(
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

private data class TicketGradient(val edge: Color, val center: Color)

private val DefaultTicketGradient = TicketGradient(Color(0xFF0B1F4D), Color(0xFF244780))

private fun ticketGradientFrom(bitmap: Bitmap): TicketGradient {
    val hueWeights = FloatArray(24)
    val hueOffsets = FloatArray(hueWeights.size)
    val saturationSums = FloatArray(hueWeights.size)
    val hsv = FloatArray(3)
    val columns = minOf(bitmap.width, 48)
    val rows = minOf(bitmap.height, 48)
    var sampled = 0
    var brightnessSum = 0f
    var colored = 0
    for (row in 0 until rows) {
        val y = row * bitmap.height / rows
        for (column in 0 until columns) {
            val x = column * bitmap.width / columns
            val pixel = bitmap.getPixel(x, y)
            if (AndroidColor.alpha(pixel) < 128) continue
            AndroidColor.colorToHSV(pixel, hsv)
            val (hue, saturation, brightness) = hsv
            sampled++
            brightnessSum += brightness
            if (saturation < 0.18f || brightness < 0.12f) continue
            colored++
            val nearest = (hue / 15f).roundToInt()
            val bucket = nearest % hueWeights.size
            val weight = 0.65f + 0.35f * saturation
            hueWeights[bucket] += weight
            hueOffsets[bucket] += (hue - nearest * 15f) * weight
            saturationSums[bucket] += saturation * weight
        }
    }
    if (sampled == 0) return DefaultTicketGradient
    val brightness = brightnessSum / sampled
    if (colored < sampled * 0.08f) {
        return TicketGradient(
            edge = Color.hsv(0f, 0f, 0.12f + brightness * 0.09f),
            center = Color.hsv(0f, 0f, 0.24f + brightness * 0.13f)
        )
    }
    val best = hueWeights.indices.filter { hueWeights[it] > 0f }.maxBy { bucket ->
        val previous = (bucket + hueWeights.lastIndex) % hueWeights.size
        val next = (bucket + 1) % hueWeights.size
        hueWeights[bucket] + (hueWeights[previous] + hueWeights[next]) * 0.5f
    }
    val hue = (best * 15f + hueOffsets[best] / hueWeights[best] + 360f) % 360f
    val saturation = (saturationSums[best] / hueWeights[best]).coerceIn(0.38f, 0.86f)
    return TicketGradient(
        edge = Color.hsv(hue, saturation, 0.18f + brightness * 0.06f),
        center = Color.hsv(hue, saturation, 0.36f + brightness * 0.09f)
    )
}

private fun encodeTicketQr(payload: String): ImageBitmap? = try {
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 320, 320)
    val pixels = IntArray(matrix.width * matrix.height) { index ->
        if (matrix[index % matrix.width, index / matrix.width]) AndroidColor.BLACK else AndroidColor.WHITE
    }
    Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888).asImageBitmap()
} catch (_: WriterException) {
    null
}

@Composable
private fun TicketQrSkeleton() {
    val label = stringResource(R.string.home_ticket_qr_loading)
    Box(
        Modifier.size(TicketQrSize)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            Modifier.width(56.dp).semantics { contentDescription = label }
        )
    }
}

@Composable
private fun TicketQr(image: ImageBitmap) {
    Image(
        bitmap = image,
        contentDescription = stringResource(R.string.home_ticket_qr_label),
        filterQuality = FilterQuality.None,
        modifier = Modifier.size(TicketQrSize)
    )
}

@Composable
private fun TicketField(label: Int, value: String, modifier: Modifier) {
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

internal fun openVenueInMaps(context: Context, venue: String): Boolean {
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
