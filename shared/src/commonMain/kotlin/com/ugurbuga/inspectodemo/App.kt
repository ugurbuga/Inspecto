package com.ugurbuga.inspectodemo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.InspectoKtorLogging
import com.ugurbuga.inspecto.launchInspecto
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// Ktor client with automatic InspectoKtorLogging plugin
val ktorClient = HttpClient {
    install(InspectoKtorLogging)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var fetchResult by remember { mutableStateOf("Test automatic Ktor, OkHttp, or Manual logging below.") }
    var isLoading by remember { mutableStateOf(false) }
    var customTag by remember { mutableStateOf("") }

    fun performKtorAutoRequest(title: String, url: String) {
        isLoading = true
        coroutineScope.launch {
            try {
                val response: HttpResponse = ktorClient.get(url)
                val body = response.bodyAsText()
                fetchResult = "[$title (Ktor Auto)] Success (${response.status.value}): Received ${body.length} chars."
            } catch (e: Exception) {
                fetchResult = "[$title (Ktor Auto)] Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun performOkHttpAutoRequest(title: String, url: String) {
        isLoading = true
        performOkHttpTestRequest(url) { result ->
            fetchResult = "[$title (OkHttp Auto)] $result"
            isLoading = false
        }
    }

    fun performManualLog() {
        val tagToUse = customTag.ifBlank { "Manual" }
        val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        Inspecto.addLog(
            url = "https://api.inspecto.local/v1/manual-action",
            method = "POST",
            statusCode = 201,
            durationMs = 35L,
            requestHeaders = mapOf("Authorization" to "Bearer custom-secret-token", "Content-Type" to "application/json"),
            requestQueryParams = mapOf("source" to "button_click"),
            requestBody = "{\"action\": \"manual_log_test\", \"user\": \"developer\", \"time\": $currentTime}",
            responseHeaders = mapOf("Content-Type" to "application/json", "X-Inspecto-Mode" to "Manual"),
            responseBody = "{\"status\": \"success\", \"message\": \"Manually logged entry successfully stored.\"}".trimIndent(),
            tag = tagToUse
        )
        fetchResult = "Manually logged entry with tag '$tagToUse' added to Inspecto!"
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Inspecto Demo") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Inspecto Network Inspector Demo",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { launchInspecto() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Inspecto Network Inspector", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Custom Tag Input
                    OutlinedTextField(
                        value = customTag,
                        onValueChange = { customTag = it },
                        label = { Text("Custom Log Tag (e.g., Ktor, OkHttp, Auth, Manual)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Automatic & Manual Logging Examples",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Ktor Auto Users
                    Button(
                        onClick = { performKtorAutoRequest("Users", "https://jsonplaceholder.typicode.com/users") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Users (Automatic via Ktor Plugin)")
                    }

                    // 2. OkHttp Auto Posts
                    Button(
                        onClick = { performOkHttpAutoRequest("Posts", "https://jsonplaceholder.typicode.com/posts") },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Posts (Automatic via OkHttp Interceptor)")
                    }

                    // 3. Ktor Auto Comments
                    Button(
                        onClick = { performKtorAutoRequest("Comments", "https://jsonplaceholder.typicode.com/comments") },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Comments (Automatic via Ktor Plugin)")
                    }

                    // 4. Manual Logging Example
                    OutlinedButton(
                        onClick = { performManualLog() },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Trigger Manual Log Entry Example")
                    }

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Result card with fixed min height to prevent screen jumping
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 130.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Execution Result:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = fetchResult,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
