package tk.horiuchi.tatekaemaru.data

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import tk.horiuchi.tatekaemaru.platform.newId
import tk.horiuchi.tatekaemaru.platform.today

@Serializable
data class Member(
    val id: String = newId(),
    val name: String
)

@Serializable
data class Expense(
    val id: String = newId(),
    val title: String,
    val amount: Int,
    val payerId: String,
    val targetMemberIds: Set<String>,
    val gasInfo: GasInfo? = null
)

@Serializable
data class GasInfo(
    val distanceKm: Double,
    val fuelEfficiencyKmPerLiter: Double,
    val gasPricePerLiter: Int
) {
    val calculatedAmount: Int
        get() = ((distanceKm / fuelEfficiencyKmPerLiter) * gasPricePerLiter).toInt()
}

@Serializable
data class Event(
    val id: String = newId(),
    val name: String,
    val date: LocalDate = today(),
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
