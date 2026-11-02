package tk.horiuchi.tatekaemaru.domain

import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.GasInfo
import tk.horiuchi.tatekaemaru.data.Member

/** Immutable editing state; UI state containers are supplied by each platform. */
data class EventEditState(val event: Event) {
    fun rename(name: String) = copy(event = event.copy(name = name))

    fun renameMember(id: String, name: String) = copy(event = event.copy(
        members = event.members.map { if (it.id == id) it.copy(name = name) else it }
    ))

    fun addMember(member: Member, includeInExistingExpenses: Boolean = false) = copy(
        event = event.copy(
            members = event.members + member,
            expenses = if (includeInExistingExpenses) event.expenses.map {
                it.copy(targetMemberIds = it.targetMemberIds + member.id)
            } else event.expenses
        )
    )

    fun removeMember(id: String) = copy(event = EventMemberRemoval.remove(event, id))

    fun saveExpense(expense: Expense): EventEditState {
        val exists = event.expenses.any { it.id == expense.id }
        return copy(event = event.copy(expenses = if (exists) event.expenses.map {
            if (it.id == expense.id) expense else it
        } else event.expenses + expense))
    }

    fun deleteExpense(id: String) = copy(event = event.copy(
        expenses = event.expenses.filterNot { it.id == id }
    ))
}

/** Raw form values are interpreted identically on Android and iOS. */
data class ExpenseInput(
    val title: String,
    val amount: String,
    val payerId: String,
    val targetMemberIds: Set<String>,
    val editingId: String? = null,
    val useGasCalculator: Boolean = false,
    val distanceKm: String = "",
    val fuelEfficiency: String = "",
    val gasPrice: String = ""
) {
    fun gasInfoOrNull(): GasInfo? {
        val distance = distanceKm.toDoubleOrNull() ?: return null
        val efficiency = fuelEfficiency.toDoubleOrNull() ?: return null
        val price = gasPrice.toIntOrNull() ?: return null
        return if (efficiency > 0.0 && price > 0) GasInfo(distance, efficiency, price) else null
    }

    fun gasAmountOrNull(): Int? = gasInfoOrNull()?.calculatedAmount

    fun toExpenseOrNull(): Expense? {
        val gasInfo = if (useGasCalculator) gasInfoOrNull() else null
        val parsedAmount = gasInfo?.calculatedAmount ?: amount.toIntOrNull() ?: 0
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty() || parsedAmount <= 0 || payerId.isBlank() || targetMemberIds.isEmpty()) return null
        val expense = Expense(
            title = trimmedTitle, amount = parsedAmount, payerId = payerId,
            targetMemberIds = targetMemberIds, gasInfo = gasInfo
        )
        return editingId?.let { expense.copy(id = it) } ?: expense
    }
}
