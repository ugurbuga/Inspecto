package com.ugurbuga.inspecto.ui.theme

import android.graphics.Typeface
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val TAG = "InspectoFont"

@Composable
internal actual fun getInspectoFontFamily(): FontFamily {
    val context = LocalContext.current
    var fontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val assetPaths = listOf(
                    "composeResources/com.ugurbuga.inspecto.generated.resources/font/outfit.ttf",
                    "composeResources/com.ugurbuga.shared.generated.resources/font/outfit.ttf",
                    "font/outfit.ttf",
                    "outfit.ttf",
                    "composeResources/inspectodemo.shared.generated.resources/font/outfit.ttf"
                )

                var matchedPath: String? = null
                var inputStream: java.io.InputStream? = null
                for (path in assetPaths) {
                    try {
                        inputStream = context.assets.open(path)
                        if (inputStream != null) {
                            matchedPath = path
                            Log.d(TAG, "SUCCESS: Opened asset via context.assets.open at path: '$path'")
                            break
                        }
                    } catch (_: Throwable) {
                        Log.d(TAG, "Tried context.assets.open('$path'): not found")
                    }
                }

                if (inputStream != null && matchedPath != null) {
                    val cacheFile = File(context.cacheDir, "inspecto_outfit.ttf")
                    if (!cacheFile.exists() || cacheFile.length() == 0L) {
                        FileOutputStream(cacheFile).use { out -> inputStream.copyTo(out) }
                    }
                    val typeface = Typeface.createFromFile(cleanCacheIfNeeded(cacheFile, inputStream))
                    if (typeface != null) {
                        fontFamily = FontFamily(typeface)
                        Log.d(TAG, "SUCCESS: Loaded font into FontFamily from asset path: '$matchedPath'")
                    }
                } else {
                    Log.e(TAG, "ERROR: All asset paths failed via context.assets.open!")
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Exception during font loading via assets: ${e.message}", e)
            }
        }
    }

    return fontFamily
}

private fun cleanCacheIfNeeded(cacheFile: File, inputStream: java.io.InputStream): File {
    if (!cacheFile.exists() || cacheFile.length() == 0L) {
        FileOutputStream(cacheFile).use { out -> inputStream.copyTo(out) }
    }
    return cacheFile
}
