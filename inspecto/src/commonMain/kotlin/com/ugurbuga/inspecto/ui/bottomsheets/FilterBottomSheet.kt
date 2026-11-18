package com.ugurbuga.inspecto.ui.bottomsheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.res.InspectoStrings
import com.ugurbuga.inspecto.ui.theme.InspectoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FilterBottomSheet(
    selectedFilters: Set<String>,
    filterMode: String,
    isDarkTheme: Boolean,
    onApply: (selectedFilters: Set<String>, filterMode: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(selectedFilters) }
    var tempMode by remember { mutableStateOf(filterMode) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    InspectoTheme(isDarkTheme = isDarkTheme) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        InspectoStrings.FILTER_REQUESTS_TITLE,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { tempFilters = emptySet() }) {
                        Text("Reset", style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Combination Mode Card
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        InspectoStrings.COMBINATION_MODE,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("AND", "OR").forEach { mode ->
                            val selected = tempMode == mode
                            Surface(
                                onClick = { tempMode = mode },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (mode == "AND") InspectoStrings.MODE_AND_DESC else InspectoStrings.MODE_OR_DESC,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Chips Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        InspectoStrings.SELECT_CHIPS,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val availableFilters = listOf(
                        "ALL", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "2XX", "ERR", "SUCCESS"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableFilters.forEach { filter ->
                            val isSelected = if (filter == "ALL") tempFilters.isEmpty() || "ALL" in tempFilters else filter in tempFilters
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    tempFilters = if (filter == "ALL") {
                                        emptySet()
                                    } else {
                                        val mutable = tempFilters.toMutableSet()
                                        mutable.remove("ALL")
                                        if (filter in mutable) mutable.remove(filter) else mutable.add(filter)
                                        mutable
                                    }
                                },
                                label = {
                                    Text(
                                        filter,
                                        fontWeight = FontWeight.Normal // Request 2: not bold when selected
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onApply(tempFilters, tempMode)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(InspectoStrings.APPLY_FILTERS, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
