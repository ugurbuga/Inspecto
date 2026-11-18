package com.ugurbuga.inspecto.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.res.InspectoStrings

@Composable
internal fun HeaderStatsTable(
    totalCount: Int,
    successCount: Int,
    errorCount: Int,
    avgLatency: Long,
    isDarkTheme: Boolean,
    onOpenFilter: () -> Unit,
    onOpenSort: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left: Table layout with 4 colored stat rows
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        InspectoStrings.TOTAL_REQUESTS,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "$totalCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        InspectoStrings.SUCCESS_2XX,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        "$successCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        InspectoStrings.ERRORS,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        "$errorCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        InspectoStrings.AVG_DURATION,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                    Text(
                        "${avgLatency}ms",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                }
            }
        }

        // Right: 2x2 Grid Square Action Icons with gap between Row 1 and Row 2 (Request 2)
        val iconButtonBg = if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.primaryContainer
        val filterTint = if (isDarkTheme) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
        val sortTint = if (isDarkTheme) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
        val themeTint = if (isDarkTheme) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary
        val clearTint = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFE11D48)

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Row 1: Filter & Sort
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = onOpenFilter,
                    shape = RoundedCornerShape(6.dp),
                    color = iconButtonBg,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            tint = filterTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    onClick = onOpenSort,
                    shape = RoundedCornerShape(6.dp),
                    color = iconButtonBg,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.SwapVert,
                            contentDescription = "Sort",
                            tint = sortTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            // Row 2: Theme & Clear
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = onToggleTheme,
                    shape = RoundedCornerShape(6.dp),
                    color = iconButtonBg,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = themeTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    onClick = { Inspecto.clear() },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDarkTheme) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, clearTint.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Clear Logs",
                            tint = clearTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
