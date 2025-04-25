package com.example.prog7313_groupwork

import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.AdapterView
import android.content.Intent

class DashboardActivity : AppCompatActivity() {
    private lateinit var monthSpinner: Spinner
    private lateinit var spendingTrendsText: TextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize views
        monthSpinner = findViewById(R.id.monthSpinner)
        spendingTrendsText = findViewById(R.id.spendingTrendsTitle)
        backButton = findViewById(R.id.back_button)

//----------------------------------------------------------------------------------
//                              Page navigation section 

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
// ----------------------------------------------------------------------------------

        // Set up month spinner listener
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = parent?.getItemAtPosition(position).toString()
                spendingTrendsText.text = "Spending trends for $selectedMonth"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
} 