package tk.horiuchi.tatekaemaru.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class JsonEventRepositoryTest {
    private class Storage : EventDocumentStorage {
        var text: String? = null
        var failWrite = false
        override fun read() = text
        override fun write(document: String) {
            check(!failWrite) { "disk full" }
            text = document
        }
    }

    @Test
    fun eventsRoundTripIncludingJapaneseGasAndTargets() = runTest {
        val storage = Storage()
        val repository = JsonEventRepository(storage)
        assertFalse(repository.hasEvents())
        val event = SampleRepository.sampleEvent
        repository.saveEvent(event)
        val reopened = JsonEventRepository(storage)
        assertEquals(event, reopened.loadEvent(event.id))
        assertEquals(listOf(EventSummary(event.id, event.name, "2026-07-05")), reopened.observeEventSummaries().first())
        reopened.deleteEvent(event.id)
        assertFalse(JsonEventRepository(storage).hasEvents())
    }

    @Test
    fun editsKeepListOrderAndRefreshHistory() = runTest {
        val storage = Storage()
        val repository = JsonEventRepository(storage)
        val first = Event(id = "a", name = "一番", members = listOf(Member("a", "太郎")))
        val second = Event(id = "b", name = "二番", members = listOf(Member("b", "花子")))
        repository.saveEvent(first)
        repository.saveEvent(second)
        repository.saveEvent(first.copy(name = "修正済み"))
        assertEquals(listOf("b", "a"), repository.observeEventSummaries().first().map { it.id })
        assertEquals(listOf("太郎", "花子"), repository.getMemberNameHistory())
        assertEquals("修正済み", JsonEventRepository(storage).loadEvent("a")?.name)
    }

    @Test
    fun failedWriteKeepsDiskAndMemoryAndCanRetry() = runTest {
        val storage = Storage()
        val repository = JsonEventRepository(storage)
        val event = Event(name = "保存済み")
        repository.saveEvent(event)
        val originalText = storage.text
        storage.failWrite = true
        assertFailsWith<IllegalStateException> { repository.saveEvent(event.copy(name = "未保存")) }
        assertEquals(event, repository.loadEvent(event.id))
        assertEquals(originalText, storage.text)
        assertFailsWith<IllegalStateException> { repository.deleteEvent(event.id) }
        assertEquals(event, repository.loadEvent(event.id))
        storage.failWrite = false
        repository.saveEvent(event.copy(name = "再試行"))
        assertEquals("再試行", JsonEventRepository(storage).loadEvent(event.id)?.name)
    }

    @Test
    fun corruptOrFutureDataIsNeverOverwritten() = runTest {
        for (text in listOf("broken json", "{\"version\":2,\"entries\":[]}")) {
            val storage = Storage().apply { this.text = text }
            val repository = JsonEventRepository(storage)
            assertFails { repository.hasEvents() }
            assertFails { repository.saveEvent(Event(name = "新規")) }
            assertEquals(text, storage.text)
        }
    }
}
