package com.example.demandmanagementsystem.viewmodel


import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.model.RequestData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.view.CreateMenuAlertDialog
import com.example.demandmanagementsystem.view.DemandListActivity

class RequestDetailViewModel(application: Application) : AndroidViewModel(application){
    init {
        setupSnapshotListener(application)
    }
    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val createMenuAlertDialog = CreateMenuAlertDialog()

    private val reference = FirebaseServiceReference()

    private val _requestData = MutableLiveData<RequestData?>()
    val requestData: LiveData<RequestData?> get() = _requestData

    private val _requestDepartmentData = MutableLiveData<String?>()
    val requestDepartmentData: LiveData<String?>
        get() = _requestDepartmentData


    val util = RequestUtil()
    private var alertDialogListener: AlertDialogListener? = null
    fun setAlertDialogListener(listener: AlertDialogListener) {
        alertDialogListener = listener
    }
    private fun setupSnapshotListener(application: Application) {

        val reference = FirebaseServiceReference()
        val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        val uuid = sharedPreferences.getString("userId","")
        if (uuid != null) {
            reference
                .userSigInTokenCollection()
                .document(uuid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DemandListViewModel", "SnapshotListener error", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val guide = sharedPreferences.getString("token","")
                        val token = snapshot.getString("token")
                        if (guide != token){
                            alertDialogListener?.showAlertDialog()
                        }

                    }
                }
        }

    }

    fun getData(requestID: String) {
        reference.requestsCollection().document(requestID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val requestDetailId = documentSnapshot.id
                    val requestDetailName = documentSnapshot.getString("requestName")
                    val requestDetailDepartment = documentSnapshot.getString("requestDepartment")
                    val requestDetailSendDepartment = documentSnapshot.getString("requestSendDepartment")
                    val requestDetailSubject = documentSnapshot.getString("requestSubject")
                    val requestDetailDescription = documentSnapshot.getString("requestDescription")
                    val requestDetailDate = documentSnapshot.getString("requestDate")
                    val requestDetailCase = documentSnapshot.getString("requestCase")
                    val requestDetailType = documentSnapshot.getString("requestType")
                    val requestWorkOrderSubDescription = documentSnapshot.getString("workOrderSubDescription")
                    val requestWorkOrderUserSubject = documentSnapshot.getString("workOrderUserSubject")
                    val requestDenied = documentSnapshot.getString("requestDenied")

                    _requestData.value = RequestData(
                        requestDetailId,
                        requestDetailName,
                        requestDetailDepartment,
                        requestDetailSendDepartment,
                        requestDetailSubject,
                        requestDetailDescription,
                        requestDetailDate,
                        requestDetailCase,
                        requestDetailType,
                        requestWorkOrderUserSubject,
                        requestWorkOrderSubDescription,
                        requestDenied
                    )


                } else {
                    _requestData.value = null
                }
            }
            .addOnFailureListener { exception ->
                _requestDepartmentData.value = null
                Log.e("RequestDetailViewModel", "getData => Veri çekme hatası: ", exception)
            }
    }



    fun workCompleted(requestID: String, context: Context){

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.completed
        )

        val updateWorkOrderData = hashMapOf<String, Any>(
            "workOrderCase" to util.completed
        )
        reference
            .requestsCollection()
            .document(requestID)
            .get()
            .addOnSuccessListener { documentSnapshot ->

                val requestCaseListener = documentSnapshot.getString("requestCase").toString()

                if (requestCaseListener == util.completed) {
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu iş Emri Daha Önce Tamamlanmıştır")
                }else if (requestCaseListener == util.deniedRequest){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu Talep Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.deniedWork){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.assignedToPerson){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Bir Personele Atanmıştır")
                }else {
                    reference
                        .requestsCollection()
                        .document(requestID)
                        .update(updateData)
                        .addOnSuccessListener { documentSnapshot ->


                            val intent = Intent(context, DemandListActivity::class.java)
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)

                            Toast.makeText(
                                context,
                                "İş Tamamlandı", Toast.LENGTH_SHORT
                            ).show()

                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Hata! İş Tamamlanamadı", Toast.LENGTH_SHORT
                            ).show()
                            Log.e("RequestDetailViewModel", "workCompleted => Hata ")
                        }

                    reference.workordersCollection()
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            for (document in documentSnapshot.documents){
                                val workOrderRequestId = document.getString("workOrderRequestId").toString()
                                if (workOrderRequestId == requestID){

                                    reference.workordersCollection()
                                        .document(document.id)
                                        .update(updateWorkOrderData)
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RequestDetailViewModel", "workCompleted => work => Hata ")
                                        }
                                    break
                                }
                            }
                        } .addOnFailureListener { e ->
                            Log.e("RequestDetailViewModel", "workCompleted => workorder => Hata ")
                        }
                }
            }
            .addOnFailureListener {
                Log.e("MyWorkOrderViewModel","Daha önce Oluşturulmuştur")
            }
    }

    fun requestDenied(requestID: String, context: Context,dataReceived: String){

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.deniedRequest,
            "requestDenied" to dataReceived
        )
        reference
            .requestsCollection()
            .document(requestID)
            .get()
            .addOnSuccessListener {documentSnapshot ->

                val requestCaseListener = documentSnapshot.getString("requestCase").toString()

                if (requestCaseListener == util.completed) {
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu iş Emri Daha Önce Tamamlanmıştır")
                }else if (requestCaseListener == util.deniedRequest){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu Talep Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.deniedWork){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.assignedToPerson){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Bir Personele Atanmıştır")
                }else{
                    reference.requestsCollection()
                        .document(requestID)
                        .update(updateData)
                        .addOnSuccessListener { documentSnapshot ->
                            val intent = Intent(context, DemandListActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            Toast.makeText(
                                context,
                                "Talep Reddedildi", Toast.LENGTH_SHORT
                            ).show()

                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Hata! Talep Reddedilemedi", Toast.LENGTH_SHORT
                            ).show()
                            Log.e("RequestDetailViewModel", "requestDenied => Hata ")
                        }
                }
            }.addOnFailureListener {
                Log.e("CreateWorkOrderActivity","Daha önce Oluşturulmuştur")
            }


    }

    fun workDenied(requestID: String, context: Context){

        val updateData = hashMapOf<String, Any>(
            "requestCase" to util.deniedWork
        )

        val updateWorkOrderData = hashMapOf<String, Any>(
            "workOrderCase" to util.deniedWork
        )
        reference
            .requestsCollection()
            .document(requestID)
            .get()
            .addOnSuccessListener {doc ->

                val requestCaseListener = doc.getString("requestCase").toString()

                if (requestCaseListener == util.completed) {
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu iş Emri Daha Önce Tamamlanmıştır")
                }else if (requestCaseListener == util.deniedRequest){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu Talep Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.deniedWork){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.assignedToPerson){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Bir Personele Atanmıştır")
                } else{
                    reference.requestsCollection()
                        .document(requestID)
                        .update(updateData)
                        .addOnSuccessListener { documentSnapshot ->
                            val intent = Intent(context, DemandListActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            Toast.makeText(
                                context,
                                "İş Reddedildi", Toast.LENGTH_SHORT
                            ).show()

                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Hata! İş Reddedilemedi", Toast.LENGTH_SHORT
                            ).show()
                            Log.e("RequestDetailViewModel", "workDenied => Hata ")
                        }

                    reference.workordersCollection()
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            for (document in documentSnapshot.documents){
                                val workOrderRequestId = document.getString("workOrderRequestId").toString()
                                if (workOrderRequestId == requestID){
                                    reference.workordersCollection()
                                        .document(document.id)
                                        .update(updateWorkOrderData)
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RequestDetailViewModel", "workDenied => work => Hata ")
                                        }
                                    break
                                }
                            }
                        } .addOnFailureListener { e ->
                            Log.e("RequestDetailViewModel", "workDenied => workOrder => Hata ")
                        }
                }


            }.addOnFailureListener {
                Log.e("CreateWorkOrderActivity","Daha önce Oluşturulmuştur")
            }
    }
}