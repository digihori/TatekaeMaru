package tk.horiuchi.tatekaemaru.data

import java.time.LocalDate

object SampleRepository {
    private val me = Member(id = "sample-member-me", name = "私")
    private val friendA = Member(id = "sample-member-a", name = "友人A")
    private val friendB = Member(id = "sample-member-b", name = "友人B")

    val sampleEvent = Event(
        id = "initial-sample-event-v1",
        name = "サンプル 釣り2026/07/05",
        date = LocalDate.of(2026, 7, 5),
        members = listOf(me, friendA, friendB),
        expenses = listOf(
            Expense(
                id = "sample-expense-highway",
                title = "高速代",
                amount = 3000,
                payerId = friendA.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id)
            ),
            Expense(
                id = "sample-expense-gas",
                title = "ガス代",
                amount = 2500,
                payerId = friendA.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id),
                gasInfo = GasInfo(distanceKm = 120.0, fuelEfficiencyKmPerLiter = 15.0, gasPricePerLiter = 170)
            ),
            Expense(
                id = "sample-expense-bait",
                title = "エサ代",
                amount = 1200,
                payerId = me.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id)
            ),
            Expense(
                id = "sample-expense-boat",
                title = "ボート代",
                amount = 9000,
                payerId = me.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id)
            )
        )
    )
}
