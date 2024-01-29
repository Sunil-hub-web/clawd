package com.pnpawd.userapp.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.OnBoardingDetailsModel
import com.pnpawd.userapp.reports.farmer_report.OnBoardingDetailsStatusActivity

internal class OnBoardingDetailsAdapter(private var onBoardingDetailsModel: List<OnBoardingDetailsModel>) : RecyclerView.Adapter<OnBoardingDetailsAdapter.MyViewHolder>(){

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var plot_no: TextView = view.findViewById(R.id.details_plot_number)
        var area: TextView = view.findViewById(R.id.details_area_hector)
        var ownership: TextView = view.findViewById(R.id.details_ownership)
        var owner_ship: TextView = view.findViewById(R.id.details_owner_name)
        var survey_number: TextView = view.findViewById(R.id.details_survey_number)
        var ivEdit : ImageView = view.findViewById(R.id.ivEdit)
        var linear : LinearLayout = view.findViewById(R.id.layoutLinear)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.onboarding_details_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reportModel = onBoardingDetailsModel[position]
        holder.plot_no.text = reportModel.getPlotNo()
        holder.area.text = reportModel.getArea()
        holder.ownership.text = reportModel.getOwnership()
        holder.owner_ship.text = reportModel.getOwnerName()
        holder.survey_number.text = reportModel.getSurveyNo()
        holder.linear

        if (reportModel.getStatus() == "Rejected"){
            holder.ivEdit.isVisible = true
            holder.linear.setBackgroundColor(Color.RED)
        }
        else if (reportModel.getStatus() == "Pending"){
            holder.linear.setBackgroundColor(Color.BLUE)
        }
        else if (reportModel.getStatus() == "Approved"){
            holder.linear.setBackgroundColor(Color.GREEN)
        }


        holder.ivEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, OnBoardingDetailsStatusActivity::class.java).apply {
                putExtra("unique_id", reportModel.getUniqueID())
                putExtra("plot_no", reportModel.getPlotNo())
                putExtra("base_value",reportModel.getBaseValue())
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return onBoardingDetailsModel.size
    }

}