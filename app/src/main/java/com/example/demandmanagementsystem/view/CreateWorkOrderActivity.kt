package com.example.demandmanagementsystem.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityCreateWorkOrderBinding
import com.example.demandmanagementsystem.model.JobDetails
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.viewmodel.CreateWorkOrderViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CreateWorkOrderActivity : AppCompatActivity(), AlertDialogListener {
    private val reference= FirebaseServiceReference()

    private lateinit var binding: ActivityCreateWorkOrderBinding
    private var requestID: String? = null


    private lateinit var viewModel: CreateWorkOrderViewModel

    private var selectedUserId: String? = null
    private val util = RequestUtil()
    private val utilWorkOrder = WorkOrderUtil()
    private lateinit var getSpinnerRequestDataAdapter: ArrayAdapter<String>
    private lateinit var jobDetailsListTemp: ArrayList<JobDetails>
    private lateinit var spinnerWorkOrderTypeList: ArrayList<String>
    private lateinit var spinnerWorkOrderSubTypeList: ArrayList<String>
    private lateinit var spinnerWorkTypeAdapter: ArrayAdapter<String>
    private lateinit var spinnerWorkSubTypeAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CreateWorkOrderActivity
            , R.layout.activity_create_work_order)

        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)


        jobDetailsListTemp = ArrayList()
        binding.toolbarWorkOrder.title = "WorkOrder"
        setSupportActionBar(binding.toolbarWorkOrder)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(CreateWorkOrderViewModel::class.java)


        val incomingData = intent.getIntExtra(util.intentRequestDetail,0)

        if (incomingData == 1){

            getSpinner()
            requestID = intent.getStringExtra(util.intentRequestId)
            binding.layoutWorkOrderDescription.visibility = View.GONE
            binding.layoutWorkOrderSubject.visibility = View.GONE
            binding.layoutWorkOrderType.visibility = View.GONE
            viewModel.loadingData(requestID!!,binding)
            val list = viewModel.getSpinnerDataList()
            getSpinnerRequestData(list)

        }else{
            binding.layoutWorkOrderType.visibility = View.VISIBLE
            binding.layoutWorkOrderRequestType.visibility = View.GONE
            binding.layoutWorkOrderRequestId.visibility = View.GONE
            binding.layoutWorkOrderRequestDescription.visibility = View.GONE
            binding.layoutWorkOrderRequestSubject.visibility = View.GONE

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    jobDetailsListTemp = viewModel.getJobDetails()

                    val username = sharedPreferences.getString("name",null)
                    val departmentType = sharedPreferences.getString("departmentType",null)

                    binding.textWorkOrderPersonToDoJob.setText(username)
                    binding.textWorkOrderDepartment.setText(departmentType)

                    getSpinner()

                    spinnerWorkOrderSubTypeList = ArrayList() // Liste başlatılıyor

                    if (departmentType != null) {
                        spinnerDataJobType(departmentType)
                    }
                    spinnerWorkOrderTypeList.add(0,"Seçiniz")
                    spinnerWorkTypeAdapter = ArrayAdapter(this@CreateWorkOrderActivity, android.R.layout.simple_list_item_1,
                        android.R.id.text1, spinnerWorkOrderTypeList
                    )

                    binding.spinnerWorkOrderType.adapter = spinnerWorkTypeAdapter

                    binding.spinnerWorkOrderType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            if (p2 == 0){

                                binding.spinnerCreateWorkOrderRequestSubject.isClickable = false
                                binding.spinnerCreateWorkOrderRequestSubject.isFocusable = false
                                spinnerWorkOrderSubTypeList.clear()

                            }else {
                                spinnerDataSubType(spinnerWorkOrderTypeList[p2])
                            }
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }

                    binding.layoutWorkOrderType.visibility = View.VISIBLE
                    binding.layoutWorkOrderRequestType.visibility = View.GONE
                    binding.layoutWorkOrderRequestId.visibility = View.GONE
                    binding.layoutWorkOrderRequestDescription.visibility = View.GONE
                    binding.layoutWorkOrderRequestSubject.visibility = View.GONE

                    // ...
                } catch (e: Exception) {
                    // Hata yönetimi burada yapılır
                    Log.e("CreateWorkOrderActivity", "CoroutineScope => Hata: ${e.message}")
                }
            }
        }
    }

    fun spinnerDataJobType(departmenType: String) {
        spinnerWorkOrderTypeList = ArrayList() // Liste başlatılıyor

        for (jobDetail in jobDetailsListTemp){
            if (jobDetail.departmentType == departmenType){
                spinnerWorkOrderTypeList.add(jobDetail.jobType)
            }
        }
    }

    fun spinnerDataSubType(jobType: String){
        spinnerWorkOrderSubTypeList = ArrayList() // Liste başlatılıyor
        val tempList = ArrayList<String>()

        for (jobDetail in jobDetailsListTemp){
            if (jobDetail.jobType == jobType){

                for (subType in jobDetail.businessSubtype!!){
                    spinnerWorkOrderSubTypeList.add(subType)
                }

            }
        }
        spinnerWorkOrderSubTypeList.add(0,"Seçiniz")
        spinnerWorkSubTypeAdapter = ArrayAdapter(this@CreateWorkOrderActivity, android.R.layout.simple_list_item_1,
            android.R.id.text1, spinnerWorkOrderSubTypeList
        )

        binding.spinnerCreateWorkOrderRequestSubject.adapter = spinnerWorkSubTypeAdapter

        binding.spinnerCreateWorkOrderRequestSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }


        }

    }

    fun getSpinnerRequestData(spinnerList: List<String>){

        getSpinnerRequestDataAdapter = ArrayAdapter(this@CreateWorkOrderActivity, android.R.layout.simple_list_item_1,
            android.R.id.text1, spinnerList
        )

        binding.spinnerCreateWorkOrderRequestSubject.adapter = getSpinnerRequestDataAdapter

        binding.spinnerCreateWorkOrderRequestSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }


    }
    fun getSpinner() {
        viewModel.getUsersDataSpinner { departmentUsersList ->
            val userNameList: List<String> = departmentUsersList.map { it.userName }
            val spinnerDataAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1
                , userNameList)
            binding.spinnerCreateWorkOrder.adapter = spinnerDataAdapter

            binding.spinnerCreateWorkOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                    selectedUserId = departmentUsersList[indeks].userId
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.request_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reject -> {

                binding.spinnerCreateWorkOrderRequestSubject.setSelection(0)
                binding.textWorkOrderDescription.setText("")
                binding.textWorkOrderAssetInformation.setText("")
                binding.spinnerWorkOrderType.setSelection(0)
                binding.spinnerCreateWorkOrder.setSelection(0)
                true
            }
            R.id.workOrderCreate -> {
                val alerDialog = AlertDialog.Builder(this@CreateWorkOrderActivity)
                Log.e("burası","$selectedUserId")
                selectedUserId?.let {
                    if (it == "0"){
                        alerDialog.setMessage("İşi Yapacak Personeli Seçiniz")
                        alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                        }
                        alerDialog.create().show()
                    }else if(binding.textWorkOrderAssetInformation.text.toString() == "")
                        {
                        alerDialog.setMessage("Lütfen Boş Alanları Doldurunuz")
                        alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                        }
                        alerDialog.create().show()
                    }else{
                        alerDialog.setMessage("İş Emri Oluşturulsun Mu?")
                        alerDialog.setPositiveButton("Evet"){ dialogInterface, i ->

                            val incomingData = intent.getStringExtra(utilWorkOrder.intentWorkOrderId)

                            if (incomingData == null){
                                viewModel.createWorkOrder(this@CreateWorkOrderActivity,binding, it)

                            }else {
                                viewModel.requestCreateWorkOrder(this@CreateWorkOrderActivity,binding, it)
                            }
                        }
                        alerDialog.setNegativeButton("Hayır"){ dialogInterface, i ->
                        }
                        alerDialog.create().show()
                    }
                }




                true
            }
            android.R.id.home -> {
                onBackPressed() // Geri dönme işlemini yapar
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun showAlertDialog() {
        val sharedPreferences = getSharedPreferences("GirisBilgi",Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@CreateWorkOrderActivity)
    }


}