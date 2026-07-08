package tk.horiuchi.tatekaemaru.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import tk.horiuchi.tatekaemaru.ui.FormatUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    event: Event,
    onEventChange: (Event) -> Unit,
    onBack: () -> Unit,
    onSave: (Event) -> Unit,
    onSaveAndShowSettlement: (Event) -> Unit,
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
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var eventName by remember { mutableStateOf(event.name) }
    val members = remember { mutableStateListOf(*event.members.toTypedArray()) }
    val expenses = remember { mutableStateListOf(*event.expenses.toTypedArray()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    fun buildCurrentEvent(): Event {
        return Event(
            id = event.id,
            name = eventName,
            date = event.date,
            members = members.toList(),
            expenses = expenses.toList()
        )
    }

    fun handleBack() {
        if (buildCurrentEvent() != event) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    fun saveEvent() {
        val updatedEvent = buildCurrentEvent()
        onEventChange(updatedEvent)
        onSave(updatedEvent)
    }

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
        //expenseTitle = ""
        expenseAmount = ""
        //payerId = members.firstOrNull()?.id.orEmpty()
        //targetMemberIds = members.map { it.id }.toSet()
        editingExpenseId = null
        useGasCalculator = false
        distanceKm = ""
        fuelEfficiency = ""
        gasPrice = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("イベント編集") },
                navigationIcon = {
                    IconButton(
                        //onClick = onBack
                        onClick = { handleBack() }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "入力済み：${expenses.size}件 / 合計 ${FormatUtils.yen(expenses.sumOf { it.amount })}",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { handleBack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("戻る")
                    }

                    Button(
                        onClick = { saveEvent() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }

                    Button(
                        enabled = members.isNotEmpty() && expenses.isNotEmpty(),
                        onClick = {
                            onSaveAndShowSettlement(buildCurrentEvent())
                        },
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text("精算結果")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val placeholderText =
                "イベント" + LocalDate.now().format(
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")
                )
            item {
                OutlinedTextField(
                    //value = event.name,
                    //onValueChange = { onEventChange(event.copy(name = it)) },
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("イベント名") },
                    placeholder = { Text(placeholderText) },
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
                        members.forEach { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.name)
                                TextButton(onClick = {
                                    //val newMembers = event.members.filterNot { it.id == member.id }
                                    //val newExpenses = event.expenses
                                    //    .filterNot { it.payerId == member.id }
                                    //    .map { it.copy(targetMemberIds = it.targetMemberIds - member.id) }
                                    //onEventChange(event.copy(members = newMembers, expenses = newExpenses))
                                    members.removeAll { it.id == member.id }

                                    val updatedExpenses = expenses
                                        .filterNot { it.payerId == member.id }
                                        .map {
                                            it.copy(
                                                targetMemberIds = it.targetMemberIds - member.id
                                            )
                                        }

                                    expenses.clear()
                                    expenses.addAll(updatedExpenses)
                                    if (payerId == member.id) payerId = members.firstOrNull()?.id.orEmpty()
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
                                    //onEventChange(event.copy(members = event.members + member))
                                    members.add(member)
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
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("参加人数：${members.size}人")
                                Text("支払項目：${expenses.size}件")
                                Text("入力済み合計：${FormatUtils.yen(expenses.sumOf { it.amount })}")
                            }
                        }
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
                            calculatedGasAmount()?.let { Text("計算結果：${FormatUtils.yen(it)}") }
                        }

                        PayerPicker(
                            members = members,
                            selectedMemberId = payerId,
                            onSelected = { payerId = it }
                        )

                        Text("立替対象メンバー")
                        members.forEach { member ->
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
                                enabled = members.isNotEmpty(),
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
                                            id = editingExpenseId ?: java.util.UUID.randomUUID().toString(),
                                            title = title,
                                            amount = amount,
                                            payerId = payerId,
                                            targetMemberIds = targetMemberIds,
                                            gasInfo = gasInfo
                                        )
                                        if (editingExpenseId == null) {
                                            expenses.add(expense)
                                        } else {
                                            val index = expenses.indexOfFirst {
                                                it.id == editingExpenseId
                                            }
                                            if (index >= 0) {
                                                expenses[index] = expense
                                            }
                                        }
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
            items(expenses) { expense ->
                val payerName = members.firstOrNull { it.id == expense.payerId }?.name ?: "不明"
                val targetNames = members
                    .filter { it.id in expense.targetMemberIds }
                    .joinToString("、") { it.name }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${expense.title}：${FormatUtils.yen(expense.amount)}")
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
                                coroutineScope.launch {
                                    listState.animateScrollToItem(2)
                                }
                            }) {
                                Text("修正")
                            }
                            TextButton(onClick = {
                                //onEventChange(event.copy(expenses = event.expenses.filterNot { it.id == expense.id }))
                                expenses.removeAll { it.id == expense.id }
                                if (editingExpenseId == expense.id) resetExpenseForm()
                            }) {
                                Text("削除")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = {
                showUnsavedDialog = false
            },
            title = {
                Text("未保存の変更があります")
            },
            text = {
                Column {
                    Text("保存してから戻りますか？")

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            saveEvent()
                            showUnsavedDialog = false
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("保存して戻る")
                    }

                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("破棄して戻る")
                    }

                    TextButton(
                        onClick = {
                            showUnsavedDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("キャンセル")
                    }
                }
            },
            confirmButton = {}
        )
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
