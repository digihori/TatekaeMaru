package tk.horiuchi.tatekaemaru.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class EventSetupTest {
    private class Settings : AppSettings {
        override var initialSampleCreated = false
        override var myName = "自分"
    }

    private class Repository : EventRepository {
        val events = mutableListOf<Event>()
        var failSave = false
        override fun observeEventSummaries(): Flow<List<EventSummary>> = flowOf(emptyList())
        override suspend fun hasEvents() = events.isNotEmpty()
        override suspend fun loadEvent(eventId: String) = events.find { it.id == eventId }
        override suspend fun saveEvent(event: Event) {
            check(!failSave)
            events.add(event)
        }
        override suspend fun deleteEvent(eventId: String) { events.removeAll { it.id == eventId } }
        override suspend fun getMemberNameHistory() = emptyList<String>()
        override suspend fun getExpenseTitleHistory() = emptyList<String>()
    }

    @Test
    fun sampleIsCreatedOnlyOnceEvenAfterUserDeletesIt() = runTest {
        val repository = Repository()
        val settings = Settings()
        val setup = EventSetup(repository, settings)
        setup.initialize()
        assertEquals(listOf(SampleRepository.sampleEvent), repository.events)
        assertTrue(settings.initialSampleCreated)
        repository.events.clear()
        setup.initialize()
        assertTrue(repository.events.isEmpty())
    }

    @Test
    fun existingEventsArePreservedAndNewEventUsesLatestName() = runTest {
        val repository = Repository()
        val existing = Event(name = "保存済み")
        repository.events.add(existing)
        val settings = Settings()
        val setup = EventSetup(repository, settings)
        setup.initialize()
        assertEquals(listOf(existing), repository.events)
        assertTrue(settings.initialSampleCreated)
        settings.myName = "花子"
        assertEquals("花子", setup.createEvent().members.single().name)
        assertTrue(setup.createEvent().expenses.isEmpty())
    }

    @Test
    fun failedSampleSaveDoesNotMarkSetupComplete() = runTest {
        val repository = Repository().apply { failSave = true }
        val settings = Settings()
        assertFailsWith<IllegalStateException> { EventSetup(repository, settings).initialize() }
        assertFalse(settings.initialSampleCreated)
    }
}
