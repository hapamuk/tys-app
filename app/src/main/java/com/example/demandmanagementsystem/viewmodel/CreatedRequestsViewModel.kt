package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.model.Requests
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.SortListByDate

class CreatedRequestsViewModel(application: Application) : AndroidViewModel(application) {
    init {
        setupSnapshotListener(application)
    }
    private val reference = FirebaseServiceReference()
    private val sort = SortListByDate()

    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val createdRequestsList: MutableLiveData<List<Requests>> = MutableLiveData()
    private val createdRequestsFilterList: MutableLiveData<List<Requests>> = MutableLiveData()
    val  createdRequestsLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    private  var createdRequestsListTemp: ArrayList<Requests>? = null


    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()

    val username: MutableLiveData<String?>
        get() = _username

    val departmentType: MutableLiveData<String?>
        get() = _departmentType

    private val _authorityType: MutableLiveData<String?> = MutableLiveData()

    val authorityType: MutableLiveData<String?>
        get() = _authorityType

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
                        Log.e("DemandListViewModel", "burada  guide $guide")
                        Log.e("DemandListViewModel", "burada  token $token")

                        if (guide != token){
                            alertDialogListener?.showAlertDialog()
                        }

                    }
                }
        }

    }

    fun fetchData() {
        val userId = sharedPreferences.getString("userId",null)
        createdRequestsLoading.value = true

        if (userId != null) {
            reference.usersCollection().document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {

                        reference.requestsCollection()
                            .get()
                            .addOnSuccessListener { documentsnapshot ->
                                var createdRequestList = ArrayList<Requests>()

                                for (document in documentsnapshot.documents) {
                                    val requests = Requests(
                                        document.id,
                                        document.getString("requestCase").toString(),
                                        document.getString("requestContactNumber").toString(),
                                        document.getString("requestDate").toString(),
                                        document.getString("requestDepartment").toString(),
                                        document.getString("requestDescription").toString(),
                                        document.getString("requestName").toString(),
                                        document.getString("requestSendDepartment").toString(),
                                        document.getString("requestSendId").toString(),
                                        document.getString("requestSubject").toString(),
                                        document.getString("requestType").toString()
                                    )

                                    if (requests.requestSendID == userId) {
                                        createdRequestList.add(requests)
                                    }
                                }
                                createdRequestsListTemp = createdRequestList
                                val sortedList = sort.sortRequestsListByDate(createdRequestList)

                                createdRequestsList.value = sortedList
                                createdRequestsLoading.value = false
                            }
                            .addOnFailureListener {
                                createdRequestsLoading.value = false
                                Log.e("CreatedRequestViewModel", "fetchData => FireStore Veri Çekme Hatası")
                            }
                    } else {
                        createdRequestsLoading.value = false
                        Log.d("CreatedRequestViewModel", "fetchData => Kullanıcı bulunamadı")
                    }
                }
                .addOnFailureListener { exception ->
                    createdRequestsLoading.value = false
                    Log.e("CreatedRequestViewModel", "fetchData => Veri çekme hatası: ", exception)
                }
        }
    }
    fun getData(){
        _username.value = sharedPreferences.getString("name","")
        _departmentType.value =  sharedPreferences.getString("departmentType","")
        _authorityType.value =  sharedPreferences.getString("authorityType","")
    }
    private val requestSearchFilterList: MutableLiveData<List<Requests>> = MutableLiveData()
    fun searchInFirestore(text: String) {

        val filteredList = mutableListOf<Requests>()
        reference
            .requestsCollection()
            .get()
            .addOnSuccessListener { result ->
                for (document in result.documents) {
                    val requests = Requests(
                        document.id,
                        document.getString("requestCase").toString(),
                        document.getString("requestContactNumber").toString(),
                        document.getString("requestDate").toString(),
                        document.getString("requestDepartment").toString(),
                        document.getString("requestDescription").toString(),
                        document.getString("requestName").toString(),
                        document.getString("requestSendDepartment").toString(),
                        document.getString("requestSendID").toString(),
                        document.getString("requestSubject").toString(),
                        document.getString("requestType").toString()
                    )
                    if (requests.requestSubject.contains(text) || requests.requestDepartment.contains(text)) {
                        filteredList.add(requests)
                    }
                }
                val sortedList = sort.sortRequestsListByDate(filteredList)
                requestSearchFilterList.value = sortedList

            }
            .addOnFailureListener { exception ->
                Log.e("Search", "Error getting documents: ", exception)
            }
    }


    fun requestSearchFilterList(): MutableLiveData<List<Requests>>{

        createdRequestsLoading.value = false
        return requestSearchFilterList

    }
    fun filterList(selectedFilter: String){
        if (createdRequestsListTemp == null) {
            return
        }

        val listFilter = ArrayList<Requests>()
        for (list in createdRequestsListTemp!!){
            if (list.requestCase == selectedFilter){
                listFilter.add(list)
            }
        }
        val sortedList = sort.sortRequestsListByDate(listFilter)
        createdRequestsFilterList.value = sortedList
    }

    fun getCreatedFilterList(): MutableLiveData<List<Requests>>{
        createdRequestsLoading.value = false
        return createdRequestsFilterList
    }


    fun getCreatedRequestsList(): MutableLiveData<List<Requests>> {
        createdRequestsLoading.value = false
        return  createdRequestsList
    }

}