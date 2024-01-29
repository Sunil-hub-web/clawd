package com.pnpawd.userapp.reports

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.reports.aeriation_report.AeriationReportActivity
import com.pnpawd.userapp.reports.benefit_report.BenefitReportActivity
import com.pnpawd.userapp.reports.crop_report.CropReportActivity
import com.pnpawd.userapp.reports.farmer_report.OnBoardingReportActivity
import com.pnpawd.userapp.reports.pipe_report.PipeReportActivity
import com.pnpawd.userapp.reports.polygon_report.PolygonReportActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportActivity : AppCompatActivity() {
    var token: String = ""

//    private lateinit var farmer_approved: LinearLayout
    private lateinit var farmer_pending: LinearLayout
    private lateinit var farmer_rejected: LinearLayout

//    private lateinit var crop_approved: LinearLayout
    private lateinit var crop_pending: LinearLayout
    private lateinit var crop_rejected: LinearLayout

//    private lateinit var benefit_approved: LinearLayout
    private lateinit var benefit_pending: LinearLayout
    private lateinit var benefit_rejected: LinearLayout

//    private lateinit var pipe_approved: LinearLayout
//    private lateinit var pipe_pending: LinearLayout
    private lateinit var pipe_rejected: LinearLayout

    private lateinit var polygon_rejected: LinearLayout
    private lateinit var polygon_pending: LinearLayout

//    private lateinit var aeration_approved: LinearLayout
    private lateinit var aeration_pending: LinearLayout
    private lateinit var aeration_rejected: LinearLayout

    private lateinit var farmer_registration: LinearLayout
    private lateinit var crop_data: LinearLayout
    private lateinit var benefit_data: LinearLayout

//    private lateinit var Farmer_approved: TextView
    private lateinit var Farmer_pending: TextView
    private lateinit var Farmer_rejected: TextView

//    private lateinit var Crop_approved: TextView
    private lateinit var Crop_pending: TextView
    private lateinit var Crop_rejected: TextView

//    private lateinit var Benefit_approved: TextView
    private lateinit var Benefit_pending: TextView
    private lateinit var Benefit_rejected: TextView

//    private lateinit var Pipe_approved: TextView
    private lateinit var Pipe_pending: TextView
    private lateinit var Pipe_rejected: TextView

    private lateinit var Polygon_pending: TextView
    private lateinit var Polygon_rejected: TextView


    //    private lateinit var Aeration_approved: TextView
    private lateinit var Aeration_pending: TextView
    private lateinit var Aeration_rejected: TextView

    private lateinit var Back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

//        farmer_approved = findViewById(R.id.farmer_registration_approved)
        farmer_pending = findViewById(R.id.farmer_registration_pending)
        farmer_rejected = findViewById(R.id.farmer_registration_rejected)

//        crop_approved = findViewById(R.id.crop_data_approved)
        crop_pending = findViewById(R.id.crop_data_pending)
        crop_rejected = findViewById(R.id.crop_data_rejected)

//        benefit_approved = findViewById(R.id.farmer_benefits_approved)
        benefit_pending = findViewById(R.id.farmer_benefits_pending)
        benefit_rejected = findViewById(R.id.farmer_benefits_rejected)

//        pipe_approved = findViewById(R.id.pipe_rejected)
//        pipe_pending = findViewById(R.id.farmer_benefits_pending)
        pipe_rejected = findViewById(R.id.pipe_rejected)

        polygon_pending = findViewById(R.id.polygon_pending)
        polygon_rejected = findViewById(R.id.polygon_rejected)

//        aeration_approved = findViewById(R.id.aeration_approved)
        aeration_pending = findViewById(R.id.aeration_pending)
        aeration_rejected = findViewById(R.id.aeration_rejected)


//        Farmer_approved = findViewById(R.id.farmer_approved)
        Farmer_pending = findViewById(R.id.farmer_pending)
        Farmer_rejected = findViewById(R.id.farmer_rejected)

//        Crop_approved = findViewById(R.id.crop_approved)
        Crop_pending = findViewById(R.id.crop_pending)
        Crop_rejected = findViewById(R.id.crop_rejected)

//        Benefit_approved = findViewById(R.id.benefit_approved)
        Benefit_pending = findViewById(R.id.benefit_pending)
        Benefit_rejected = findViewById(R.id.benefit_rejected)

//        Aeration_approved = findViewById(R.id.aeration_approved_count)
        Aeration_pending = findViewById(R.id.aeration_pending_count)
        Aeration_rejected = findViewById(R.id.aeration_rejected_count)

//        Pipe_approved = findViewById(R.id.pipe_approved_count)
        Pipe_pending = findViewById(R.id.pipe_pending_count)
        Pipe_rejected = findViewById(R.id.pipe_rejected_count)

        Polygon_pending = findViewById(R.id.polygon_pending_count)
        Polygon_rejected = findViewById(R.id.polygon_rejected_count)


        farmer_registration = findViewById(R.id.farmer_registration_report)
        crop_data = findViewById(R.id.crop_data_report)
        benefit_data = findViewById(R.id.farmer_benefits_report)




        Back = findViewById(R.id.report_back)
        Back.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

//        farmer_approved.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, OnBoardingReportActivity::class.java).apply {
//                putExtra("status", "Approved")
//            }
//            startActivity(intent)
//        })

        farmer_pending.setOnClickListener {
            val intent = Intent(this, OnBoardingReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }

        farmer_rejected.setOnClickListener {
            val intent = Intent(this, OnBoardingReportActivity::class.java).apply {
                putExtra("status", "Rejected")
            }
            startActivity(intent)
        }


//        crop_approved.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, CropReportActivity::class.java).apply {
//                putExtra("status", "Approved")
//            }
//            startActivity(intent)
//        })

        crop_pending.setOnClickListener {
            val intent = Intent(this, CropReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }

        crop_rejected.setOnClickListener {
            val intent = Intent(this, CropReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }


//        benefit_approved.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, BenefitReportActivity::class.java).apply {
//                putExtra("status", "Approved")
//            }
//            startActivity(intent)
//        })


        benefit_pending.setOnClickListener {
            val intent = Intent(this, BenefitReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }

        benefit_rejected.setOnClickListener {
            val intent = Intent(this, BenefitReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }


//        aeration_approved.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, AeriationReportActivity::class.java).apply {
//                putExtra("status", "Approved")
//            }
//            startActivity(intent)
//        })
//
//
//        aeration_pending.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this, AeriationReportActivity::class.java).apply {
//                putExtra("status", "Pending")
//            }
//            startActivity(intent)
//        })

        aeration_rejected.setOnClickListener {
            val intent = Intent(this, AeriationReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }


        polygon_rejected.setOnClickListener {
            val intent = Intent(this, PolygonReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }


//        polygon_pending.setOnClickListener {
//            val intent = Intent(this, PolygonReportActivity::class.java).apply {
//                putExtra("status", "Pending")
//            }
//            startActivity(intent)
//        }


        pipe_rejected.setOnClickListener {
            val intent = Intent(this, PipeReportActivity::class.java).apply {
                putExtra("status", "Pending")
            }
            startActivity(intent)
        }



        getFormCount()
    }

    private fun getFormCount() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.formCount("Bearer $token").enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())

                        val FarmerApproved = stringResponse.optString("FarmersApproved")
                        val FarmerPending = stringResponse.optString("FarmerPending")
                        val FarmersReject = stringResponse.optString("FarmersReject")

                        val CropdataApproved = stringResponse.optString("CropdataApproved")
                        val CropdataPending = stringResponse.optString("CropdataPending")
                        val CropdataRejected = stringResponse.optString("CropdataRejected")

                        val PipeApproved = stringResponse.optString("appr_pipes")
                        val PipePending = stringResponse.optString("pending_pipes")
                        val PipeRejected = stringResponse.optString("reject_pipes")

                        val PolygonPending = stringResponse.optString("pending_polygon")
                        val PolygonRejected = stringResponse.optString("reject_polygon")

                        val BenefitApproved = stringResponse.optString("BenefitApproved")
                        val BenefitPending = stringResponse.optString("BenefitPending")
                        val BenefitRejected = stringResponse.optString("BenefitRejected")

                        val AerationApproved = stringResponse.optString("approved_awd")
                        val AerationPending = stringResponse.optString("pending_awd")
                        val AerationRejected = stringResponse.optString("reject_awd")

//                        Farmer_approved.text = FarmerApproved
                        Farmer_pending.text = FarmerPending
                        Farmer_rejected.text = FarmersReject

//                        Crop_approved.text = CropdataApproved
                        Crop_pending.text = CropdataPending
                        Crop_rejected.text = CropdataRejected

//                        Benefit_approved.text = BenefitApproved
                        Benefit_pending.text = BenefitPending
                        Benefit_rejected.text = BenefitRejected

//                        Pipe_approved.text = PipeApproved
                        Pipe_pending.text = PipePending
                        Pipe_rejected.text = PipeRejected

                        Polygon_rejected.text = PolygonRejected
                        Polygon_pending.text = PolygonPending

//                        Aeration_approved.text = AerationApproved
                        Aeration_pending.text = AerationPending
                        Aeration_rejected.text = AerationRejected
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}