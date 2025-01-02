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
import com.example.demandmanagementsystem.adapter.RequestAdapter
import com.example.demandmanagementsystem.databinding.ActivityCreatedWorkOrderBinding
import com.example.demandmanagementsystem.service.FirebaseServiceReference
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.viewmodel.CreatedWorkOrderViewModel
import com.example.demandmanagementsystem.viewmodel.DemandListViewModel

class CreatedWorkOrderActivity : AppCompatActivity()
    , SearchView.OnQueryTextListener, AlertDialogListener {
    private val reference= FirebaseServiceReference()
    private lateinit var binding: ActivityCreatedWorkOrderBinding
    private lateinit var viewModel: CreatedWorkOrderViewModel
    private lateinit var viewModelDemand: DemandListViewModel
    private lateinit var adapter: MyWorkOrdersAdapter
    private val util = WorkOrderUtil()
    private lateinit var spinnerDataAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this@CreatedWorkOrderActivity,
            R.layout.activity_created_work_order
        )

        binding.recyclerViewCreatedWorkOrder.setHasFixedSize(true)
        binding.recyclerViewCreatedWorkOrder.layoutManager =
            LinearLayoutManager(this@CreatedWorkOrderActivity)

        binding.toolbarCreatedWorkOrder.title = "WorkOrderCreated"
        binding.toolbarCreatedWorkOrder.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbarCreatedWorkOrder)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(CreatedWorkOrderViewModel::class.java)
        viewModelDemand = ViewModelProvider(this).get(DemandListViewModel::class.java)

        binding.swipeRefreshLayoutCreatedWorkOrder.setOnRefreshListener {
            viewModel.fetchData()
            viewModel.getData()
            binding.spinnerCreatedWorkOrderFilter.setSelection(0)
            binding.swipeRefreshLayoutCreatedWorkOrder.isRefreshing = false
        }
        viewModel.createdWorkOrderLoading.observe(this) { loading ->
            if (loading) {
                binding.createdWorkOrderListloading.visibility = View.VISIBLE
                binding.recyclerViewCreatedWorkOrder.visibility = View.GONE
            } else {
                binding.createdWorkOrderListloading.visibility = View.GONE
                binding.recyclerViewCreatedWorkOrder.visibility = View.VISIBLE
            }
        }

        viewModel.getCreatedWorkOrdersList()
            .observe(this@CreatedWorkOrderActivity) { createdWorkOrder ->
                adapter = MyWorkOrdersAdapter(this@CreatedWorkOrderActivity, createdWorkOrder)
                binding.recyclerViewCreatedWorkOrder.adapter = adapter
            }

        viewModel.getData()
        viewModel.fetchData()

        spinnerDataAdapter = ArrayAdapter(this@CreatedWorkOrderActivity, android.R.layout.simple_list_item_1,
            android.R.id.text1,util.workOrderUtilList)

        binding.spinnerCreatedWorkOrderFilter.adapter = spinnerDataAdapter

        binding.spinnerCreatedWorkOrderFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedFilter = util.workOrderUtilList[p2]
                if(p2 == 0){
                    viewModel.getCreatedWorkOrdersList().observe(this@CreatedWorkOrderActivity) { createdWorkOrders ->

                        adapter = MyWorkOrdersAdapter(this@CreatedWorkOrderActivity, createdWorkOrders)
                        binding.recyclerViewCreatedWorkOrder.adapter = adapter
                    }

                    viewModel.getData()
                    viewModel.fetchData()
                }else{
                    viewModel.filterList(selectedFilter)

                    viewModel.getCreatedFilterList().observe(this@CreatedWorkOrderActivity) { createdRequests ->

                        adapter = MyWorkOrdersAdapter(this@CreatedWorkOrderActivity, createdRequests)
                        binding.recyclerViewCreatedWorkOrder.adapter = adapter
                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }


        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)

        val item = menu.findItem(R.id.search_demand)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this@CreatedWorkOrderActivity)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                binding.spinnerCreatedWorkOrderFilter.setSelection(0)

                viewModel.fetchData()
                viewModel.getData()
                true
            }
            android.R.id.home -> {
                onBackPressed() // Geri dönme işlemini yapar
                return true
            }
            R.id.add_action -> {
                viewModelDemand.onCreateWorkOrderClick(this@CreatedWorkOrderActivity)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            Log.e("onQueryTextChange",newText)
            viewModel.searchInFirestore(newText)

            viewModel.workOrderSearchFilterList().observe(this@CreatedWorkOrderActivity) { workOrder ->
                adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                binding.recyclerViewCreatedWorkOrder.adapter = adapter
            }

        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            Log.e("onQueryTextChange",query)
            viewModel.searchInFirestore(query)

            viewModel.workOrderSearchFilterList().observe(this@CreatedWorkOrderActivity) { workOrder ->

                adapter = MyWorkOrdersAdapter(applicationContext, workOrder!!)
                binding.recyclerViewCreatedWorkOrder.adapter = adapter
            }
        }
        return true
    }
    override fun showAlertDialog() {
        val sharedPreferences = getSharedPreferences("GirisBilgi", Context.MODE_PRIVATE)
        reference.sigInOut(sharedPreferences, this@CreatedWorkOrderActivity)
    }

}
