package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R

internal enum class EventLoadingLayout { Home, Explore, OrganizerList, OrganizerDetail }

@Composable
internal fun EventLoading(layout: EventLoadingLayout) {
    val label = stringResource(when (layout) {
        EventLoadingLayout.Home, EventLoadingLayout.Explore -> R.string.home_catalog_loading
        EventLoadingLayout.OrganizerList -> R.string.organizer_loading
        EventLoadingLayout.OrganizerDetail -> R.string.organizer_loading_details
    })
    Column(
        Modifier.fillMaxWidth().semantics {
            contentDescription = label
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (layout) {
            EventLoadingLayout.Home -> {
                LoadingBlock(Modifier.fillMaxWidth(0.38f).height(18.dp))
                LoadingBlock(Modifier.fillMaxWidth().height(168.dp))
                LoadingBlock(Modifier.fillMaxWidth(0.48f).height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(2) {
                        Column(Modifier.weight(1f)) { LoadingEventCard(16f / 9f) }
                    }
                }
            }
            EventLoadingLayout.Explore, EventLoadingLayout.OrganizerList -> repeat(3) {
                ProfilePanel {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = if (layout == EventLoadingLayout.OrganizerList) Alignment.Top
                        else Alignment.CenterVertically
                    ) {
                        val coverSize = if (layout == EventLoadingLayout.OrganizerList) {
                            Modifier.widthIn(max = 168.dp).weight(0.42f, fill = false)
                                .fillMaxWidth().aspectRatio(4f / 3f)
                        } else {
                            Modifier.width(96.dp).aspectRatio(1.08f)
                        }
                        LoadingBlock(coverSize)
                        Column(Modifier.weight(if (layout == EventLoadingLayout.OrganizerList) 0.58f else 1f)) {
                            LoadingEventLines()
                        }
                    }
                }
            }
            EventLoadingLayout.OrganizerDetail -> {
                LoadingEventLines()
                LoadingBlock(Modifier.fillMaxWidth().height(48.dp))
                LoadingBlock(Modifier.fillMaxWidth(0.5f).height(18.dp))
                ProfilePanel {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) { LoadingEventLines() }
                }
            }
        }
    }
}

@Composable
internal fun BuyerEventLoading(coverHeight: Dp, body: Modifier) {
    val label = stringResource(R.string.organizer_loading_details)
    Column(Modifier.fillMaxWidth().semantics {
        contentDescription = label
        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }) {
        Box(Modifier.fillMaxWidth().height(coverHeight).background(MaterialTheme.colorScheme.surfaceVariant))
        Column(body.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LoadingBlock(Modifier.fillMaxWidth(0.78f).height(28.dp))
            LoadingBlock(Modifier.fillMaxWidth(0.52f).height(12.dp))
            LoadingBlock(Modifier.fillMaxWidth(0.64f).height(12.dp))
            LoadingBlock(Modifier.fillMaxWidth().height(52.dp))
            LoadingBlock(Modifier.fillMaxWidth().height(96.dp))
            Spacer(Modifier.height(4.dp))
            LoadingBlock(Modifier.fillMaxWidth(0.48f).height(20.dp))
            LoadingBlock(Modifier.fillMaxWidth().height(116.dp))
        }
    }
}

@Composable
private fun LoadingEventCard(coverRatio: Float) {
    ProfilePanel {
        LoadingBlock(Modifier.fillMaxWidth().aspectRatio(coverRatio))
        Column(Modifier.fillMaxWidth().padding(12.dp)) { LoadingEventLines() }
    }
}

@Composable
private fun LoadingEventLines() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LoadingBlock(Modifier.fillMaxWidth(0.82f).height(16.dp))
        LoadingBlock(Modifier.fillMaxWidth(0.60f).height(12.dp))
        LoadingBlock(Modifier.fillMaxWidth(0.42f).height(12.dp))
    }
}

@Composable
private fun LoadingBlock(modifier: Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)))
}

