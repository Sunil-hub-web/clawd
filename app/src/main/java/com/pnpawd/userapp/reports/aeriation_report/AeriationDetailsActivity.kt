package com.pnpawd.userapp.reports.aeriation_report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pnpawd.userapp.R

class AeriationDetailsActivity : AppCompatActivity() {
    private lateinit var txtUniqueID: TextView
    private lateinit var txtFarmerName: TextView
    private lateinit var txtPlotNo: TextView
    private lateinit var txtPlotUniqueId: TextView
    private lateinit var txtAerationNumber: TextView
    private lateinit var txtReason: TextView

    private lateinit var imgBack: ImageView
    private lateinit var imgEdt: ImageView

    private lateinit var pipe_installation_id: String
    private lateinit var aeration_no: String
    private lateinit var pipe_no: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeriation_details)

        txtAerationNumber = findViewById(R.id.aeration_details_aeration_number)
        txtPlotUniqueId = findViewById(R.id.aeration_details_plot_unique_id)
        txtFarmerName = findViewById(R.id.aeration_details_farmer_name)
        txtReason = findViewById(R.id.aeriation_report_detail_reason)
        txtPlotNo = findViewById(R.id.aeration_details_plot_number)
        txtUniqueID = findViewById(R.id.aeration_details_uniqueId)

        imgEdt = findViewById(R.id.aeriation_report_detail_edit)
        imgBack = findViewById(R.id.aeration_report_back)

        val bundle = intent.extras
        if (bundle != null) {
            txtPlotUniqueId.text = bundle.getString("farmer_uniqueId").toString()
            pipe_installation_id = bundle.getString("pipe_installation_id")!!
            txtFarmerName.text = bundle.getString("farmer_name")!!
            txtUniqueID.text = bundle.getString("uniqueId")!!
            txtAerationNumber.text = bundle.getString("aeration_no")!!
            txtPlotNo.text = bundle.getString("plot_no")!!
            txtReason.text = bundle.getString("reasons")!!
            pipe_no = bundle.getString("pipe_no")!!
        }

        imgBack.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })


        imgEdt.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AeriationReportMapActivity::class.java).apply {
                putExtra("farmer_plot_uniqueid", txtPlotUniqueId.text)
                putExtra("pipe_installation_id", pipe_installation_id)
                putExtra("aeration_no", txtAerationNumber.text)
                putExtra("farmer_name", txtFarmerName.text)
                putExtra("unique_id", txtUniqueID.text.toString())
                putExtra("plot_no", txtPlotNo.text)
                putExtra("pipe_no", pipe_no)
            }
            startActivity(intent)
        })
    }
}