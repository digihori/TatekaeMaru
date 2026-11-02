package tk.horiuchi.tatekaemaru.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.AndroidAppSettings
import tk.horiuchi.tatekaemaru.data.EventSetup
import tk.horiuchi.tatekaemaru.data.RoomEventRepository
import tk.horiuchi.tatekaemaru.data.EventRepository
import tk.horiuchi.tatekaemaru.data.local.AppDatabase
import tk.horiuchi.tatekaemaru.ui.screens.EventEditScreen
import tk.horiuchi.tatekaemaru.ui.screens.HomeScreen
import tk.horiuchi.tatekaemaru.ui.screens.PrivacyPolicyTextScreen
import tk.horiuchi.tatekaemaru.ui.screens.SettlementScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

object Routes {
    const val Home = "home"
    const val EventEdit = "eventEdit"
    const val Settlement = "settlement"
    const val PrivacyPolicy = "privacyPolicy"
}

@Composable
fun TatekaeMaruApp() {
    val context = LocalContext.current
    val repository: EventRepository = remember {
        RoomEventRepository(AppDatabase.getInstance(context).tatekaeDao())
    }
    val summaries = remember(repository) { repository.observeEventSummaries() }
    val savedEvents by summaries.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var currentEvent by remember { mutableStateOf(Event(name = "")) }
    val settings = remember { AndroidAppSettings(context) }
    val eventSetup = remember(repository, settings) { EventSetup(repository, settings) }
    var memberHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var expenseTitleHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        eventSetup.initialize()
        memberHistory = repository.getMemberNameHistory()
        expenseTitleHistory = repository.getExpenseTitleHistory()
    }

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable(Routes.Home) {
            HomeScreen(
                events = savedEvents,
                versionName = tk.horiuchi.tatekaemaru.BuildConfig.VERSION_NAME,
                onCreateEvent = {
                    currentEvent = eventSetup.createEvent()
                    navController.navigate(Routes.EventEdit)
                },
                onOpenEvent = { eventId ->
                    scope.launch {
                        repository.loadEvent(eventId)?.let { loadedEvent ->
                            currentEvent = loadedEvent
                            navController.navigate(Routes.EventEdit)
                        }
                    }
                },
                onDeleteEvent = { eventId ->
                    scope.launch { repository.deleteEvent(eventId) }
                },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) }
            )
        }
        composable(Routes.EventEdit) {
            EventEditScreen(
                event = currentEvent,
                onEventChange = { currentEvent = it },
                onBack = { navController.popBackStack() },
                onSaveWithoutBack = { eventToSave ->
                    currentEvent = eventToSave
                    scope.launch {
                        repository.saveEvent(eventToSave)
                    }
                },
                onSaveAndShowSettlement = { eventToSave ->
                    currentEvent = eventToSave
                    scope.launch {
                        repository.saveEvent(eventToSave)
                        navController.navigate(Routes.Settlement)
                    }
                },
                onMyNameChanged = { newName ->
                    settings.myName = newName
                },
                memberHistory = memberHistory,
                expenseTitleHistory = expenseTitleHistory,
                onExpenseTitleHistoryChanged = {
                    expenseTitleHistory = it
                },
            )
        }
        composable(Routes.Settlement) {
            SettlementScreen(
                event = currentEvent,
                onBack = { navController.popBackStack() },
                onShare = { ShareUtils.shareText(context, it, "精算結果を共有") },
                onShareToLine = { ShareUtils.shareTextToLine(context, it) }
            )
        }
        composable(Routes.PrivacyPolicy) {
            PrivacyPolicyTextScreen(
                text = BundledPrivacyPolicy.Text,
                onBack = { navController.popBackStack() }
            )
        }
    }

}
