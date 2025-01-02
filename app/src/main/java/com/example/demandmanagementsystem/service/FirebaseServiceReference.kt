package com.example.demandmanagementsystem.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.demandmanagementsystem.view.CreateAlertDialog
import com.example.demandmanagementsystem.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseServiceReference {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val createAlertDialog = CreateAlertDialog()





    fun getFirebaseAuth(): FirebaseAuth {
        return auth
    }

    fun usersCollection(): CollectionReference {
        return firestore.collection("users")
    }

    fun requestsCollection(): CollectionReference {
        return firestore.collection("requests")
    }

    fun workordersCollection(): CollectionReference {
        return firestore.collection("workorders")
    }

    fun departmentTypeCollection(): CollectionReference {
        return firestore.collection("departmentType")
    }

    fun employeeCollection(): CollectionReference {
        return firestore.collection("employee")
    }

    fun jobDetailsCollection(): CollectionReference {
        return firestore.collection("jobdetails")
    }

    fun userSigInTokenCollection(): CollectionReference {
        return firestore.collection("userSigInToken")
    }

    fun profilePhotoCollection(): CollectionReference {
        return firestore.collection("profilePhoto")
    }
    fun sigInOut(sharedPreferences: SharedPreferences,context: Context) {
        auth.signOut()
        sharedPreferences.edit().apply {
            remove("token")
            remove("userId")
            remove("tcIdentityNo")
            remove("email")
            remove("name")
            remove("password")
            remove("telNo")
            remove("authorityType")
            remove("departmentType")
            remove("profilePhoto")
            apply()
        }
        Log.e("DemandListActivitys", "Bilgileriniz silindi-----------------------------")

        createAlertDialog.createAlertDialog(context)
    }

}