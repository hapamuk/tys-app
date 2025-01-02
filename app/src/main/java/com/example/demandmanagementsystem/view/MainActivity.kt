package com.example.demandmanagementsystem.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.demandmanagementsystem.databinding.ActivityMainBinding
import com.example.demandmanagementsystem.service.UserSigInToken
import com.example.demandmanagementsystem.util.GetUserSaveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth;
    private val getUserData = GetUserSaveData()
    private val userToken = UserSigInToken()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        auth = Firebase.auth
        binding.buttonEnter.setOnClickListener {
            binding.buttonEnter.isEnabled = false
            val email=binding.textUserName.text.toString()
            val password=binding.textPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        val userId = user?.uid
                        val guideId = UUID.randomUUID().toString()
                        if (userId != null){
                            try {

                                getUserData.getUserSaveData(userId,guideId,this@MainActivity){
                                    if (it){
                                        userToken.saveUserSigInToken(userId, guideId, this@MainActivity,){ success->
                                            if (success){
                                                Toast.makeText(this@MainActivity, "Hoşgeldiniz!", Toast.LENGTH_SHORT)
                                                    .show()

                                                val intent =Intent(this@MainActivity, DemandListActivity::class.java)
                                                binding.buttonEnter.isEnabled = true
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                    }else{
                                        Log.e("MainActivity","oncreate => kullanıcı kayıt hatası")
                                    }
                                }
                            }catch (e: Exception){
                                Toast.makeText(this@MainActivity, "Lütfen tekrar deneyiniz!", Toast.LENGTH_SHORT).show()
                                Log.e("MainActivity","Lütfen tekrar deneyiniz!")
                            }
                        }


                    }

                }.addOnFailureListener { exception ->
                    binding.buttonEnter.isEnabled = true
                    Log.e("MainActivity","oncreate => ${exception.localizedMessage}")
                    Toast.makeText(this@MainActivity, "Kullanıcı Adı veya Şifre Hatalı", Toast.LENGTH_LONG).show()

                }
            }else{
                binding.buttonEnter.isEnabled = true
                Toast.makeText(this@MainActivity,"Bilgileriniz Boş Olamaz",Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}