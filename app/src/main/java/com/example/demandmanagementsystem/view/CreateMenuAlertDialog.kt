package com.example.demandmanagementsystem.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog

class CreateMenuAlertDialog {

    fun createMenuAlertDialog(
        context: Context,
        massage: String
    ){
        val alerDialog = AlertDialog.Builder(context)

        alerDialog.setMessage(massage) //Bu Talep Daha Önce Reddedilmiştir
        alerDialog.setPositiveButton("Ana Sayfaya Git"){ dialogInterface, i ->
            val intent = Intent(context, DemandListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        alerDialog.setNegativeButton("Çıkış"){ dialogInterface, i ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        alerDialog.create().show()
    }

}