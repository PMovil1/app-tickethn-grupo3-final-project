package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class PreviewIcon { Music, Calendar, Ball, Ticket }

@Composable
internal fun PreviewGlyph(icon: PreviewIcon, color: Color, iconSize: Dp = 16.dp) {
    Canvas(Modifier.size(iconSize)) {
        val unit = size.width / 16f
        val stroke = 1.4f * unit
        fun point(x: Float, y: Float) = Offset(x * unit, y * unit)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(color, point(x1, y1), point(x2, y2), stroke, StrokeCap.Round)
        }
        fun outlineCircle(x: Float, y: Float, radius: Float) {
            drawCircle(color, radius * unit, point(x, y), style = Stroke(stroke))
        }

        when (icon) {
            PreviewIcon.Music -> {
                line(6f, 3f, 6f, 12f)
                line(13f, 2f, 13f, 10f)
                line(6f, 3f, 13f, 2f)
                drawOval(color, point(1f, 10.5f), Size(5 * unit, 3.6f * unit))
                drawOval(color, point(8f, 8.5f), Size(5 * unit, 3.6f * unit))
            }
            PreviewIcon.Calendar -> {
                drawRoundRect(color, point(2f, 4f), Size(12 * unit, 10 * unit), CornerRadius(2 * unit), style = Stroke(stroke))
                line(2f, 7f, 14f, 7f)
                line(5f, 2f, 5f, 5f)
                line(11f, 2f, 11f, 5f)
            }
            PreviewIcon.Ball -> {
                outlineCircle(8f, 8f, 6.5f)
                val pentagon = Path().apply {
                    moveTo(8f * unit, 5f * unit)
                    lineTo(11f * unit, 7.2f * unit)
                    lineTo(9.8f * unit, 10.6f * unit)
                    lineTo(6.2f * unit, 10.6f * unit)
                    lineTo(5f * unit, 7.2f * unit)
                    close()
                }
                drawPath(pentagon, color)
                line(8f, 5f, 8f, 1.6f)
                line(11f, 7.2f, 14f, 6.2f)
                line(9.8f, 10.6f, 11.7f, 13.3f)
                line(6.2f, 10.6f, 4.3f, 13.3f)
                line(5f, 7.2f, 2f, 6.2f)
            }
            PreviewIcon.Ticket -> {
                val ticket = Path().apply {
                    moveTo(1.5f * unit, 4f * unit)
                    lineTo(14.5f * unit, 4f * unit)
                    lineTo(14.5f * unit, 6f * unit)
                    quadraticTo(10.8f * unit, 8f * unit, 14.5f * unit, 10f * unit)
                    lineTo(14.5f * unit, 12f * unit)
                    lineTo(1.5f * unit, 12f * unit)
                    lineTo(1.5f * unit, 10f * unit)
                    quadraticTo(5.2f * unit, 8f * unit, 1.5f * unit, 6f * unit)
                    close()
                }
                drawPath(ticket, color, style = Stroke(stroke))
                line(8f, 6.6f, 8f, 9.4f)
            }
        }
    }
}
