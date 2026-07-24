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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalFocusManager
import java.time.LocalDateTime
import androidx.compose.foundation.layout.imePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    event: Event,
    onEventChange: (Event) -> Unit,
    onBack: () -> Unit,
    //onSave: (Event) -> Unit,
    onSaveWithoutBack: (Event) -> Unit,
    onSaveAndShowSettlement: (Event) -> Unit,
    //onShowSettlement: () -> Unit,
    onMyNameChanged: (String) -> Unit,
    memberHistory: List<String>,
    expenseTitleHistory: List<String>,
    onExpenseTitleHistoryChanged: (List<String>) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var showMyNameDialog by remember { mutableStateOf(false) }
    var editingMyName by remember { mutableStateOf("") }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var showExpenseForm by remember { mutableStateOf(false) }
    var payerId by remember(event.members) { mutableStateOf(event.members.firstOrNull()?.id.orEmpty()) }
    var targetMemberIds by remember(event.members) { mutableStateOf(event.members.map { it.id }.toSet()) }
    var editingExpenseId by remember { mutableStateOf<String?>(null) }
    var useGasCalculator by remember { mutableStateOf(false) }
    var distanceKm by remember { mutableStateOf("") }
    var fuelEfficiency by remember { mutableStateOf("") }
    var gasPrice by remember { mutableStateOf("") }
