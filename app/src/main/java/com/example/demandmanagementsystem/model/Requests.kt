package com.example.demandmanagementsystem.model

data class Requests(
    var requestID: String,
    var requestCase: String,
    var requestContactNumber: String,
    var requestDate: String,
    var requestDepartment: String,
    var requestDescription: String,
    var requestName: String,
    var requestSendDepartment: String,
    var requestSendID: String,
    var requestSubject: String,
    var requestType: String

) {
}
