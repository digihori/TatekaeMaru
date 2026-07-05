package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.horiuchi.tatekaemaru.data.local.EventEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    events: List<EventEntity>,
    onCreateEvent: () -> Unit,
    onOpenEvent: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onOpenSampleEvent: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("たてかえ丸") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Text("︙")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("プライバシーポリシー") },
                            onClick = {
                                menuExpanded = false
                                onOpenPrivacyPolicy()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("レジャーの立替を、さくっと精算。", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCreateEvent, modifier = Modifier.fillMaxWidth()) {
                    Text("新規イベントを作成")
                }
            }

            item {
                Text("保存済みイベント", style = MaterialTheme.typography.titleSmall)
            }

            if (events.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "まだ保存済みイベントはありません。イベント編集画面で保存するとここに表示されます。",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(events, key = { it.id }) { event ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(event.name, style = MaterialTheme.typography.titleMedium)
                            Text(event.date)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(onClick = { onOpenEvent(event.id) }) {
                                    Text("開く")
                                }
                                TextButton(onClick = { onDeleteEvent(event.id) }) {
                                    Text("削除")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("サンプル", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("釣り2026/07/05")
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onOpenSampleEvent) {
                            Text("開く")
                        }
                    }
                }
            }
        }
    }
}
