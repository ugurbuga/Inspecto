package com.ugurbuga.inspecto.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.ugurbuga.inspecto.ui.components.noRippleClickable

@Composable
internal fun DetailRow(
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
                    .noRippleClickable {
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
                    .noRippleClickable {
                        clipboardManager.setText(AnnotatedString(value))
                        onToast("Value copied!")
                    },
                textAlign = TextAlign.End
            )
        }
    }
}
