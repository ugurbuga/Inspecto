package com.ugurbuga.inspectodemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.InspectoApp
import com.ugurbuga.inspecto.launchInspecto
import com.ugurbuga.inspectodemo.shared.generated.resources.Res
import com.ugurbuga.inspectodemo.shared.generated.resources.outfit
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val outfitFont = FontFamily(Font(Res.font.outfit))
    InspectoApp(fontFamily = outfitFont) {
        val coroutineScope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }
        var snackbarColor by remember { mutableStateOf(Color(0xFF2563EB)) }
        var snackbarContentColor by remember { mutableStateOf(Color.White) }

        fun showMessage(msg: String, containerColor: Color, contentColor: Color = Color.White) {
            snackbarColor = containerColor
            snackbarContentColor = contentColor
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(msg)
            }
        }

        InspectoDemoContent(
            isLoading = isLoading,
            snackbarHostState = snackbarHostState,
            snackbarColor = snackbarColor,
            snackbarContentColor = snackbarContentColor,
            onUsersClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = InspectoKtorRunner.fetchUsers()
                        showMessage("[Users (KTOR)] $result", Color(0xFF2563EB))
                    } catch (e: Exception) {
                        showMessage("[Users (KTOR)] Error: ${e.message}", Color(0xFFE11D48))
                    } finally {
                        isLoading = false
                    }
                }
            },
            onPostsGetClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = InspectoKtorRunner.fetchPosts()
                        showMessage("[Posts (GET)] $result", Color(0xFF0EA5E9))
                    } catch (e: Exception) {
                        showMessage("[Posts (GET)] Error: ${e.message}", Color(0xFFE11D48))
                    } finally {
                        isLoading = false
                    }
                }
            },
            onPostsPostClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = InspectoKtorRunner.submitPost()
                        showMessage("[Posts (POST)] $result", Color(0xFF10B981))
                    } catch (e: Exception) {
                        showMessage("[Posts (POST)] Error: ${e.message}", Color(0xFFE11D48))
                    } finally {
                        isLoading = false
                    }
                }
            },
            onOkHttpClick = {
                isLoading = true
                performOkHttpTestRequest("https://jsonplaceholder.typicode.com/posts") { result ->
                    showMessage("[Posts (OkHttp)] $result", Color(0xFFF59E0B))
                    isLoading = false
                }
            },
            onNSURLSessionClick = {
                isLoading = true
                performNSURLSessionTestRequest("https://jsonplaceholder.typicode.com/posts") { result ->
                    showMessage("[Posts (NSURLSession)] $result", Color(0xFFF59E0B))
                    isLoading = false
                }
            },
            onApolloClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = InspectoApolloRunner.executeQuery()
                        showMessage("[Apollo] $result", Color(0xFFEC4899))
                    } catch (e: Exception) {
                        showMessage("[Apollo] Error: ${e.message}", Color(0xFFE11D48))
                    } finally {
                        isLoading = false
                    }
                }
            },
            onCommentsClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = InspectoKtorRunner.fetchComments()
                        showMessage("[Comments (KTOR)] $result", Color(0xFF8B5CF6))
                    } catch (e: Exception) {
                        showMessage("[Comments (KTOR)] Error: ${e.message}", Color(0xFFE11D48))
                    } finally {
                        isLoading = false
                    }
                }
            },
            onOpenInspecto = { launchInspecto() },
            onManualLogClick = {
                val result = InspectoManualLogRunner.triggerManualLog()
                showMessage(result, Color(0xFF64748B))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InspectoDemoContent(
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    snackbarColor: Color = Color(0xFF2563EB),
    snackbarContentColor: Color = Color.White,
    onUsersClick: () -> Unit = {},
    onPostsGetClick: () -> Unit = {},
    onPostsPostClick: () -> Unit = {},
    onOkHttpClick: () -> Unit = {},
    onNSURLSessionClick: () -> Unit = {},
    onApolloClick: () -> Unit = {},
    onCommentsClick: () -> Unit = {},
    onOpenInspecto: () -> Unit = {},
    onManualLogClick: () -> Unit = {}
) {
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Inspecto") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = snackbarColor,
                        contentColor = snackbarContentColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
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
                        onClick = onOpenInspecto,
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
                        onClick = onUsersClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Users (KTOR)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPostsGetClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Posts (KTOR - GET)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onPostsPostClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Post (KTOR - POST)", fontWeight = FontWeight.Bold)
                    }

                    if (isOkHttpSupported) {
                        Button(
                            onClick = onOkHttpClick,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fetch Posts (OkHttp)", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isNSURLSessionSupported) {
                        Button(
                            onClick = onNSURLSessionClick,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fetch Posts (NSURLSession)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onApolloClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Posts (Apollo)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCommentsClick,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fetch Comments (KTOR)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onManualLogClick,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Trigger Manual Log", fontWeight = FontWeight.Bold)
                    }

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isLoading) 1f else 0f)
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Inspecto Demo - Light")
@Composable
fun InspectoDemoLightPreview() {
    InspectoDemoContent()
}

@Preview(name = "Inspecto Demo - Dark")
@Composable
fun InspectoDemoDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface {
            InspectoDemoContent()
        }
    }
}
