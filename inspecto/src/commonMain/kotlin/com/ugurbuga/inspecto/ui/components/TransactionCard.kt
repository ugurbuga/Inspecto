package com.ugurbuga.inspecto.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.InspectoLogEntry
import com.ugurbuga.inspecto.ui.theme.InspectoLightColorScheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TransactionCard(
    entry: InspectoLogEntry,
    onClick: () -> Unit,
    urlDisplayMode: String = "FULL",
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

    val host = entry.host
    val path = if (urlDisplayMode == "PATH") {
        runCatching {
            val withoutScheme = entry.url.substringAfter("://")
            val slashIndex = withoutScheme.indexOf('/')
            if (slashIndex == -1) return@runCatching "/"
            var p = withoutScheme.substring(slashIndex + 1)
            if (p.startsWith("api/")) p = p.removePrefix("api/")
            if (p.isEmpty()) "/" else p
        }.getOrDefault(entry.path)
    } else {
        entry.path
    }

    val timeStr = runCatching {
        val instant = Instant.fromEpochMilliseconds(entry.timestampMs)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val h = dateTime.hour.toString().padStart(2, '0')
        val m = dateTime.minute.toString().padStart(2, '0')
        val s = dateTime.second.toString().padStart(2, '0')
        "$h:$m:$s"
    }.getOrDefault("")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left: Combined Method + Status Code Badge Block (4dp radius)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .width(60.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(methodBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.method,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${entry.statusCode}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusBg,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }
            }

            // Center: Path, BaseUrl, Footer Metadata (Time, Duration, Size, Tag)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Row 1: Path
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Row 2: Host / BaseUrl (2-part display)
                if (host.isNotBlank()) {
                    Text(
                        text = "🔒 $host",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Row 3: Timestamp, Duration, Size, Tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timeStr.isNotBlank()) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${entry.durationMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = entry.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.tag.isNotBlank() && entry.tag != "HTTP") {
                        Text(text = "•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val tagColor = when {
                            entry.tag.contains("Ktor", ignoreCase = true) -> Color(0xFF8B5CF6)
                            entry.tag.contains("OkHttp", ignoreCase = true) -> Color(0xFFF59E0B)
                            entry.tag.contains("GraphQL", ignoreCase = true) -> Color(0xFFEC4899)
                            entry.tag.contains("NSURLSession", ignoreCase = true) -> Color(0xFF10B981)
                            entry.tag.equals("Manual", ignoreCase = true) -> Color(0xFF0EA5E9)
                            else -> Color(0xFF10B981)
                        }
                        Surface(
                            color = tagColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = entry.tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tagColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Transaction Card - Light")
@Composable
internal fun TransactionCardPreview() {
    val sampleEntry = InspectoLogEntry(
        id = "1",
        url = "https://api.example.com/v1/users",
        method = "GET",
        statusCode = 200,
        durationMs = 120L,
        responseSizeByte = 1042L,
        tag = "Ktor"
    )
    MaterialTheme(colorScheme = InspectoLightColorScheme) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TransactionCard(entry = sampleEntry, onClick = {})
        }
    }
}
