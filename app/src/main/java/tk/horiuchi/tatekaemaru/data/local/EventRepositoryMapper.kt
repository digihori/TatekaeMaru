package tk.horiuchi.tatekaemaru.data.local

import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.GasInfo
import tk.horiuchi.tatekaemaru.data.Member
import java.time.LocalDate

object EventRepositoryMapper {
    data class EntitySet(
        val event: tk.horiuchi.tatekaemaru.data.local.EventEntity,
        val members: List<tk.horiuchi.tatekaemaru.data.local.MemberEntity>,
        val expenses: List<tk.horiuchi.tatekaemaru.data.local.ExpenseEntity>,
        val targets: List<tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity>
    )

    fun toEntities(event: Event): EntitySet {
        val eventEntity = tk.horiuchi.tatekaemaru.data.local.EventEntity(
            id = event.id,
            name = event.name,
            date = event.date.toString(),
            updatedAt = System.currentTimeMillis()
        )
        val memberEntities = event.members.map { member ->
            tk.horiuchi.tatekaemaru.data.local.MemberEntity(
                id = member.id,
                eventId = event.id,
                name = member.name
            )
        }
        val expenseEntities = event.expenses.map { expense ->
            tk.horiuchi.tatekaemaru.data.local.ExpenseEntity(
                id = expense.id,
                eventId = event.id,
                title = expense.title,
                amount = expense.amount,
                payerId = expense.payerId,
                distanceKm = expense.gasInfo?.distanceKm,
                fuelEfficiencyKmPerLiter = expense.gasInfo?.fuelEfficiencyKmPerLiter,
                gasPricePerLiter = expense.gasInfo?.gasPricePerLiter
            )
        }
        val targetEntities = event.expenses.flatMap { expense ->
            expense.targetMemberIds.map { memberId ->
                tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity(
                    expenseId = expense.id,
                    memberId = memberId
                )
            }
        }
        return EntitySet(eventEntity, memberEntities, expenseEntities, targetEntities)
    }

    fun toModel(
        event: tk.horiuchi.tatekaemaru.data.local.EventEntity,
        members: List<tk.horiuchi.tatekaemaru.data.local.MemberEntity>,
        expenses: List<tk.horiuchi.tatekaemaru.data.local.ExpenseEntity>,
        targets: List<tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity>
    ): Event {
        val targetsByExpense = targets.groupBy { it.expenseId }
        return Event(
            id = event.id,
            name = event.name,
            date = runCatching { LocalDate.parse(event.date) }.getOrDefault(LocalDate.now()),
            members = members.map { Member(id = it.id, name = it.name) },
            expenses = expenses.map { expense ->
                Expense(
                    id = expense.id,
                    title = expense.title,
                    amount = expense.amount,
                    payerId = expense.payerId,
                    targetMemberIds = targetsByExpense[expense.id]?.map { it.memberId }?.toSet().orEmpty(),
                    gasInfo = if (
                        expense.distanceKm != null &&
                        expense.fuelEfficiencyKmPerLiter != null &&
                        expense.gasPricePerLiter != null
                    ) {
                        GasInfo(
                            distanceKm = expense.distanceKm,
                            fuelEfficiencyKmPerLiter = expense.fuelEfficiencyKmPerLiter,
                            gasPricePerLiter = expense.gasPricePerLiter
                        )
                    } else {
                        null
                    }
                )
            }
        )
    }
}
