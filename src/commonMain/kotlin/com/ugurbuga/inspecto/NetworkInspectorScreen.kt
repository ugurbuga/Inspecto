package com.ugurbuga.inspecto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

enum class NetworkSortOption(val label: String) {
    DATE_DESC("Date (Newest first)"),
    DATE_ASC("Date (Oldest first)"),
    SIZE_DESC("Size (Largest first)"),
    SIZE_ASC("Size (Smallest first)"),
    DURATION_DESC("Duration (Longest first)"),
    DURATION_ASC("Duration (Shortest first)")
}

@Composable
fun NetworkInspectorScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkThemeOverride by remember { mutableStateOf<Boolean?>(null) }
    val isDark = isDarkThemeOverride ?: systemDark

    val colorScheme = if (isDark) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        NetworkInspectorScreenContent(
            onDismiss = onDismiss,
            isDarkTheme = isDark,
            onToggleTheme = { isDarkThemeOverride = !isDark },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkInspectorScreenContent(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by NetworkInspector.logs.collectAsStateWithLifecycle()
    var selectedEntry by remember { mutableStateOf<NetworkLogEntry?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var sortOption by remember { mutableStateOf(NetworkSortOption.DATE_DESC) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedEntry != null) {
        selectedEntry = null
    }

    val filteredLogs = remember(logs, searchQuery, selectedFilter) {
        logs.filter { entry ->
            val matchesFilter = when (selectedFilter) {
                "GET" -> entry.method.equals("GET", ignoreCase = true)
                "POST" -> entry.method.equals("POST", ignoreCase = true)
                "2XX" -> entry.statusCode in 200..299
                "ERR" -> entry.isError
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                entry.url.contains(searchQuery, ignoreCase = true) ||
                        entry.method.contains(searchQuery, ignoreCase = true) ||
                        entry.statusCode.toString().contains(searchQuery) ||
                        (entry.requestBody?.contains(searchQuery, ignoreCase = true) == true) ||
                        (entry.responseBody?.contains(searchQuery, ignoreCase = true) == true)
            }

            matchesFilter && matchesSearch
        }
    }

    val sortedLogs = remember(filteredLogs, sortOption) {
        when (sortOption) {
            NetworkSortOption.DATE_DESC -> filteredLogs.sortedByDescending { it.timestampMs }
            NetworkSortOption.DATE_ASC -> filteredLogs.sortedBy { it.timestampMs }
            NetworkSortOption.SIZE_DESC -> filteredLogs.sortedByDescending { it.responseSizeByte }
            NetworkSortOption.SIZE_ASC -> filteredLogs.sortedBy { it.responseSizeByte }
            NetworkSortOption.DURATION_DESC -> filteredLogs.sortedByDescending { it.durationMs }
            NetworkSortOption.DURATION_ASC -> filteredLogs.sortedBy { it.durationMs }
        }
    }

    val totalCount = logs.size
    val successCount = logs.count { it.isSuccess }
    val errorCount = logs.count { it.isError }
    val avgLatency = if (logs.isNotEmpty()) logs.map { it.durationMs }.average().toLong() else 0L

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (selectedEntry != null) {
            NetworkDetailScreen(
                entry = selectedEntry!!,
                onBackClick = { selectedEntry = null },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Inspecto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        actions = {
                            // Demo Data Button
                            IconButton(onClick = { NetworkInspector.addDemoLogs() }) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Load Demo Logs",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Theme Toggle Button
                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = { NetworkInspector.clear() }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Clear Logs",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
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
                    // Summary Stats Cards Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "TOTAL",
                            value = "$totalCount",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "2XX SUCCESS",
                            value = "$successCount",
                            containerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                            valueColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "ERRORS",
                            value = "$errorCount",
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                            valueColor = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "AVG DURATION",
                            value = "${avgLatency}ms",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        placeholder = { Text("Filter by URL, Status, Payload...", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp)
                    )

                    // Filter Chips & Sort Button Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val filters = listOf("ALL", "GET", "POST", "2XX", "ERR")
                            items(filters) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            text = filter,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sort Dropdown Button at the far right
                        Box {
                            IconButton(
                                onClick = { isSortMenuExpanded = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapVert,
                                    contentDescription = "Sort",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                NetworkSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.label,
                                                fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                                color = if (sortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            sortOption = option
                                            isSortMenuExpanded = false
                                        },
                                        leadingIcon = if (sortOption == option) {
                                            { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Transactions List
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
private fun StatCard(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = valueColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TransactionCard(
    entry: NetworkLogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methodBg = when (entry.method.uppercase()) {
        "GET" -> Color(0xFF0284C7)
        "POST" -> Color(0xFF10B981)
        "PUT" -> Color(0xFFF59E0B)
        "DELETE" -> Color(0xFFEF4444)
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
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Method Chip + Status Badge + Latency & Size
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = methodBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.method.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = statusBg.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, statusBg.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${entry.statusCode}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusBg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚡ ${entry.durationMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = entry.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Path & Host
            Text(
                text = entry.path,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = entry.host,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailScreen(
    entry: NetworkLogEntry,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
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
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("OVERVIEW", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("REQUEST", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                        text = { Text("RESPONSE", fontWeight = FontWeight.Bold) }
                    )
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
                        onToast = { msg -> copiedMessage = msg }
                    )
                    1 -> HeadersAndBodyTabContent(
                        queryParams = entry.requestQueryParams,
                        headers = entry.requestHeaders,
                        rawBody = entry.requestBody ?: "",
                        isDarkTheme = isDarkTheme,
                        onCopyBody = {
                            if (!entry.requestBody.isNullOrBlank()) {
                                clipboardManager.setText(AnnotatedString(entry.requestBody))
                                copiedMessage = "Request payload copied!"
                            }
                        },
                        onToast = { msg -> copiedMessage = msg }
                    )
                    2 -> HeadersAndBodyTabContent(
                        queryParams = emptyMap(),
                        headers = entry.responseHeaders,
                        rawBody = entry.responseBody ?: "",
                        isDarkTheme = isDarkTheme,
                        onCopyBody = {
                            if (!entry.responseBody.isNullOrBlank()) {
                                clipboardManager.setText(AnnotatedString(entry.responseBody))
                                copiedMessage = "Response payload copied!"
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
    entry: NetworkLogEntry,
    onCopyCurl: () -> Unit,
    onCopyUrl: () -> Unit,
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
            // Status & Method Banner (Clickable to copy URL)
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyUrl() }
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

            // Action Buttons Row: Copy cURL & Copy Request URL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopyCurl,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy cURL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onCopyUrl,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy URL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            // Overview Key-Value Details
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow("Host", entry.host, onToast)
                    DetailRow("Path", entry.path, onToast)
                    DetailRow("Status Code", "${entry.statusCode}", onToast)
                    DetailRow("Duration (Latency)", "${entry.durationMs} ms", onToast)
                    DetailRow("Response Size", entry.formattedSize, onToast)
                }
            }
        }
    }
}

@Composable
private fun HeadersAndBodyTabContent(
    queryParams: Map<String, String>,
    headers: Map<String, String>,
    rawBody: String,
    isDarkTheme: Boolean,
    onCopyBody: () -> Unit,
    onToast: (String) -> Unit = {}
) {
    var isQueryParamsExpanded by remember { mutableStateOf(false) }
    var isHeadersExpanded by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var formattedLines by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(rawBody) {
        if (rawBody.isBlank()) {
            formattedLines = emptyList()
            isProcessing = false
        } else {
            isProcessing = true
            val linesResult = withContext(Dispatchers.Default) {
                val formatted = NetworkInspector.formatJson(rawBody)
                formatted.lines()
            }
            formattedLines = linesResult
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
        // Query Parameters Section (Collapsible, default collapsed)
        if (queryParams.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isQueryParamsExpanded = !isQueryParamsExpanded }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Query Parameters (${queryParams.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (isQueryParamsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isQueryParamsExpanded) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
        }

        // Headers Section (Collapsible, default collapsed)
        if (headers.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHeadersExpanded = !isHeadersExpanded }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Headers (${headers.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (isHeadersExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isHeadersExpanded) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
        }

        // Body Section with Virtualized Line LazyColumn for zero-lag rendering
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payload Body", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (rawBody.isNotBlank()) {
                    IconButton(onClick = onCopyBody, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                                            JsonSyntaxHighlighter.highlightJsonLine(line, isDarkTheme)
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

@Preview
@Composable
fun NetworkInspectorScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        NetworkInspectorScreenContent(
            onDismiss = {},
            isDarkTheme = false,
            onToggleTheme = {}
        )
    }
}

@Preview
@Composable
fun NetworkInspectorScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        NetworkInspectorScreenContent(
            onDismiss = {},
            isDarkTheme = true,
            onToggleTheme = {}
        )
    }
}

@Preview
@Composable
fun TransactionCardPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        TransactionCard(
            entry = NetworkLogEntry(
                id = "1",
                url = "https://api.example.com/v1/series/data",
                method = "GET",
                statusCode = 200,
                durationMs = 45L,
                responseSizeByte = 320000L
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun NetworkDetailScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        NetworkDetailScreen(
            entry = NetworkLogEntry(
                id = "1",
                url = "https://api.example.com/v1/series/data",
                method = "GET",
                statusCode = 200,
                durationMs = 45L,
                requestHeaders = mapOf("User-Agent" to "MobileApp/1.0", "Accept" to "application/json"),
                responseHeaders = mapOf("Content-Type" to "application/json; charset=utf-8", "Cache-Control" to "no-cache"),
                responseBody = "{\"status\": \"ok\", \"items\": [{\"id\": 1, \"title\": \"Sample Series\"}]}",
                responseSizeByte = 320000L
            ),
            onBackClick = {},
            isDarkTheme = true,
            onToggleTheme = {}
        )
    }
}
