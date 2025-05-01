package com.example.prog7313_groupwork.astraDatabase

// Imports
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prog7313_groupwork.entities.*

// ------------------------------------ Astra Database Class ----------------------------------------
// This class represents the main database for the application using Room persistence library
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
    version = 7,
    exportSchema = false
)
abstract class AstraDatabase : RoomDatabase() {

    // ------------------------------------------------------------------------------------
    // DAO (Data Access Object) declarations for each entity
    abstract fun userDAO(): UserDAO
    abstract fun expenseDAO(): ExpenseDAO
    abstract fun incomeDAO(): IncomeDAO
    abstract fun debtPlanDAO(): DebtPlanDAO
    abstract fun budgetDAO(): BudgetDAO
    abstract fun awardDAO(): AwardDAO
    abstract fun savingsDAO(): SavingsDAO
    abstract fun categoryDAO(): CategoryDAO

    // ------------------------------------------------------------------------------------
    // Companion object for implementing the Singleton pattern
    companion object {
        @Volatile
        private var INSTANCE: AstraDatabase? = null

        // ------------------------------------------------------------------------------------
        // Returns the database instance, creating it if necessary
        fun getDatabase(context: Context): AstraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraDatabase::class.java,
                    "astra-database"
                )
                .fallbackToDestructiveMigration() // Allows database to be recreated on version mismatch
                .allowMainThreadQueries() // Allows database operations on main thread
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------

