package com.singularis.eateria.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.singularis.eateria.ui.theme.DarkPrimary
import com.singularis.eateria.ui.theme.Gray3

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
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("OK")
                    }
                },
                containerColor = Gray3,
                textContentColor = Color.White
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
