package com.mka.airbilinest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inisialisasi SharedPreferences untuk login
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        // Tombol Sign Out
        val btnSignOut: Button = findViewById(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            signOut()
        }

        // Logika pemilihan bahasa
        val languageRadioGroup = findViewById<RadioGroup>(R.id.languageRadioGroup)
        val englishRadioButton = findViewById<RadioButton>(R.id.englishRadioButton)
        val indonesianRadioButton = findViewById<RadioButton>(R.id.indonesianRadioButton)

        // Set radio button berdasarkan bahasa yang tersimpan
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val savedLanguage = prefs.getString("My_Lang", "en")
        if (savedLanguage == "en") englishRadioButton.isChecked = true else indonesianRadioButton.isChecked = true

        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedLanguage = if (checkedId == R.id.englishRadioButton) "en" else "in"

            // Update bahasa dan restart activity
            LocaleHelper.setLocale(this, selectedLanguage)
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun signOut() {
        // Hapus semua data login
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect ke LoginActivity dan tutup semua activity sebelumnya
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}