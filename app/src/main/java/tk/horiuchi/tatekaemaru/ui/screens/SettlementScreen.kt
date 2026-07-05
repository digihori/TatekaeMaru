package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.domain.SettlementCalculator
import tk.horiuchi.tatekaemaru.ui.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementScreen(
    event: Event,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val result = SettlementCalculator.calculate(event)
    val memberName = { memberId: String -> event.members.firstOrNull { it.id == memberId }?.name ?: "不明" }

    val shareText = buildString {
        appendLine("【${event.name} 精算】")
        appendLine()
        appendLine("参加者：${event.members.joinToString("、") { it.name }}")
        appendLine()
        appendLine("支払明細：")
        event.expenses.forEach { expense ->
            appendLine("${memberName(expense.payerId)}：${expense.title} ${expense.amount}円")
            expense.gasInfo?.let { gas ->
                appendLine("  ガス代計算：${gas.distanceKm}km ÷ ${gas.fuelEfficiencyKmPerLiter}km/L × ${gas.gasPricePerLiter}円/L")
            }
        }
        appendLine()
        appendLine("精算：")
        if (result.transfers.isEmpty()) {
            appendLine("精算はありません")
        } else {
            result.transfers.forEach { transfer ->
                appendLine("${memberName(transfer.fromMemberId)} → ${memberName(transfer.toMemberId)}：${transfer.amount}円")
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("精算結果") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("メンバー別")
            result.balances.forEach { balance ->
                Text("${memberName(balance.memberId)}：支払 ${balance.paid}円 / 負担 ${balance.shouldPay}円 / 差額 ${balance.balance}円")
            }

            Text("精算")
            if (result.transfers.isEmpty()) {
                Text("精算はありません")
            } else {
                result.transfers.forEach { transfer ->
                    Text("${memberName(transfer.fromMemberId)} → ${memberName(transfer.toMemberId)}：${transfer.amount}円")
                }
            }

            Button(onClick = {
                ShareUtils.shareTextToLine(context, shareText)
            }) {
                Text("LINEへ共有")
            }

            OutlinedButton(onClick = {
                ShareUtils.shareText(context, shareText, "精算結果を共有")
            }) {
                Text("ほかのアプリへ共有")
            }

            OutlinedButton(onClick = onBack) { Text("戻る") }
        }
    }
}
