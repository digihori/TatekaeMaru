package tk.horiuchi.tatekaemaru.data

import kotlinx.coroutines.flow.Flow

/** Storage-independent list item. Date keeps the existing ISO display format. */
data class EventSummary(val id: String, val name: String, val date: String)

interface EventRepository {
    fun observeEventSummaries(): Flow<List<EventSummary>>
    suspend fun hasEvents(): Boolean
    suspend fun loadEvent(eventId: String): Event?
    suspend fun saveEvent(event: Event)
    suspend fun deleteEvent(eventId: String)
    suspend fun getMemberNameHistory(): List<String>
    suspend fun getExpenseTitleHistory(): List<String>
}
