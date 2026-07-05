package tk.horiuchi.tatekaemaru.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TatekaeDao {
    @Query("SELECT * FROM events ORDER BY updatedAt DESC")
    abstract fun observeEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    abstract suspend fun getEvent(eventId: String): EventEntity?

    @Query("SELECT * FROM members WHERE eventId = :eventId ORDER BY rowid")
    abstract suspend fun getMembers(eventId: String): List<MemberEntity>

    @Query("SELECT * FROM expenses WHERE eventId = :eventId ORDER BY rowid")
    abstract suspend fun getExpenses(eventId: String): List<ExpenseEntity>

    @Query("SELECT * FROM expense_targets WHERE expenseId IN (:expenseIds)")
    abstract suspend fun getTargets(expenseIds: List<String>): List<ExpenseTargetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMembers(members: List<MemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTargets(targets: List<ExpenseTargetEntity>)

    @Query("DELETE FROM members WHERE eventId = :eventId")
    abstract suspend fun deleteMembersForEvent(eventId: String)

    @Query("DELETE FROM expenses WHERE eventId = :eventId")
    abstract suspend fun deleteExpensesForEvent(eventId: String)

    @Query("DELETE FROM events WHERE id = :eventId")
    abstract suspend fun deleteEvent(eventId: String)

    @Transaction
    open suspend fun replaceEvent(
        event: EventEntity,
        members: List<MemberEntity>,
        expenses: List<ExpenseEntity>,
        targets: List<ExpenseTargetEntity>
    ) {
        upsertEvent(event)
        deleteMembersForEvent(event.id)
        deleteExpensesForEvent(event.id)
        if (members.isNotEmpty()) insertMembers(members)
        if (expenses.isNotEmpty()) insertExpenses(expenses)
        if (targets.isNotEmpty()) insertTargets(targets)
    }
}
