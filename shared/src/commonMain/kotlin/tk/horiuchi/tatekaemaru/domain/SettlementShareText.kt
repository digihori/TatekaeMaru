package tk.horiuchi.tatekaemaru.domain

import tk.horiuchi.tatekaemaru.data.Event

/** Shared text payload; opening the destination app remains platform specific. */
object SettlementShareText {
    fun build(event: Event): String {
        val result = SettlementCalculator.calculate(event)
        val memberName = { id: String -> event.members.firstOrNull { it.id == id }?.name ?: "不明" }
        val totalAmount = event.expenses.sumOf { it.amount }
        return buildString {
            appendLine("【${event.name}】")
            appendLine()

            appendLine("■ 精算結果")
            if (result.transfers.isEmpty()) {
                appendLine("精算はありません")
            } else {
                result.transfers.forEach { transfer ->
                    appendLine(
                        "${memberName(transfer.fromMemberId)} → ${memberName(transfer.toMemberId)}：${yen(transfer.amount)}"
                    )
                }
            }

            appendLine()
            appendLine("■ 各自負担額")
            result.balances.forEach { balance ->
                appendLine("${memberName(balance.memberId)}：${yen(balance.shouldPay)}")
            }

            appendLine()
            appendLine("■ 支払い明細")
            event.expenses.forEach { expense ->
                appendLine(
                    "${expense.title}：${yen(expense.amount)}（${memberName(expense.payerId)}）"
                )

                expense.gasInfo?.let { gas ->
                    appendLine(
                        "  ガス代計算：${gas.distanceKm}km ÷ ${gas.fuelEfficiencyKmPerLiter}km/L × ${gas.gasPricePerLiter}円/L"
                    )
                }
            }

            appendLine()
            appendLine("合計：${yen(totalAmount)}")
            appendLine()
            appendLine("※たてかえ丸で作成")
        }
    }

    private fun yen(amount: Int): String {
        val digits = amount.toLong().let { if (it < 0) -it else it }.toString()
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return (if (amount < 0) "-" else "") + grouped + "円"
    }
}
