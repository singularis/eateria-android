package com.singularis.eateria.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3
import com.singularis.eateria.ui.theme.Gray4

object AlertHelper {
    
    @Composable
    fun SimpleAlert(
        title: String,
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit
    ) {
        if (isVisible) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK", color = DarkPrimary)
                    }
                },
                containerColor = Gray4
            )
        }
    }
    
    @Composable
    fun ErrorDialog(
        isVisible: Boolean,
        title: String = "Error",
        message: String,
        onDismiss: () -> Unit
    ) {
        SimpleAlert(title, message, isVisible, onDismiss)
    }
    
    @Composable
    fun SuccessDialog(
        isVisible: Boolean,
        title: String = "Success", 
        message: String,
        onDismiss: () -> Unit
    ) {
        SimpleAlert(title, message, isVisible, onDismiss)
    }
}
