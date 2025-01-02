package com.example.demandmanagementsystem.util

import android.graphics.Color
import android.util.Log

class RequestUtil {

        val assignedToPerson = "İş Yapacak Kişiye Atandı"

        val waitingForApproval = "Onay Bekliyor"

        val selectedSpinnerItem = "REQUEST_LIST"
        val completed = "Tamamlandı"

        val newRequest = "Yeni"

        val deniedRequest = "Talep Reddedildi"

        val deniedWork = "İş Reddedildi"

        val menuTitleWorkCompleted = "İŞİ ONAYLA"

        val menuTitleWorkDenied = "İŞİ REDDET"

        val menuTitleDenied = "REDDET"

        val menuTitleCreateWorkOrder = "İŞ EMRİ OLUŞTUR"

        val intentRequestId = "REQUEST_ID"

        val intentRequestDetail = "REQUEST_DETAIL"

        val departmentManager = "Departman Müdürü"

        val departmentChief = "Departman Şefi"

        val generalManager = "Genel Müdür"

        val requestUtilList = listOf("Talepleri Filtrele",assignedToPerson,waitingForApproval,completed,newRequest,deniedRequest,deniedWork)

        fun getRequestStatusColor(requestCase: String): Int {
                val color = when (requestCase) {
                        assignedToPerson -> Color.BLUE
                        waitingForApproval -> Color.MAGENTA
                        completed -> Color.parseColor("#006400")
                        deniedRequest -> Color.RED
                        deniedWork -> Color.RED
                        else -> Color.BLACK
                }

                return color
        }


}