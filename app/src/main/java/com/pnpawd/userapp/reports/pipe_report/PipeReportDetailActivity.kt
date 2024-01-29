package com.pnpawd.userapp.reports.pipe_report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.reports.polygon_report.PolygonReportMapActivity

class PipeReportDetailActivity : AppCompatActivity() {
    private lateinit var txtUniqueID: TextView
    private lateinit var txtFarmerName: TextView
    private lateinit var txtPlotNo: TextView
    private lateinit var txtPlotUniqueId: TextView
    private lateinit var txtPipeNumber: TextView
    private lateinit var txtReason: TextView
    private lateinit var txtPlotArea: TextView

    private lateinit var imgBack: ImageView
    private lateinit var imgEdt: ImageView

    private lateinit var distance: String
    private lateinit var reason_id: String
    private lateinit var pipe_img_id: String
    private lateinit var pipeImageLatitude: String
    private lateinit var pipeImageLongitude: String

    private lateinit var state: TextView
    private lateinit var district: TextView
    private lateinit var taluka: TextView
    private lateinit var villageTv: TextView
    private lateinit var aadharTv: TextView
    private lateinit var mobileTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipe_report_detail)


        txtUniqueID = findViewById(R.id.pipe_report_detail_uniqueId)
        txtFarmerName = findViewById(R.id.pipe_report_detail_name)
        txtPlotNo = findViewById(R.id.pipe_report_detail_plot)
        txtPlotUniqueId = findViewById(R.id.pipe_report_detail_plot_uniqueId)
        txtPipeNumber = findViewById(R.id.pipe_report_detail_pipe)
        txtReason  = findViewById(R.id.pipe_report_detail_reason)
        txtPlotArea = findViewById(R.id.pipe_report_detail_plot_area)

        imgBack = findViewById(R.id.pipe_report_detail_back)
        imgEdt = findViewById(R.id.pipe_report_detail_edit)

        state = findViewById(R.id.pipe_report_detail_plot_state)
        district = findViewById(R.id.pipe_report_detail_district)
        taluka = findViewById(R.id.pipe_report_detail_taluka)
        villageTv = findViewById(R.id.pipe_report_detail_village)
        aadharTv = findViewById(R.id.pipe_report_detail_aadhar)
        mobileTv = findViewById(R.id.pipe_report_detail_mobile)

        val bundle = intent.extras
        if (bundle != null) {
            pipe_img_id = bundle.getString("pipe_img_id").toString()
            txtPlotUniqueId.text = bundle.getString("farmer_uniqueId").toString()
            txtFarmerName.text = bundle.getString("farmer_name").toString()
            txtUniqueID.text = bundle.getString("uniqueId").toString()
            pipeImageLatitude = bundle.getString("lat").toString()
            pipeImageLongitude = bundle.getString("lng").toString()
            txtPlotNo.text = bundle.getString("plot_no").toString()
            txtPipeNumber.text = bundle.getString("pipe_no").toString()
            txtPlotArea.text = bundle.getString("distance").toString()
            txtReason.text = bundle.getString("reasons").toString()
            reason_id = bundle.getString("reason_id").toString()

            state.text = bundle.getString("state").toString()
            district.text = bundle.getString("district").toString()
            taluka.text = bundle.getString("taluka").toString()
            villageTv.text = bundle.getString("village").toString()
            aadharTv.text = bundle.getString("aadhar").toString()
            mobileTv.text = bundle.getString("mobile").toString()

        } else {
            Log.e("unique_id", "Nope")
        }

        imgBack.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })


        imgEdt.setOnClickListener(View.OnClickListener{
            if (reason_id == "8"){
                val intent = Intent(it.context, PipeReportImageActivity::class.java).apply {
                    putExtra("pipe_img_id", pipe_img_id)
                    putExtra("farmer_uniqueId",  txtPlotUniqueId.text)
                    putExtra("pipe_no", txtPipeNumber.text)
                    putExtra("uniqueId", txtUniqueID.text)
                    putExtra("plot_no", txtPlotNo.text)
                    putExtra("lat", pipeImageLatitude)
                    putExtra("lng", pipeImageLongitude)
                    putExtra("distance", txtPlotArea.text)
                }
                startActivity(intent)
            }
            else if (reason_id == "10"){
                val intent = Intent(it.context, PipeReportMapActivity::class.java).apply {
                    putExtra("pipe_img_id", pipe_img_id)
                    putExtra("farmer_uniqueId",  txtPlotUniqueId.text)
                    putExtra("pipe_no", txtPipeNumber.text)
                    putExtra("uniqueId", txtUniqueID.text)
                    putExtra("plot_no", txtPlotNo.text)
                    putExtra("lat", pipeImageLatitude)
                    putExtra("lng", pipeImageLongitude)
                    putExtra("distance", txtPlotArea.text)
                }
                startActivity(intent)
            }
            else if (reason_id == "11"){
                val intent = Intent(it.context, PolygonReportMapActivity::class.java).apply {
                    putExtra("pipe_img_id", pipe_img_id)
                    putExtra("farmer_uniqueId",  txtPlotUniqueId.text)
                    putExtra("pipe_no", txtPipeNumber.text)
                    putExtra("uniqueId", txtUniqueID.text)
                    putExtra("plot_no", txtPlotNo.text)
                    putExtra("lat", pipeImageLatitude)
                    putExtra("lng", pipeImageLongitude)
                    putExtra("distance", txtPlotArea.text)
                }
                startActivity(intent)
            }
        })
    }
}