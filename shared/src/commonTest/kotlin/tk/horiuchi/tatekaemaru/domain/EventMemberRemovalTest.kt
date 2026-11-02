package tk.horiuchi.tatekaemaru.domain

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.Member

class EventMemberRemovalTest {
    private val alice = Member(id = "alice", name = "Alice")
    private val bob = Member(id = "bob", name = "Bob")
    private val carol = Member(id = "carol", name = "Carol")

    @Test
    fun `対象者が0人になる支払いを事前に判定して削除する`() {
        val orphanedExpense = Expense(
            id = "orphaned",
            title = "Bobだけの食事",
            amount = 800,
            payerId = alice.id,
            targetMemberIds = setOf(bob.id)
        )
        val event = Event(
            name = "旅行",
            members = listOf(alice, bob),
            expenses = listOf(orphanedExpense)
        )

        val preview = EventMemberRemoval.preview(event, bob.id)
        val updated = EventMemberRemoval.remove(event, bob.id)

        assertEquals(listOf(orphanedExpense), preview.orphanedExpenses)
        assertTrue(updated.expenses.isEmpty())
        assertEquals(listOf(alice), updated.members)
    }

    @Test
    fun `削除メンバーが支払った項目を削除する`() {
        val paidByBob = Expense(
            id = "paid-by-bob",
            title = "高速代",
            amount = 1_200,
            payerId = bob.id,
            targetMemberIds = setOf(alice.id, bob.id)
        )
        val event = Event(
            name = "旅行",
            members = listOf(alice, bob),
            expenses = listOf(paidByBob)
        )

        val preview = EventMemberRemoval.preview(event, bob.id)
        val updated = EventMemberRemoval.remove(event, bob.id)

        assertEquals(listOf(paidByBob), preview.paidExpenses)
        assertTrue(updated.expenses.isEmpty())
    }

    @Test
    fun `複数対象者の支払いは削除せず対象者だけ更新する`() {
        val sharedExpense = Expense(
            id = "shared",
            title = "駐車場代",
            amount = 900,
            payerId = alice.id,
            targetMemberIds = setOf(alice.id, bob.id, carol.id)
        )
        val event = Event(
            name = "旅行",
            members = listOf(alice, bob, carol),
            expenses = listOf(sharedExpense)
        )

        val preview = EventMemberRemoval.preview(event, bob.id)
        val updated = EventMemberRemoval.remove(event, bob.id)

        assertTrue(preview.orphanedExpenses.isEmpty())
        assertEquals(listOf(sharedExpense), preview.expensesLosingTarget)
        assertEquals(setOf(alice.id, carol.id), updated.expenses.single().targetMemberIds)
        assertFalse(updated.members.any { it.id == bob.id })
    }

    @Test
    fun `削除後のイベントは精算残高の合計が0になる`() {
        val event = Event(
            name = "旅行",
            members = listOf(alice, bob, carol),
            expenses = listOf(
                Expense("a", "Bobのみ", 800, alice.id, setOf(bob.id)),
                Expense("b", "全員", 900, alice.id, setOf(alice.id, bob.id, carol.id))
            )
        )

        val updated = EventMemberRemoval.remove(event, bob.id)
        val settlement = SettlementCalculator.calculate(updated)

        assertEquals(0, settlement.balances.sumOf { it.balance })
        assertEquals(listOf("b"), updated.expenses.map { it.id })
    }
}
