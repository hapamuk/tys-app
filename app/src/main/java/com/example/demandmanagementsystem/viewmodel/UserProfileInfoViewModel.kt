package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityUserProfileInfoBinding
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.view.DemandListActivity
import com.example.demandmanagementsystem.view.MainActivity
import com.google.firebase.auth.EmailAuthProvider
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.UUID

class UserProfileInfoViewModel(application: Application) : AndroidViewModel(application) {
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
    private val reference = FirebaseServiceReference()
    fun getData(callbacks: (UserData) -> Unit) {
        val tc = sharedPreferences.getString("tcIdentityNo", "")
        val email = sharedPreferences.getString("email", "")
        val name = sharedPreferences.getString("name", "")
        val password = sharedPreferences.getString("password", "")
        val tel = sharedPreferences.getString("telNo", "")
        val authorityType = sharedPreferences.getString("authorityType", "")
        val departmentType = sharedPreferences.getString("departmentType", "")


        if ((tc != null) && (email != null) && (name != null) && (password != null) && (tel != null) && (authorityType != null) && (departmentType != null)) {
            val user = UserData(
                tc, email, name, password, tel, authorityType, departmentType
            )
            callbacks.invoke(user)
        }


    }

    fun getProfileImage(binding: ActivityUserProfileInfoBinding) {

        encodeImageToBase64{bitmap ->

            if (bitmap == "null") {
                binding.userProfilInfoProfilImage.setImageResource(R.drawable.gorselsecimi)

            } else {

                val selectedBitmap = decodeBase64ToBitmap(bitmap)
                binding.userProfilInfoProfilImage.setBackgroundResource(0) // Önceki arkaplanı temizle (isteğe bağlı)
                binding.userProfilInfoProfilImage.setImageBitmap(selectedBitmap)

            }

        }


    }
    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        val byteDizisi = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteDizisi, 0, byteDizisi.size)
    }
    fun encodeImageToBase64(callbacks: (String) -> Unit) {
        val userId = sharedPreferences.getString("userId", null)
        if (userId != null) {
            reference
                .profilePhotoCollection()
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    val bitmap = documentSnapshot.getString("image")

                    if (bitmap != null) {
                        callbacks(bitmap)
                    }

                    Log.e("UserProfileInfo", "save image")
                }
                .addOnFailureListener {

                    Log.e("UserProfileInfo", "save image save error")
                }
        }
        callbacks(null.toString())
    }

        fun saveImage(secilenBitmap: Bitmap, context: Context) {


            if (secilenBitmap != null) {

                val kucukBitmap = createBitmap(secilenBitmap!!, 300)

                val outputStream = ByteArrayOutputStream()
                kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                val byteArray = outputStream.toByteArray()

                val image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                Log.e("image",image)

                val setImage = hashMapOf(
                    "image" to image
                )

                val userId = sharedPreferences.getString("userId", null)
                try {
                    context.let {
                        if (userId != null) {
                            val editor = sharedPreferences.edit()
                            editor.putString("profilePhoto", image)
                            editor.apply()
                            reference
                                .profilePhotoCollection()
                                .document(userId)
                                .set(setImage)
                                .addOnSuccessListener {

                                    Log.e("UserProfileInfo", "save image")
                                }
                                .addOnFailureListener {
                                    Log.e("UserProfileInfo", "save image save error")
                                }
                        }

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

        fun createBitmap(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int): Bitmap {

            var width = kullanicininSectigiBitmap.width
            var height = kullanicininSectigiBitmap.height

            val bitmapOrani: Double = width.toDouble() / height.toDouble()

            if (bitmapOrani > 1) {
                // görselimiz yatay
                width = maximumBoyut
                val kisaltilmisHeight = width / bitmapOrani
                height = kisaltilmisHeight.toInt()
            } else {
                //görselimiz dikey
                height = maximumBoyut
                val kisaltilmisWidth = height * bitmapOrani
                width = kisaltilmisWidth.toInt()

            }


            return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)
        }

        fun resetPassword(
            newPassword: String,
            context: Context,
            viewModelDemand: DemandListViewModel
        ) {

            val email = sharedPreferences.getString("email", "")
            val password = sharedPreferences.getString("password", "")



            if ((email != null) && (password != null)) {

                reference
                    .getFirebaseAuth()
                    .signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            val auth = reference.getFirebaseAuth().currentUser
                            if (auth != null) {
                                auth.updatePassword(newPassword)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            // Şifre güncelleme başarılı
                                            Toast.makeText(
                                                context,
                                                "Şifre başarıyla güncellendi.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            reference.getFirebaseAuth().signOut()
                                            val alertDialog = AlertDialog.Builder(context)

                                            alertDialog.setTitle("Şifre Değişti")
                                            alertDialog.setMessage("Lütfen Giriş Yapınız")
                                            alertDialog.setPositiveButton("Giriş Yap") { dialogInterface, i ->
                                                viewModelDemand.onLogOutClick(context)
                                                val intent =
                                                    Intent(context, MainActivity::class.java)
                                                intent.flags =
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                context.startActivity(intent)
                                            }

                                            alertDialog.setNegativeButton("Çıkış") { dialogInterface, i ->
                                                val intent = Intent(Intent.ACTION_MAIN)
                                                intent.addCategory(Intent.CATEGORY_HOME)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                context.startActivity(intent)
                                            }

                                            alertDialog.create().show()
                                        } else {
                                            // Şifre güncelleme başarısız
                                            Toast.makeText(
                                                context,
                                                "Şifre güncelleme başarısız. Hata: ${updateTask.exception?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        }
                    }

            }

        }
}

