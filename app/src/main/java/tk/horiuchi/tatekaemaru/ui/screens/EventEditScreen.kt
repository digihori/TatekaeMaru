package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.GasInfo
import tk.horiuchi.tatekaemaru.data.Member
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    event: Event,
    onEventChange: (Event) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShowSettlement: () -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var payerId by remember(event.members) { mutableStateOf(event.members.firstOrNull()?.id.orEmpty()) }
    var targetMemberIds by remember(event.members) { mutableStateOf(event.members.map { it.id }.toSet()) }
    var editingExpenseId by remember { mutableStateOf<String?>(null) }
    var useGasCalculator by remember { mutableStateOf(false) }
    var distanceKm by remember { mutableStateOf("") }
    var fuelEfficiency by remember { mutableStateOf("") }
    var gasPrice by remember { mutableStateOf("") }

    fun calculatedGasAmount(): Int? {
        val distance = distanceKm.toDoubleOrNull()
        val efficiency = fuelEfficiency.toDoubleOrNull()
        val price = gasPrice.toIntOrNull()
        return if (distance != null && efficiency != null && efficiency > 0.0 && price != null && price > 0) {
            ((distance / efficiency) * price).toInt()
        } else {
            null
        }
    }

    fun resetExpenseForm() {
        expenseTitle = ""
        expenseAmount = ""
        payerId = event.members.firstOrNull()?.id.orEmpty()
        targetMemberIds = event.members.map { it.id }.toSet()
        editingExpenseId = null
        useGasCalculator = false
        distanceKm = ""
        fuelEfficiency = ""
        gasPrice = ""
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("イベント編集") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = event.name,
                    onValueChange = { onEventChange(event.copy(name = it)) },
                    label = { Text("イベント名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("参加メンバー")
                        event.members.forEach { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.name)
                                TextButton(onClick = {
                                    val newMembers = event.members.filterNot { it.id == member.id }
                                    val newExpenses = event.expenses
                                        .filterNot { it.payerId == member.id }
                                        .map { it.copy(targetMemberIds = it.targetMemberIds - member.id) }
                                    onEventChange(event.copy(members = newMembers, expenses = newExpenses))
                                    if (payerId == member.id) payerId = newMembers.firstOrNull()?.id.orEmpty()
                                    targetMemberIds = targetMemberIds - member.id
                                }) {
                                    Text("削除")
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = memberName,
                                onValueChange = { memberName = it },
                                label = { Text("メンバー名") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(onClick = {
                                val name = memberName.trim()
                                if (name.isNotEmpty()) {
                                    val member = Member(name = name)
                                    onEventChange(event.copy(members = event.members + member))
                                    if (payerId.isBlank()) payerId = member.id
                                    targetMemberIds = targetMemberIds + member.id
                                    memberName = ""
                                }
                            }) {
                                Text("追加")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(if (editingExpenseId == null) "支払項目を追加" else "支払項目を修正")
                        OutlinedTextField(
                            value = expenseTitle,
                            onValueChange = { expenseTitle = it },
                            label = { Text("項目名") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it.filter { ch -> ch.isDigit() } },
                            label = { Text("金額") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = useGasCalculator,
                                onCheckedChange = { checked ->
                                    useGasCalculator = checked
                                    if (checked && expenseTitle.isBlank()) expenseTitle = "ガス代"
                                    calculatedGasAmount()?.let { expenseAmount = it.toString() }
                                }
                            )
                            Text("ガス代計算を使う")
                        }

                        if (useGasCalculator) {
                            OutlinedTextField(
                                value = distanceKm,
                                onValueChange = {
                                    distanceKm = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    calculatedGasAmount()?.let { amount -> expenseAmount = amount.toString() }
                                },
                                label = { Text("走行距離 km") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = fuelEfficiency,
                                onValueChange = {
                                    fuelEfficiency = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    calculatedGasAmount()?.let { amount -> expenseAmount = amount.toString() }
                                },
                                label = { Text("燃費 km/L") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = gasPrice,
                                onValueChange = {
                                    gasPrice = it.filter { ch -> ch.isDigit() }
                                    calculatedGasAmount()?.let { amount -> expenseAmount = amount.toString() }
                                },
                                label = { Text("ガソリン単価 円/L") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text("計算式：走行距離 ÷ 燃費 × 単価")
                            calculatedGasAmount()?.let { Text("計算結果：${it}円") }
                        }

                        PayerPicker(
                            members = event.members,
                            selectedMemberId = payerId,
                            onSelected = { payerId = it }
                        )

                        Text("立替対象メンバー")
                        event.members.forEach { member ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = member.id in targetMemberIds,
                                    onCheckedChange = { checked ->
                                        targetMemberIds = if (checked) {
                                            targetMemberIds + member.id
                                        } else {
                                            targetMemberIds - member.id
                                        }
                                    }
                                )
                                Text(member.name)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                enabled = event.members.isNotEmpty(),
                                onClick = {
                                    val title = expenseTitle.trim()
                                    val gasAmount = if (useGasCalculator) calculatedGasAmount() else null
                                    val amount = gasAmount ?: expenseAmount.toIntOrNull() ?: 0
                                    val gasInfo = if (useGasCalculator && gasAmount != null) {
                                        GasInfo(
                                            distanceKm = distanceKm.toDoubleOrNull() ?: 0.0,
                                            fuelEfficiencyKmPerLiter = fuelEfficiency.toDoubleOrNull() ?: 0.0,
                                            gasPricePerLiter = gasPrice.toIntOrNull() ?: 0
                                        )
                                    } else {
                                        null
                                    }
                                    if (title.isNotEmpty() && amount > 0 && payerId.isNotBlank() && targetMemberIds.isNotEmpty()) {
                                        val expense = Expense(
                                            id = editingExpenseId ?: UUID.randomUUID().toString(),
                                            title = title,
                                            amount = amount,
                                            payerId = payerId,
                                            targetMemberIds = targetMemberIds,
                                            gasInfo = gasInfo
                                        )
                                        val newExpenses = if (editingExpenseId == null) {
                                            event.expenses + expense
                                        } else {
                                            event.expenses.map { if (it.id == editingExpenseId) expense else it }
                                        }
                                        onEventChange(event.copy(expenses = newExpenses))
                                        resetExpenseForm()
                                    }
                                }
                            ) {
                                Text(if (editingExpenseId == null) "支払項目を追加" else "修正を保存")
                            }
                            if (editingExpenseId != null) {
                                OutlinedButton(onClick = { resetExpenseForm() }) {
                                    Text("キャンセル")
                                }
                            }
                        }
                    }
                }
            }

            item { Text("支払項目一覧") }
            items(event.expenses) { expense ->
                val payerName = event.members.firstOrNull { it.id == expense.payerId }?.name ?: "不明"
                val targetNames = event.members
                    .filter { it.id in expense.targetMemberIds }
                    .joinToString("、") { it.name }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${expense.title}：${expense.amount}円")
                        expense.gasInfo?.let { gas ->
                            Text("ガス代：${gas.distanceKm}km ÷ ${gas.fuelEfficiencyKmPerLiter}km/L × ${gas.gasPricePerLiter}円/L")
                        }
                        Text("支払者：$payerName")
                        Text("対象：$targetNames")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                editingExpenseId = expense.id
                                expenseTitle = expense.title
                                expenseAmount = expense.amount.toString()
                                payerId = expense.payerId
                                targetMemberIds = expense.targetMemberIds
                                useGasCalculator = expense.gasInfo != null
                                distanceKm = expense.gasInfo?.distanceKm?.toString().orEmpty()
                                fuelEfficiency = expense.gasInfo?.fuelEfficiencyKmPerLiter?.toString().orEmpty()
                                gasPrice = expense.gasInfo?.gasPricePerLiter?.toString().orEmpty()
                            }) {
                                Text("修正")
                            }
                            TextButton(onClick = {
                                onEventChange(event.copy(expenses = event.expenses.filterNot { it.id == expense.id }))
                                if (editingExpenseId == expense.id) resetExpenseForm()
                            }) {
                                Text("削除")
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBack) { Text("戻る") }
                    Button(onClick = onSave) { Text("保存") }
                    Button(
                        enabled = event.members.isNotEmpty() && event.expenses.isNotEmpty(),
                        onClick = onShowSettlement
                    ) {
                        Text("精算結果を見る")
                    }
                }
            }
        }
    }
}

@Composable
private fun PayerPicker(
    members: List<Member>,
    selectedMemberId: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = members.firstOrNull { it.id == selectedMemberId }?.name ?: "支払者を選択"

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("支払者")
        OutlinedButton(
            enabled = members.isNotEmpty(),
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedName)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            members.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member.name) },
                    onClick = {
                        onSelected(member.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
