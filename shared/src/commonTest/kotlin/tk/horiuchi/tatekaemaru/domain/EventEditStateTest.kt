package tk.horiuchi.tatekaemaru.domain

import kotlin.test.*
import tk.horiuchi.tatekaemaru.data.*

class EventEditStateTest {
    private val a = Member("a", "太郎")
    private val b = Member("b", "花子")
    private val expense = Expense("e", "食事", 101, a.id, setOf(a.id))
    private val initial = EventEditState(Event(name = "旅行", members = listOf(a), expenses = listOf(expense)))

    @Test
    fun addingMemberCanIncludeOrExcludeExistingExpenses() {
        val excluded = initial.addMember(b)
        val included = initial.addMember(b, includeInExistingExpenses = true)
        assertEquals(listOf(a, b), included.event.members)
        assertEquals(setOf(a.id), excluded.event.expenses.single().targetMemberIds)
        assertEquals(setOf(a.id, b.id), included.event.expenses.single().targetMemberIds)
        assertEquals(listOf(a), initial.event.members)
    }

    @Test
    fun editKeepsIdentityDateAndExpenseOrder() {
        val second = expense.copy(id = "second")
        val state = initial.saveExpense(second).rename("遠足").renameMember(a.id, "自分")
            .saveExpense(expense.copy(amount = 300))
        assertEquals(initial.event.id, state.event.id)
        assertEquals(initial.event.date, state.event.date)
        assertEquals("遠足", state.event.name)
        assertEquals("自分", state.event.members.single().name)
        assertEquals(listOf("e", "second"), state.event.expenses.map { it.id })
        assertEquals(300, state.event.expenses.first().amount)
        assertEquals(listOf(second), state.deleteExpense("e").event.expenses)
    }

    @Test
    fun removingMemberAppliesSettlementRules() {
        val state = initial.addMember(b, true).saveExpense(expense.copy(id = "b-paid", payerId = b.id))
        val removed = state.removeMember(b.id)
        assertEquals(listOf(a), removed.event.members)
        assertEquals(listOf(expense), removed.event.expenses)
        assertEquals(0, SettlementCalculator.calculate(removed.event).balances.sumOf { it.balance })
    }

    @Test
    fun expenseInputPreservesIdTargetsAndGasRounding() {
        val input = ExpenseInput(
            title = " ガス代 ", amount = "999", payerId = a.id, targetMemberIds = setOf(a.id, b.id),
            editingId = "existing", useGasCalculator = true,
            distanceKm = "100", fuelEfficiency = "13", gasPrice = "170"
        )
        val result = assertNotNull(input.toExpenseOrNull())
        assertEquals("existing", result.id)
        assertEquals("ガス代", result.title)
        assertEquals(1307, result.amount)
        assertEquals(setOf(a.id, b.id), result.targetMemberIds)
        assertEquals(GasInfo(100.0, 13.0, 170), result.gasInfo)
    }

    @Test
    fun invalidExpenseIsRejectedAndLegacyGasFallbackIsPreserved() {
        val input = ExpenseInput("食事", "100", a.id, setOf(a.id))
        assertNull(input.copy(title = " ").toExpenseOrNull())
        assertNull(input.copy(amount = "0").toExpenseOrNull())
        assertNull(input.copy(amount = "2147483648").toExpenseOrNull())
        assertNull(input.copy(payerId = "").toExpenseOrNull())
        assertNull(input.copy(targetMemberIds = emptySet()).toExpenseOrNull())
        val fallback = assertNotNull(input.copy(useGasCalculator = true, fuelEfficiency = "0").toExpenseOrNull())
        assertEquals(100, fallback.amount)
        assertNull(fallback.gasInfo)
    }
}
