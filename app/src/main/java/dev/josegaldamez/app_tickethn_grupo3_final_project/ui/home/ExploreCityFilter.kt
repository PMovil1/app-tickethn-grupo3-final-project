package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R

internal enum class HomeCity(@param:StringRes val label: Int) {
    Tegucigalpa(R.string.home_tegucigalpa),
    SanPedroSula(R.string.home_san_pedro_sula),
    Choloma(R.string.home_choloma),
    LaCeiba(R.string.home_la_ceiba),
    ElProgreso(R.string.home_el_progreso),
    Comayagua(R.string.home_comayagua),
    Choluteca(R.string.home_choluteca),
    PuertoCortes(R.string.home_puerto_cortes),
    Villanueva(R.string.home_villanueva),
    Siguatepeque(R.string.home_siguatepeque),
    Danli(R.string.home_danli)
}

@Composable
internal fun ExploreCityFilter(selected: HomeCity?, onCityChange: (HomeCity?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box {
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                expanded = true
            },
            shape = CircleShape
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_home_pin), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(selected?.label ?: R.string.home_all_cities))
                Icon(
                    painterResource(R.drawable.ic_home_next),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).rotate(90f)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            (listOf(null) + HomeCity.entries).forEach { city ->
                val active = city == selected
                DropdownMenuItem(
                    text = { Text(stringResource(city?.label ?: R.string.home_all_cities)) },
                    onClick = {
                        expanded = false
                        onCityChange(city)
                    },
                    modifier = Modifier.semantics { this.selected = active },
                    trailingIcon = if (active) {
                        { RadioButton(selected = true, onClick = null) }
                    } else {
                        null
                    }
                )
            }
        }
    }
}
