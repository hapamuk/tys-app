package com.example.demandmanagementsystem.model

data class JobDetails(
    var jobDetailsID: String,
    var jobType: String,
    var departmentType: String,
    var businessSubtype: List<String>?
) {
}