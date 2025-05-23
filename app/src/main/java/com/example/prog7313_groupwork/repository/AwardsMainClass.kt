package com.example.prog7313_groupwork.repository

// imports
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.HomeActivity
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import com.example.prog7313_groupwork.entities.Award
import kotlinx.coroutines.launch

//                                   P3 FEATURE
// ------------------------------ Awards Activity Class -------------------------------
class AwardsMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var giftCardImage: ImageView
    private lateinit var giftCardProgressBar: ProgressBar
    private lateinit var giftCardProgressPercent: TextView
    private lateinit var giftCardCongrats: TextView
    private var currentUserId: Long = -1L

    //----------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.awards_page)

        db = AstraDatabase.getDatabase(this)

        // Get user ID from SharedPreferences
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)
        if (currentUserId == -1L) {
            Toast.makeText(this, "Please log in to use awards feature", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        giftCardImage = findViewById(R.id.ivGiftCard)
        giftCardProgressBar = findViewById(R.id.giftCardProgressBar)
        giftCardProgressPercent = findViewById(R.id.giftCardProgressPercent)
        giftCardCongrats = findViewById(R.id.giftCardCongrats)

        updateGiftCardProgress()

        // Navigation
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun updateGiftCardProgress() {
        lifecycleScope.launch {
            val totalSavings = db.savingsDAO().getTotalSavings(currentUserId) ?: 0.0
            val monthlyGoal = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getFloat("monthly_savings_goal", 0f).toDouble()
            val percent = if (monthlyGoal > 0) {
                ((totalSavings / monthlyGoal) * 100).coerceAtMost(100.0).toInt()
            } else 0
            runOnUiThread {
                giftCardProgressBar.progress = percent
                giftCardProgressPercent.text = "$percent%"
                if (percent >= 100) {
                    giftCardImage.visibility = View.VISIBLE
                    giftCardCongrats.visibility = View.VISIBLE
                } else {
                    giftCardImage.visibility = View.GONE
                    giftCardCongrats.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateGiftCardProgress()
    }
}