package com.example.demandmanagementsystem.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.demandmanagementsystem.R
import com.example.demandmanagementsystem.databinding.RequestCardBinding
import com.example.demandmanagementsystem.model.Requests
import android.view.View
import com.example.demandmanagementsystem.util.RequestUtil
import com.example.demandmanagementsystem.util.WorkOrderUtil
import com.example.demandmanagementsystem.view.RequestDetailActivity


class RequestAdapter(private val context:Context,
                     private val requestList: List<Requests>): RecyclerView.Adapter<RequestAdapter.RequestViewHolder>(){
    private val util = RequestUtil()
    class RequestViewHolder(var view: RequestCardBinding): RecyclerView.ViewHolder(view.root){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<RequestCardBinding>(inflater,
            R.layout.request_card,parent,false)

        view.requestCase = util

        return RequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }


    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.view.request = requestList[position]
        holder.view.objectCardView.setOnClickListener {
            val intent = Intent(context, RequestDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(util.intentRequestId, requestList[position].requestID)
            context.startActivity(intent)
        }

    }


}

