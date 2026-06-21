package com.singularis.eateria.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.HapticsService
import com.singularis.eateria.services.HealthDataService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.*
import com.singularis.eateria.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSettingsView(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onLimitsChanged: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("70") }
    var targetWeight by remember { mutableStateOf("65") }
    var age by remember { mutableStateOf("25") }
    var isMale by remember { mutableStateOf(true) }
    var activityLevel by remember { mutableStateOf("Sedentary") }
    
    var showResults by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Load data if needed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.tr(context, "nav.health_settings", "Health Settings"), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = CalorieRed, modifier = Modifier.size(60.dp).fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Localization.tr(context, "health.update.title", "Update Your Health Data"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
            item {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
            item {
                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = { targetWeight = it },
                    label = { Text("Target Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
            item {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Gender: ", color = Color.White)
                    RadioButton(selected = isMale, onClick = { isMale = true })
                    Text("Male", color = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isMale, onClick = { isMale = false })
                    Text("Female", color = Color.White)
                }
            }
            item {
                Button(
                    onClick = {
                        HapticsService.getInstance().success()
                        showResults = true
                        onLimitsChanged?.invoke()
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
