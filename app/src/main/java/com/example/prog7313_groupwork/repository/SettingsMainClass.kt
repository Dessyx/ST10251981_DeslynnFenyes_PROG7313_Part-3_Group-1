package com.example.prog7313_groupwork.repository

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.prog7313_groupwork.R
import com.example.prog7313_groupwork.astraDatabase.AstraDatabase
import android.graphics.Color
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class SettingsMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var spinnerCurrency: Spinner
    private lateinit var languageGroup: RadioGroup
    private lateinit var rbEnglish: RadioButton
    private lateinit var rbAfrikaans: RadioButton
    private lateinit var layoutSystemSettings: LinearLayout
    private lateinit var etChangeEmail: EditText
    private lateinit var etConfirmEmail: EditText
    private lateinit var etChangePassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnLogout: Button

    private var currentUserId: Long = 1
    private var selectedColor: Int = Color.parseColor("#EEC5D9")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        initializeViews()
        setupDatabase()
        setupListeners()
        loadUserSettings()
    }

    private fun initializeViews() {
        spinnerCurrency = findViewById(R.id.spinnerCurrency)
        languageGroup = findViewById(R.id.languageGroup)
        rbEnglish = findViewById(R.id.rbEnglish)
        rbAfrikaans = findViewById(R.id.rbAfrikaans)
        layoutSystemSettings = findViewById(R.id.layoutSystemSettings)
        etChangeEmail = findViewById(R.id.etChangeEmail)
        etConfirmEmail = findViewById(R.id.etConfirmEmail)
        etChangePassword = findViewById(R.id.etChangePassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnLogout = findViewById(R.id.btnLogout)

        // Setup currency spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCurrency.adapter = adapter
        }
    }

    private fun setupDatabase() {
        db = AstraDatabase.getDatabase(this)
    }

    private fun setupListeners() {
        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbEnglish -> updateUserLanguage("en")
                R.id.rbAfrikaans -> updateUserLanguage("af")
            }
        }

        layoutSystemSettings.setOnClickListener {
            openColorPicker()
        }

        btnSaveChanges.setOnClickListener {
            updateUserCredentials()
        }

        btnLogout.setOnClickListener {
            handleLogout()
        }

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedCurrency = parent.getItemAtPosition(pos).toString()
                updateUserCurrency(selectedCurrency)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadUserSettings() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = db.userDAO().getUserById(currentUserId)
                withContext(Dispatchers.Main) {
                    user?.let { loadedUser ->
                        etChangeEmail.setText(loadedUser.userEmail)
                        // Load other settings as needed
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to load settings")
                }
            }
        }
    }

    private fun updateUserLanguage(langCode: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserLanguage(currentUserId, langCode)
                withContext(Dispatchers.Main) {
                    updateLocale(langCode)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsMainClass,
                        "Failed to update language",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    private fun openColorPicker() {
        val colors = arrayOf(
            Color.parseColor("#EEC5D9"),
            Color.parseColor("#FF0000"),
            Color.parseColor("#00FF00"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#FFFF00")
        )

        AlertDialog.Builder(this)
            .setTitle("Choose Theme Color")
            .setItems(Array(colors.size) { "" }) { _, which ->
                selectedColor = colors[which]
                window.decorView.setBackgroundColor(selectedColor)
                saveUserThemeColor(selectedColor)
            }
            .show()
    }

    private fun updateUserCredentials() {
        val email = etChangeEmail.text.toString()
        val confirmEmail = etConfirmEmail.text.toString()
        val password = etChangePassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (email != confirmEmail) {
            showToast("Emails do not match")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserCredentials(currentUserId, email, password)
                withContext(Dispatchers.Main) {
                    showToast("Credentials updated successfully")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to update credentials")
                }
            }
        }
    }

    private fun updateUserCurrency(currency: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserCurrency(currentUserId, currency)
                withContext(Dispatchers.Main) {
                    showToast("Currency updated to $currency")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to update currency")
                }
            }
        }
    }

    private fun saveUserThemeColor(color: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserThemeColor(currentUserId, color)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsMainClass,
                        "Theme color saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SettingsMainClass,
                        "Failed to save theme color",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleLogout() {
        // Add your logout logic here
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}