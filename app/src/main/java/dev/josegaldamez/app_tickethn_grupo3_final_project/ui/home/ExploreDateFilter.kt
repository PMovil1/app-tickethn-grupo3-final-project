package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private val eventTimeZone = TimeZone.getTimeZone("UTC")
private val eventLocale = Locale.forLanguageTag("es-HN")

internal fun eventDay(year: Int, month: Int, day: Int): Long =
    GregorianCalendar(eventTimeZone).apply {
        clear()
        set(year, month - 1, day)
    }.timeInMillis

internal fun formatEventDay(dayMillis: Long, short: Boolean = false): String =
    SimpleDateFormat(if (short) "d MMM" else "EEE, d 'de' MMMM, yyyy", eventLocale).apply {
        timeZone = eventTimeZone
    }.format(Date(dayMillis)).replaceFirstChar { it.titlecase(eventLocale) }

@Composable
internal fun ExploreDateFilter(
    availableDates: Set<Long>,
    selectedDate: Long?,
    onDateChange: (Long?) -> Unit
) {
    var showingPicker by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    OutlinedButton(
        onClick = {
            focusManager.clearFocus()
            showingPicker = true
        },
        enabled = availableDates.isNotEmpty(),
        shape = CircleShape
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.ic_home_calendar),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(selectedDate?.let { formatEventDay(it) } ?: stringResource(R.string.home_all_dates))
        }
    }
    if (selectedDate != null) {
        TextButton(onClick = { onDateChange(null) }) {
            Text(stringResource(R.string.home_clear_date))
        }
    }

    if (showingPicker && availableDates.isNotEmpty()) {
        ExploreDatePicker(
            availableDates = availableDates,
            selectedDate = selectedDate,
            onDismiss = { showingPicker = false },
            onApply = { date ->
                showingPicker = false
                onDateChange(date)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreDatePicker(
    availableDates: Set<Long>,
    selectedDate: Long?,
    onDismiss: () -> Unit,
    onApply: (Long) -> Unit
) {
    val state = remember(availableDates, selectedDate) {
        val selected = selectedDate?.takeIf { it in availableDates }
        val years = availableDates.mapTo(mutableSetOf()) { date ->
            GregorianCalendar(eventTimeZone).apply { timeInMillis = date }.get(Calendar.YEAR)
        }
        DatePickerState(
            locale = eventLocale,
            initialSelectedDateMillis = selected,
            initialDisplayedMonthMillis = selected ?: availableDates.min(),
            yearRange = years.min()..years.max(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in availableDates
                override fun isSelectableYear(year: Int) = year in years
            }
        )
    }
    val pendingDate = state.selectedDateMillis?.takeIf { it in availableDates }
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pendingDate?.let(onApply) },
                enabled = pendingDate != null
            ) { Text(stringResource(R.string.home_apply_date)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.home_cancel_date)) }
        }
    ) {
        DatePicker(
            state = state,
            showModeToggle = false,
            headline = null,
            title = {
                Text(
                    stringResource(R.string.home_choose_date),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        )
    }
}
