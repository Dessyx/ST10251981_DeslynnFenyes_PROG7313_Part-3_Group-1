package com.example.prog7313_groupwork.astraDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//import com.example.prog7313_groupwork.entities.Category
//import com.example.prog7313_groupwork.entities.CategoryDAO
import com.example.prog7313_groupwork.entities.Expense
import com.example.prog7313_groupwork.entities.ExpenseDAO
import com.example.prog7313_groupwork.entities.Income
import com.example.prog7313_groupwork.entities.IncomeDAO
import com.example.prog7313_groupwork.entities.User
import com.example.prog7313_groupwork.entities.UserDAO
import com.example.prog7313_groupwork.entities.DebtPlan
import com.example.prog7313_groupwork.entities.DebtPlanDAO
import com.example.prog7313_groupwork.entities.Budget
import com.example.prog7313_groupwork.entities.BudgetCategory
import com.example.prog7313_groupwork.entities.BudgetDAO

@Database(
    entities = [
        User::class,
        Expense::class,
        Income::class,
        DebtPlan::class,
        Budget::class,
        BudgetCategory::class
    ],
    version = 4
)
abstract class AstraDatabase : RoomDatabase() {

    abstract fun userDAO(): UserDAO
    abstract fun expenseDAO(): ExpenseDAO
    abstract fun incomeDAO(): IncomeDAO
    abstract fun debtPlanDAO(): DebtPlanDAO
    abstract fun budgetDAO(): BudgetDAO
    //abstract fun CategoryDAO(): CategoryDAO

    companion object {
        @Volatile
        private var INSTANCE: AstraDatabase? = null

        fun getDatabase(context: Context): AstraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraDatabase::class.java,
                    "astra-Database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}