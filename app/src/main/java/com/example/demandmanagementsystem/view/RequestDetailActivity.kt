package com.example.demandmanagementsystem.view


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityRequestDetailBinding
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.viewmodel.RequestDetailViewModel

class RequestDetailActivity : AppCompatActivity() , AlertDialogListener {


    private val reference= FirebaseServiceReference()
    private lateinit var binding: ActivityRequestDetailBinding
    private lateinit var viewModel: RequestDetailViewModel
    private var util = RequestUtil()
    private var requestID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@RequestDetailActivity
            , R.layout.activity_request_detail)

        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(RequestDetailViewModel::class.java)

        binding.toolbarRequest.title = "Detail"
        setSupportActionBar(binding.toolbarRequest)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.requestData.observe(this) { requestData ->
            binding.selectedRequest = requestData
        }

        requestID = intent.getStringExtra(util.intentRequestId)
        viewModel.getData(requestID!!)

        val userDepartmentType = sharedPreferences.getString("departmentType",null)
        val authorityType = sharedPreferences.getString("authorityType",null)

        viewModel.requestData.observe(this) { requestData ->
            if (requestData != null) {
                val requestCase = requestData.requestCase

                if (authorityType == "Departman Çalışanı") {
                    binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible = false
                    binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false


                    if (requestCase == util.waitingForApproval) {
                        // burada department çalışanı olarak ayar yapılacak gibi
                        binding.relativeLayoutWorkOrder.visibility = View.VISIBLE
                        binding.layoutRequestWorkOrderSubDescription.visibility = View.VISIBLE
                        binding.layoutRequestWorkOrderUserSubject.visibility = View.VISIBLE
                    } else if (requestCase == util.completed) {

                        binding.relativeLayoutWorkOrder.visibility = View.VISIBLE

                    } else if (requestCase == util.deniedRequest) {

                        //binding.relativeLayoutWorkOrder.visibility = View.VISIBLE
                        binding.layoutRequestDenied.visibility = View.VISIBLE

                    }

                } else if ((authorityType == util.generalManager) || (authorityType == util.departmentManager) || (authorityType == util.departmentChief)) {

                    if ((requestCase == util.assignedToPerson)) {
                        binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible = false
                        binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false

                    } else if (requestCase == util.waitingForApproval) {


                        if (userDepartmentType == requestData.requestSendDepartment) {
                            binding.relativeLayoutWorkOrder.visibility = View.VISIBLE
                            binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).title =
                                util.menuTitleWorkCompleted
                            binding.toolbarRequest.menu.findItem(R.id.reject).title =
                                util.menuTitleWorkDenied

                            binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible =
                                true
                            binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = true

                        } else {

                            binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible =
                                false
                            binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false

                        }

                    } else if (requestCase == util.completed) {
                        binding.relativeLayoutWorkOrder.visibility = View.VISIBLE
                        binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible = false
                        binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false

                    } else if (requestCase == util.newRequest) {

                        if (userDepartmentType == requestData.requestSendDepartment) {

                            binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible =
                                true
                            binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = true

                        } else {

                            binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible =
                                false
                            binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false

                        }

                    } else if (requestCase == util.deniedRequest) {
                        binding.openMenuButton.visibility = View.GONE
                        binding.layoutRequestDenied.visibility = View.VISIBLE
                        binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible = false
                        binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false

                    } else if (requestCase == util.deniedWork) {

                        binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible = false
                        binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = false

                    } else {
                        binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).isVisible = true
                        binding.toolbarRequest.menu.findItem(R.id.reject).isVisible = true
                    }

                }
            }
        }

        binding.openMenuButton.setOnClickListener {
            if (binding.menuCardView.visibility == View.VISIBLE) {
                binding.menuCardView.visibility = View.GONE

            } else {
                binding.menuCardView.visibility = View.VISIBLE
            }
        }

        binding.openMenuButtonRequest.setOnClickListener {
            if (binding.menuCardViewRequest.visibility == View.VISIBLE) {
                binding.menuCardViewRequest.visibility = View.GONE

            } else {
                binding.menuCardViewRequest.visibility = View.VISIBLE
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reject -> {
                    val title = binding.toolbarRequest.menu.findItem(R.id.reject).title.toString()

                    if(title == util.menuTitleDenied){

                        val design = layoutInflater.inflate(R.layout.alert_tasarim,null)
                        val editTextAlert = design.findViewById(R.id.editTextAlert) as EditText

                        val ad = AlertDialog.Builder(this@RequestDetailActivity)

                        ad.setTitle("Emin Misiniz")
                        ad.setView(design)
                        ad.setPositiveButton("Talebi Reddet"){ dialogInterface, i ->

                            val dataReceived = editTextAlert.text.toString()
                            viewModel.requestDenied(requestID!!,this@RequestDetailActivity,dataReceived)

                        }
                        ad.setNegativeButton("İptal"){ dialogInterface, i ->
                        }
                        ad.create().show()

                    }else if(title == util.menuTitleWorkDenied){
                        viewModel.workDenied(requestID!!,this@RequestDetailActivity)

                    }

                true
            }
            R.id.workOrderCreate -> {
                val title = binding.toolbarRequest.menu.findItem(R.id.workOrderCreate).title.toString()
                if(title == util.menuTitleWorkCompleted){
                    viewModel.workCompleted(requestID!!,this@RequestDetailActivity)



                }else if(title == util.menuTitleCreateWorkOrder){

                    val intent = Intent(this@RequestDetailActivity, CreateWorkOrderActivity::class.java)
                    intent.putExtra(util.intentRequestId, requestID)
                    intent.putExtra(util.intentRequestDetail, 1)
                    startActivity(intent)
                }
                true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.worker_order_menu, menu)
        return true
    }
    override fun showAlertDialog() {
        val sharedPreferences = getSharedPreferences("GirisBilgi",Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@RequestDetailActivity)
    }

}