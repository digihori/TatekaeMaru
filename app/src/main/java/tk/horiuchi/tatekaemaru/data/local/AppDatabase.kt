package tk.horiuchi.tatekaemaru.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        tk.horiuchi.tatekaemaru.data.local.EventEntity::class,
        tk.horiuchi.tatekaemaru.data.local.MemberEntity::class,
        tk.horiuchi.tatekaemaru.data.local.ExpenseEntity::class,
        tk.horiuchi.tatekaemaru.data.local.ExpenseTargetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tatekaeDao(): TatekaeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tatekae_maru.db"
                ).build().also { instance = it }
            }
        }
    }
}
