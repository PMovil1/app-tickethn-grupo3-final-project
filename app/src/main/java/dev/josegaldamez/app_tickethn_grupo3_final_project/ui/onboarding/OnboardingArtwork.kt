package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R

@Composable
internal fun EventCategories() {
    val colors = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2
    ) {
        CategoryBadge(
            R.string.onboarding_concerts, PreviewIcon.Music, colors.primaryContainer,
            if (dark) colors.onPrimaryContainer else Color(0xFF153BEE), Modifier.weight(1f)
        )
        CategoryBadge(
            R.string.onboarding_festivals, PreviewIcon.Calendar, colors.secondaryContainer,
            if (dark) colors.onSecondaryContainer else Color(0xFF008354), Modifier.weight(1f)
        )
        CategoryBadge(
            R.string.onboarding_sports, PreviewIcon.Ball, colors.tertiaryContainer,
            if (dark) colors.onTertiaryContainer else Color(0xFF5337E8), Modifier.weight(1f)
        )
        CategoryBadge(
            R.string.onboarding_other, PreviewIcon.Ticket, colors.surfaceVariant,
            colors.onSurface, Modifier.weight(1f)
        )
    }
}

@Composable
private fun CategoryBadge(
    @StringRes label: Int,
    icon: PreviewIcon,
    background: Color,
    iconColor: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        color = background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.heightIn(min = 46.dp).padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviewGlyph(icon, iconColor, iconSize = 22.dp)
            Text(
                stringResource(label),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
            )
        }
    }
}
