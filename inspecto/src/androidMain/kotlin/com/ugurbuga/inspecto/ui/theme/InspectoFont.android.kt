package com.ugurbuga.inspecto.ui.theme

import android.graphics.Typeface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
internal actual fun getInspectoFontFamily(): FontFamily {
    val context = LocalContext.current
    var fontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, "inspecto_outfit.ttf")
                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    context.assets.open("outfit.ttf").use { input ->
                        FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
                    }
                }
                val typeface = Typeface.createFromFile(cacheFile)
                if (typeface != null) {
                    fontFamily = FontFamily(typeface)
                }
            } catch (_: Throwable) {
                // Silently fallback to default font if loading fails
            }
        }
    }

    return fontFamily
}
