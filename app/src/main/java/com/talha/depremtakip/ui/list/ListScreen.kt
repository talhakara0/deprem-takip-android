package com.talha.depremtakip.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talha.depremtakip.R
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.SortOrder
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.ui.common.RelativeTimeLabel
import com.talha.depremtakip.ui.common.relativeTimeLabel
import java.time.Instant
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onOpenDetail: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.refresh)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            FilterBar(
                state = state,
                onRangeChange = viewModel::setRange,
                onMinMagnitudeChange = { value ->
                    viewModel.setMinMagnitude((value * 2).roundToInt() / 2.0)
                },
                onQueryChange = viewModel::setQuery,
                onSortChange = viewModel::setSortOrder
            )

            if (state.isOffline) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.offline_banner),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        state.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        state.earthquakes.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_state),
                                    modifier = Modifier.padding(24.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Text(
                                        text = stringResource(
                                            R.string.earthquake_count,
                                            state.earthquakes.size
                                        ) + " · " + stringResource(
                                            R.string.total_cached,
                                            state.totalCount
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                items(state.earthquakes, key = { it.id }) { quake ->
                                    EarthquakeCard(
                                        earthquake = quake,
                                        onClick = { onOpenDetail(quake.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    state: ListUiState,
    onRangeChange: (TimeRange) -> Unit,
    onMinMagnitudeChange: (Double) -> Unit,
    onQueryChange: (String) -> Unit,
    onSortChange: (SortOrder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.range == TimeRange.LAST_24_HOURS,
                onClick = { onRangeChange(TimeRange.LAST_24_HOURS) },
                label = { Text(stringResource(R.string.range_24h)) }
            )
            FilterChip(
                selected = state.range == TimeRange.LAST_7_DAYS,
                onClick = { onRangeChange(TimeRange.LAST_7_DAYS) },
                label = { Text(stringResource(R.string.range_7d)) }
            )
            Spacer(modifier = Modifier.weight(1f))
            FilterChip(
                selected = state.filter.sortOrder == SortOrder.TIME_DESCENDING,
                onClick = { onSortChange(SortOrder.TIME_DESCENDING) },
                label = { Text(stringResource(R.string.sort_by_time)) }
            )
            FilterChip(
                selected = state.filter.sortOrder == SortOrder.MAGNITUDE_DESCENDING,
                onClick = { onSortChange(SortOrder.MAGNITUDE_DESCENDING) },
                label = { Text(stringResource(R.string.sort_by_magnitude)) }
            )
        }

        Text(
            text = stringResource(
                R.string.min_magnitude,
                String.format(Locale.ROOT, "%.1f", state.filter.minMagnitude)
            ),
            style = MaterialTheme.typography.labelMedium
        )
        Slider(
            value = state.filter.minMagnitude.toFloat(),
            onValueChange = { onMinMagnitudeChange(it.toDouble()) },
            valueRange = 0f..7f
        )

        OutlinedTextField(
            value = state.filter.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.province_search)) },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EarthquakeCard(earthquake: Earthquake, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MagnitudeBadge(earthquake.magnitude)
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = earthquake.location
                        ?: listOfNotNull(earthquake.district, earthquake.province)
                            .joinToString(", ")
                            .ifBlank { stringResource(R.string.unknown) },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(earthquake.province, earthquake.district)
                        .distinct()
                        .joinToString(", ")
                        .ifBlank { stringResource(R.string.unknown) },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = relativeTimeText(earthquake.date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    earthquake.depth?.let { depth ->
                        Text(
                            text = stringResource(
                                R.string.depth_km,
                                String.format(Locale.ROOT, "%.1f", depth)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MagnitudeBadge(magnitude: Double?) {
    val color = magnitudeColor(magnitude)
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(color = color, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = magnitude?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "?",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
internal fun relativeTimeText(date: Instant?): String {
    val now = remember { Instant.now() }
    return when (val label = relativeTimeLabel(date, now)) {
        RelativeTimeLabel.Unknown -> stringResource(R.string.time_unknown)
        RelativeTimeLabel.Now -> stringResource(R.string.time_now)
        is RelativeTimeLabel.Minutes -> stringResource(R.string.time_minutes, label.value)
        is RelativeTimeLabel.Hours -> stringResource(R.string.time_hours, label.value)
        is RelativeTimeLabel.Days -> stringResource(R.string.time_days, label.value)
    }
}

internal fun magnitudeColor(magnitude: Double?): Color = when {
    magnitude == null -> Color(0xFF757575)
    magnitude >= 5.0 -> Color(0xFFB71C1C)
    magnitude >= 4.0 -> Color(0xFFE65100)
    magnitude >= 3.0 -> Color(0xFFF9A825)
    else -> Color(0xFF2E7D32)
}
