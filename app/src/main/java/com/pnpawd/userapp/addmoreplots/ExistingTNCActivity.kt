package com.pnpawd.userapp.addmoreplots

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.pnpawd.userapp.R
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class ExistingTNCActivity : AppCompatActivity() {
    private lateinit var btnNext: Button
    private lateinit var txtCarbonLink: TextView
    private lateinit var checkCarbonCredit: CheckBox

    lateinit var tnc: TextView
    var whichTNC: Int = 0

    private var imageList = ArrayList<String>()
    private var unique_id: String = ""
    private var Pattadhar_number: String = ""
    private var khatian_number: String = ""
    private var survey_number: String = ""
    private var khatha_number: String = ""
    private var relationship: String = ""
    private var patta_number: String = ""
    private var daag_number: String = ""
    private var plot_number: String = ""
    private var owner_name: String = ""
    private var longitude: String = ""
    private var areaOther: String = ""
    private var areaAcres: String = ""
    private var latitude: String = ""
    private var farmerId: String = ""
    private var plot: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_existing_tncactivity)

        tnc = findViewById(R.id.tnc_exi)
        btnNext = findViewById(R.id.tncNext_exi)
        txtCarbonLink = findViewById(R.id.carbon_credit_link_exi)
        checkCarbonCredit = findViewById(R.id.carbon_credit_checkBox_exi)

        /**
         * Getting some data's from previous screen.
         */
        val bundle = intent.extras
        if (bundle != null) {
            whichTNC = bundle.getInt("tnc")
            plot = bundle.getString("plot")!!
            relationship = bundle.getString("relationship")!!
            plot_number = bundle.getString("plot_number")!!
            imageList = bundle.getStringArrayList("imageList")!!
            survey_number = bundle.getString("survey_number")!!
            unique_id = bundle.getString("unique_id")!!
            owner_name = bundle.getString("owner_name")!!
            farmerId = bundle.getString("FarmerId")!!
            latitude = bundle.getString("latitude")!!
            longitude = bundle.getString("longitude")!!

            areaOther = bundle.getString("area_other_awd")!!
            areaAcres = bundle.getString("area_acre_awd")!!
            patta_number = bundle.getString("patta_number")!!
            daag_number = bundle.getString("daag_number")!!
            khatha_number = bundle.getString("khatha_number")!!
            Pattadhar_number = bundle.getString("pattadhar_number")!!
            khatian_number = bundle.getString("khatian_number")!!



            Log.e("plot_number", plot_number)
        } else {
            Log.e("total_plot", "Nope")
        }

        /**
         * Checking if terms & condition is accepted or not.
         */
        checkCarbonCredit.setOnClickListener{
            var checked = checkCarbonCredit.isChecked
            if(checked) {
                Log.e("selected", checked.toString())
                btnNext.visibility = View.VISIBLE
            }
            else{
                Log.e("selected", checked.toString())
                btnNext.visibility = View.GONE
            }
        }


        /**
         * Going to the next screen with so data.
         */
        btnNext.setOnClickListener {
            val intent = Intent(this, ExistingSubmitActivity::class.java).apply {
                putStringArrayListExtra("imageList", imageList)
                putExtra("plot_number", plot_number)
                putExtra("plot", plot)
                putExtra("relationship", relationship)
                putExtra("owner_name", owner_name)
                putExtra("unique_id", unique_id)
                putExtra("survey_number", survey_number)
                putExtra("FarmerId", farmerId)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("area_other_awd", areaOther)
                putExtra("area_acre_awd", areaAcres)
                putExtra("patta_number", patta_number)
                putExtra("daag_number", daag_number)
                putExtra("khatha_number", khatha_number)
                putExtra("pattadhar_number", Pattadhar_number)
                putExtra("khatian_number", khatian_number)

            }
            startActivity(intent)
        }

        getCarbonTNC()
    }

    /**
     * Getting the tnc from API.
     */
    private fun getCarbonTNC() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.tnc().enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())

                    val carbon_credit = stringResponse.optString("carbon_credit")
                    Log.e("carbon_credit", carbon_credit)

                    tnc.text = HtmlCompat.fromHtml(carbon_credit, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }
        })
    }
}