//    var showUnsavedDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var eventName by remember { mutableStateOf(event.name) }
    val members = remember { mutableStateListOf(*event.members.toTypedArray()) }
    val expenses = remember { mutableStateListOf(*event.expenses.toTypedArray()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMemberSuggestions by remember { mutableStateOf(false) }
    var showExpenseTitleSuggestions by remember { mutableStateOf(false) }
    var pendingMemberToAdd by remember { mutableStateOf<Member?>(null) }

    val focusManager = LocalFocusManager.current

    fun buildCurrentEvent(): Event {
        return Event(
            id = event.id,
            name = eventName,
            date = event.date,
            members = members.toList(),
            expenses = expenses.toList()
        )
    }

    /*
    fun handleBack() {
        val current = buildCurrentEvent()

        val eventToSave = if (current.name.isBlank()) {
            current.copy(
                name = LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern(
                        "yyyy/MM/dd HH:mm"
                    )
                )
            )
        } else {
            current.copy(
                name = current.name.trim()
            )
        }

        onEventChange(eventToSave)
        onSaveWithoutBack(eventToSave)
        onBack()
    }

     */
    fun handleBack() {
        val current = buildCurrentEvent()

        val eventToSave = current.copy(
            name = current.name.trim().ifBlank {
                LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern(
                        "yyyy/MM/dd HH:mm"
                    )
                )
            }
        )

        onEventChange(eventToSave)
        onSaveWithoutBack(eventToSave)
        onBack()
    }

    //fun saveEvent() {
    //    val updatedEvent = buildCurrentEvent()
    //    onEventChange(updatedEvent)
    //    onSave(updatedEvent)
    //}

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
        editingExpenseId = null
        useGasCalculator = false
        distanceKm = ""
        fuelEfficiency = ""
        gasPrice = ""
    }

    fun saveExpense() {
        // 今の追加・修正処理
        val title = expenseTitle.trim()
        val gasAmount =
            if (useGasCalculator) calculatedGasAmount() else null
        val amount = gasAmount ?: expenseAmount.toIntOrNull() ?: 0
        val gasInfo = if (useGasCalculator && gasAmount != null) {
            GasInfo(
                distanceKm = distanceKm.toDoubleOrNull() ?: 0.0,
                fuelEfficiencyKmPerLiter = fuelEfficiency.toDoubleOrNull()
                    ?: 0.0,
                gasPricePerLiter = gasPrice.toIntOrNull() ?: 0
            )
        } else {
            null
        }
        if (title.isNotEmpty() && amount > 0 && payerId.isNotBlank() && targetMemberIds.isNotEmpty()) {
            val expense = Expense(
                id = editingExpenseId ?: java.util.UUID.randomUUID()
                    .toString(),
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

            // ★ 履歴を即更新
            val updatedHistory =
                listOf(expense.title) + expenseTitleHistory.filter { it != expense.title }
            onExpenseTitleHistoryChanged(updatedHistory)

            val updatedEvent = buildCurrentEvent()
            onEventChange(updatedEvent)
            onSaveWithoutBack(updatedEvent)

            resetExpenseForm()
            showExpenseForm = false

            coroutineScope.launch {
                listState.animateScrollToItem(1)
            }
        }
    }

    fun cancelExpenseEdit() {
        resetExpenseForm()
        showExpenseForm = false

        coroutineScope.launch {
            listState.animateScrollToItem(1)
        }

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
            if (showExpenseForm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { saveExpense() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (editingExpenseId == null)
                                "項目を追加"
                            else
                                "変更を保存"
                        )
                    }

                    OutlinedButton(
                        onClick = { cancelExpenseEdit() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("キャンセル")
                    }
                }
            } else {
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
                        Button(
                            onClick = { handleBack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("戻る")
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
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("イベント名") },
                    placeholder = { Text(placeholderText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        /*.onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                val updatedEvent = buildCurrentEvent()
                                onEventChange(updatedEvent)
                                onSaveWithoutBack(updatedEvent)
                            }
                        }*/
                        ,
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
                        members.forEachIndexed { index, member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {

                                    val isMe = member.id == members.firstOrNull()?.id

                                    Text(
                                        text = member.name,
                                        color = if (isMe) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        textDecoration = if (isMe) {
                                            TextDecoration.Underline
                                        } else {
                                            TextDecoration.None
                                        },
                                        modifier = if (isMe) {
                                            Modifier.clickable {
                                                editingMyName = member.name
                                                showMyNameDialog = true
                                            }
                                        } else {
                                            Modifier
                                        }
                                    )

                                    val paidExpenses = expenses.filter { it.payerId == member.id }
                                    val paidTotal = paidExpenses.sumOf { it.amount }

                                    Text(
                                        text = "${paidExpenses.size}件 / ${FormatUtils.yen(paidTotal)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row {

                                    TextButton(
                                        onClick = {
                                            payerId = member.id
                                            editingExpenseId = null
                                            showExpenseForm = true

                                            coroutineScope.launch {
                                                listState.animateScrollToItem(2)
                                            }
                                        }
                                    ) {
                                        Text("＋支払い")
                                    }

                                    TextButton(onClick = {
                                        memberToDelete = member
                                    }) {
                                        Text("削除")
                                    }
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = memberName,
                                onValueChange = {
                                    memberName = it
                                    showMemberSuggestions = it.isBlank()
                                },
                                label = {
                                    Text("メンバー名")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { focusState ->
                                        showMemberSuggestions = focusState.isFocused
                                    },
                                singleLine = true
                            )

                            Button(onClick = {
                                val name = memberName.trim()
                                if (name.isNotEmpty()) {
                                    val member = Member(name = name)
                                    if (expenses.isNotEmpty()) {
                                        pendingMemberToAdd = member
                                    } else {
                                        members.add(member)
                                        if (payerId.isBlank()) payerId = member.id
                                        targetMemberIds = targetMemberIds + member.id
                                        //memberName = ""

                                        val updatedEvent = buildCurrentEvent()
                                        onEventChange(updatedEvent)
                                        onSaveWithoutBack(updatedEvent)
                                    }

                                    // 入力状態をリセット
                                    memberName = ""
                                    showMemberSuggestions = false
                                    focusManager.clearFocus()
                                }
                            }) {
                                Text("追加")
                            }
                        }

                        val availableMemberHistory = memberHistory.filter { historyName ->
                            members.none { it.name == historyName }
                        }

                        if (showMemberSuggestions && availableMemberHistory.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .widthIn(
                                        min = 160.dp,
                                        max = 280.dp
                                    ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                )
                            ) {
                                Column {
                                    availableMemberHistory
                                        .take(6)
                                        .forEachIndexed { index, historyName ->

                                            Text(
                                                text = historyName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        memberName = historyName
                                                        showMemberSuggestions = false
                                                        focusManager.clearFocus()
                                                    }
                                                    .padding(
                                                        horizontal = 16.dp,
                                                        vertical = 12.dp
                                                    )
                                            )
                                            if (index < availableMemberHistory.take(6).lastIndex) {
                                                HorizontalDivider()
                                            }
                                        }
                                }
                            }
                        }

                    }
                }
            }

            if (showExpenseForm || editingExpenseId != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            //Text(if (editingExpenseId == null) "支払項目を追加" else "支払項目を修正")
                            val selectedPayerName =
                                members.firstOrNull { it.id == payerId }?.name ?: "未選択"

                            Text(
                                text = if (editingExpenseId == null)
                                    "支払いを追加"
                                else
                                    "支払いを修正",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "支払者：$selectedPayerName",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            OutlinedTextField(
                                value = expenseTitle,
                                onValueChange = {
                                    expenseTitle = it
                                    showExpenseTitleSuggestions = it.isBlank()
                                },
                                label = {
                                    Text("項目名")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        showExpenseTitleSuggestions = focusState.isFocused
                                    },
                                singleLine = true
                            )
                            if (showExpenseTitleSuggestions && expenseTitleHistory.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .widthIn(
                                            min = 160.dp,
                                            max = 280.dp
                                        ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 6.dp
                                    )
                                ) {
                                    Column {
                                        expenseTitleHistory
                                            .take(6)
                                            .forEachIndexed { index, title ->

                                                Text(
                                                    text = title,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            expenseTitle = title
                                                            showExpenseTitleSuggestions = false
                                                            focusManager.clearFocus()
                                                        }
                                                        .padding(
                                                            horizontal = 16.dp,
                                                            vertical = 12.dp
                                                        )
                                                )

                                                if (
                                                    index <
                                                    expenseTitleHistory.take(6).lastIndex
                                                ) {
                                                    HorizontalDivider()
                                                }
                                            }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = expenseAmount,
                                onValueChange = {
                                    expenseAmount = it.filter { ch -> ch.isDigit() }
                                },
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
                                        if (checked) expenseTitle = "ガス代"
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
                                        calculatedGasAmount()?.let { amount ->
                                            expenseAmount = amount.toString()
                                        }
                                    },
                                    label = { Text("走行距離 km") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = fuelEfficiency,
                                    onValueChange = {
                                        fuelEfficiency =
                                            it.filter { ch -> ch.isDigit() || ch == '.' }
                                        calculatedGasAmount()?.let { amount ->
                                            expenseAmount = amount.toString()
                                        }
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
                                        calculatedGasAmount()?.let { amount ->
                                            expenseAmount = amount.toString()
                                        }
                                    },
                                    label = { Text("ガソリン単価 円/L") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Text("計算式：走行距離 ÷ 燃費 × 単価")
                                calculatedGasAmount()?.let { Text("計算結果：${FormatUtils.yen(it)}") }
                            }

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
                        }
                    }
                }
            }

            item { Text("支払項目一覧") }

            items(
                items = members,
                key = { it.id }
            ) { member ->

                val memberExpenses = expenses.filter {
                    it.payerId == member.id
                }

                val paidTotal = memberExpenses.sumOf {
                    it.amount
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                Text(
                                    text = "${memberExpenses.size}件 / ${FormatUtils.yen(paidTotal)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        memberExpenses.forEach { expense ->

                            val targetNames = members
                                .filter {
                                    it.id in expense.targetMemberIds
                                }
                                .joinToString("、") {
                                    it.name
                                }

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        "${expense.title}：${FormatUtils.yen(expense.amount)}"
                                    )

                                    Text(
                                        "対象：$targetNames",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                editingExpenseId = expense.id
                                                expenseTitle = expense.title
                                                expenseAmount = expense.amount.toString()
                                                payerId = expense.payerId
                                                targetMemberIds = expense.targetMemberIds

                                                useGasCalculator =
                                                    expense.gasInfo != null

                                                distanceKm =
                                                    expense.gasInfo
                                                        ?.distanceKm
                                                        ?.toString()
                                                        .orEmpty()

                                                fuelEfficiency =
                                                    expense.gasInfo
                                                        ?.fuelEfficiencyKmPerLiter
                                                        ?.toString()
                                                        .orEmpty()

                                                gasPrice =
                                                    expense.gasInfo
                                                        ?.gasPricePerLiter
                                                        ?.toString()
                                                        .orEmpty()

                                                showExpenseForm = true

                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(2)
                                                }
                                            }
                                        ) {
                                            Text("修正")
                                        }

                                        TextButton(
                                            onClick = {
                                                expenses.removeAll {
                                                    it.id == expense.id
                                                }

                                                val updatedEvent =
                                                    buildCurrentEvent()

                                                onEventChange(updatedEvent)
                                                onSaveWithoutBack(updatedEvent)
                                            }
                                        ) {
                                            Text("削除")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*
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

     */
    if (showMyNameDialog) {
        AlertDialog(
            onDismissRequest = {
                showMyNameDialog = false
            },
            title = {
                Text("自分の名前を変更")
            },
            text = {
                OutlinedTextField(
                    value = editingMyName,
                    onValueChange = {
                        editingMyName = it
                    },
                    label = {
                        Text("名前")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newName = editingMyName.trim()

                        if (newName.isNotEmpty() &&
                            members.isNotEmpty()
                        ) {
                            val oldMember = members[0]

                            members[0] = oldMember.copy(
                                name = newName
                            )

                            onMyNameChanged(newName)

                            val updatedEvent = buildCurrentEvent()
                            onEventChange(updatedEvent)
                            onSaveWithoutBack(updatedEvent)
                        }

                        showMyNameDialog = false
                    }
                ) {
                    Text("変更")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMyNameDialog = false
                    }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }

    memberToDelete?.let { member ->

        val paidExpenses = expenses.filter {
            it.payerId == member.id
        }

        val targetExpenses = expenses.filter {
            member.id in it.targetMemberIds
        }

        val paidTotal = paidExpenses.sumOf {
            it.amount
        }

        AlertDialog(
            onDismissRequest = {
                memberToDelete = null
            },
            title = {
                Text("${member.name}を削除しますか？")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (paidExpenses.isNotEmpty()) {
                        Text(
                            "${member.name}には " +
                                    "${paidExpenses.size}件、" +
                                    "合計${FormatUtils.yen(paidTotal)}の支払いが登録されています。"
                        )

                        Text(
                            "メンバーを削除すると、これらの支払いも削除されます。"
                        )
                    }

                    if (targetExpenses.isNotEmpty()) {
                        Text(
                            "${targetExpenses.size}件の支払いで" +
                                    "立替対象メンバーに含まれています。"
                        )

                        Text(
                            "削除後は、それらの支払いの対象から外れます。"
                        )
                    }

                    if (
                        paidExpenses.isEmpty() &&
                        targetExpenses.isEmpty()
                    ) {
                        Text(
                            "このメンバーをイベントから削除します。"
                        )
                    }

                    Text("この操作は元に戻せません。")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        // ここで初めて実際に削除する

                        members.removeAll {
                            it.id == member.id
                        }

                        val updatedExpenses = expenses
                            // この人が支払った項目は削除
                            .filterNot {
                                it.payerId == member.id
                            }
                            // 他の支払いでは対象者から除外
                            .map {
                                it.copy(
                                    targetMemberIds =
                                        it.targetMemberIds - member.id
                                )
                            }

                        expenses.clear()
                        expenses.addAll(updatedExpenses)

                        if (payerId == member.id) {
                            payerId =
                                members.firstOrNull()
                                    ?.id
                                    .orEmpty()
                        }

                        targetMemberIds =
                            targetMemberIds - member.id

                        val updatedEvent =
                            buildCurrentEvent()

                        onEventChange(updatedEvent)
                        onSaveWithoutBack(updatedEvent)

                        memberToDelete = null
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        memberToDelete = null
                    }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }

    pendingMemberToAdd?.let { member ->

        AlertDialog(
            onDismissRequest = {
                pendingMemberToAdd = null
            },
            title = {
                Text("既存の支払いにも追加しますか？")
            },
            text = {
                Text(
                    "${member.name}を追加します。\n\n" +
                            "すでに${expenses.size}件の支払いがあります。\n" +
                            "このメンバーを既存の支払いの対象にも含めますか？"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        members.add(member)

                        // 既存の全支払い対象に追加
                        val updatedExpenses = expenses.map { expense ->
                            expense.copy(
                                targetMemberIds =
                                    expense.targetMemberIds + member.id
                            )
                        }

                        expenses.clear()
                        expenses.addAll(updatedExpenses)

                        if (payerId.isBlank()) {
                            payerId = member.id
                        }

                        targetMemberIds =
                            targetMemberIds + member.id

                        val updatedEvent =
                            buildCurrentEvent()

                        onEventChange(updatedEvent)
                        onSaveWithoutBack(updatedEvent)

                        pendingMemberToAdd = null
                    }
                ) {
                    Text("すべてに追加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        members.add(member)

                        if (payerId.isBlank()) {
                            payerId = member.id
                        }

                        // 今後の新規支払いでは対象候補に含める
                        targetMemberIds =
                            targetMemberIds + member.id

                        val updatedEvent =
                            buildCurrentEvent()

                        onEventChange(updatedEvent)
                        onSaveWithoutBack(updatedEvent)

                        pendingMemberToAdd = null
                    }
                ) {
                    Text("追加しない")
                }
            }
        )
    }

}

