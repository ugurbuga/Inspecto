package com.ugurbuga.inspectodemo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.ugurbuga.shared.generated.resources.Res
import com.github.ugurbuga.shared.generated.resources.outfit
import com.ugurbuga.inspecto.Inspecto
import com.ugurbuga.inspecto.InspectoApp
import com.ugurbuga.inspecto.InspectoKtorLogging
import com.ugurbuga.inspecto.launchInspecto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.Font

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
    val outfitFontFamily = FontFamily(Font(Res.font.outfit))
    val customTypography = Typography(
        displayLarge = TextStyle(fontFamily = outfitFontFamily),
        displayMedium = TextStyle(fontFamily = outfitFontFamily),
        displaySmall = TextStyle(fontFamily = outfitFontFamily),
        headlineLarge = TextStyle(fontFamily = outfitFontFamily),
        headlineMedium = TextStyle(fontFamily = outfitFontFamily),
        headlineSmall = TextStyle(fontFamily = outfitFontFamily),
        titleLarge = TextStyle(fontFamily = outfitFontFamily),
        titleMedium = TextStyle(fontFamily = outfitFontFamily),
        titleSmall = TextStyle(fontFamily = outfitFontFamily),
        bodyLarge = TextStyle(fontFamily = outfitFontFamily),
        bodyMedium = TextStyle(fontFamily = outfitFontFamily),
        bodySmall = TextStyle(fontFamily = outfitFontFamily),
        labelLarge = TextStyle(fontFamily = outfitFontFamily),
        labelMedium = TextStyle(fontFamily = outfitFontFamily),
        labelSmall = TextStyle(fontFamily = outfitFontFamily)
    )

    InspectoApp {
        val coroutineScope = rememberCoroutineScope()
        var fetchResult by remember { mutableStateOf("Test automatic Ktor, OkHttp, or Manual logging below.") }
        var isLoading by remember { mutableStateOf(false) }

        val pulseScale = remember { Animatable(1f) }

        LaunchedEffect(fetchResult) {
            if (fetchResult.isNotBlank()) {
                pulseScale.animateTo(
                    targetValue = 1.04f,
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                )
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
            }
        }

        fun performKtorAutoRequest(title: String, url: String) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val response: HttpResponse = ktorClient.get(url)
                    val body = response.bodyAsText()
                    fetchResult =
                        "[$title (KTOR)] Success (${response.status.value}): Received ${body.length} chars."
                } catch (e: Exception) {
                    fetchResult = "[$title (KTOR)] Error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }

        fun performOkHttpAutoRequest(title: String, url: String) {
            isLoading = true
            performOkHttpTestRequest(url) { result ->
                fetchResult = "[$title (OkHttp)] $result"
                isLoading = false
            }
        }

        fun performManualLog() {
            val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
            Inspecto.addLog(
                url = "https://api.inspecto.local/v1/manual-action",
                method = "POST",
                statusCode = 201,
                durationMs = 35L,
                requestHeaders = mapOf("Authorization" to "Bearer secret-token"),
                requestQueryParams = mapOf("source" to "button"),
                requestBody = "{\"action\": \"test\", \"time\": $currentTime}",
                responseBody = "{\"status\": \"success\"}",
                tag = "Manual"
            )
            fetchResult = "Manual log entry added successfully!"
        }

        MaterialTheme(typography = customTypography) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Inspecto") },
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
                            Text("Open Inspecto", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Button(
                            onClick = {
                                performKtorAutoRequest(
                                    "Users",
                                    "https://jsonplaceholder.typicode.com/users"
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fetch Users (KTOR)")
                        }

                        Button(
                            onClick = {
                                performKtorAutoRequest(
                                    "Posts",
                                    "https://jsonplaceholder.typicode.com/posts"
                                )
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fetch Posts (KTOR)")
                        }

                        Button(
                            onClick = {
                                performOkHttpAutoRequest(
                                    "Posts",
                                    "https://jsonplaceholder.typicode.com/posts"
                                )
                            },
                            enabled = !isLoading && isOkHttpSupported,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isOkHttpSupported) "Fetch Posts (OkHttp)" else "Fetch Posts (OkHttp - Android/JVM Only)")
                        }

                        Button(
                            onClick = {
                                performKtorAutoRequest(
                                    "Comments",
                                    "https://jsonplaceholder.typicode.com/comments"
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fetch Comments (KTOR)")
                        }

                        OutlinedButton(
                            onClick = { performManualLog() },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Trigger Manual Log")
                        }

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isLoading) 1f else 0f)
                                .padding(vertical = 4.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = pulseScale.value
                                    scaleY = pulseScale.value
                                }
                                .heightIn(min = 100.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Result:",
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
}
