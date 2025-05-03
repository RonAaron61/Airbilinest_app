package com.mka.airbilinest

import ApiResponse
import ApiService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class LoginActivity : BaseActivity() {
    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://airbilinest.com/API/") // Ganti dengan URL server Anda
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Cek apakah token masih valid
        if (!isTokenValid()) {
            clearLoginDetails() // Hapus data login jika token kedaluwarsa
        }

        // Cek apakah ada data login yang tersimpan
        checkSavedLogin()

        btnLoginListener()

        val web_btn = findViewById<Button>(R.id.WebBTN)
        web_btn.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://airbilinest.com/hcp/register.php"))
            startActivity(i)
        }
    }

    private fun checkSavedLogin() {
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)
        val isKeepMeSignedIn = sharedPreferences.getBoolean("keepMeSignedIn", false)

        if (isKeepMeSignedIn && savedEmail != null && savedPassword != null) {
            // Auto-fill email dan password
            findViewById<EditText>(R.id.txtEmail).setText(savedEmail)
            findViewById<EditText>(R.id.txtPassword).setText(savedPassword)
            findViewById<CheckBox>(R.id.checkKeepMeSignedIn).isChecked = true

            // Langsung masuk ke MainActivity tanpa pengecekan ulang ke server
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup LoginActivity
        } else if (savedEmail != null && savedPassword != null) {
            // Auto-fill email dan password
            findViewById<EditText>(R.id.txtEmail).setText(savedEmail)
            findViewById<EditText>(R.id.txtPassword).setText(savedPassword)
            findViewById<CheckBox>(R.id.checkRememberMe).isChecked = true
        }
    }

    private fun btnLoginListener() {
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        val checkKeepMeSignedIn = findViewById<CheckBox>(R.id.checkKeepMeSignedIn)
        val checkRememberMe = findViewById<CheckBox>(R.id.checkRememberMe)

        btnLogin.setOnClickListener {
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()

            if (checkKeepMeSignedIn.isChecked) {
                // Simpan email, password, dan status "Keep Me Signed In" jika dicentang
                saveLoginDetails(email, password)
                saveKeepMeSignedInStatus(true)
            } else if (checkRememberMe.isChecked) {
                // Simpan email dan password jika "Ingat Saya" dicentang
                saveLoginDetails(email, password)
            } else {
                // Hapus data yang tersimpan jika tidak dicentang
                clearLoginDetails()
                saveKeepMeSignedInStatus(false)
            }

            // Proses login
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        apiService.login(email, password).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val token = response.body()?.token ?: ""
                    val appAccess = response.body()?.app_access ?: 0 // Ambil nilai app_access
                    val expirationTime = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 14) // Token berlaku selama 14 hari
                    }.timeInMillis

                    // Simpan token dan waktu kedaluwarsa
                    saveToken(token, expirationTime)

                    // Cek apakah pengguna memiliki akses ke aplikasi
                    if (appAccess == 1) {
                        // Redirect ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Tutup LoginActivity
                        Toast.makeText(this@LoginActivity, "Login successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        // Tampilkan popup/notifikasi jika tidak memiliki akses
                        showAccessDeniedPopup()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAccessDeniedPopup() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Akses Ditolak")
            .setMessage("Anda tidak memiliki akses ke aplikasi ini. \n\nSilakan hubungi admin di: \nadmin.airbilinest@airbilinest.com \nSubjek: \"Aktivasi Akses Aplikasi\" " +
                    "Kirimkan data diri berupa username akun. Mohon email dengan email yang saya yang anda gunakan saat registrasi akun. \n\nSilahkan tunggu beberapa saat dan coba kembali.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun saveToken(token: String, expirationTime: Long) {
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.putLong("token_expiration", expirationTime)
        editor.apply()
    }

    private fun isTokenValid(): Boolean {
        val expirationTime = sharedPreferences.getLong("token_expiration", 0)
        return expirationTime > System.currentTimeMillis() // Cek apakah token masih berlaku
    }

    private fun saveLoginDetails(email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

    private fun saveKeepMeSignedInStatus(isKeepMeSignedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("keepMeSignedIn", isKeepMeSignedIn)
        editor.apply()
    }

    private fun clearLoginDetails() {
        val editor = sharedPreferences.edit()
        editor.remove("email")
        editor.remove("password")
        editor.remove("keepMeSignedIn")
        editor.remove("token")
        editor.remove("token_expiration")
        editor.apply()
    }
}