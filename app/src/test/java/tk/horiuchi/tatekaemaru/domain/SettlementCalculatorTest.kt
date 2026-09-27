package tk.horiuchi.tatekaemaru.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.Member

class SettlementCalculatorTest {
    private val alice = Member(id = "alice", name = "Alice")
    private val bob = Member(id = "bob", name = "Bob")
    private val carol = Member(id = "carol", name = "Carol")

    @Test
    fun `全員負担の支払いを精算する`() {
        val event = Event(
            name = "旅行",
            members = listOf(alice, bob, carol),
            expenses = listOf(
                Expense(
                    title = "宿泊費",
                    amount = 3_000,
                    payerId = alice.id,
                    targetMemberIds = setOf(alice.id, bob.id, carol.id)
                )
            )
        )

        val result = SettlementCalculator.calculate(event)

        assertEquals(
            mapOf(alice.id to 2_000, bob.id to -1_000, carol.id to -1_000),
            result.balances.associate { it.memberId to it.balance }
        )
        assertEquals(2_000, result.transfers.sumOf { it.amount })
        assertEquals(setOf(bob.id, carol.id), result.transfers.map { it.fromMemberId }.toSet())
        assertEquals(setOf(alice.id), result.transfers.map { it.toMemberId }.toSet())
    }

    @Test
    fun `一部メンバーだけの支払いを精算する`() {
        val event = Event(
            name = "食事",
            members = listOf(alice, bob, carol),
            expenses = listOf(
                Expense(
                    title = "昼食",
                    amount = 1_000,
                    payerId = bob.id,
                    targetMemberIds = setOf(bob.id, carol.id)
                )
            )
        )

        val result = SettlementCalculator.calculate(event)

        assertEquals(
            mapOf(alice.id to 0, bob.id to 500, carol.id to -500),
            result.balances.associate { it.memberId to it.balance }
        )
        assertEquals(1, result.transfers.size)
        assertEquals(carol.id, result.transfers.single().fromMemberId)
        assertEquals(bob.id, result.transfers.single().toMemberId)
        assertEquals(500, result.transfers.single().amount)
    }

    @Test
    fun `同じ対象者の端数をまとめて公平に配分する`() {
        val event = Event(
            name = "端数",
            members = listOf(alice, bob),
            expenses = listOf(
                Expense("one", "支払い1", 1, alice.id, setOf(alice.id, bob.id)),
                Expense("two", "支払い2", 1, alice.id, setOf(alice.id, bob.id))
            )
        )

        val result = SettlementCalculator.calculate(event)

        assertEquals(
            mapOf(alice.id to 1, bob.id to -1),
            result.balances.associate { it.memberId to it.balance }
        )
        assertEquals(1, result.transfers.single().amount)
    }
}
