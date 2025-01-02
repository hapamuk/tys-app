package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityCreateWorkOrderBinding
import com.example.demandmanagementsystem.model.JobDetails
import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.model.User
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.CurrentDateTime
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.view.CreateMenuAlertDialog
import com.example.demandmanagementsystem.view.DemandListActivity
import com.example.demandmanagementsystem.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CreateWorkOrderViewModel(application: Application) : AndroidViewModel(application){
    init {
        setupSnapshotListener(application)
    }
    private val createMenuAlertDialog = CreateMenuAlertDialog()
    private val util = WorkOrderUtil()
    private val currentDateTime = CurrentDateTime()
    private val reference = FirebaseServiceReference()
    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
    private lateinit var arrayRequestInfo: ArrayList<String>

    private var spinnerDataList = ArrayList<String>()
    private val departmentUsersList = ArrayList<User>()
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


    suspend fun getJobDetails(): ArrayList<JobDetails> {
        return suspendCoroutine { continuation ->
           reference
               .jobDetailsCollection()
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val tempList = ArrayList<JobDetails>()
                    for (document in documentSnapshot) {
                        val jobDetails = JobDetails(
                            document.id,
                            document.getString("jobType").toString(),
                            document.getString("departmentType").toString(),
                            document.get("businessSubtype") as? List<String>
                        )
                        tempList.add(jobDetails)
                    }
                    continuation.resume(tempList)
                }
                .addOnFailureListener {
                    Log.e("CreateWorkOrderActivity", "getJobDetails => fonksiyonunda hata")
                    continuation.resumeWithException(it)
                }
        }
    }
    fun  loadingData(requestID: String, binding: ActivityCreateWorkOrderBinding){

        arrayRequestInfo = ArrayList()
        spinnerDataList = ArrayList()

        val requestCollectionRef = reference.requestsCollection()

        val textWorkOrderRequestId = binding.textWorkOrderRequestId
        val textWorkOrderRequestSubject = binding.textWorkOrderRequestSubject
        val textWorkOrderRequestDescription = binding.textWorkOrderRequestDescription
        val textWorkOrderRequestType = binding.textWorkOrderRequestType

        if(requestID != ""){
            textWorkOrderRequestId.setText(requestID)  // talep ID

            requestCollectionRef.document(requestID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        val workOrderRequestSendDepartmen = documentSnapshot.getString("requestSendDepartment")
                        val workOrderRequestType = documentSnapshot.getString("requestType")
                        val workOrderRequestSubject = documentSnapshot.getString("requestSubject")
                        val workOrderRequestDescription = documentSnapshot.getString("requestDescription")

                        if ((workOrderRequestType != null) && (workOrderRequestSendDepartmen != null)) {
                            arrayRequestInfo.add(workOrderRequestType)
                            arrayRequestInfo.add(workOrderRequestSendDepartmen)

                            getDataSpinnerRequest { list ->

                                spinnerDataList = list
                            }

                        }
                        textWorkOrderRequestSubject.setText(workOrderRequestSubject)
                        textWorkOrderRequestDescription.setText(workOrderRequestDescription)
                        textWorkOrderRequestType.setText(workOrderRequestType)

                    } else {

                        Log.d("CreateWorkOrderActivity", "loadingData => Talep bulunamadı")
                    }
                }
                .addOnFailureListener { exception ->

                    Log.e("CreateWorkOrderActivity", "loadingData => Veri çekme hatası: ", exception)
                }
        }

        val name = binding.textWorkOrderPersonToDoJob
        val department = binding.textWorkOrderDepartment

        name.setText(sharedPreferences.getString("name",""))
        department.setText(sharedPreferences.getString("departmentType",""))

    }

    fun getDataSpinnerRequest(completion: (ArrayList<String>) -> Unit) {
        val resultList =ArrayList<String>()
        reference
            .jobDetailsCollection()
            .get()
            .addOnSuccessListener { documentSnapshot ->

                resultList.add("Seçiniz")
                for (document in documentSnapshot) {
                    val jobDetails = JobDetails(
                        document.id,
                        document.getString("jobType").toString(),
                        document.getString("departmentType").toString(),
                        document.get("businessSubtype") as? ArrayList<String>
                    )

                    if ((jobDetails.departmentType == arrayRequestInfo[1]) &&
                        (jobDetails.jobType == arrayRequestInfo[0])) {

                        val list = jobDetails.businessSubtype
                        resultList.addAll(list ?: emptyList())
                    }
                }

                completion(resultList)
            }
            .addOnFailureListener {
                Log.e("CreateWorkOrderActivity", "getDataSpinnerRequest => getDataSpinnerRequest")
                completion(resultList)
            }
    }


    fun getUsersDataSpinner(callback: (List<User>) -> Unit) {
        val userDepartmentType = sharedPreferences.getString("departmentType","")
        val userId = sharedPreferences.getString("userId","")
        reference
            .usersCollection()
            .get()
            .addOnSuccessListener { documentsnapshot ->
                departmentUsersList.clear()
                departmentUsersList.add(0,User("Seçiniz","0"))
                for (document in documentsnapshot.documents) {
                    val departmentType = document.getString("deparmentType").toString()
                    if ((document.id != userId) && (departmentType == userDepartmentType)) {
                        val user = User(document.getString("name").toString(), document.id)
                        departmentUsersList.add(user)
                    }
                }
                callback.invoke(departmentUsersList)
            }
            .addOnFailureListener {
                Log.e("CreateWorkOrderActivity", "getUsersDataSpinner => FireStore Veri Çekme Hatası")
                callback.invoke(emptyList())
            }

    }

    fun requestCreateWorkOrder(context: Context,binding: ActivityCreateWorkOrderBinding,selectedUserId: String){

        val selectedWorkOrderUserId = selectedUserId
        val workOrderRequestId = binding.textWorkOrderRequestId.text.toString()
        val workOrderPersonToDoJob = binding.textWorkOrderPersonToDoJob.text.toString()
        val workOrdercreateUserName = binding.spinnerCreateWorkOrder.selectedItem.toString()
        val workOrderDepartment = binding.textWorkOrderDepartment.text.toString()
        val workOrderRequestSubject = binding.textWorkOrderRequestSubject.text.toString()
        val workOrderRequestDescription = binding.textWorkOrderRequestDescription.text.toString()
        val workOrderSubject = binding.spinnerCreateWorkOrderRequestSubject.selectedItem
        val workOrderDescription = binding.textWorkOrderDescription.text.toString()
        val workOrderAssetInformation = binding.textWorkOrderAssetInformation.text.toString()
        val workOrderCase = util.assignedToPerson
        val workOrderDate = currentDateTime.getCurrentDateTime()
        val workOrderRequestType =binding.textWorkOrderRequestType.text.toString()
        val workOrderType = binding.spinnerWorkOrderType.selectedItem
        val createWorkOrderId = sharedPreferences.getString("userId",null)

        val workOrder = hashMapOf(
            "workOrderRequestId" to workOrderRequestId,
            "workOrderPersonToDoJob" to workOrderPersonToDoJob,
            "workOrdercreateUserName" to workOrdercreateUserName,
            "workOrderDepartment" to workOrderDepartment,
            "workOrderRequestSubject" to workOrderRequestSubject,
            "workOrderRequestDescription" to workOrderRequestDescription,
            "workOrderSubject" to workOrderSubject,
            "workOrderDescription" to workOrderDescription,
            "workOrderAssetInformation" to workOrderAssetInformation,
            "workOrderDate" to workOrderDate,
            "selectedWorkOrderUserId" to selectedWorkOrderUserId,
            "workOrderCase" to workOrderCase,
            "workOrderRequestType" to workOrderRequestType,
            "workOrderType" to workOrderType,
            "createWorkOrderId" to createWorkOrderId
        )

        reference
            .requestsCollection()
            .document(workOrderRequestId)
            .get()
            .addOnSuccessListener {documentSnapshot ->

                val requestCaseListener = documentSnapshot.getString("requestCase").toString()

                if (requestCaseListener == util.assignedToPerson){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu iş Emri Daha Önce Oluşturulmuştur")
                }else if (requestCaseListener == util.deniedRequest){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu Talep Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.deniedWork){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Reddedilmiştir")
                }else if (requestCaseListener == util.jobReturn){
                    createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce İade Edilmiştir")
                }else {
                    reference
                        .workordersCollection()
                        .document()
                        .set(workOrder)
                        .addOnSuccessListener {
                            Log.d("CreateWorkOrderActivity", "onOptionsItemSelected => Firestore'a iş emri başarıyla eklendi.")
                            Toast.makeText(context,"İş Emri Gönderildi", Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, DemandListActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)


                        }
                        .addOnFailureListener {
                            Toast.makeText(context,"Hata! İş Emri Gönderilemedi", Toast.LENGTH_SHORT).show()
                            Log.e("CreateWorkOrderActivity","")
                        }



                    val updateData = hashMapOf<String, Any>(
                        "requestCase" to "İş Yapacak Kişiye Atandı"
                    )

                    if (workOrderRequestId != "") {
                        reference
                            .requestsCollection()
                            .document(workOrderRequestId)
                            .update(updateData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "İş Yapacak Kişiye Atandı!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("CreateWorkOrderActivity","onOptionsItemSelected => requestCollectionRef")
                                Toast.makeText(context, "Hata! İş Yapacak Kişiye Atanamadı", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

            }.addOnFailureListener {
                Log.e("CreateWorkOrderActivity","Daha önce Oluşturulmuştur")
            }



    }

    fun createWorkOrder(context: Context,
                        binding: ActivityCreateWorkOrderBinding,
                        selectedUserId: String,
                        ){

        val selectedWorkOrderUserId = selectedUserId
        val workOrderRequestId = binding.textWorkOrderRequestId.text.toString()
        val workOrderPersonToDoJob = binding.textWorkOrderPersonToDoJob.text.toString()
        val workOrdercreateUserName = binding.spinnerCreateWorkOrder.selectedItem.toString()
        val workOrderDepartment = binding.textWorkOrderDepartment.text.toString()
        val workOrderRequestSubject = binding.textWorkOrderRequestSubject.text.toString()
        val workOrderRequestDescription = binding.textWorkOrderRequestDescription.text.toString()
        val workOrderSubject = binding.spinnerCreateWorkOrderRequestSubject.selectedItem
        val workOrderDescription = binding.textWorkOrderDescription.text.toString()
        val workOrderAssetInformation = binding.textWorkOrderAssetInformation.text.toString()
        val workOrderCase = util.assignedToPerson
        val workOrderDate = currentDateTime.getCurrentDateTime()
        val workOrderRequestType =binding.textWorkOrderRequestType.text.toString()
        val workOrderType = binding.spinnerWorkOrderType.selectedItem
        val createWorkOrderId = sharedPreferences.getString("userId",null)

        val workOrder = hashMapOf(
            "workOrderRequestId" to workOrderRequestId,
            "workOrderPersonToDoJob" to workOrderPersonToDoJob,
            "workOrdercreateUserName" to workOrdercreateUserName,
            "workOrderDepartment" to workOrderDepartment,
            "workOrderRequestSubject" to workOrderRequestSubject,
            "workOrderRequestDescription" to workOrderRequestDescription,
            "workOrderSubject" to workOrderSubject,
            "workOrderDescription" to workOrderDescription,
            "workOrderAssetInformation" to workOrderAssetInformation,
            "workOrderDate" to workOrderDate,
            "selectedWorkOrderUserId" to selectedWorkOrderUserId,
            "workOrderCase" to workOrderCase,
            "workOrderRequestType" to workOrderRequestType,
            "workOrderType" to workOrderType,
            "createWorkOrderId" to createWorkOrderId
        )

        reference
            .workordersCollection()
            .document()
            .set(workOrder)
            .addOnSuccessListener {
                Log.d("CreateWorkOrderActivity", "onOptionsItemSelected => Firestore'a iş emri başarıyla eklendi.")
                Toast.makeText(context,"İş Emri Gönderildi", Toast.LENGTH_SHORT).show()

                val intent = Intent(context, DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context,"Hata! İş Emri Gönderilemedi", Toast.LENGTH_SHORT).show()
                Log.e("CreateWorkOrderActivity","onOptionsItemSelected => WorkOrderRef")
            }


        val updateData = hashMapOf<String, Any>(
            "requestCase" to "İş Yapacak Kişiye Atandı"
        )

        if (workOrderRequestId != "") {
            reference
                .requestsCollection()
                .document(workOrderRequestId)
                .get()
                .addOnSuccessListener {documentSnapshot ->

                    val requestCaseListener = documentSnapshot.getString("requestCase").toString()

                    if (requestCaseListener == util.assignedToPerson){
                        createMenuAlertDialog.createMenuAlertDialog(context,"Bu iş Emri Daha Önce Oluşturulmuştur")
                    }else if (requestCaseListener == util.deniedRequest){
                        createMenuAlertDialog.createMenuAlertDialog(context,"Bu Talep Daha Önce Reddedilmiştir")
                    }else if (requestCaseListener == util.deniedWork){
                        createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce Reddedilmiştir")
                    }else if (requestCaseListener == util.jobReturn){
                        createMenuAlertDialog.createMenuAlertDialog(context,"Bu İş Daha Önce İade Edilmiştir")
                    }else {
                        reference
                            .requestsCollection()
                            .document(workOrderRequestId)
                            .update(updateData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "İş Yapacak Kişiye Atandı!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Log.e("CreateWorkOrderActivity"," onOptionsItemSelected =< reqCollec")
                                Toast.makeText(context, "Hata: İş Yapacak Kişiye Atanamadı", Toast.LENGTH_SHORT).show()
                            }
                    }




                }.addOnFailureListener {
                    Log.e("CreateWorkOrderActivity","Daha önce Oluşturulmuştur")
                }

        }
    }



    fun getSpinnerDataList(): List<String> {
        return spinnerDataList
    }



}