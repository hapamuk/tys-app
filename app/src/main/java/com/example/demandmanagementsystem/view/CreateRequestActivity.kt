package com.example.demandmanagementsystem.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityCreateRequestBinding
import com.example.demandmanagementsystem.model.CreateRequest
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.viewmodel.CreateRequestViewModel
import com.google.android.material.textfield.TextInputEditText

class CreateRequestActivity : AppCompatActivity() , AlertDialogListener {
    private val reference= FirebaseServiceReference()
    private lateinit var binding: ActivityCreateRequestBinding
    private lateinit var viewModel: CreateRequestViewModel
    private val departmentTypeList = ArrayList<String>()
    private lateinit var spinnerDataAdapter: ArrayAdapter<String>
    private lateinit var spinnerDataJobTypeAdapter: ArrayAdapter<String>
    private lateinit var spinnerDataSubJobTypeAdapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@CreateRequestActivity
            , R.layout.activity_create_request)

        binding.toolbarRequest.title = "Request"
        setSupportActionBar(binding.toolbarRequest)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel = ViewModelProvider(this).get(CreateRequestViewModel::class.java)
        telNoController()
        viewModel.requestFill(binding)

        viewModel.departmentTypeList.observe(this, Observer { departmentList ->
            departmentList?.let {
                departmentTypeList.clear()
                departmentTypeList.addAll(it)
                spinnerDataAdapter.notifyDataSetChanged()
            }
        })

        viewModel.getJobDetails()

        spinnerDataAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1
            , android.R.id.text1, departmentTypeList)
        binding.spinnerRequestDepartment.adapter = spinnerDataAdapter

        binding.spinnerRequestDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {
                if (indeks == 0){
                    binding.spinnerRequestType.isClickable = false
                    binding.spinnerRequestType.isFocusable = false

                }else{
                    spinnerDataJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity
                        ,android.R.layout.simple_list_item_1, android.R.id.text1,listOf())

                    binding.spinnerRequestType.adapter = spinnerDataJobTypeAdapter

                    spinnerDataSubJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,
                        android.R.layout.simple_list_item_1,android.R.id.text1, listOf()
                    )
                    binding.spinnerRequestSubject.adapter = spinnerDataSubJobTypeAdapter

                    spinner(departmentTypeList[indeks])
                }





            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

    }

    fun spinner(temp: String){

        viewModel.spinnerDataJobType(temp)
        val jobTypeList = viewModel.getJobDetailsJobTypeList() ?: listOf()
        spinnerDataJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,android.R.layout.simple_list_item_1, android.R.id.text1,jobTypeList)

        binding.spinnerRequestType.adapter = spinnerDataJobTypeAdapter

        binding.spinnerRequestType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                if (p2 == 0){
                    binding.spinnerRequestSubject.isClickable = false
                    binding.spinnerRequestSubject.isFocusable = false

                }else {
                    spinnerSubtype(jobTypeList[p2])
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }
    fun spinnerSubtype(temp: String){

        viewModel.spinnerDataSubType(temp)

        val subTypeList = viewModel.getJobDetailsSubTypeList() ?: listOf()
        spinnerDataSubJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,
            android.R.layout.simple_list_item_1,android.R.id.text1,subTypeList)
        binding.spinnerRequestSubject.adapter = spinnerDataSubJobTypeAdapter

        binding.spinnerRequestSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
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
                spinnerDataJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,android.R.layout.simple_list_item_1, android.R.id.text1,listOf())

                binding.spinnerRequestType.adapter = spinnerDataJobTypeAdapter

                spinnerDataSubJobTypeAdapter = ArrayAdapter(this@CreateRequestActivity,
                    android.R.layout.simple_list_item_1,android.R.id.text1, listOf()
                )
                binding.spinnerRequestSubject.adapter = spinnerDataSubJobTypeAdapter
                binding.spinnerRequestSubject.setSelection(0)
                binding.spinnerRequestDepartment.setSelection(0)
                binding.textRequestDescription.setText("")
                binding.spinnerRequestType.setSelection(0)

                true
            }
            R.id.workOrderCreate -> {

                val alerDialog = AlertDialog.Builder(this@CreateRequestActivity)
                val requestType = binding.spinnerRequestType.selectedItem
                val requestName = binding.textRequestUserName.text.toString()
                val requestDepartment = binding.textRequestDepartment.text.toString()
                val requestSendDepartment = binding.spinnerRequestDepartment.selectedItem
                val requestSubject = binding.spinnerRequestSubject.selectedItem
                val requestDescription = binding.textRequestDescription.text.toString()
                val requestContactNumber = binding.textRequestContactNumber.text.toString()
                val telNoLenght = requestContactNumber.trim().length

                if ((requestType == null) || (requestSendDepartment == null) || (requestSubject == null)
                    || (requestDescription == "")|| (requestContactNumber == "")
                    || (requestSendDepartment == "Seçiniz") || (requestType == "Seçiniz")|| (requestSubject == "Seçiniz")){

                    alerDialog.setTitle("Talep Oluşturulamadı")
                    alerDialog.setMessage("Boş Alanları Doldurunuz")
                    alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                    }
                    alerDialog.setNegativeButton("Ana Sayfa"){ dialogInterface, i ->
                        val intent = Intent(this@CreateRequestActivity,
                            DemandListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                    alerDialog.create().show()
                }else{
                        Log.e("buradayım","${requestContactNumber.length}")
                    if (telNoLenght < 17 ){
                        alerDialog.setMessage("Telefon Numaranız 11 Haneden Az Olamaz")
                        alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                        }
                        alerDialog.create().show()
                    }else{
                        alerDialog.setMessage("Talep Oluşturulsun Mu?")
                        alerDialog.setPositiveButton("Evet"){ dialogInterface, i ->

                            viewModel.createRequest(
                                CreateRequest(
                                    requestType.toString(),
                                    requestName,
                                    requestDepartment,
                                    requestSendDepartment.toString(),
                                    requestSubject.toString(),
                                    requestDescription,
                                    requestContactNumber
                                ),this@CreateRequestActivity
                            )

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
        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@CreateRequestActivity)
    }
    fun telNoController(){
        binding.textRequestContactNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting: Boolean = false
            private var deleting: Boolean = false
            private val formatPattern = "# (###) ### ## ##"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Bu işlevi kullanmıyoruz
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Bu işlevi kullanmıyoruz
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) {
                    isFormatting = false
                    return
                }

                val unformattedText = s?.toString()?.replace("[^\\d]".toRegex(), "") ?: ""
                val formattedText = StringBuilder()
                var charIndex = 0

                for (i in formatPattern.indices) {
                    if (charIndex >= unformattedText.length) {
                        break
                    }

                    val formatChar = formatPattern[i]
                    if (formatChar == '#') {
                        formattedText.append(unformattedText[charIndex])
                        charIndex++
                    } else {
                        formattedText.append(formatChar)
                    }
                }

                isFormatting = true
                binding.textRequestContactNumber.setText(formattedText)
                binding.textRequestContactNumber.setSelection(formattedText.length)
            }
        })

    }


}