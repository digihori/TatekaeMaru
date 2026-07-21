package tk.horiuchi.tatekaemaru.domain

import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.MemberBalance
import tk.horiuchi.tatekaemaru.data.SettlementResult
import tk.horiuchi.tatekaemaru.data.SettlementTransfer

object SettlementCalculator {
    fun calculate(event: Event): SettlementResult {
        val paidByMember = event.members.associate { member ->
            member.id to event.expenses.filter { it.payerId == member.id }.sumOf { it.amount }
        }

        val shouldPayByMember = mutableMapOf<String, Int>().apply {
            event.members.forEach { put(it.id, 0) }
        }

        val groupedExpenses = event.expenses
            .filter { it.targetMemberIds.isNotEmpty() }
            .groupBy { expense ->
                expense.targetMemberIds.sorted()
            }

        groupedExpenses.forEach { (targets, expenses) ->
            val totalAmount = expenses.sumOf { it.amount }

            val base = totalAmount / targets.size
            val remainder = totalAmount % targets.size

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

        return SettlementResult(
            balances = balances,
            transfers = minimizeTransfers(balances)
        )
    }

    private fun minimizeTransfers(
        balances: List<MemberBalance>
    ): List<SettlementTransfer> {

        data class WorkBalance(
            val memberId: String,
            var amount: Int
        )

        // 受け取る側
        val creditors = balances
            .filter { it.balance > 0 }
            .map {
                WorkBalance(
                    memberId = it.memberId,
                    amount = it.balance
                )
            }
            .toMutableList()

        // 支払う側
        val debtors = balances
            .filter { it.balance < 0 }
            .map {
                WorkBalance(
                    memberId = it.memberId,
                    amount = -it.balance
                )
            }
            .toMutableList()

        val transfers = mutableListOf<SettlementTransfer>()

        var debtorIndex = 0
        var creditorIndex = 0

        while (
            debtorIndex < debtors.size &&
            creditorIndex < creditors.size
        ) {
            val debtor = debtors[debtorIndex]
            val creditor = creditors[creditorIndex]

            val amount = minOf(
                debtor.amount,
                creditor.amount
            )

            if (amount > 0) {
                transfers += SettlementTransfer(
                    fromMemberId = debtor.memberId,
                    toMemberId = creditor.memberId,
                    amount = amount
                )

                debtor.amount -= amount
                creditor.amount -= amount
            }

            if (debtor.amount == 0) {
                debtorIndex++
            }

            if (creditor.amount == 0) {
                creditorIndex++
            }
        }

        return transfers
    }
}
