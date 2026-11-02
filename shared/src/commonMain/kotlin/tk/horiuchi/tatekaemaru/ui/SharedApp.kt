package tk.horiuchi.tatekaemaru.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tk.horiuchi.tatekaemaru.data.*
import tk.horiuchi.tatekaemaru.ui.screens.*

private enum class Screen { Home, Edit, Settlement, PrivacyPolicy }

/** Platform supplies storage, settings and the system share sheet. */
@Composable
fun SharedApp(
    repository: EventRepository,
    settings: AppSettings,
    versionName: String,
    onShare: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val setup = remember(repository, settings) { EventSetup(repository, settings) }
    val saveMutex = remember { Mutex() }
    var ready by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf(Screen.Home) }
    var currentEvent by remember { mutableStateOf(Event(name = "")) }
    var memberHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var expenseHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableStateOf<(() -> Unit)?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun operation(block: suspend () -> Unit) {
        fun launchOperation() {
            scope.launch {
                try {
                    block()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    error = failure.message ?: "操作を完了できませんでした。"
                    retry = { operation(block) }
                }
            }
        }
        launchOperation()
    }

    suspend fun refreshHistory() {
        memberHistory = repository.getMemberNameHistory()
        expenseHistory = repository.getExpenseTitleHistory()
    }

    fun save(event: Event, showSettlement: Boolean = false) {
        currentEvent = event
        operation {
            saveMutex.withLock {
                saving = true
                try {
                    repository.saveEvent(event)
                    refreshHistory()
                    if (showSettlement) screen = Screen.Settlement
                } finally {
                    saving = false
                }
            }
        }
    }

    LaunchedEffect(setup) {
        operation {
            setup.initialize()
            refreshHistory()
            ready = true
        }
    }

    MaterialTheme {
        if (!ready) {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Text("たてかえ丸")
                CircularProgressIndicator()
            }
        } else {
            val summaries = remember(repository) {
                repository.observeEventSummaries().catch { error = it.message ?: "一覧を読み込めませんでした。" }
            }
            val events by summaries.collectAsState(initial = emptyList())
            when (screen) {
                Screen.Home -> HomeScreen(
                    events = events, versionName = versionName,
                    onCreateEvent = {
                        currentEvent = setup.createEvent()
                        screen = Screen.Edit
                    },
                    onOpenEvent = { id -> operation {
                        val loaded = repository.loadEvent(id) ?: error("イベントが見つかりません。")
                        currentEvent = loaded
                        refreshHistory()
                        screen = Screen.Edit
                    } },
                    onDeleteEvent = { id -> operation { saveMutex.withLock { repository.deleteEvent(id) } } },
                    onOpenPrivacyPolicy = { screen = Screen.PrivacyPolicy }
                )
                Screen.Edit -> key(currentEvent.id) {
                    EventEditScreen(
                        event = currentEvent,
                        onEventChange = { currentEvent = it },
                        onBack = { screen = Screen.Home },
                        onSaveWithoutBack = { save(it) },
                        onSaveAndShowSettlement = { if (!saving) save(it, true) },
                        onMyNameChanged = { settings.myName = it },
                        memberHistory = memberHistory,
                        expenseTitleHistory = expenseHistory,
                        onExpenseTitleHistoryChanged = { expenseHistory = it }
                    )
                }
                Screen.PrivacyPolicy -> PrivacyPolicyTextScreen(
                    text = BundledPrivacyPolicy.Text,
                    onBack = { screen = Screen.Home }
                )
                Screen.Settlement -> SettlementScreen(
                    event = currentEvent,
                    onBack = { screen = Screen.Edit },
                    onShare = onShare
                )
            }
        }
        if (error != null) {
            AlertDialog(
                onDismissRequest = { error = null },
                title = { Text("操作を完了できませんでした") },
                text = { Text(error.orEmpty()) },
                confirmButton = {
                    TextButton(onClick = {
                        error = null
                        val action = retry
                        retry = null
                        action?.invoke()
                    }) { Text("再試行") }
                },
                dismissButton = { TextButton(onClick = { error = null }) { Text("閉じる") } }
            )
        }
    }
}
