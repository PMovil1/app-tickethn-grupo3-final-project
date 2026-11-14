package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEvent
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val priceLimits = listOf(0, 1_000, 2_000, 3_000, 5_000, 10_000, 20_000, null)

internal data class ExploreFilters(
    val address: String? = null,
    val date: Long? = null,
    val maxPrice: Int? = null
) {
    val count: Int get() = listOfNotNull(address, date, maxPrice).size

    fun matches(event: PublicEvent): Boolean =
        (address == null || address == event.address?.trim()) &&
            (maxPrice == null || event.availablePrice?.let { it <= maxPrice.toBigDecimal() } == true)
}

@Composable
internal fun ExploreFiltersButton(activeCount: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = CircleShape) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_home_filters), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                if (activeCount == 0) stringResource(R.string.home_filters)
                else stringResource(R.string.home_filters_active, activeCount)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreFiltersSheet(
    applied: ExploreFilters,
    searchedEvents: List<PublicEvent>,
    onDismiss: () -> Unit,
    onApply: (ExploreFilters) -> Unit
) {
    var address by rememberSaveable { mutableStateOf(applied.address) }
    var date by rememberSaveable { mutableStateOf(applied.date) }
    var maxPrice by rememberSaveable { mutableStateOf(applied.maxPrice) }
    val draft = ExploreFilters(address, date, maxPrice)
    val availableDates = searchedEvents.filter(draft::matches).mapNotNull { it.calendarDay }.toSet()
    val addresses = (searchedEvents.mapNotNull { it.address?.trim()?.takeIf(String::isNotBlank) } +
        listOfNotNull(address)).distinct().sorted()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                stringResource(R.string.home_filters),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 12.dp).semantics { heading() }
            )
            Column(
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.home_filter_location), style = MaterialTheme.typography.titleMedium)
                    ExploreAddressFilter(addresses, selected = address, onAddressChange = { address = it })
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.home_filter_date), style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.CenterVertically
                    ) {
                        ExploreDateFilter(availableDates, date, onDateChange = { date = it })
                    }
                }
                ExplorePriceSlider(maxPrice, onPriceChange = { maxPrice = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        address = null
                        date = null
                        maxPrice = null
                    },
                    enabled = draft.count > 0
                ) { Text(stringResource(R.string.home_reset_filters)) }
                Button(onClick = { onApply(draft) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_apply_filters))
                }
            }
        }
    }
}

@Composable
private fun ExplorePriceSlider(maxPrice: Int?, onPriceChange: (Int?) -> Unit) {
    val label = stringResource(R.string.home_filter_price)
    val free = stringResource(R.string.home_filter_free)
    val all = stringResource(R.string.home_filter_all_prices)
    val selectedLabel = when (maxPrice) {
        null -> all
        0 -> free
        else -> stringResource(
            R.string.home_filter_price_value,
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-HN")).format(maxPrice)
        )
    }
    val colors = MaterialTheme.colorScheme
    Column {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(selectedLabel, color = colors.primary, style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = priceLimits.indexOf(maxPrice).toFloat(),
            onValueChange = { onPriceChange(priceLimits[it.roundToInt()]) },
            valueRange = 0f..priceLimits.lastIndex.toFloat(),
            steps = priceLimits.size - 2,
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = label
                stateDescription = selectedLabel
            }
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(free, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text(all, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

