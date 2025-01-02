package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityCreateRequestBinding
import com.example.demandmanagementsystem.model.CreateRequest
import com.example.demandmanagementsystem.model.JobDetails
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.CurrentDateTime
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.view.DemandListActivity

class CreateRequestViewModel(application: Application) : AndroidViewModel(application) {
    init {
        setupSnapshotListener(application)

    }
    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val reference = FirebaseServiceReference()
    private val dateTime = CurrentDateTime()
    val departmentTypeList = MutableLiveData<List<String>?>()
    private val _requestCreationStatus = MutableLiveData<Boolean>()
    private val util = RequestUtil()

    private val jobDetailsList: MutableLiveData<List<JobDetails>?> = MutableLiveData()
    private var jobDetailsListTemp: ArrayList<JobDetails>? = null
    private var jobDetailsJobTypeList: ArrayList<String>? = null
    private var jobDetailsSubTypeList: ArrayList<String>? = null


    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()

    val usernames: MutableLiveData<String?>
        get() = _username


    val departmentTypes: MutableLiveData<String?>
        get() = _departmentType

    private var alertDialogListener: AlertDialogListener? = null
    fun setAlertDialogListener(listener: AlertDialogListener) {
        alertDialogListener = listener
    }
    init {
        fetchDepartmentTypes()
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
                        Log.e("DemandListViewModel", "burada  guide $guide")
                        Log.e("DemandListViewModel", "burada  token $token")

                        if (guide != token){
                            alertDialogListener?.showAlertDialog()
                        }

                    }
                }
        }

    }


    private fun fetchDepartmentTypes() {
      reference.departmentTypeCollection()
            .get()
            .addOnSuccessListener { documentsnapshot ->
                val departments = mutableListOf<String>()
                departments.add("Seçiniz")
                for (document in documentsnapshot.documents) {
                    val departmentType = document.getString("departmentType")
                    departmentType?.let { departments.add(it) }
                }
                departmentTypeList.postValue(departments)
            }
            .addOnFailureListener { exception ->
                departmentTypeList.postValue(null)
                Log.e("CreateRequestViewModel", "fetchDepartmentTypes => Firestore Veri Çekme Hatası: $exception")
            }
    }

    fun requestFill(binding: ActivityCreateRequestBinding){

        val userId = sharedPreferences.getString("userId","")
        val textRequestName = binding.textRequestUserName
        val textRequestDepartment = binding.textRequestDepartment
        val textRequestTelNo = binding.textRequestContactNumber

        if (userId != null) {

            val username = sharedPreferences.getString("name","")
            val departmentType = sharedPreferences.getString("departmentType","")
            val telNo = sharedPreferences.getString("telNo","")

            if (username != null) {
                usernames.value = username
                departmentTypes.value = departmentType

                textRequestName.setText(username)
                textRequestDepartment.setText(departmentType)
                textRequestTelNo.setText(telNo)
            }

        }
    }

    fun createRequest(request: CreateRequest, context: Context) {
        val userId = sharedPreferences.getString("userId","")

        if (userId == null) {
            _requestCreationStatus.value = false
            return
        }

        val requestMap = hashMapOf(
            "requestSendId" to userId,
            "requestType" to request.requestType,
            "requestName" to request.requestName,
            "requestDepartment" to request.requestDepartment,
            "requestSendDepartment" to request.requestSendDepartment,
            "requestSubject" to request.requestSubject,
            "requestDescription" to request.requestDescription,
            "requestContactNumber" to request.requestContactNumber,
            "requestDate" to dateTime.getCurrentDateTime(),
            "requestCase" to util.newRequest
        )


       reference.requestsCollection()
            .add(requestMap)
            .addOnSuccessListener {

                val intent = Intent(context,
                    DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "Talep Oluşturuldu", Toast.LENGTH_SHORT
                ).show()

                Log.d("CreateRequestViewModel", "fetchDepartmentTypes => Firestore'a talep başarıyla eklendi.")
                _requestCreationStatus.value = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Hata! Talep Oluşturulamadı", Toast.LENGTH_SHORT
                ).show()
                Log.e("CreateRequestViewModel", "fetchDepartmentTypes => Firestore'a talep ekleme hatası: $e")

                _requestCreationStatus.value = false
            }
    }

    fun getJobDetails(){
        reference
            .jobDetailsCollection()
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val tempList = ArrayList<JobDetails>() // Yeni bir liste oluştur
                for (document in documentSnapshot){
                    val jobDetails = JobDetails(
                        document.id,
                        document.getString("jobType").toString(),
                        document.getString("departmentType").toString(),
                        document.get("businessSubtype") as? List<String>

                    )

                    tempList.add(jobDetails) // Geçici listeye ekle
                }

                jobDetailsListTemp = tempList // Geçici listeyi asıl listeye ata
                jobDetailsList.value = jobDetailsListTemp

            }.addOnFailureListener {
                Log.e("CreateRequestViewModel","getJobDetails =>  fonksiyonunda hata")
            }
    }

    fun spinnerDataJobType(departmenType: String) {
        jobDetailsJobTypeList = ArrayList()

        if (jobDetailsListTemp == null) {
            return
        }
        jobDetailsJobTypeList!!.add("Seçiniz")
        for (jobDetail in jobDetailsListTemp!!){
            if (jobDetail.departmentType == departmenType){
                jobDetailsJobTypeList!!.add(jobDetail.jobType)
            }
        }
    }

    fun spinnerDataSubType(jobType: String){
        jobDetailsSubTypeList = ArrayList()

        if (jobDetailsListTemp == null) {
            return
        }
        jobDetailsSubTypeList!!.add("Seçiniz")
        for (jobDetail in jobDetailsListTemp!!){
            if (jobDetail.jobType == jobType){

                for (subType in jobDetail.businessSubtype!!){
                    jobDetailsSubTypeList!!.add(subType)
                }



            }
        }
    }

    fun getJobDetailsSubTypeList(): List<String>? {
        return jobDetailsSubTypeList
    }


    fun getJobDetailsJobTypeList(): List<String>?{
        return jobDetailsJobTypeList
    }


}