package com.pnpawd.userapp.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R

import com.pnpawd.userapp.reports.aeriation_report.AeriationDetailsActivity
import com.pnpawd.userapp.models.AeriationReportModel

internal class Aeriation_Report_Adapter(private var aerationReportModel: List<AeriationReportModel>) : RecyclerView.Adapter<Aeriation_Report_Adapter.MyViewHolder>() {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var srNo: TextView = view.findViewById(R.id.firstColumn)
        var uniqueId: TextView = view.findViewById(R.id.secondColumn)
        var farmerUniqueId: TextView = view.findViewById(R.id.thirdColumn)
        var pipeNo: TextView = view.findViewById(R.id.fourthColumn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Aeriation_Report_Adapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.aeriation_report_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: Aeriation_Report_Adapter.MyViewHolder, position: Int) {
        val reportModel = aerationReportModel[position]
        holder.srNo.text = reportModel.getId()
        holder.uniqueId.text = reportModel.getFarmerId()
        holder.farmerUniqueId.text = reportModel.getFarmerPlotUniqueId()
        holder.pipeNo.text = reportModel.getPipeNo()

        holder.itemView.setOnClickListener {
            Log.e("uniqueId", holder.uniqueId.text.toString())
            val intent = Intent(it.context, AeriationDetailsActivity::class.java).apply {
                putExtra("farmer_uniqueId", holder.farmerUniqueId.text.toString())
                putExtra("pipe_no", holder.pipeNo.text.toString())
                putExtra("uniqueId", holder.uniqueId.text.toString())
                putExtra("pipe_installation_id", reportModel.getPipeInstallationId())
                putExtra("aeration_no", reportModel.getAerationNo())
                putExtra("plot_no", reportModel.getPlotNo())
                putExtra("farmer_name", reportModel.getFarmerName())
                putExtra("reasons", reportModel.getReason())
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return aerationReportModel.size
    }
}