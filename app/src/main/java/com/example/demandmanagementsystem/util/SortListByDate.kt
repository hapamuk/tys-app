package com.example.demandmanagementsystem.util

import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.model.Requests
import java.text.SimpleDateFormat
import java.util.Locale

class SortListByDate {

    private val requestUtil = RequestUtil()
    private val workOrderUtil = WorkOrderUtil()

    fun sortRequestsListByDate(requestList: List<Requests>): List<Requests> {
        val dateFormat = SimpleDateFormat("HH:mm-dd/MM/yyyy", Locale.getDefault())

        val statusPriorityMap = mapOf(
            requestUtil.newRequest to 1,
            requestUtil.assignedToPerson to 2,
            requestUtil.waitingForApproval to 3,
            requestUtil.completed to 4,
            requestUtil.deniedRequest to 5,
            requestUtil.deniedWork to 6
        )

        return requestList.sortedWith(compareBy<Requests> { statusPriorityMap[it.requestCase] }
            .thenBy { dateFormat.parse(it.requestDate) })
    }





    fun sortWorkOrderListByDate(myWorkOrderList: List<MyWorkOrders>): List<MyWorkOrders> {
        val dateFormat = SimpleDateFormat("HH:mm-dd/MM/yyyy", Locale.getDefault())

        val statusPriorityMap = mapOf(
            workOrderUtil.activityProcessed to 1,
            workOrderUtil.assignedToPerson to 2,
            workOrderUtil.waitingForApproval to 3,
            workOrderUtil.completed to 4,
            workOrderUtil.jobReturn to 5,
            workOrderUtil.deniedWork to 6
        )
        return myWorkOrderList.sortedWith(
            compareBy <MyWorkOrders>{ statusPriorityMap[it.workOrderCase]  }
                .thenBy { it.workOrderDate?.let { date -> dateFormat.parse(date) } }
        )
    }

}