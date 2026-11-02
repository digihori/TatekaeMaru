package tk.horiuchi.tatekaemaru.data.local

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.GasInfo
import tk.horiuchi.tatekaemaru.data.Member

class EventRepositoryMapperTest {
    @Test
    fun existingStorageFormatRoundTripsSharedModel() {
        val event = Event(
            id = "event", name = "旅行", date = LocalDate(2026, 7, 5),
            members = listOf(Member("a", "太郎"), Member("b", "花子")),
            expenses = listOf(Expense(
                "gas", "ガソリン代", 1360, "a", setOf("a", "b"),
                GasInfo(120.0, 15.0, 170)
            ))
        )
        val entities = EventRepositoryMapper.toEntities(event, 123L)
        assertEquals("2026-07-05", entities.event.date)
        assertEquals(123L, entities.event.updatedAt)
        assertEquals(event, EventRepositoryMapper.toModel(
            entities.event, entities.members, entities.expenses, entities.targets
        ))
    }
}
