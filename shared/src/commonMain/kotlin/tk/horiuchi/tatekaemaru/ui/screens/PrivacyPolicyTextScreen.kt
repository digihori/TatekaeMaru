package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The policy shipped with the app stays available without network access. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyTextScreen(text: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("プライバシーポリシー") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            text.lines().forEach { line ->
                when {
                    line.startsWith("# ") -> Text(line.removePrefix("# "), style = MaterialTheme.typography.headlineMedium)
                    line.startsWith("## ") -> Text(line.removePrefix("## "), style = MaterialTheme.typography.titleLarge)
                    line.startsWith("* ") || line.startsWith("- ") -> Text("• " + line.drop(2))
                    line.isBlank() -> Spacer(Modifier.height(4.dp))
                    else -> Text(line)
                }
            }
        }
    }
}
