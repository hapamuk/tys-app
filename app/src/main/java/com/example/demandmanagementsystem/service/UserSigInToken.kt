package com.example.demandmanagementsystem.service

import android.content.Context
import android.util.Log

class UserSigInToken() {
    private val reference = FirebaseServiceReference()
    fun userSigInToken(context: Context, callback: (Boolean) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        val token = sharedPreferences.getString("token", "")
        Log.e("logSons", "logSons $token")

        if (userId != null) {
            reference
                .userSigInTokenCollection()
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val guide = documentSnapshot.getString("token").toString()
                    if (token == guide) {
                        callback.invoke(true)
                    } else {
                        callback.invoke(false) // Token eşleşmiyorsa burada çağrılmalı
                    }
                }
                .addOnFailureListener {
                    Log.e("UserSigInToken", "userSigInToken")
                    callback.invoke(false) // Hata durumunda burada çağrılmalı
                }
        } else {
            callback.invoke(false) // userId null ise burada çağrılmalı
        }
    }


    fun saveUserSigInToken(userId: String, guideId: String, context: Context, callback: (Boolean) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        val token = hashMapOf(
            "token" to guideId
        )

        sharedPreferences.edit().apply {
            remove("token")
            putString("token", guideId)
            apply()
        }
        Log.e("deneme","${sharedPreferences.getString("token","boşboş")}")
        reference
            .userSigInTokenCollection()
            .document(userId)
            .set(token)
            .addOnSuccessListener {

                callback(true) // İşlem başarılı olduğunda true döndürüyoruz
            }
            .addOnFailureListener {
                Log.e("UserSigInToken", "saveUserSigInToken")
                callback(false) // İşlem başarısız olduğunda false döndürüyoruz
            }
    }



}







/*
 val user = auth.currentUser
                        val userId = user!!.uid
                        val guideId = UUID.randomUUID()
                        lifecycleScope.launch {
                            Log.e("MainActivitySS", guideId.toString())
                            Log.e("MainActivitySS", "BURADAYIM ON CREATE")
                            getUserData.getUserSaveData(userId, this@MainActivity)
                            Intent(this@MainActivity, DemandListActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            Log.e("MainActivitySS","oncreate => token da hata")
                        }
                            userToken.saveUserSigInToken(userId, guideId, this@MainActivity) { isSuccess ->
                                if (isSuccess) {

                                    Toast.makeText(this@MainActivity, "Hoşgeldiniz!", Toast.LENGTH_LONG)
                                                .show()
                                    val intent =
                            }

                        }

 */


















