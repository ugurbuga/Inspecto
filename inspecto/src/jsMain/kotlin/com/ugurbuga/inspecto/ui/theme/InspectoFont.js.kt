package com.ugurbuga.inspecto.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.ugurbuga.inspecto.generated.resources.Res
import com.ugurbuga.inspecto.generated.resources.outfit
import org.jetbrains.compose.resources.Font

@Composable
internal actual fun getInspectoFontFamily(): FontFamily {
    return FontFamily(Font(Res.font.outfit))
}
