package tk.horiuchi.tatekaemaru.domain

import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense

data class MemberRemovalPreview(
    val paidExpenses: List<Expense>,
    val orphanedExpenses: List<Expense>,
    val expensesLosingTarget: List<Expense>
)

object EventMemberRemoval {
    fun preview(event: Event, memberId: String): MemberRemovalPreview {
        val paidExpenses = event.expenses.filter { it.payerId == memberId }
        val expensesPaidByOthers = event.expenses.filter { it.payerId != memberId }
        val targetedExpenses = expensesPaidByOthers.filter { memberId in it.targetMemberIds }
        val orphanedExpenses = targetedExpenses.filter {
            (it.targetMemberIds - memberId).isEmpty()
        }

        return MemberRemovalPreview(
            paidExpenses = paidExpenses,
            orphanedExpenses = orphanedExpenses,
            expensesLosingTarget = targetedExpenses - orphanedExpenses.toSet()
        )
    }

    fun remove(event: Event, memberId: String): Event {
        val remainingExpenses = event.expenses.mapNotNull { expense ->
            if (expense.payerId == memberId) {
                return@mapNotNull null
            }

            val remainingTargets = expense.targetMemberIds - memberId
            if (remainingTargets.isEmpty()) {
                null
            } else {
                expense.copy(targetMemberIds = remainingTargets)
            }
        }

        return event.copy(
            members = event.members.filterNot { it.id == memberId },
            expenses = remainingExpenses
        )
    }
}
