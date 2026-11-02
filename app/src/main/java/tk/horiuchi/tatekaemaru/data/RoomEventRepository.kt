package tk.horiuchi.tatekaemaru.data

import tk.horiuchi.tatekaemaru.data.local.EventRepositoryMapper
import tk.horiuchi.tatekaemaru.data.local.TatekaeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomEventRepository(private val dao: TatekaeDao) : EventRepository {
    override fun observeEventSummaries(): Flow<List<EventSummary>> = dao.observeEvents().map { events ->
        events.map { EventSummary(it.id, it.name, it.date) }
    }

    override suspend fun hasEvents(): Boolean = dao.countEvents() > 0

    override suspend fun loadEvent(eventId: String): Event? {
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

    override suspend fun saveEvent(event: Event) {
        val existingEvent = dao.getEvent(event.id)
        val sortTimestamp = existingEvent?.updatedAt ?: System.currentTimeMillis()
        val mapped = EventRepositoryMapper.toEntities(event, sortTimestamp)
        dao.replaceEvent(mapped.event, mapped.members, mapped.expenses, mapped.targets)
    }

    override suspend fun deleteEvent(eventId: String) {
        dao.deleteEvent(eventId)
    }

    override suspend fun getMemberNameHistory(): List<String> {
        return dao.getMemberNameHistory()
    }

    override suspend fun getExpenseTitleHistory(): List<String> {
        return dao.getExpenseTitleHistory()
    }
}
