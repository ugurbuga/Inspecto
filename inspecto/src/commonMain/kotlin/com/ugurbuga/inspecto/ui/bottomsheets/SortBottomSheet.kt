package com.ugurbuga.inspecto.ui.bottomsheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ugurbuga.inspecto.InspectoSortOption
import com.ugurbuga.inspecto.res.InspectoStrings
import com.ugurbuga.inspecto.ui.components.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SortBottomSheet(
    sortOption: InspectoSortOption,
    onApply: (InspectoSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSort by remember { mutableStateOf(sortOption) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                InspectoStrings.SORT_REQUESTS_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            InspectoSortOption.entries.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { tempSort = option }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tempSort == option,
                        onClick = { tempSort = option }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (tempSort == option) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (index < InspectoSortOption.entries.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onApply(tempSort)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(InspectoStrings.APPLY_SORT, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
