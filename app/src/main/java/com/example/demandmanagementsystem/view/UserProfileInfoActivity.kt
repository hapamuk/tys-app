package com.example.demandmanagementsystem.view

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.ActivityUserProfileInfoBinding
import com.example.demandmanagementsystem.databinding.ContentProfileBinding
import com.example.demandmanagementsystem.viewmodel.DemandListViewModel
import com.example.demandmanagementsystem.viewmodel.UserProfileInfoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Exception

class UserProfileInfoActivity : AppCompatActivity() {

    var selectedImage : Uri? = null
    var selectedBitmap : Bitmap? = null

    private lateinit var binding: ActivityUserProfileInfoBinding
    private lateinit var bindingContentProfileBinding: ContentProfileBinding
    private lateinit var viewModel: UserProfileInfoViewModel
    private lateinit var viewModelDemand: DemandListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@UserProfileInfoActivity,R.layout.activity_user_profile_info)
        bindingContentProfileBinding = binding.contentProfile
        viewModel = ViewModelProvider(this).get(UserProfileInfoViewModel::class.java)
        viewModelDemand = ViewModelProvider(this@UserProfileInfoActivity).get(DemandListViewModel::class.java)

        viewModel.getData {
            binding.userData = it
            bindingContentProfileBinding.userData = it
        }

        viewModel.getProfileImage(binding)

        bindingContentProfileBinding.saveButton.setOnClickListener {
            showChangePasswordPopup()
        }

        binding.profileStyledBackButtonText.setOnClickListener {

            val intent = Intent(this, DemandListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }



    }

    private fun showChangePasswordPopup() {
        val popupView = layoutInflater.inflate(R.layout.popup_change_password, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(popupView)
            .show()

        val newPasswordEditText: EditText = popupView.findViewById(R.id.editTextNewPassword)
        val confirmPasswordEditText: EditText = popupView.findViewById(R.id.editTextConfirmPassword)
        val confirmButton: Button = popupView.findViewById(R.id.buttonChangePassword)

        confirmButton.setOnClickListener {
            // Yeni parola işlemlerini burada gerçekleştir
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (newPassword == confirmPassword) {

               viewModel.resetPassword(newPassword,this@UserProfileInfoActivity,viewModelDemand)


                dialog.dismiss()
            } else {
                Toast.makeText(this, "Şifreler Eşleşmiyor. Tekrar Deneyiniz.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun selectedImage(view: View){

        this@UserProfileInfoActivity.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi, izin istememiz gerekiyor
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO),1)
                } else {
                    requestPermissions(arrayOf(READ_EXTERNAL_STORAGE),1)
                }

            } else {
                //izin zaten verilmiş, tekrar istemeden galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){

            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izni aldık
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)

            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            selectedImage = data.data

            try {

                this@UserProfileInfoActivity.let {
                    if(selectedImage != null) {
                        if( Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.userProfilInfoProfilImage.setImageBitmap(selectedBitmap)
                            viewModel.saveImage(selectedBitmap!!,this@UserProfileInfoActivity)

                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,selectedImage)
                            binding.userProfilInfoProfilImage.setImageBitmap(selectedBitmap)
                            viewModel.saveImage(selectedBitmap!!,this@UserProfileInfoActivity)
                        }



                    }
                }


            } catch (e: Exception){
                e.printStackTrace()
            }


        }



        super.onActivityResult(requestCode, resultCode, data)
    }



}