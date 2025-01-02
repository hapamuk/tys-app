package com.example.demandmanagementsystem.model

data class CreateWorkOrder(
    val workOrderRequestId: String,
    val workOrderPersonToDoJob: String,
    val workOrdercreateUserName: String,
    val workOrderDepartment: String,
    val workOrderRequestSubject: String,
    val workOrderRequestDescription: String,
    val workOrderSubject: String,
    val workOrderDescription: String,
    val workOrderAssetInformation: String,
    val workOrderDate: String,
    val workOrderRequestType: String
) {
}