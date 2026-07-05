package tk.horiuchi.tatekaemaru.data

import tk.horiuchi.tatekaemaru.data.local.EventEntity
import tk.horiuchi.tatekaemaru.data.local.EventRepositoryMapper
import tk.horiuchi.tatekaemaru.data.local.TatekaeDao
import kotlinx.coroutines.flow.Flow

class EventRepository(private val dao: TatekaeDao) {
    fun observeEventSummaries(): Flow<List<EventEntity>> = dao.observeEvents()

    suspend fun loadEvent(eventId: String): Event? {
        val eventEntity = dao.getEvent(eventId) ?: return null
        val members = dao.getMembers(eventId)
        val expenses = dao.getExpenses(eventId)
        val targets = if (expenses.isEmpty()) {
            emptyList()
        } else {
            dao.getTargets(expenses.map { it.id })
        }
        return EventRepositoryMapper.toModel(eventEntity, members, expenses, targets)
    }

    suspend fun saveEvent(event: Event) {
        val mapped = EventRepositoryMapper.toEntities(event)
        dao.replaceEvent(mapped.event, mapped.members, mapped.expenses, mapped.targets)
    }

    suspend fun deleteEvent(eventId: String) {
        dao.deleteEvent(eventId)
    }
}
