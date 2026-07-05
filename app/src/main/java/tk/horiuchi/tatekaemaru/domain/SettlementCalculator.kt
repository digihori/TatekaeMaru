package tk.horiuchi.tatekaemaru.domain

import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.MemberBalance
import tk.horiuchi.tatekaemaru.data.SettlementResult
import tk.horiuchi.tatekaemaru.data.SettlementTransfer
import kotlin.math.min

object SettlementCalculator {
    fun calculate(event: Event): SettlementResult {
        val paidByMember = event.members.associate { member ->
            member.id to event.expenses.filter { it.payerId == member.id }.sumOf { it.amount }
        }

        val shouldPayByMember = mutableMapOf<String, Int>().apply {
            event.members.forEach { put(it.id, 0) }
        }

        event.expenses.forEach { expense ->
            val targets = expense.targetMemberIds.toList()
            if (targets.isEmpty()) return@forEach

            val base = expense.amount / targets.size
            val remainder = expense.amount % targets.size
            targets.forEachIndexed { index, memberId ->
                val share = base + if (index < remainder) 1 else 0
                shouldPayByMember[memberId] = shouldPayByMember.getValue(memberId) + share
            }
        }

        val balances = event.members.map { member ->
            val paid = paidByMember.getValue(member.id)
            val shouldPay = shouldPayByMember.getValue(member.id)
            MemberBalance(
                memberId = member.id,
                paid = paid,
                shouldPay = shouldPay,
                balance = paid - shouldPay
            )
        }

        val debtors = balances
            .filter { it.balance < 0 }
            .map { it.memberId to -it.balance }
            .toMutableList()

        val creditors = balances
            .filter { it.balance > 0 }
            .map { it.memberId to it.balance }
            .toMutableList()

        val transfers = mutableListOf<SettlementTransfer>()
        var d = 0
        var c = 0
        while (d < debtors.size && c < creditors.size) {
            val debtor = debtors[d]
            val creditor = creditors[c]
            val amount = min(debtor.second, creditor.second)
            if (amount > 0) {
                transfers += SettlementTransfer(
                    fromMemberId = debtor.first,
                    toMemberId = creditor.first,
                    amount = amount
                )
            }
            debtors[d] = debtor.first to (debtor.second - amount)
            creditors[c] = creditor.first to (creditor.second - amount)
            if (debtors[d].second == 0) d++
            if (creditors[c].second == 0) c++
        }

        return SettlementResult(balances = balances, transfers = transfers)
    }
}
