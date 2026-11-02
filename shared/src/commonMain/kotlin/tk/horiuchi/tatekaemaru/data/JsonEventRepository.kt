package tk.horiuchi.tatekaemaru.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** write must replace the document atomically or throw without replacing it. */
interface EventDocumentStorage {
    fun read(): String?
    fun write(document: String)
}

/** Small local event collection. Failed reads/writes never silently reset saved data. */
class JsonEventRepository(private val storage: EventDocumentStorage) : EventRepository {
    @Serializable
    private data class Entry(val event: Event, val revision: Long)
    @Serializable
    private data class Document(val version: Int = 1, val entries: List<Entry> = emptyList())

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var loaded = false
    private var document = Document()
    private val summaries = MutableStateFlow<List<EventSummary>>(emptyList())

    private fun load() {
        if (loaded) return
        val saved = storage.read()?.let { json.decodeFromString<Document>(it) } ?: Document()
        check(saved.version == 1) { "未対応の保存データ形式です。" }
        document = saved
        loaded = true
        publish()
    }

    private fun publish() {
        summaries.value = document.entries.map { EventSummary(it.event.id, it.event.name, it.event.date.toString()) }
    }

    private fun replace(next: Document) {
        storage.write(json.encodeToString(next))
        document = next
        publish()
    }

    override fun observeEventSummaries(): Flow<List<EventSummary>> = flow {
        mutex.withLock { load() }
        emitAll(summaries)
    }

    override suspend fun hasEvents(): Boolean = mutex.withLock { load(); document.entries.isNotEmpty() }
    override suspend fun loadEvent(eventId: String): Event? = mutex.withLock {
        load()
        document.entries.find { it.event.id == eventId }?.event
    }
    override suspend fun saveEvent(event: Event) = mutex.withLock {
        load()
        val revision = (document.entries.maxOfOrNull { it.revision } ?: 0) + 1
        val entry = Entry(event, revision)
        val exists = document.entries.any { it.event.id == event.id }
        replace(document.copy(entries = if (exists) document.entries.map {
            if (it.event.id == event.id) entry else it
        } else listOf(entry) + document.entries))
    }
    override suspend fun deleteEvent(eventId: String) = mutex.withLock {
        load()
        replace(document.copy(entries = document.entries.filterNot { it.event.id == eventId }))
    }
    override suspend fun getMemberNameHistory(): List<String> = mutex.withLock {
        load()
        document.entries.sortedByDescending { it.revision }.flatMap { it.event.members.asReversed() }
            .map { it.name }.filter { it.isNotBlank() }.distinct()
    }
    override suspend fun getExpenseTitleHistory(): List<String> = mutex.withLock {
        load()
        document.entries.sortedByDescending { it.revision }.flatMap { it.event.expenses.asReversed() }
            .map { it.title }.filter { it.isNotBlank() }.distinct()
    }
}
