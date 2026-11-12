package com.talha.depremtakip.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.talha.depremtakip.R
import com.talha.depremtakip.domain.model.Earthquake
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                state.earthquake == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.not_found))
                }
                else -> DetailContent(state.earthquake!!)
            }
        }
    }
}

@Composable
private fun DetailContent(earthquake: Earthquake) {
    val context = LocalContext.current
    val unknown = stringResource(R.string.unknown)
    val formatter = DateTimeFormatter
        .ofPattern("dd MMMM yyyy HH:mm", Locale("tr"))
        .withZone(ZoneId.of("Europe/Istanbul"))

    val rows = buildList {
        add(stringResource(R.string.field_location) to (earthquake.location ?: unknown))
        add(stringResource(R.string.field_province) to (earthquake.province ?: unknown))
        add(stringResource(R.string.field_district) to (earthquake.district ?: unknown))
        add(
            stringResource(R.string.field_magnitude) to
                (earthquake.magnitude?.let { String.format(Locale.ROOT, "%.1f", it) } ?: unknown)
        )
        add(
            stringResource(R.string.field_depth) to
                (earthquake.depth?.let { String.format(Locale.ROOT, "%.1f km", it) } ?: unknown)
        )
        add(
            stringResource(R.string.field_coordinates) to
                if (earthquake.hasCoordinates) {
                    String.format(
                        Locale.ROOT,
                        "%.4f, %.4f",
                        earthquake.latitude,
                        earthquake.longitude
                    )
                } else {
                    unknown
                }
        )
        add(stringResource(R.string.field_type) to (earthquake.type ?: unknown))
        add(
            stringResource(R.string.field_date) to
                (earthquake.date?.let { formatter.format(it) } ?: unknown)
        )
        add(stringResource(R.string.field_id) to earthquake.id)
        add(
            stringResource(R.string.field_updated) to
                stringResource(if (earthquake.isUpdate) R.string.yes else R.string.no)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(rows) { (label, value) ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            HorizontalDivider()
        }
        item {
            if (earthquake.hasCoordinates) {
                Button(
                    onClick = {
                        val geoUri = Uri.parse(
                            "geo:${earthquake.latitude},${earthquake.longitude}" +
                                "?q=${earthquake.latitude},${earthquake.longitude}" +
                                "(${Uri.encode(earthquake.location ?: "Deprem")})"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Place, contentDescription = null)
                    Row(modifier = Modifier.padding(start = 8.dp)) {
                        Text(stringResource(R.string.open_in_maps))
                    }
                }
            }
        }
    }
}
