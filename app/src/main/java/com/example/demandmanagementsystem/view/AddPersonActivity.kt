package com.example.demandmanagementsystem.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.databinding.ActivityAddPersonBinding
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.viewmodel.AddPersonViewModel
import com.google.android.material.textfield.TextInputEditText

class AddPersonActivity : AppCompatActivity(), AlertDialogListener {
    private val reference= FirebaseServiceReference()
    private lateinit var binding: ActivityAddPersonBinding
    private lateinit var viewModel: AddPersonViewModel
    private val departmentTypeList = ArrayList<String>()
    private lateinit  var spinnerDataAdapter: ArrayAdapter<String>
    private val typeOfStaffList = ArrayList<String>()
    private lateinit  var typeOfStaffAdapter: ArrayAdapter<String>


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this@AddPersonActivity
                , R.layout.activity_add_person)

           telNoController()
            viewModel = ViewModelProvider(this)[AddPersonViewModel::class.java]
            binding.toolbarAddPerson.title = "Kullanıcı Tanımları"
            setSupportActionBar(binding.toolbarAddPerson)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            viewModel.departmentTypeList.observe(this, Observer { departmentList ->
                departmentList?.let {
                    spinnerDataAdapter.clear()
                    spinnerDataAdapter.addAll(it)
                    spinnerDataAdapter.notifyDataSetChanged()
                }
            })

            spinnerDataAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1
                ,android.R.id.text1,departmentTypeList)

            binding.spinnerAddPersonDepartment.adapter = spinnerDataAdapter

            binding.spinnerAddPersonDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            viewModel.typeOfStaffList.observe(this, Observer { staffList ->
                staffList?.let {
                    typeOfStaffAdapter.clear()
                    typeOfStaffAdapter.addAll(it)
                    typeOfStaffAdapter.notifyDataSetChanged()
                }
            })


            typeOfStaffAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,android.R.id.text1,typeOfStaffList)

            binding.spinnerAddPersonAuthorizotionType.adapter = typeOfStaffAdapter

            binding.spinnerAddPersonAuthorizotionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, indeks: Int, p3: Long) {

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            viewModel.departmentTypeList
            viewModel.typeOfStaffList

        }
    fun telNoController(){

        binding.textAddPersonTC.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Bu işlevi kullanmıyoruz
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Bu işlevi kullanmıyoruz
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace(" ", "")
                if (input.length > 11) {
                    // 11 haneden fazla girişi engelle
                    s?.delete(s.length - 1, s.length)
                }
            }
        })

        val phoneNumberEditText = findViewById<TextInputEditText>(R.id.textAddPersonTelNo)

        phoneNumberEditText.addTextChangedListener(object : TextWatcher {
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
                phoneNumberEditText.setText(formattedText)
                phoneNumberEditText.setSelection(formattedText.length)
            }
        })

    }


        override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reject -> {
                binding.textAddPersonName.setText("")
                binding.textAddPersonTelNo.setText("")
                binding.textAddPersonEmail.setText("")
                binding.textAddPersonTC.setText("")

                true
            }
            R.id.workOrderCreate -> {

                val alerDialog = AlertDialog.Builder(this@AddPersonActivity)
                val tcIdentityNo = binding.textAddPersonTC.text.toString()
                val email = binding.textAddPersonEmail.text.toString()
                val name = binding.textAddPersonName.text.toString()
                val telNo = binding.textAddPersonTelNo.text.toString()
                val authorityType = binding.spinnerAddPersonAuthorizotionType.selectedItem.toString()
                val departmentType = binding.spinnerAddPersonDepartment.selectedItem.toString()

                Log.e("spinner","$authorityType - $departmentType")

                if((tcIdentityNo == "" ) || (email == "") || (name == "") || (telNo == "") || (authorityType == "") || (departmentType == "")){
                    alerDialog.setTitle("Kullanıcı Eklenmedi")
                    alerDialog.setMessage("Boş Alanları Doldurunuz")
                    alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                    }

                    alerDialog.setNegativeButton("Ana Sayfa"){ dialogInterface, i ->
                        val intent = Intent(this@AddPersonActivity,
                            DemandListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                    alerDialog.create().show()
                }else{

                    viewModel.userController(this@AddPersonActivity,email){controll ->

                        if (controll){
                            if(tcIdentityNo.length < 11){
                                alerDialog.setMessage("T.C. Kimlik Numaranız 11 Haneden Az Olamaz")
                                alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                                }
                                alerDialog.create().show()
                            }else if (telNo.length < 17){
                                alerDialog.setMessage("Telefon Numaranız 11 Haneden Az Olamaz")
                                alerDialog.setPositiveButton("Tamam"){ dialogInterface, i ->

                                }
                                alerDialog.create().show()
                            }else{
                                alerDialog.setMessage("Kullanıcı Eklensin mi?")
                                alerDialog.setPositiveButton("Evet"){ dialogInterface, i ->

                                    val password = tcIdentityNo.substring(0, 6)
                                    val userData = UserData(tcIdentityNo, email, name,password, telNo, authorityType, departmentType)
                                    viewModel.addUser(userData,this@AddPersonActivity)

                                }

                                alerDialog.setNegativeButton("Hayır"){ dialogInterface, i ->

                                }

                                alerDialog.create().show()
                            }


                        }else{
                            alerDialog.setTitle("Kullanıcı Eklenmedi")
                            alerDialog.setMessage("Bu Kullanıcı Kayıtlıdır")
                            alerDialog.setPositiveButton("Ana Sayfa"){ dialogInterface, i ->
                                val intent = Intent(this@AddPersonActivity,
                                    DemandListActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                            alerDialog.setNegativeButton("Tekrar Dene"){ dialogInterface, i ->
                                val intent = Intent(this@AddPersonActivity,
                                    AddPersonActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                            alerDialog.create().show()
                        }
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.request_menu, menu)
        return true
    }
    override fun showAlertDialog() {
        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@AddPersonActivity)
    }

}