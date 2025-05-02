package com.example.prog7313_groupwork.repository

//imports
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
import com.example.prog7313_groupwork.LoginActivity
import android.graphics.Color
import android.content.res.Configuration
import com.example.prog7313_groupwork.HomeActivity
import java.util.Locale
import at.favre.lib.crypto.bcrypt.BCrypt

// ---------------------- Functionality for settings_page.xml ---------------------------------
class SettingsMainClass : AppCompatActivity() {

    private lateinit var db: AstraDatabase
    private lateinit var spinnerCurrency: Spinner
    private lateinit var languageGroup: RadioGroup              // Variable declaration
    private lateinit var rbEnglish: RadioButton
    private lateinit var rbAfrikaans: RadioButton
    private lateinit var layoutSystemSettings: LinearLayout
    private lateinit var etChangeEmail: EditText
    private lateinit var etConfirmEmail: EditText
    private lateinit var etChangePassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnLogout: Button
    private lateinit var backButton: ImageButton
    private var currentUserId: Long = -1
    private var selectedColor: Int = Color.parseColor("#EEC5D9")
    private var isUpdatingLanguage = false
    private var currentLanguage = "en"

    //----------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getLong("current_user_id", -1L)

        if (currentUserId == -1L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initializeViews()
        setupDatabase()
        setupListeners()
        loadUserSettings()
    }
//---------------------------------------------------------------------------------------
    // initialize views
    private fun initializeViews() {
        try {
            spinnerCurrency = findViewById(R.id.spinnerCurrency)
            languageGroup = findViewById(R.id.languageGroup)
            rbEnglish = findViewById(R.id.rbEnglish)
            /*rbAfrikaans = findViewById(R.id.rbAfrikaans)*/
            layoutSystemSettings = findViewById(R.id.layoutSystemSettings)
            etChangeEmail = findViewById(R.id.etChangeEmail)
            etConfirmEmail = findViewById(R.id.etConfirmEmail)
            etChangePassword = findViewById(R.id.etChangePassword)
            etConfirmPassword = findViewById(R.id.etConfirmPassword)
            btnLogout = findViewById(R.id.btnLogout)
            btnSaveChanges = findViewById(R.id.btnSaveChanges)
            backButton = findViewById(R.id.back_button)

      //----------------------------------------------------------------------------------
            // on click listener
            backButton.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }

       //---------------------------------------------------------------------------------
            // currency adapter for dropdown
            ArrayAdapter.createFromResource(
                this,
                R.array.currencies,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCurrency.adapter = adapter
            }
        } catch (e: Exception) {
            showToast("Error initializing views: ${e.message}")
        }
    }

    //-------------------------------------------------------------------------------------
    // sets up database
    private fun setupDatabase() {
        try {
            db = AstraDatabase.getDatabase(this)
        } catch (e: Exception) {
            showToast("Error setting up database: ${e.message}")
        }
    }

    //--------------------------------------------------------------------------------------
    // on click listeners
    private fun setupListeners() {
        try {
            languageGroup.setOnCheckedChangeListener { group, checkedId ->
                if (isUpdatingLanguage) return@setOnCheckedChangeListener
                
                val newLanguage = when (checkedId) {
                    R.id.rbEnglish -> "en"
                   /* R.id.rbAfrikaans -> "af"*/
                    else -> return@setOnCheckedChangeListener
                }

                if (newLanguage != currentLanguage) {
                    isUpdatingLanguage = true
                    currentLanguage = newLanguage
                    updateUserLanguage(newLanguage)
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
        } catch (e: Exception) {
            showToast("Error setting up listeners: ${e.message}")
        }
    }

    //--------------------------------------------------------------------------------------
    // fetches current user settings
    private fun loadUserSettings() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = db.userDAO().getUserById(currentUserId)
                withContext(Dispatchers.Main) {
                    user?.let { loadedUser ->
                        isUpdatingLanguage = true
                        etChangeEmail.setText(loadedUser.userEmail)
                        currentLanguage = loadedUser.language
                        when (currentLanguage) {
                            "en" -> rbEnglish.isChecked = true
                            "af" -> rbAfrikaans.isChecked = true
                        }
                        isUpdatingLanguage = false

                        val currencyAdapter = spinnerCurrency.adapter as ArrayAdapter<*>
                        val position = (0 until currencyAdapter.count).firstOrNull { 
                            currencyAdapter.getItem(it).toString() == loadedUser.currency 
                        } ?: 0
                        spinnerCurrency.setSelection(position)
                        selectedColor = loadedUser.themeColor
                        window.decorView.setBackgroundColor(selectedColor)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to load settings: ${e.message}")
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    // sets app language to chosen language
    private fun updateUserLanguage(langCode: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserLanguage(currentUserId, langCode)
                withContext(Dispatchers.Main) {
                    try {
                        val locale = Locale(langCode)
                        Locale.setDefault(locale)
                        val config = Configuration()
                        config.locale = locale
                        resources.updateConfiguration(config, resources.displayMetrics)
                        

                        lifecycleScope.launch(Dispatchers.Main) {
                            recreate()
                        }
                    } catch (e: Exception) {
                        showToast("Failed to update locale: ${e.message}")
                    } finally {
                        isUpdatingLanguage = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to update language: ${e.message}")
                    isUpdatingLanguage = false
                }
            }
        }
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

    //-------------------------------------------------------------------------------
    // Sets the users details to the entered details
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

        if (password.isEmpty()) {
            showToast("Password cannot be empty")
            return
        }

        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserCredentials(currentUserId, email, hashedPassword)
                withContext(Dispatchers.Main) {
                    // Update SharedPreferences with new email
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("user_email", email)
                        apply()
                    }
                    showToast("Credentials updated successfully")
                    etChangePassword.text.clear()
                    etConfirmPassword.text.clear()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to update credentials: ${e.message}")
                }
            }
        }
    }

    //-----------------------------------------------------------------------------------
    // Updates the user currency to selected
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

    // --------------------------------------------------------------------------------
    // Updates theme color
    private fun saveUserThemeColor(color: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDAO().updateUserThemeColor(currentUserId, color)
                withContext(Dispatchers.Main) {
                    showToast("Theme color saved")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Failed to save theme color")
                }
            }
        }
    }
    //---------------------------------------------------------------------------------
    // logs out
    private fun handleLogout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// -----------------------------------<<< End Of File >>>------------------------------------------