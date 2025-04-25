package com.example.prog7313_groupwork.repository

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Award
import kotlinx.coroutines.launch

class AwardsMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var etGoal: EditText
    private lateinit var goalStatusText: TextView
    private lateinit var giftCardImage: ImageView
    private lateinit var badgeImage: ImageView
    private lateinit var trophyLayout: LinearLayout
    private var currentUserId: Long = 1 // You should get this from your login/session management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.awards_page)

        db = AstraDatabase.getDatabase(this)

        // Initialize views
        etGoal = findViewById(R.id.etGoal)
        giftCardImage = findViewById(R.id.ivGiftCard)
        badgeImage = findViewById(R.id.ivBadge)
        goalStatusText = findViewById(R.id.tvTitle) // Using existing TextView from layout
        trophyLayout = findViewById(R.id.trophyLayout) // Updated ID

        // Hide reward visuals initially
        trophyLayout.visibility = View.GONE
        giftCardImage.visibility = View.GONE
        badgeImage.visibility = View.GONE

        etGoal.setOnEditorActionListener { _, _, _ ->
            checkAndSaveGoal()
            true
        }

        // Load existing awards
        loadUserAwards()
    }

    private fun checkAndSaveGoal() {
        val goalText = etGoal.text.toString()
        if (goalText.isBlank()) {
            Toast.makeText(this, "Please enter a goal amount", Toast.LENGTH_SHORT).show()
            return
        }

        val goal = goalText.toIntOrNull()
        if (goal == null || goal <= 0) {
            Toast.makeText(this, "Enter a valid positive number", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Create a new award
            val award = Award(
                userId = currentUserId,
                goalAmount = goal,
                awardType = if (goal >= 5000) "GIFT_CARD" else "BADGE"
            )

            // Save to database
            db.awardDAO().insertAward(award)

            // Check if goal is achieved based on total savings
            checkGoalAchievement(award)
        }
    }

    private suspend fun checkGoalAchievement(award: Award) {
        val totalSavings = db.userDAO().getTotalSavings(currentUserId) ?: 0

        if (totalSavings >= award.goalAmount) {
            award.achieved = true
            award.dateAchieved = System.currentTimeMillis()
            db.awardDAO().updateAward(award)

            // Update UI
            runOnUiThread {
                showAwardAchieved(award)
            }
        }
    }

    private fun showAwardAchieved(award: Award) {
        trophyLayout.visibility = View.VISIBLE

        when (award.awardType) {
            "GIFT_CARD" -> {
                giftCardImage.visibility = View.VISIBLE
                badgeImage.visibility = View.GONE
            }
            "BADGE" -> {
                badgeImage.visibility = View.VISIBLE
                giftCardImage.visibility = View.GONE
            }
        }

        goalStatusText.text = getString(R.string.goal_achieved)
    }

    private fun loadUserAwards() {
        lifecycleScope.launch {
            val awards = db.awardDAO().getUserAwards(currentUserId)
            for (award in awards) {
                if (award.achieved) {
                    showAwardAchieved(award)
                }
            }
        }
    }
}