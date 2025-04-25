package com.example.prog7313_groupwork.astraDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313_groupwork.entities.*

@Database(
    entities = [
        User::class,
        Expense::class,
        Income::class,
        DebtPlan::class,
        Budget::class,
        BudgetCategory::class,
        Award::class,
        Savings::class,
        Category::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AstraDatabase : RoomDatabase() {

    abstract fun userDAO(): UserDAO
    abstract fun expenseDAO(): ExpenseDAO
    abstract fun incomeDAO(): IncomeDAO
    abstract fun debtPlanDAO(): DebtPlanDAO
    abstract fun budgetDAO(): BudgetDAO
    abstract fun awardDAO(): AwardDAO
    abstract fun savingsDAO(): SavingsDAO
    abstract fun categoryDAO(): CategoryDAO

    companion object {
        @Volatile
        private var INSTANCE: AstraDatabase? = null

        fun getDatabase(context: Context): AstraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraDatabase::class.java,
                    "astra-database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

//Final Commit