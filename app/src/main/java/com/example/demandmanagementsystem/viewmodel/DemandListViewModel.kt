package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.adapter.RequestAdapter
import com.example.demandmanagementsystem.databinding.ActivityDemandListBinding
import com.example.demandmanagementsystem.databinding.ActivityUserProfileInfoBinding
import com.example.demandmanagementsystem.databinding.NavViewImageTextBinding
import com.example.demandmanagementsystem.model.Requests
import com.example.demandmanagementsystem.model.User
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.SortListByDate
import com.example.demandmanagementsystem.view.AddPersonActivity
import com.example.demandmanagementsystem.view.CreateRequestActivity
import com.example.demandmanagementsystem.view.CreateWorkOrderActivity
import com.example.demandmanagementsystem.view.CreatedRequestsActivity
import com.example.demandmanagementsystem.view.CreatedWorkOrderActivity
import com.example.demandmanagementsystem.view.MainActivity
import com.example.demandmanagementsystem.view.MyWorkOrdersActivity
import com.example.demandmanagementsystem.view.UserProfileInfoActivity
import java.util.Locale

class DemandListViewModel(application: Application) : AndroidViewModel(application) {

    init {
        val reference = FirebaseServiceReference()
        setupSnapshotListener(application,reference)
    }

    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    private val reference = FirebaseServiceReference()
    private val sort = SortListByDate()

    private val requestList: MutableLiveData<List<Requests>> = MutableLiveData()
    private val requestFilterList: MutableLiveData<List<Requests>> = MutableLiveData()
    val requestLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _username: MutableLiveData<String?> = MutableLiveData()
    private val _departmentType: MutableLiveData<String?> = MutableLiveData()

    val username: MutableLiveData<String?>
        get() = _username

    val departmentType: MutableLiveData<String?>
        get() = _departmentType

    private val _authorityType: MutableLiveData<String?> = MutableLiveData()

    val authorityType: MutableLiveData<String?>
        get() = _authorityType

    private  var requestListTemp: ArrayList<Requests>? = null

    private var alertDialogListener: AlertDialogListener? = null

    private val requestSearchFilterList: MutableLiveData<List<Requests>> = MutableLiveData()


