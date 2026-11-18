package com.ugurbuga.inspecto.ui.theme

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

@Composable
internal actual fun getInspectoFontFamily(): FontFamily {
    val context = LocalContext.current
    return remember(context) {
        try {
            val typeface = Typeface.createFromAsset(context.assets, "outfit.ttf")
            FontFamily(typeface)
        } catch (e: Exception) {
            FontFamily.Default
        }
    }
}
