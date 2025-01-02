package com.example.demandmanagementsystem.util

import java.util.Calendar

class CurrentDateTime {

    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Ay 0 ile başlar, bu yüzden 1 ekliyoruz
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return "$hour:$minute-$day/$month/$year"
    }

}