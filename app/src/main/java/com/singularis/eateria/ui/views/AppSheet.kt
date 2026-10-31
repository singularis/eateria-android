package com.singularis.eateria.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularis.eateria.ui.theme.AppTheme
import com.singularis.eateria.ui.theme.DestructiveButton
import com.singularis.eateria.ui.theme.PrimaryButton
import com.singularis.eateria.ui.theme.SecondaryButton
import com.singularis.eateria.ui.theme.appSheetBackground

enum class AppNoticeKind {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE,
}

data class AppNoticeAction(
    val title: String,
    val kind: AppNoticeKind = AppNoticeKind.PRIMARY,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.surface(),
        content = {
            Column(Modifier.appSheetBackground().padding(bottom = 24.dp), content = content)
        },
    )
}

@Composable
fun AppNotice(
    title: String,
    message: String,
    actions: List<AppNoticeAction>,
    onDismiss: () -> Unit,
) {
    AppSheet(onDismiss = onDismiss) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.textPrimary(),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        if (message.isNotEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.textSecondary(),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            actions.forEach { action ->
                val click = {
                    action.onClick()
                    onDismiss()
                }
                when (action.kind) {
                    AppNoticeKind.PRIMARY -> PrimaryButton(onClick = click, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(action.title, color = androidx.compose.ui.graphics.Color.White)
                    }
                    AppNoticeKind.SECONDARY -> SecondaryButton(onClick = click, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(action.title, color = AppTheme.textPrimary())
                    }
                    AppNoticeKind.DESTRUCTIVE -> DestructiveButton(onClick = click, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(action.title, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
    }
}
