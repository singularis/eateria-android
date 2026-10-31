package com.singularis.eateria.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.singularis.eateria.services.GRPCService
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme

@Composable
fun MyFriendsView(onAdd: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var friends by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(Unit) {
        val (list, _) = GRPCService(context).getFriends(limit = 200)
        friends = list
    }
    AppSheet(onDismiss = onDismiss) {
        Column(Modifier.padding(20.dp)) {
            Text(Localization.tr(context, "friends.title", "My friends"), color = AppTheme.textPrimary())
            LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(friends) { (email, nickname) ->
                    Text(nickname.ifBlank { email }, color = AppTheme.textSecondary(), modifier = Modifier.padding(vertical = 6.dp))
                }
            }
            TextButton(onClick = onAdd) {
                Text(Localization.tr(context, "friends.add", "Add friend"))
            }
        }
    }
}
