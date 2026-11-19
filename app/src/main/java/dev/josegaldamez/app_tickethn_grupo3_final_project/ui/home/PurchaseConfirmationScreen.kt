package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.TicketOrder

@Composable
internal fun PurchaseConfirmationScreen(
    order: TicketOrder,
    eventTitle: String?,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxSize(), color = colors.background) {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 600.dp).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        ConfirmationHero(order.tickets.size)
                    }
                    item {
                        ProfilePanel {
                            Column(
                                Modifier.fillMaxWidth().padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    stringResource(R.string.event_order_summary),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.semantics { heading() }
                                )
                                if (!eventTitle.isNullOrBlank()) {
                                    Text(eventTitle, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    stringResource(R.string.event_order_total, formatEventPrice(order.total)),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.primary
                                )
                                if (order.tickets.isNotEmpty()) {
                                    HorizontalDivider(color = colors.outlineVariant)
                                    Text(
                                        stringResource(R.string.event_order_folios),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.onSurfaceVariant
                                    )
                                    order.tickets.forEach { ticket ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.ic_home_ticket),
                                                contentDescription = null,
                                                tint = colors.primary
                                            )
                                            Text(ticket.code, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Surface(color = colors.surface) {
                    Column {
                        HorizontalDivider(color = colors.outlineVariant)
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
                                .heightIn(min = 48.dp)
                        ) {
                            Text(stringResource(R.string.event_order_back_to_events))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationHero(ticketCount: Int) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConfirmationMark()
            Text(
                stringResource(R.string.event_order_confirmed),
                modifier = Modifier.semantics { heading() },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                stringResource(R.string.event_order_thanks),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                pluralStringResource(R.plurals.event_order_issued, ticketCount, ticketCount),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ConfirmationMark() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(if (visible) 1f else 0.8f, tween(320), label = "Check")
    val colors = MaterialTheme.colorScheme
    val ink = colors.onSecondaryContainer
    val mint = colors.secondaryContainer
    val cobalt = colors.primary
    Canvas(Modifier.widthIn(max = 208.dp).fillMaxWidth().height(116.dp)) {
        val radius = 34.dp.toPx()
        val stroke = 2.dp.toPx()
        val offset = minOf(size.width * 0.32f, 69.dp.toPx())
        val left = center.x - offset
        val right = center.x + offset
        drawCircle(ink.copy(alpha = 0.23f), radius * 1.25f, style = Stroke(stroke))
        drawCircle(cobalt, stroke, Offset(left, center.y - radius * 0.60f))
        drawCircle(ink, stroke, Offset(right, center.y + radius * 0.64f))
        drawLine(ink, Offset(left - stroke, center.y + radius * 0.56f),
            Offset(left + stroke * 2, center.y + radius * 0.42f), stroke, StrokeCap.Round)
        drawLine(cobalt, Offset(right - stroke * 2, center.y - radius * 0.66f),
            Offset(right + stroke, center.y - radius * 0.82f), stroke, StrokeCap.Round)
        scale(scale, scale, pivot = center) {
            drawCircle(ink, radius)
            val check = Path().apply {
                moveTo(center.x - radius * 0.40f, center.y)
                lineTo(center.x - radius * 0.12f, center.y + radius * 0.28f)
                lineTo(center.x + radius * 0.44f, center.y - radius * 0.30f)
            }
            drawPath(check, mint, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
