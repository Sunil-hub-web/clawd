package com.pnpawd.userapp.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.FarmerReportModel
import com.pnpawd.userapp.reports.farmer_report.OnBoardingDetailsActivity

internal class Farmer_Report_Adapter(private var farmerReportModel: List<FarmerReportModel>) : RecyclerView.Adapter<Farmer_Report_Adapter.MyViewHolder>()  {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var srNo: TextView = view.findViewById(R.id.firstColumn)
        var uniqueId: TextView = view.findViewById(R.id.secondColumn)
        var date: TextView = view.findViewById(R.id.thirdColumn)
        var time: TextView = view.findViewById(R.id.fourthColumn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.onboarding_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reportModel = farmerReportModel[position]
        holder.srNo.text = reportModel.getId()
        holder.uniqueId.text = reportModel.getFarmerId()
        holder.date.text = reportModel.getDate()
        holder.time.text = reportModel.getTime()

        holder.itemView.setOnClickListener {
            Log.e("uniqueId", holder.uniqueId.text.toString())
            val intent = Intent(it.context, OnBoardingDetailsActivity::class.java).apply {
                putExtra("uniqueId", holder.uniqueId.text.toString())
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return farmerReportModel.size
    }
}