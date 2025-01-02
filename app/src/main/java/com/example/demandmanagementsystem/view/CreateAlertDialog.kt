package com.example.demandmanagementsystem.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.model.CreateRequest

class CreateAlertDialog {

    fun createAlertDialog(context: Context){
        val alertDialog = AlertDialog.Builder(context)


        alertDialog.setMessage("Farklı Bir Cihazda Oturum Açtınız. Tekrar Giriş Yapmak İçin Tıklayınız")
        alertDialog.setPositiveButton("Giriş Yap"){ dialogInterface, i ->
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

        }

        alertDialog.setNegativeButton("Çıkış"){ dialogInterface, i ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        alertDialog.create().show()
    }

}