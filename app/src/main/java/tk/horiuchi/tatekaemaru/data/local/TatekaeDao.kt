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
    abstract fun observeEvents(): Flow<List<tk.horiuchi.tatekaemaru.data.local.EventEntity>>

    @Query("SELECT COUNT(*) FROM events")
    abstract suspend fun countEvents(): Int

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    abstract suspend fun getEvent(eventId: String): tk.horiuchi.tatekaemaru.data.local.EventEntity?

    @Query("SELECT * FROM members WHERE eventId = :eventId ORDER BY rowid")
    abstract suspend fun getMembers(eventId: String): List<tk.horiuchi.tatekaemaru.data.local.MemberEntity>

    @Query("SELECT * FROM expenses WHERE eventId = :eventId ORDER BY rowid")
    abstract suspend fun getExpenses(eventId: String): List<tk.horiuchi.tatekaemaru.data.local.ExpenseEntity>

    @Query("SELECT * FROM expense_targets WHERE expenseId IN (:expenseIds)")
    abstract suspend fun getTargets(expenseIds: List<String>): List<tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertEvent(event: tk.horiuchi.tatekaemaru.data.local.EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMembers(members: List<tk.horiuchi.tatekaemaru.data.local.MemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExpenses(expenses: List<tk.horiuchi.tatekaemaru.data.local.ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTargets(targets: List<tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity>)

    @Query("DELETE FROM members WHERE eventId = :eventId")
    abstract suspend fun deleteMembersForEvent(eventId: String)

    @Query("DELETE FROM expenses WHERE eventId = :eventId")
    abstract suspend fun deleteExpensesForEvent(eventId: String)

    @Query("DELETE FROM events WHERE id = :eventId")
    abstract suspend fun deleteEvent(eventId: String)

    @Transaction
    open suspend fun replaceEvent(
        event: tk.horiuchi.tatekaemaru.data.local.EventEntity,
        members: List<tk.horiuchi.tatekaemaru.data.local.MemberEntity>,
        expenses: List<tk.horiuchi.tatekaemaru.data.local.ExpenseEntity>,
        targets: List<tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity>
    ) {
        upsertEvent(event)
        deleteMembersForEvent(event.id)
        deleteExpensesForEvent(event.id)
        if (members.isNotEmpty()) insertMembers(members)
        if (expenses.isNotEmpty()) insertExpenses(expenses)
        if (targets.isNotEmpty()) insertTargets(targets)
    }

    @Query("""
        SELECT m.name
        FROM members m
        WHERE TRIM(m.name) != ''
        GROUP BY m.name
        ORDER BY MAX(m.rowid) DESC
    """)
    abstract suspend fun getMemberNameHistory(): List<String>

    @Query("""
        SELECT ex.title
        FROM expenses ex
        WHERE TRIM(ex.title) != ''
        GROUP BY ex.title
        ORDER BY MAX(ex.rowid) DESC
    """)
    abstract suspend fun getExpenseTitleHistory(): List<String>
}
