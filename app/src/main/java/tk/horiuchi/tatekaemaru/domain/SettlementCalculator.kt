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

        return SettlementResult(
            balances = balances,
            transfers = minimizeTransfers(balances)
        )
    }

    /**
     * 送金回数が最小になる組み合わせを探索する。
     * メンバー数はレジャー用途では少人数想定なので、バックトラックで厳密に最小化する。
     */
    private fun minimizeTransfers(balances: List<MemberBalance>): List<SettlementTransfer> {
        val entries = balances
            .filter { it.balance != 0 }
            .map { BalanceWork(it.memberId, it.balance) }
            .toMutableList()

        if (entries.isEmpty()) return emptyList()

        var bestTransfers: List<SettlementTransfer>? = null

        fun search(startIndex: Int, currentTransfers: MutableList<SettlementTransfer>) {
            if (bestTransfers != null && currentTransfers.size >= bestTransfers!!.size) return

            var i = startIndex
            while (i < entries.size && entries[i].balance == 0) i++
            if (i == entries.size) {
                bestTransfers = currentTransfers.toList()
                return
            }

            val seenBalances = mutableSetOf<Int>()
            for (j in i + 1 until entries.size) {
                if (entries[i].balance * entries[j].balance >= 0) continue
                if (!seenBalances.add(entries[j].balance)) continue

                val originalI = entries[i].balance
                val originalJ = entries[j].balance

                val transfer = if (originalI < 0) {
                    SettlementTransfer(
                        fromMemberId = entries[i].memberId,
                        toMemberId = entries[j].memberId,
                        amount = minOf(-originalI, originalJ)
                    )
                } else {
                    SettlementTransfer(
                        fromMemberId = entries[j].memberId,
                        toMemberId = entries[i].memberId,
                        amount = minOf(originalI, -originalJ)
                    )
                }

                entries[j].balance = originalI + originalJ
                entries[i].balance = 0
                currentTransfers += transfer

                search(i + 1, currentTransfers)

                currentTransfers.removeAt(currentTransfers.lastIndex)
                entries[i].balance = originalI
                entries[j].balance = originalJ

                if (originalI + originalJ == 0) break
            }
        }

        search(0, mutableListOf())
        return bestTransfers.orEmpty()
    }

    private data class BalanceWork(
        val memberId: String,
        var balance: Int
    )
}
