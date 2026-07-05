package tk.horiuchi.tatekaemaru.data

import java.time.LocalDate

object SampleRepository {
    private val me = Member(name = "私")
    private val friendA = Member(name = "友人A")
    private val friendB = Member(name = "友人B")

    val sampleEvent = Event(
        name = "釣り2026/07/05",
        date = LocalDate.of(2026, 7, 5),
        members = listOf(me, friendA, friendB),
        expenses = listOf(
            Expense(
                title = "高速代",
                amount = 3000,
                payerId = friendA.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id)
            ),
            Expense(
                title = "ガス代",
                amount = 2500,
                payerId = friendA.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id),
                gasInfo = GasInfo(distanceKm = 120.0, fuelEfficiencyKmPerLiter = 15.0, gasPricePerLiter = 170)
            ),
            Expense(
                title = "エサ代",
                amount = 1200,
                payerId = me.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id)
            ),
            Expense(
                title = "ボート代",
                amount = 9000,
                payerId = me.id,
                targetMemberIds = setOf(me.id, friendA.id, friendB.id)
            )
        )
    )
}
