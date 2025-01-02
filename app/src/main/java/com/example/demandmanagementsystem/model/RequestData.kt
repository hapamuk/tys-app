package com.example.demandmanagementsystem.model

data class RequestData(
    val requestID: String?,
    val requestName: String?,
    val requestDepartment: String?,
    val requestSendDepartment: String?,
    val requestSubject: String?,
    val requestDescription: String?,
    val requestDate: String?,
    val requestCase: String?,
    val requestType: String?,
    val requestWorkOrderUserSubject: String?,
    val requestWorkOrderSubDescription: String?,
    val requestDenied: String?
)