    fun setAlertDialogListener(listener: AlertDialogListener) {
        alertDialogListener = listener
    }
    fun getProfileImage(imageView: ImageView) {

        encodeImageToBase64{bitmap ->

            if(bitmap == "null") {
                imageView.setImageResource(R.drawable.baseline_account_circle_24)

            } else {

                val selectedBitmap = decodeBase64ToBitmap(bitmap)
                imageView.setBackgroundResource(0) // Önceki arkaplanı temizle (isteğe bağlı)
                imageView.setImageBitmap(selectedBitmap)

            }

        }
    }
    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        val byteDizisi = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteDizisi, 0, byteDizisi.size)
    }
    fun encodeImageToBase64(callbacks: (String) -> Unit) {
        val userId = sharedPreferences.getString("userId", null)
        if (userId != null) {
            reference
                .profilePhotoCollection()
                .document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    val bitmap = documentSnapshot.getString("image")

                    if (bitmap != null) {
                        callbacks(bitmap)
                    }

                    Log.e("UserProfileInfo", "save image")
                }
                .addOnFailureListener {

                    Log.e("UserProfileInfo", "save image save error")
                }
        }
        callbacks(null.toString())
    }
    fun notificationListener(context: Context,binding:ActivityDemandListBinding){
        val sharedPreferences = context.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        val uuid = sharedPreferences.getString("userId","")
        val util = RequestUtil()
        if (uuid != null){

            reference
                .workordersCollection()
                .whereEqualTo("selectedWorkOrderUserId", uuid)
                .whereEqualTo("workOrderCase",util.assignedToPerson)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val numberOfFilteredDocuments = querySnapshot.size()

                    if (numberOfFilteredDocuments > 0 && numberOfFilteredDocuments > 9) {
                        val marginInDp = 15
                        val marginInPx = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            marginInDp.toFloat(),
                            context.resources.displayMetrics
                        ).toInt()

                        binding.notificationCounter.layoutParams = (binding.notificationCounter.layoutParams as ViewGroup.MarginLayoutParams).apply {
                            marginStart = marginInPx
                        }
                        binding.notificationCounter.visibility = View.VISIBLE
                        binding.notificationCounter.text = "9+"
                    }else if (numberOfFilteredDocuments > 0) {

                        binding.notificationCounter.visibility = View.VISIBLE
                        binding.notificationCounter.text = numberOfFilteredDocuments.toString()
                    } else {
                        binding.notificationCounter.visibility = View.GONE
                        binding.circleImageView.visibility = View.GONE
                    }


                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting documents: ", exception)
                }

        }



    }

    private fun setupSnapshotListener(application: Application,reference: FirebaseServiceReference) {


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

        val userId = sharedPreferences.getString("userId","")

        requestLoading.value = true

        if (userId != null) {


            reference
                .requestsCollection()
                .get()
                .addOnSuccessListener { documentsnapshot ->
                    var requestsList = ArrayList<Requests>()

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
                            document.getString("requestSendID").toString(),
                            document.getString("requestSubject").toString(),
                            document.getString("requestType").toString()
                        )

                        if (departmentType.value == requests.requestSendDepartment) {
                            requestsList.add(requests)
                        }
                    }
                    requestListTemp = requestsList
                    val sortedList = sort.sortRequestsListByDate(requestsList)

                    requestList.value = sortedList
                    requestLoading.value = false
                }
                .addOnFailureListener {
                    requestLoading.value = false
                    Log.e("DemandListViewModel", "fetchData => FireStore Veri Çekme Hatası")
                }
        }
    }
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
                    if (requests.requestSubject.lowercase(Locale.ROOT).contains(text) || requests.requestDepartment.lowercase(Locale.ROOT).contains(text)) {
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

    // ViewModel içerisinde

    fun requestSearchFilterList(): MutableLiveData<List<Requests>>{

        requestLoading.value = false
        return requestSearchFilterList

    }
    fun getData(){
        _username.value = sharedPreferences.getString("name","")
        _departmentType.value =  sharedPreferences.getString("departmentType","")
        _authorityType.value =  sharedPreferences.getString("authorityType","")
    }

    fun filterList(selectedFilter: String) {
        if (requestListTemp == null) {
            // requestListTemp null ise, hata oluşmasını önlemek için işlem yapmayın
            return
        }

        val listFilter = ArrayList<Requests>()
        for (list in requestListTemp!!){
            if (list.requestCase == selectedFilter){
                listFilter.add(list)
            }
        }
        val sortedList = sort.sortRequestsListByDate(listFilter)
        requestFilterList.value = sortedList

    }


    fun getRequestFilterList(): MutableLiveData<List<Requests>>{

        requestLoading.value = false
        return requestFilterList

    }


    fun getRequestList(): MutableLiveData<List<Requests>> {
        requestLoading.value = false
        return requestList
    }

    fun onCreateRequestClick(context: Context) {
        val intent = Intent(context, CreateRequestActivity::class.java)
        context.startActivity(intent)
    }

    fun onCreateWorkOrderClick(context: Context) {
        val intent = Intent(context, CreateWorkOrderActivity::class.java)
        context.startActivity(intent)
    }

    fun onLogOutClick(context: Context) {
        reference.getFirebaseAuth().signOut()

        sharedPreferences.edit().apply {
            remove("userId")
            remove("token")
            remove("tcIdentityNo")
            remove("email")
            remove("name")
            remove("password")
            remove("telNo")
            remove("authorityType")
            remove("departmentType")
            apply()
        }
        Log.e("DemandListActivitys", "Bilgileriniz silindi-----------------------------")

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun onCreatedRequestsClick(context: Context) {
        val intent = Intent(context, CreatedRequestsActivity::class.java)
        context.startActivity(intent)
    }

    fun onAddPersonClick(context: Context) {
        val intent = Intent(context, AddPersonActivity::class.java)
        context.startActivity(intent)
    }
    fun onMyWorkOrders(context: Context) {
        val intent = Intent(context, MyWorkOrdersActivity::class.java)
        context.startActivity(intent)
    }

    fun onCreatedWorkOrdersClick(context: Context) {
        val intent = Intent(context, CreatedWorkOrderActivity::class.java)
        context.startActivity(intent)
    }

    fun onProfileUserInfoClick(context: Context) {
        val intent = Intent(context, UserProfileInfoActivity::class.java)
        context.startActivity(intent)
    }

}