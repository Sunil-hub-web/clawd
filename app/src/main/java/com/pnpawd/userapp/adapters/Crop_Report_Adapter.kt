package com.pnpawd.userapp.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.CropReportModel
import com.pnpawd.userapp.reports.crop_report.CropDetailsActivity

internal class Crop_Report_Adapter(private var cropReportModel: List<CropReportModel>): RecyclerView.Adapter<Crop_Report_Adapter.MyViewHolder>(){

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var plot_no: TextView = view.findViewById(R.id.firstColumn)
        var uniqueId: TextView = view.findViewById(R.id.secondColumn)
        var area: TextView = view.findViewById(R.id.thirdColumn)
        var season: TextView = view.findViewById(R.id.fourthColumn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.onboarding_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reportModel = cropReportModel[position]
        holder.plot_no.text = reportModel.getPlotNo()
        holder.uniqueId.text = reportModel.getFarmerId()
        holder.area.text = reportModel.getArea()
        holder.season.text = reportModel.getSeason()

        holder.itemView.setOnClickListener {
            Log.e("uniqueId", holder.uniqueId.text.toString())
            val intent = Intent(it.context, CropDetailsActivity::class.java).apply {
                putExtra("uniqueId", holder.uniqueId.text.toString())
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return cropReportModel.size
    }
}