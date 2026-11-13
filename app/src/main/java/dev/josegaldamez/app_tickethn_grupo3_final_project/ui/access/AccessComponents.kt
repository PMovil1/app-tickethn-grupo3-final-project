package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R

@Composable
internal fun ScreenHeaderDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    HorizontalDivider(modifier = modifier, color = color)
}

@Composable
internal fun TicketHNWordmark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.tickethn_wordmark),
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier.width(160.dp)
    )
}

@Composable
internal fun AccessButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        enabled = enabled
    ) {
        Text(label)
    }
}

@Composable
internal fun AuthPrimaryButton(
    label: String,
    onClick: () -> Unit,
    color: Color,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp, lineHeight = 24.sp))
    }
}
