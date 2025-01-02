package com.example.demandmanagementsystem.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.MyWorkOrdersCardBinding
import com.example.demandmanagementsystem.databinding.RequestCardBinding
import com.example.demandmanagementsystem.model.MyWorkOrders
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.view.MyWorkOrderDetailActivity
import com.example.demandmanagementsystem.view.RequestDetailActivity

class MyWorkOrdersAdapter(
    private val context: Context,
    private val myWorkOrderList: List<MyWorkOrders>
): RecyclerView.Adapter<MyWorkOrdersAdapter.MyWorkOrderViewHolder>() {

    private val util = WorkOrderUtil()

    class MyWorkOrderViewHolder(var view: MyWorkOrdersCardBinding): RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyWorkOrdersAdapter.MyWorkOrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<MyWorkOrdersCardBinding>(inflater,
            R.layout.my_work_orders_card,parent,false)

        view.workCase = util

        return MyWorkOrdersAdapter.MyWorkOrderViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyWorkOrdersAdapter.MyWorkOrderViewHolder,
        position: Int
    ) {
        holder.view.myWorkOrder = myWorkOrderList[position]

        holder.view.objectCardView.setOnClickListener {
            val intent = Intent(context, MyWorkOrderDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(util.intentWorkOrderId, myWorkOrderList[position].workOrderID)
            context.startActivity(intent)



        }

    }

    override fun getItemCount(): Int {
       return  myWorkOrderList.size
    }


}