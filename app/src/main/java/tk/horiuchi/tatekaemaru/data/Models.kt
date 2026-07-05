package tk.horiuchi.tatekaemaru.data

import java.time.LocalDate
import java.util.UUID

data class Member(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Int,
    val payerId: String,
    val targetMemberIds: Set<String>,
    val gasInfo: GasInfo? = null
)

data class GasInfo(
    val distanceKm: Double,
    val fuelEfficiencyKmPerLiter: Double,
    val gasPricePerLiter: Int
) {
    val calculatedAmount: Int
        get() = ((distanceKm / fuelEfficiencyKmPerLiter) * gasPricePerLiter).toInt()
}

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: LocalDate = LocalDate.now(),
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList()
)

data class SettlementTransfer(
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Int
)

data class MemberBalance(
    val memberId: String,
    val paid: Int,
    val shouldPay: Int,
    val balance: Int
)

data class SettlementResult(
    val balances: List<MemberBalance>,
    val transfers: List<SettlementTransfer>
)
