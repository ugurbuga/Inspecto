package com.ugurbuga.inspecto.ui.bottomsheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.res.InspectoStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FilterBottomSheet(
    selectedFilters: Set<String>,
    filterMode: String,
    onApply: (selectedFilters: Set<String>, filterMode: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilters by remember { mutableStateOf(selectedFilters) }
    var tempMode by remember { mutableStateOf(filterMode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                InspectoStrings.FILTER_REQUESTS_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Combination Mode Tabbed Control (Request 9)
            Text(
                InspectoStrings.COMBINATION_MODE,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("AND", "OR").forEach { mode ->
                    val selected = tempMode == mode
                    Surface(
                        onClick = { tempMode = mode },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (mode == "AND") InspectoStrings.MODE_AND_DESC else InspectoStrings.MODE_OR_DESC,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Text(
                InspectoStrings.SELECT_CHIPS,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            val availableFilters = listOf(
                "ALL",
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS",
                "HEAD",
                "2XX",
                "ERR",
                "SUCCESS"
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableFilters.forEach { filter ->
                    val isSelected =
                        if (filter == "ALL") tempFilters.isEmpty() || "ALL" in tempFilters else filter in tempFilters
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempFilters = if (filter == "ALL") {
                                emptySet()
                            } else {
                                val mutable = tempFilters.toMutableSet()
                                mutable.remove("ALL")
                                if (filter in mutable) {
                                    mutable.remove(filter)
                                } else {
                                    mutable.add(filter)
                                }
                                mutable
                            }
                        },
                        label = {
                            Text(
                                filter,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onApply(tempFilters, tempMode)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(InspectoStrings.APPLY_FILTERS, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
