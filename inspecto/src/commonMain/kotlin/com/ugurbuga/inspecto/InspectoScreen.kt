package com.ugurbuga.inspecto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val InspectoLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF0284C7),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFE11D48)
)

private val InspectoDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF1E3A8A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF38BDF8),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerLow = Color(0xFF020617),
    surfaceContainerHigh = Color(0xFF334155),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF475569),
    error = Color(0xFFF87171)
)

internal enum class InspectoSortOption(val label: String) {
    DATE_DESC("Date (Newest first)"),
    DATE_ASC("Date (Oldest first)"),
    SIZE_DESC("Size (Largest first)"),
    SIZE_ASC("Size (Smallest first)"),
    DURATION_DESC("Duration (Longest first)"),
    DURATION_ASC("Duration (Shortest first)")
}

@Composable
internal fun InspectoScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkThemeOverride by remember { mutableStateOf<Boolean?>(null) }
    val isDark = isDarkThemeOverride ?: systemDark

    val colorScheme = if (isDark) InspectoDarkColorScheme else InspectoLightColorScheme

    MaterialTheme(colorScheme = colorScheme) {
        InspectoScreenContent(
            onDismiss = onDismiss,
            isDarkTheme = isDark,
            onToggleTheme = { isDarkThemeOverride = !isDark },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectoScreenContent(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by Inspecto.logs.collectAsStateWithLifecycle()
    var selectedEntry by remember { mutableStateOf<InspectoLogEntry?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var filterMode by remember { mutableStateOf("AND") }
    var sortOption by remember { mutableStateOf(InspectoSortOption.DATE_DESC) }

    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }
    var isSortBottomSheetOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedEntry != null) {
        selectedEntry = null
    }

    val filteredLogs = remember(logs, searchQuery, selectedFilter, filterMode) {
        logs.filter { entry ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "2XX" -> entry.statusCode in 200..299
                "ERR" -> entry.isError
                "SUCCESS" -> entry.isSuccess
                else -> entry.method.equals(selectedFilter, ignoreCase = true)
            }

            val query = searchQuery.trim().lowercase()
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                when (query) {
                    "success" -> entry.isSuccess
                    "error", "err" -> entry.isError
                    "2xx" -> entry.statusCode in 200..299
                    else -> {
                        entry.url.contains(query, ignoreCase = true) ||
                                entry.method.contains(query, ignoreCase = true) ||
                                entry.statusCode.toString().contains(query) ||
                                entry.tag.contains(query, ignoreCase = true) ||
                                (entry.requestBody?.contains(query, ignoreCase = true) == true) ||
                                (entry.responseBody?.contains(query, ignoreCase = true) == true)
                    }
                }
            }

            if (filterMode == "AND") {
                matchesFilter && matchesSearch
            } else {
                matchesFilter || matchesSearch
            }
        }
    }

    val sortedLogs = remember(filteredLogs, sortOption) {
        when (sortOption) {
            InspectoSortOption.DATE_DESC -> filteredLogs.sortedByDescending { it.timestampMs }
            InspectoSortOption.DATE_ASC -> filteredLogs.sortedBy { it.timestampMs }
            InspectoSortOption.SIZE_DESC -> filteredLogs.sortedByDescending { it.responseSizeByte }
            InspectoSortOption.SIZE_ASC -> filteredLogs.sortedBy { it.responseSizeByte }
            InspectoSortOption.DURATION_DESC -> filteredLogs.sortedByDescending { it.durationMs }
            InspectoSortOption.DURATION_ASC -> filteredLogs.sortedBy { it.durationMs }
        }
    }

    val totalCount = logs.size
    val successCount = logs.count { it.isSuccess }
    val errorCount = logs.count { it.isError }
    val avgLatency = if (logs.isNotEmpty()) logs.map { it.durationMs }.average().toLong() else 0L

    if (isFilterBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterBottomSheetOpen = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Filter Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                val filters = listOf(
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
                filters.forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFilter = filter
                                isFilterBottomSheetOpen = false
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                                isFilterBottomSheetOpen = false
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            filter,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Text(
                    "Filter Combination Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterMode == "AND",
                        onClick = { filterMode = "AND" },
                        label = { Text("AND") },
                        shape = RoundedCornerShape(4.dp)
                    )
                    FilterChip(
                        selected = filterMode == "OR",
                        onClick = { filterMode = "OR" },
                        label = { Text("OR") },
                        shape = RoundedCornerShape(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (isSortBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSortBottomSheetOpen = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Sort Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                InspectoSortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sortOption = option
                                isSortBottomSheetOpen = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOption == option,
                            onClick = {
                                sortOption = option
                                isSortBottomSheetOpen = false
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (selectedEntry != null) {
            InspectoDetailScreen(
                entry = selectedEntry!!,
                onBackClick = { selectedEntry = null },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Inspecto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Header Section: Left = 4 Table Stat Rows; Right = 2x2 Grid Action Icons (Filter, Sort, Theme, Clear)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left: Table layout with 4 stat rows (both left and right texts use onSurface color)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Total Requests",
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
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                        alpha = 0.2f
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Success (2XX)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "$successCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                        alpha = 0.2f
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Errors",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "$errorCount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                        alpha = 0.2f
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Avg Duration",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "${avgLatency}ms",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Right: 2x2 Grid Action Icons with High-Contrast Colors (especially in Dark Mode)
                        val iconButtonBg = if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.primaryContainer
                        val filterTint = if (isDarkTheme) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
                        val sortTint = if (isDarkTheme) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary
                        val themeTint = if (isDarkTheme) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary
                        val clearTint = if (isDarkTheme) Color(0xFFF87171) else Color(0xFFE11D48)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Row 1: Filter & Sort
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    onClick = { isFilterBottomSheetOpen = true },
                                    shape = RoundedCornerShape(6.dp),
                                    color = iconButtonBg,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.size(36.dp)
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
                                    onClick = { isSortBottomSheetOpen = true },
                                    shape = RoundedCornerShape(6.dp),
                                    color = iconButtonBg,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.size(36.dp)
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
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    onClick = onToggleTheme,
                                    shape = RoundedCornerShape(6.dp),
                                    color = iconButtonBg,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.size(36.dp)
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
                                    color = if (isDarkTheme) Color(0xFF451A03) else Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, clearTint.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(36.dp)
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

                    // Search Input (Compact, unclipped spacing)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        placeholder = {
                            Text(
                                "Search URL, method, status, tag...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    if (sortedLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Inbox,
                                    contentDescription = "No Calls",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Network Calls Captured",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "HTTP requests and responses will be logged here in real-time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(sortedLogs, key = { it.id }) { entry ->
                                TransactionCard(
                                    entry = entry,
                                    onClick = { selectedEntry = entry }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    entry: InspectoLogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methodBg = when (entry.method.uppercase()) {
        "GET" -> Color(0xFF0284C7)
        "POST" -> Color(0xFF10B981)
        "PUT" -> Color(0xFFF59E0B)
        "DELETE" -> Color(0xFFEF4444)
        "PATCH" -> Color(0xFF8B5CF6)
        "OPTIONS" -> Color(0xFF6B7280)
        "HEAD" -> Color(0xFF374151)
        else -> MaterialTheme.colorScheme.secondary
    }

    val statusBg = when {
        entry.statusCode in 200..299 -> Color(0xFF10B981)
        entry.statusCode in 300..399 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = methodBg,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = entry.method,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entry.url,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entry.durationMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${entry.responseSizeByte} B",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.tag.isNotBlank() && entry.tag != "HTTP") {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val tagColor = when {
                            entry.tag.contains("Ktor", ignoreCase = true) -> Color(0xFF8B5CF6)
                            entry.tag.contains("OkHttp", ignoreCase = true) -> Color(0xFFF59E0B)
                            entry.tag.equals("Manual", ignoreCase = true) -> Color(0xFF0EA5E9)
                            else -> Color(0xFF10B981)
                        }
                        Surface(
                            color = tagColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = entry.tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tagColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                color = statusBg.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${entry.statusCode}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusBg,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InspectoDetailScreen(
    entry: InspectoLogEntry,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    val clipboardManager = LocalClipboardManager.current
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = {
                        Text(
                            text = entry.path,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            val (text, label) = when (pagerState.currentPage) {
                                0 -> entry.formattedOverview() to "Overview"
                                1 -> entry.formattedRequestSection() to "Request Section"
                                2 -> entry.formattedResponseSection() to "Response Section"
                                else -> "" to ""
                            }
                            if (text.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(text))
                                copiedMessage = "Copied $label to clipboard!"
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Active Tab",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            val fullText = entry.toFormattedLogText()
                            shareText(fullText, "Inspecto Log - ${entry.method} ${entry.path}")
                            copiedMessage = "Opening share sheet..."
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Log",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tabs = listOf("OVERVIEW", "REQUEST", "RESPONSE")
                    tabs.forEachIndexed { index, title ->
                        val selected = pagerState.currentPage == index
                        Surface(
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            shape = RoundedCornerShape(4.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.4f
                                )
                            ),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OverviewTabContent(
                        entry = entry,
                        onCopyCurl = {
                            clipboardManager.setText(AnnotatedString(entry.toCurlCommand()))
                            copiedMessage = "cURL command copied!"
                        },
                        onCopyUrl = {
                            clipboardManager.setText(AnnotatedString(entry.url))
                            copiedMessage = "Request URL copied!"
                        },
                        onCopyOverviewDetails = {
                            clipboardManager.setText(AnnotatedString(entry.formattedOverview()))
                            copiedMessage = "Overview details copied!"
                        },
                        onToast = { msg -> copiedMessage = msg }
                    )

                    1 -> HeadersAndBodyTabContent(
                        sectionTitle = "Request",
                        queryParams = entry.requestQueryParams,
                        headers = entry.requestHeaders,
                        rawBody = entry.requestBody ?: "",
                        isDarkTheme = isDarkTheme,
                        onCopyHeaders = {
                            val text = entry.formattedRequestHeaders()
                            if (text.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(text))
                                copiedMessage = "Request Headers copied!"
                            }
                        },
                        onCopyQueryParams = {
                            val text = entry.formattedQueryParams()
                            if (text.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(text))
                                copiedMessage = "Query Parameters copied!"
                            }
                        },
                        onCopyBody = {
                            if (!entry.requestBody.isNullOrBlank()) {
                                clipboardManager.setText(AnnotatedString(entry.requestBody))
                                copiedMessage = "Request Payload copied!"
                            }
                        },
                        onToast = { msg -> copiedMessage = msg }
                    )

                    2 -> HeadersAndBodyTabContent(
                        sectionTitle = "Response",
                        queryParams = emptyMap(),
                        headers = entry.responseHeaders,
                        rawBody = entry.responseBody ?: "",
                        isDarkTheme = isDarkTheme,
                        onCopyHeaders = {
                            val text = entry.formattedResponseHeaders()
                            if (text.isNotBlank()) {
                                clipboardManager.setText(AnnotatedString(text))
                                copiedMessage = "Response Headers copied!"
                            }
                        },
                        onCopyQueryParams = {},
                        onCopyBody = {
                            if (!entry.responseBody.isNullOrBlank()) {
                                clipboardManager.setText(AnnotatedString(entry.responseBody))
                                copiedMessage = "Response Payload copied!"
                            }
                        },
                        onToast = { msg -> copiedMessage = msg }
                    )
                }
            }

            copiedMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2000)
                    copiedMessage = null
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    entry: InspectoLogEntry,
    onCopyCurl: () -> Unit,
    onCopyUrl: () -> Unit,
    onCopyOverviewDetails: () -> Unit,
    onToast: (String) -> Unit = {}
) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Request URL Card with Copy Button aligned on Far Right
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.method,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = if (entry.isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${entry.statusCode}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onCopyUrl,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Request URL",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Overview Details Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overview Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onCopyOverviewDetails,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Overview Details",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DetailRow("Host", entry.host, onToast)
                    DetailRow("Path", entry.path, onToast)
                    DetailRow("Status Code", "${entry.statusCode}", onToast)
                    DetailRow("Duration (Latency)", "${entry.durationMs} ms", onToast)
                    DetailRow("Response Size", entry.formattedSize, onToast)
                    if (entry.tag.isNotBlank()) {
                        DetailRow("Tag", entry.tag, onToast)
                    }
                    DetailRow("Timestamp", "${entry.timestampMs}", onToast)
                }
            }

            // cURL Button placed below Overview Details section as a Half-Width button
            Button(
                onClick = onCopyCurl,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Copy cURL",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun HeadersAndBodyTabContent(
    sectionTitle: String,
    queryParams: Map<String, String>,
    headers: Map<String, String>,
    rawBody: String,
    isDarkTheme: Boolean,
    onCopyHeaders: () -> Unit,
    onCopyQueryParams: () -> Unit,
    onCopyBody: () -> Unit,
    onToast: (String) -> Unit
) {
    var isHeadersExpanded by remember { mutableStateOf(true) }
    var isQueryParamsExpanded by remember { mutableStateOf(true) }
    var formattedLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(true) }

    LaunchedEffect(rawBody) {
        isProcessing = true
        withContext(Dispatchers.Default) {
            formattedLines = Inspecto.formatJson(rawBody).lines()
            isProcessing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (queryParams.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isQueryParamsExpanded = !isQueryParamsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Query Parameters (${queryParams.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onCopyQueryParams,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Query Parameters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isQueryParamsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isQueryParamsExpanded) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            queryParams.forEach { (key, value) ->
                                DetailRow(key, value, onToast = onToast)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }

        if (headers.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isHeadersExpanded = !isHeadersExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$sectionTitle Headers (${headers.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onCopyHeaders,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Headers",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isHeadersExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isHeadersExpanded) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            headers.forEach { (key, value) ->
                                DetailRow(key, value, onToast = onToast)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$sectionTitle Body",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (rawBody.isNotBlank()) {
                    IconButton(onClick = onCopyBody, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy $sectionTitle Body",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    when {
                        rawBody.isBlank() -> {
                            Text(
                                text = "(Empty Body)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        isProcessing -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Formatting payload...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            SelectionContainer {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 500.dp)
                                ) {
                                    items(
                                        count = formattedLines.size,
                                        key = { index -> index }
                                    ) { index ->
                                        val line = formattedLines[index]
                                        val highlightedLine = remember(line, isDarkTheme) {
                                            InspectoJsonSyntaxHighlighter.highlightJsonLine(
                                                line,
                                                isDarkTheme
                                            )
                                        }
                                        Text(
                                            text = highlightedLine,
                                            style = MaterialTheme.typography.bodySmall,
                                            lineHeight = 18.sp,
                                            fontFamily = FontFamily.Monospace
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
}

@Composable
private fun DetailRow(
    key: String,
    value: String,
    onToast: (String) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current

    SelectionContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(0.35f)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(key))
                        onToast("Key '$key' copied!")
                    }
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .weight(0.65f)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(value))
                        onToast("Value copied!")
                    },
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(name = "Inspecto List - Light")
@Composable
internal fun InspectoListLightPreview() {
    LaunchedEffect(Unit) {
        Inspecto.clear()
        Inspecto.addLog(
            url = "https://api.example.com/v1/users",
            method = "GET",
            statusCode = 200,
            durationMs = 120L,
            requestHeaders = mapOf("Authorization" to "Bearer token123"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[{\"id\": 1, \"name\": \"John Doe\"}]",
            tag = "Ktor"
        )
        Inspecto.addLog(
            url = "https://api.example.com/v1/posts",
            method = "POST",
            statusCode = 500,
            durationMs = 450L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = "{\"title\": \"New Post\"}",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Internal Server Error\"}",
            tag = "OkHttp"
        )
    }
    InspectoScreen(onDismiss = {})
}

@Preview(name = "Inspecto List - Dark")
@Composable
internal fun InspectoListDarkPreview() {
    LaunchedEffect(Unit) {
        Inspecto.clear()
        Inspecto.addLog(
            url = "https://api.example.com/v1/users",
            method = "GET",
            statusCode = 200,
            durationMs = 120L,
            requestHeaders = mapOf("Authorization" to "Bearer token123"),
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "[{\"id\": 1, \"name\": \"John Doe\"}]",
            tag = "Ktor"
        )
        Inspecto.addLog(
            url = "https://api.example.com/v1/posts",
            method = "POST",
            statusCode = 500,
            durationMs = 450L,
            requestHeaders = mapOf("Content-Type" to "application/json"),
            requestBody = "{\"title\": \"New Post\"}",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Internal Server Error\"}",
            tag = "OkHttp"
        )
    }
    MaterialTheme(colorScheme = InspectoDarkColorScheme) {
        Surface {
            InspectoScreen(onDismiss = {})
        }
    }
}

@Preview(name = "Inspecto Detail - Light")
@Composable
internal fun InspectoDetailLightPreview() {
    val sampleEntry = InspectoLogEntry(
        id = "1",
        url = "https://api.example.com/v1/users",
        method = "GET",
        statusCode = 200,
        durationMs = 120L,
        requestHeaders = mapOf("Authorization" to "Bearer token123", "Accept" to "application/json"),
        requestQueryParams = mapOf("page" to "1", "limit" to "10"),
        requestBody = null,
        responseHeaders = mapOf("Content-Type" to "application/json", "Cache-Control" to "no-cache"),
        responseBody = "[\n  {\n    \"id\": 1,\n    \"name\": \"John Doe\",\n    \"email\": \"john@example.com\"\n  }\n]",
        responseSizeByte = 1042L,
        tag = "Ktor"
    )
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        InspectoDetailScreen(
            entry = sampleEntry,
            onBackClick = {},
            isDarkTheme = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Inspecto Detail - Dark")
@Composable
internal fun InspectoDetailDarkPreview() {
    val sampleEntry = InspectoLogEntry(
        id = "1",
        url = "https://api.example.com/v1/users",
        method = "GET",
        statusCode = 200,
        durationMs = 120L,
        requestHeaders = mapOf("Authorization" to "Bearer token123", "Accept" to "application/json"),
        requestQueryParams = mapOf("page" to "1", "limit" to "10"),
        requestBody = null,
        responseHeaders = mapOf("Content-Type" to "application/json", "Cache-Control" to "no-cache"),
        responseBody = "[\n  {\n    \"id\": 1,\n    \"name\": \"John Doe\",\n    \"email\": \"john@example.com\"\n  }\n]",
        responseSizeByte = 1042L,
        tag = "Ktor"
    )
    MaterialTheme(colorScheme = InspectoDarkColorScheme) {
        InspectoDetailScreen(
            entry = sampleEntry,
            onBackClick = {},
            isDarkTheme = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}
