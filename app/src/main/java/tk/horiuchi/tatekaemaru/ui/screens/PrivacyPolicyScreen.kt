package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private sealed interface PrivacyPolicyUiState {
    data object Loading : PrivacyPolicyUiState
    data class Success(val text: String) : PrivacyPolicyUiState
    data class Error(val message: String) : PrivacyPolicyUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    policyUrl: String,
    onBack: () -> Unit
) {
    var uiState by remember(policyUrl) { mutableStateOf<PrivacyPolicyUiState>(PrivacyPolicyUiState.Loading) }

    fun loadPolicy() {
        uiState = PrivacyPolicyUiState.Loading
    }

    LaunchedEffect(policyUrl, uiState) {
        if (uiState is PrivacyPolicyUiState.Loading) {
            uiState = fetchPrivacyPolicy(policyUrl)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("プライバシーポリシー") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                PrivacyPolicyUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PrivacyPolicyUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MarkdownText(state.text)
                    }
                }
                is PrivacyPolicyUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "プライバシーポリシーを読み込めませんでした。",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(state.message)
                        Button(onClick = { loadPolicy() }) {
                            Text("再読み込み")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun fetchPrivacyPolicy(policyUrl: String): PrivacyPolicyUiState = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(policyUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext PrivacyPolicyUiState.Error("HTTP $responseCode: $policyUrl")
            }
            val text = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            PrivacyPolicyUiState.Success(text.ifBlank { "プライバシーポリシーの内容が空です。" })
        } finally {
            connection.disconnect()
        }
    }.getOrElse { error ->
        PrivacyPolicyUiState.Error(error.message ?: "通信エラーが発生しました。")
    }
}

@Composable
private fun MarkdownText(markdown: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        markdown.lines().forEach { line ->
            when {
                line.startsWith("# ") -> Text(
                    text = line.removePrefix("# "),
                    style = MaterialTheme.typography.headlineMedium
                )

                line.startsWith("## ") -> Text(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.titleLarge
                )

                line.startsWith("- ") -> Text(
                    text = "• ${line.removePrefix("- ")}",
                    style = MaterialTheme.typography.bodyMedium
                )

                line.isBlank() -> Text("")

                else -> Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
