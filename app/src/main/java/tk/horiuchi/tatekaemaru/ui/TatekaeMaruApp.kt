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
import tk.horiuchi.tatekaemaru.data.Member
import tk.horiuchi.tatekaemaru.data.EventRepository
import tk.horiuchi.tatekaemaru.data.SampleRepository
import tk.horiuchi.tatekaemaru.data.local.AppDatabase
import tk.horiuchi.tatekaemaru.ui.screens.EventEditScreen
import tk.horiuchi.tatekaemaru.ui.screens.HomeScreen
import tk.horiuchi.tatekaemaru.ui.screens.PrivacyPolicyScreen
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
    val repository = remember {
        EventRepository(AppDatabase.getInstance(context).tatekaeDao())
    }
    val savedEvents by repository.observeEventSummaries().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var currentEvent by remember { mutableStateOf(SampleRepository.sampleEvent) }
    val preferences = remember { context.getSharedPreferences("tatekae_maru_settings", android.content.Context.MODE_PRIVATE) }
    //val myName = preferences.getString("my_name", "自分") ?: "自分"
    var showSampleEvent by remember { mutableStateOf(preferences.getBoolean("show_sample_event", true)) }
    var memberHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var expenseTitleHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        memberHistory = repository.getMemberNameHistory()
        expenseTitleHistory = repository.getExpenseTitleHistory()
    }

    NavHost(navController = navController, startDestination = Routes.Home) {
        composable(Routes.Home) {
            HomeScreen(
                events = savedEvents,
                onCreateEvent = {
                    val latestMyName = preferences.getString("my_name", "自分") ?: "自分"
                    currentEvent = Event(
                        name = "",
                        members = listOf(
                            Member(name = latestMyName)
                        )
                    )
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
                showSampleEvent = showSampleEvent,
                onOpenSampleEvent = {
                    currentEvent = SampleRepository.sampleEvent
                    navController.navigate(Routes.EventEdit)
                },
                onDeleteSampleEvent = {
                    showSampleEvent = false
                    preferences.edit().putBoolean("show_sample_event", false).apply()
                },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) }
            )
        }
        composable(Routes.EventEdit) {
            EventEditScreen(
                event = currentEvent,
                onEventChange = { currentEvent = it },
                onBack = { navController.popBackStack() },
                onSave = { eventToSave ->
                    scope.launch {
                        currentEvent = eventToSave
                        repository.saveEvent(eventToSave)
                        navController.popBackStack(Routes.Home, inclusive = false)
                    }
                },
                onSaveWithoutBack = { eventToSave ->
                    scope.launch {
                        currentEvent = eventToSave
                        repository.saveEvent(eventToSave)
                    }
                },
                onSaveAndShowSettlement = { eventToSave ->
                    scope.launch {
                        currentEvent = eventToSave
                        repository.saveEvent(eventToSave)
                        navController.navigate(Routes.Settlement)
                    }
                },
                //onShowSettlement = { navController.navigate(Routes.Settlement) },
                onMyNameChanged = { newName ->
                    preferences.edit()
                        .putString("my_name", newName)
                        .apply()
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
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PrivacyPolicy) {
            PrivacyPolicyScreen(
                policyUrl = PrivacyPolicyConfig.Url,
                onBack = { navController.popBackStack() }
            )
        }
    }

}
