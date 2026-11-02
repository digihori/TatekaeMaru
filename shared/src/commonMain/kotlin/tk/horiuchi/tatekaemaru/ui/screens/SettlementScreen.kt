package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.domain.SettlementShareText
import tk.horiuchi.tatekaemaru.domain.SettlementCalculator
import tk.horiuchi.tatekaemaru.ui.FormatUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementScreen(
    event: Event,
    onBack: () -> Unit,
    onShare: (String) -> Unit,
    onShareToLine: ((String) -> Unit)? = null
) {
    val result = SettlementCalculator.calculate(event)
    val memberName = { memberId: String -> event.members.firstOrNull { it.id == memberId }?.name ?: "不明" }

    val shareText = SettlementShareText.build(event)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("精算結果")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onBack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("戻る")
                }

                if (onShareToLine != null) {
                    Button(
                        onClick = { onShareToLine(shareText) },
                        modifier = Modifier.weight(1f)
                    ) { Text("LINE") }
                }

                OutlinedButton(
                    onClick = {
                        onShare(shareText)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("共有")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("メンバー別")
            result.balances.forEach { balance ->
                Text("${memberName(balance.memberId)}：支払 ${FormatUtils.yen(balance.paid)} / 負担 ${FormatUtils.yen(balance.shouldPay)} / 差額 ${FormatUtils.yen(balance.balance)}")
            }

            Text("精算")
            if (result.transfers.isEmpty()) {
                Text("精算はありません")
            } else {
                result.transfers.forEach { transfer ->
                    Text("${memberName(transfer.fromMemberId)} → ${memberName(transfer.toMemberId)}：${FormatUtils.yen(transfer.amount)}")
                }
            }
            Text(
                text = "支払い明細",
                style = MaterialTheme.typography.titleMedium
            )

            event.expenses.forEach { expense ->
                val payerName = event.members
                    .firstOrNull { it.id == expense.payerId }
                    ?.name ?: "不明"

                val targetNames = event.members
                    .filter { it.id in expense.targetMemberIds }
                    .joinToString("、") { it.name }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${expense.title}：${FormatUtils.yen(expense.amount)}")
                        Text("支払者：$payerName")
                        Text("対象：$targetNames")

                        expense.gasInfo?.let { gas ->
                            Text(
                                "ガス代：${gas.distanceKm}km ÷ ${gas.fuelEfficiencyKmPerLiter}km/L × ${gas.gasPricePerLiter}円/L"
                            )
                        }
                    }
                }
            }
        }
    }
}
