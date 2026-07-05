package tk.horiuchi.tatekaemaru.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val date: String,
    val updatedAt: Long
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class MemberEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val name: String
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId"), Index("payerId")]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val title: String,
    val amount: Int,
    val payerId: String,
    val distanceKm: Double?,
    val fuelEfficiencyKmPerLiter: Double?,
    val gasPricePerLiter: Int?
)

@Entity(
    tableName = "expense_targets",
    primaryKeys = ["expenseId", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId"), Index("memberId")]
)
data class ExpenseTargetEntity(
    val expenseId: String,
    val memberId: String
)
