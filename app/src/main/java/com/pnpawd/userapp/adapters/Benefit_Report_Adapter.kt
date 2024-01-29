package com.pnpawd.userapp.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.BenefitReportModel
import com.pnpawd.userapp.reports.benefit_report.BenefitDetailActivity

internal class Benefit_Report_Adapter(private var benefitReportModel: List<BenefitReportModel>): RecyclerView.Adapter<Benefit_Report_Adapter.MyViewHolder>() {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var total_plot_area: TextView = view.findViewById(R.id.firstColumn)
        var uniqueId: TextView = view.findViewById(R.id.secondColumn)
        var benefit: TextView = view.findViewById(R.id.thirdColumn)
        var seasons: TextView = view.findViewById(R.id.fourthColumn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.benefit_report_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val benfitModel = benefitReportModel[position]
        holder.total_plot_area.text = benfitModel.getTotalPlot()
        holder.uniqueId.text = benfitModel.getFarmerId()
        holder.benefit.text = benfitModel.getBenefit()
        holder.seasons.text = benfitModel.getSeason()

        holder.itemView.setOnClickListener {
            Log.e("uniqueId", holder.uniqueId.text.toString())
            val intent = Intent(it.context, BenefitDetailActivity::class.java).apply {
                putExtra("uniqueId", holder.uniqueId.text.toString())
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return benefitReportModel.size
    }
}