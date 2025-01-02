package com.example.demandmanagementsystem.util

import android.graphics.Color
import android.util.Log

class WorkOrderUtil {

    val intentWorkOrderId = "WORKORDER_ID"

    val assignedToPerson = "İş Yapacak Kişiye Atandı"

    val waitingForApproval = "Onay Bekliyor"

    val completed = "Tamamlandı"

    val deniedWork = "İş Reddedildi"

    val deniedRequest = "Talep Reddedildi"

    val activityProcessed = "Aktivite İşleme Alındı"

    val jobReturn = "İş İade Edildi"

    val tempKindWorkOrder = 2

    val tempKindRequest = 1

    val workOrderUtilList = listOf("İş Durumlarını Filtere",assignedToPerson,waitingForApproval,
        completed,deniedWork,activityProcessed)

    fun getRequestStatusColor(requestCase: String): Int {
        val color = when (requestCase) {
            assignedToPerson -> Color.BLUE
            waitingForApproval -> Color.MAGENTA
            completed -> Color.parseColor("#006400")
            deniedWork -> Color.RED
            else -> Color.BLACK
        }

        return color
    }

}