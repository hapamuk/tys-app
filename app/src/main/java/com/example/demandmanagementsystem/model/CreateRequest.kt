package com.example.demandmanagementsystem.model

data class CreateRequest(
    var requestType: String,
    var requestName: String,
    var requestDepartment: String,
    var requestSendDepartment: String,
    var requestSubject: String,
    var requestDescription: String,
    var requestContactNumber: String
) {
}