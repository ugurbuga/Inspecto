package com.ugurbuga.inspecto.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.BackHandler
import com.ugurbuga.inspecto.InspectoLogEntry
import com.ugurbuga.inspecto.res.InspectoStrings
import com.ugurbuga.inspecto.shareText
import kotlinx.coroutines.launch

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
                                0 -> entry.formattedOverview() to InspectoStrings.OVERVIEW_TAB
                                1 -> entry.formattedRequestSection() to InspectoStrings.REQUEST_TAB
                                2 -> entry.formattedResponseSection() to InspectoStrings.RESPONSE_TAB
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
                    val tabs = listOf(
                        InspectoStrings.OVERVIEW_TAB,
                        InspectoStrings.REQUEST_TAB,
                        InspectoStrings.RESPONSE_TAB
                    )
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
        containerColor = MaterialTheme.colorScheme.surface,
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
