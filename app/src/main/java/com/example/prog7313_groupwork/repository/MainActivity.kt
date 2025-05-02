package com.example.prog7313_groupwork.repository
// imports
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.prog7313_groupwork.LoginActivity
import com.example.prog7313_groupwork.R

// ------------------------------------------------------------------------
// Class that handles the activity_main.xml functionality
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getStartedButton = findViewById<Button>(R.id.LetsGobutton)
        // ----------------------------------------------------------------
        // Navigation section
        getStartedButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
// -----------------------------------<<< End Of File >>>------------------------------------------