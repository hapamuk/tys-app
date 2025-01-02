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
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.adapter.AlertDialogListener
import com.example.demandmanagementsystem.adapter.MyWorkOrdersAdapter
import com.example.demandmanagementsystem.databinding.ActivityMyWorkOrdersBinding
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.viewmodel.MyWorkOrdersViewModel

class MyWorkOrdersActivity : AppCompatActivity(), SearchView.OnQueryTextListener ,
    AlertDialogListener {

    private val reference= FirebaseServiceReference()
    private lateinit var binding: ActivityMyWorkOrdersBinding
    private lateinit var viewModel: MyWorkOrdersViewModel
    private lateinit var adapter: MyWorkOrdersAdapter
    private val util = WorkOrderUtil()
    private val utilRequest = RequestUtil()

    private lateinit  var spinnerDataAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MyWorkOrdersActivity
            , R.layout.activity_my_work_orders)

        binding.recyclerViewMyWorkOrderList.setHasFixedSize(true)
        binding.recyclerViewMyWorkOrderList.layoutManager = LinearLayoutManager(this@MyWorkOrdersActivity)

        binding.toolbarMyWorkOrder.title = "MyWorkOrder"
        binding.toolbarMyWorkOrder.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbarMyWorkOrder)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        viewModel = ViewModelProvider(this).get(MyWorkOrdersViewModel::class.java)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.spinnerWorkOrderFilter.setSelection(0)

            viewModel.fetchData()
            viewModel.getData()

            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.myWorkOrderLoading.observe(this) { loading ->
            if (loading) {
                binding.myWorkOrderListloading.visibility = View.VISIBLE
                binding.recyclerViewMyWorkOrderList.visibility = View.GONE
            } else {
                binding.myWorkOrderListloading.visibility = View.GONE
                binding.recyclerViewMyWorkOrderList.visibility = View.VISIBLE
            }
        }
        viewModel.getMyOrderWorkList().observe(this@MyWorkOrdersActivity) { workOrder ->
            adapter = MyWorkOrdersAdapter(this@MyWorkOrdersActivity, workOrder!!)
            binding.recyclerViewMyWorkOrderList.adapter = adapter

        }
        viewModel.getData()
        viewModel.fetchData()
        val incomingData = intent.getIntExtra(utilRequest.selectedSpinnerItem,0)
        if (incomingData == 1){
            binding.spinnerWorkOrderFilter.visibility = View.GONE
            binding.toolbarMyWorkOrder.title = "Atanan İşlerim"
            viewModel.getMyOrderWorkList().observe(this@MyWorkOrdersActivity) { workOrder ->
                viewModel.filterList(util.assignedToPerson)
                viewModel.getWorkOrderFilterList().observe(this@MyWorkOrdersActivity) { workOrder ->
                    adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                    binding.recyclerViewMyWorkOrderList.adapter = adapter
                }

            }




        }else {
            viewModel.getMyOrderWorkList().observe(this@MyWorkOrdersActivity) { workOrder ->
                adapter = MyWorkOrdersAdapter(this@MyWorkOrdersActivity, workOrder!!)
                binding.recyclerViewMyWorkOrderList.adapter = adapter
            }
            viewModel.getData()
            viewModel.fetchData()

            spinnerDataAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,
                android.R.id.text1,util.workOrderUtilList)
            binding.spinnerWorkOrderFilter.adapter = spinnerDataAdapter
            binding.spinnerWorkOrderFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedFilter = util.workOrderUtilList[p2]
                    if(p2 == 0){
                        viewModel.getMyOrderWorkList().observe(this@MyWorkOrdersActivity) { workOrder ->
                            adapter = MyWorkOrdersAdapter(this@MyWorkOrdersActivity, workOrder!!)
                            binding.recyclerViewMyWorkOrderList.adapter = adapter
                        }
                        viewModel.getData()
                        viewModel.fetchData()
                    }else{
                        viewModel.filterList(selectedFilter)
                        viewModel.getWorkOrderFilterList().observe(this@MyWorkOrdersActivity) { workOrder ->
                            adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                            binding.recyclerViewMyWorkOrderList.adapter = adapter
                        }
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)
        val item = menu.findItem(R.id.search_demand)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this@MyWorkOrdersActivity)
        binding.toolbarMyWorkOrder.menu.findItem(R.id.add_action).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                binding.spinnerWorkOrderFilter.setSelection(0)
                viewModel.fetchData()
                viewModel.getData()
                true
            }
            android.R.id.home -> {
                onBackPressed() // Geri dönme işlemini yapar
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            viewModel.searchInFirestore(newText)

            viewModel.workOrderSearchFilterList().observe(this@MyWorkOrdersActivity) { workOrder ->
                adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                binding.recyclerViewMyWorkOrderList.adapter = adapter
            }

        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            viewModel.searchInFirestore(query)

            viewModel.workOrderSearchFilterList().observe(this@MyWorkOrdersActivity) { workOrder ->

                adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                binding.recyclerViewMyWorkOrderList.adapter = adapter
            }
        }
        return true
    }
    override fun showAlertDialog() {
        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@MyWorkOrdersActivity)
    }


}