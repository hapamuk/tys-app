package com.example.demandmanagementsystem.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.model.User
import com.example.demandmanagementsystem.model.UserData
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.view.DemandListActivity

class AddPersonViewModel(application: Application) : AndroidViewModel(application) {

    private val reference = FirebaseServiceReference()
    val departmentTypeList = MutableLiveData<List<String>?>()
    val typeOfStaffList = MutableLiveData<List<String>?>()
    val sharedPreferences = application.getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)

    init {
        fetchDepartmentTypes()
        fetchTypeOfStaffList()
        setupSnapshotListener(application)

    }
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

    private fun fetchDepartmentTypes() {
            reference.departmentTypeCollection()
            .get()
            .addOnSuccessListener { documentsnapshot ->
                val departments = mutableListOf<String>()
                for (document in documentsnapshot.documents) {
                    val departmentType = document.getString("departmentType")
                    departmentType?.let { departments.add(it) }
                }
                departmentTypeList.postValue(departments)
            }
            .addOnFailureListener { exception ->
                departmentTypeList.postValue(null)
                Log.e("AddPersonViewModel", "fetchDepartmentTypes => FireStore Veri Çekme Hatası: $exception")
            }
    }

    private fun fetchTypeOfStaffList() {
        reference.employeeCollection()
            .get()
            .addOnSuccessListener { documentsnapshot ->
                val staffTypes = mutableListOf<String>()
                for (document in documentsnapshot.documents) {
                    val typeOfStaff = document.getString("typeOfStaff")
                    typeOfStaff?.let { staffTypes.add(it) }
                }
                typeOfStaffList.postValue(staffTypes)
            }
            .addOnFailureListener { exception ->
                typeOfStaffList.postValue(null)
                Log.e("AddPersonViewModel", "fetchTypeOfStaffList => FireStore Veri Çekme Hatası: $exception")
            }
    }

    fun addUser(userData: UserData,context: Context) {

        val userMap = hashMapOf(
            "tcIdentityNo" to userData.tcIdentityNo,
            "email" to userData.email,
            "name" to userData.name,
            "telNo" to userData.telNo,
            "authorityType" to userData.authorityType,
            "deparmentType" to userData.departmentType
        )

        reference.getFirebaseAuth()
            .createUserWithEmailAndPassword(userData.email,userData.password)
            .addOnSuccessListener {
                val userId = reference.getFirebaseAuth().currentUser?.uid
                if (userId != null) {
                    reference.usersCollection()
                        .document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            reference.getFirebaseAuth().signOut()

                            getUser(context)

                           Toast.makeText(
                               context,
                               "Kullanıcı Eklendi",Toast.LENGTH_SHORT
                           ).show()
                            Log.d("AddPersonViewModel", "addUser => Firestore'a kayıt başarıyla eklendi.")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Hata!! Kullanıcı Eklenmedi",Toast.LENGTH_SHORT
                            ).show()
                            Log.e("AddPersonViewModel", "addUser => Firestore'a kayıt ekleme hatası: $e")
                        }
                }
            }

    }


    fun getUser(context: Context){


        val email = sharedPreferences.getString("email","Boş").toString()
        val password = sharedPreferences.getString("password","Boş").toString()
        reference.getFirebaseAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val intent = Intent(context,
                    DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                context.startActivity(intent)
            }

        }.addOnFailureListener { exception ->
            Log.e("AddPersonViewModel","getUser => ${exception.localizedMessage}")
        }

    }


    fun userController(context: Context,email: String,callback: (Boolean) -> Unit){

        reference
            .getFirebaseAuth()
            .fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // Bu e-posta adresiyle kayıtlı kullanıcı yok
                        // Kullanıcıyı eklemek veya ilgili işlemleri gerçekleştirmek için gerekli adımları yapabilirsiniz
                        callback.invoke(true)
                    } else {
                        // Bu e-posta adresiyle kayıtlı kullanıcı var
                        // Gerekirse kullanıcıya bir hata mesajı gösterebilirsiniz
                        callback.invoke(false)
                    }
                } else {
                    val exception = task.exception
                    Log.e("AddPersonViewModel","userController => Hata: $exception")


                }
            }.addOnFailureListener {
                Toast.makeText(context,"Tekrar Deneyiniz",Toast.LENGTH_SHORT).show()
                Log.e("AddPersonViewModel","userController => Hata: $it")
                val intent = Intent(context,
                    DemandListActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }


    }



}
