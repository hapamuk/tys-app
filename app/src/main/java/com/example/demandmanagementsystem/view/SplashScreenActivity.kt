package com.example.demandmanagementsystem.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityMainBinding
import com.example.demandmanagementsystem.databinding.ActivitySplashScreenBinding
import com.example.demandmanagementsystem.service.UserSigInToken
import com.example.demandmanagementsystem.util.GetUserSaveData
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 3000 // 3 seconds
    private val TOTAL_PROGRESS = 100
    private var progressBarStatus = 0
    private var dummy: Int = 0
    private val progressBarIncrement = TOTAL_PROGRESS / (SPLASH_DELAY / 100) // Her 100ms'de bir artış
    private var control = false
    private lateinit var binding: ActivitySplashScreenBinding
    private val handler = Handler()
    private val userToken = UserSigInToken()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userToken.userSigInToken(this@SplashScreenActivity) { isLoggedIn ->
            if (isLoggedIn) {
                control = true
            }
            handler.postDelayed(mRunnable, 100) // 100ms'de bir artır
        }
    }

    private val mRunnable: Runnable = Runnable {
        while (progressBarStatus < TOTAL_PROGRESS) {
            try {
                dummy += progressBarIncrement.toInt()
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            progressBarStatus = dummy
            runOnUiThread {
                binding.splashScreenProgressBar.progress = progressBarStatus
            }
        }

        // İnternet bağlantısını kontrol et
        if (isInternetConnected()) {
            if (control) {
                launchDemandListActivity()
            } else {
                launchMainActivity()
            }
        } else {
            val alertDialog = AlertDialog.Builder(this@SplashScreenActivity)


            alertDialog.setMessage("İnternet Bağlantısı Yok!")
            alertDialog.setPositiveButton("Tekrar Dene"){ dialogInterface, i ->
                val intent = Intent(this@SplashScreenActivity, SplashScreenActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                this@SplashScreenActivity.startActivity(intent)

            }

            alertDialog.setNegativeButton("Çıkış"){ dialogInterface, i ->
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                this@SplashScreenActivity.startActivity(intent)
            }

            alertDialog.create().show()
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
        handler.removeCallbacks(mRunnable)
    }

    private fun launchDemandListActivity() {
        Toast.makeText(this@SplashScreenActivity, "Hoşgeldiniz!", Toast.LENGTH_LONG).show()
        val intent = Intent(this@SplashScreenActivity, DemandListActivity::class.java)
        startActivity(intent)
        finish()
        handler.removeCallbacks(mRunnable)
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onDestroy() {
        handler.removeCallbacks(mRunnable)
        super.onDestroy()
    }
}


