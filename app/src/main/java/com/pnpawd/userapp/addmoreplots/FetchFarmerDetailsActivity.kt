package com.pnpawd.userapp.addmoreplots

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.pnpawd.userapp.R
import com.pnpawd.userapp.farmeronboarding.LocationActivity
import com.pnpawd.userapp.models.ExistingPlotsDetailsModel
import com.pnpawd.userapp.models.FarmerInfoModel
import com.pnpawd.userapp.models.FarmerInfoModelNew
import com.pnpawd.userapp.models.OrganizationModel
import com.pnpawd.userapp.models.PlotDetails
import com.pnpawd.userapp.models.PlotDetailsModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import kotlinx.android.synthetic.main.activity_fetch_farmer_details.guardian_name_et
import kotlinx.android.synthetic.main.activity_sign_in.login
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class FetchFarmerDetailsActivity : AppCompatActivity() {
    val access = arrayOf("--Select--", "Own Number", "Relatives Number")
    private lateinit var progress: SweetAlertDialog
    private  lateinit var nextBtn : Button
    private  lateinit var backBtn : Button
    private  lateinit var searchBtn : ImageView
    private  lateinit var farmerName : EditText
    private  lateinit var farmerNumber : EditText
    private  lateinit var searchText : EditText
    private lateinit var linearList: LinearLayout
    private lateinit var plotIdTv : TextView
    private lateinit var plotNum: EditText
    private lateinit var edtGuardian: EditText
    private lateinit var edtAadhar: EditText
    private lateinit var owner_spinner: Spinner

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    var realtionshipIDList = java.util.ArrayList<Int>()
    var relationshipNameList = java.util.ArrayList<String>()

    var bValue: Double = 0.0
    var state: String = ""
    var state_id: String = ""
    var unit: String = ""
    var userId: String = ""
    var areaValue: Double? = 0.0
    var maxBValue: Double? = 0.0
    var minBValue: Double? = 0.0
    var token: String = ""
    var existingPlots:Int = 0;

    var mobile_access: String = ""
    var relationship: String = ""

    var FarmerId: String = ""
    var FarmerUniqueID: String = ""

    var orgID : Int = 0;

    var i:Int = 1;
    var clear: Boolean = false
    var close:  Int = 0

   var editablePlot:Boolean = true;

    val common: Common = Common()

    var farmerDetails :ExistingPlotsDetailsModel? = null;

    // on below line we are creating
    // variables for grid view and course list
    lateinit var plotGRV: GridView
    lateinit var plotList: List<PlotDetails>
    var plotNumberList = ArrayList<String>()
    var plotAreaList = ArrayList<String>()
    var listOfPlots = ArrayList<String>()

    var lkjs: Int = 0


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_farmer_details)

        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!
        userId = sharedPreference.getString("user_id","")!!
        state_id =sharedPreference.getString("state_id","")!!

        ////// Initialization
        nextBtn = findViewById(R.id.fetch_details_Next)
        backBtn = findViewById(R.id.fetch_details_back)
        searchBtn = findViewById(R.id.fetch_search)
        farmerName = findViewById(R.id.fetch_farmer_name_et)
        farmerNumber = findViewById(R.id.fetch_farmer_number_et)
        searchText = findViewById(R.id.fetch_detail_et)
        linearList = findViewById(R.id.plot_layout_list)
        plotIdTv = findViewById(R.id.plot_id_tv)
        plotNum = findViewById(R.id.no_plot_et)
        edtGuardian = findViewById(R.id.guardian_name_et)
        edtAadhar = findViewById(R.id.aadhar_number_et)

        radioGroup = findViewById(R.id.gender_radioGroup)
        val access_spinner = findViewById<Spinner>(R.id.mobile_access_sp)
        owner_spinner = findViewById(R.id.relationship_sp)
        // initializing variables of grid view with their ids.

        plotGRV = findViewById(R.id.idGVPlots)
        plotList = ArrayList<PlotDetails>()

        progress = SweetAlertDialog(this@FetchFarmerDetailsActivity, SweetAlertDialog.PROGRESS_TYPE)

        /** Disable  edit texts */
        farmerName.isEnabled = false
        farmerNumber.isEnabled = false
        plotNum.isEnabled = false


        /**
         * Assign the adapter to mobile access spinner.
         * Also if index 2nd is selected we enable the Relation ship with owner spinner.
         */
        if (access_spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, access)
            access_spinner.adapter = adapter

            access_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    mobile_access = access[position]
                    Log.e("access", access[position])
                    owner_spinner.isEnabled = position == 2

                    if(position != 2){
                        owner_spinner.setSelection(0)
                    }
                    relationshipAPI()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        /** On Clicks Listeners */
        backBtn.setOnClickListener{
            this.finish()
        }

        searchBtn.setOnClickListener(View.OnClickListener {
            var searchTextEmpty = searchText.text.trim().isEmpty();
            if (!searchTextEmpty){
                searchForDetails(searchText.text.toString());
            }
        })

        plotNum.addTextChangedListener(object  : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.e("TEXD","beforeTextChanged $s");
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.e("TEXD","onTextChanged $s");
            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("TEXD","afterTextChanged $s");

                if(!s.isNullOrEmpty()){
                    var enteredNum = Integer.parseInt(s.toString())
                    if (editablePlot){
                        if(enteredNum > 0){
                            Log.e("TEXD","afterTextChanged $s");
                            plotNum.isEnabled = false;
                            Log.i("plot_number","plot_number $existingPlots")
                            addView(enteredNum)
                        }
                    }

                }
            }

        })


        nextBtn.setOnClickListener(View.OnClickListener {
            val WarningDialog =
                SweetAlertDialog(this@FetchFarmerDetailsActivity, SweetAlertDialog.WARNING_TYPE)

            if (farmerName.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (edtGuardian.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.guardian_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (access_spinner.selectedItemPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_access_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (access_spinner.selectedItemPosition == 2 && owner_spinner.selectedItemPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.relationship_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtAadhar.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.aadhar_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (farmerNumber.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (common.isValid(farmerNumber.text.toString()) == false) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (farmerNumber.text.length < 10) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else if (plotIdTv.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.missing_farmer_id_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else if (linearList.childCount == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.number_plot_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else {
                plotNumberList.clear()
                plotAreaList.clear()
                listOfPlots.clear()

                checkIfValidAndRead()
            }
        })

    }


    /**
     * Checking if all the data is present in the area of lands fields.
     */
    private fun checkIfValidAndRead() {
        lkjs = 0

        for (i in 0 until linearList.childCount) {
            val cricketerView: View = linearList.getChildAt(i)

            val editTextPlotName = cricketerView.findViewById<View>(R.id.assam_plot_number) as TextView
            val editTextLandArea = cricketerView.findViewById<View>(R.id.assam_area_bigha) as EditText
            val editTextHectareName = cricketerView.findViewById<View>(R.id.assam_area_hector) as EditText
            Log.e("PlotName", editTextPlotName.text.toString())
            Log.e("PlotName LandArea", editTextLandArea.text.toString())
            Log.e("PlotName LandAreaAcres", editTextHectareName.text.toString())
            if (editTextPlotName.text.toString() != "") {
                Log.e("PlotName", editTextPlotName.text.toString())
                plotNumberList.add(editTextPlotName.text.toString())
            } else {
                break
            }

            if (!editTextLandArea.text.toString().isNullOrEmpty()) {
                Log.e("LandArea", editTextLandArea.text.toString())
                listOfPlots.add(editTextLandArea.text.toString())
            }
            else {
                break
            }

            if (!editTextHectareName.text.toString().isNullOrEmpty()) {
                Log.e("LandAreaAcres", editTextHectareName.text.toString())
                plotAreaList.add(editTextHectareName.text.toString())
            }
            else {
                break
            }


        }


        first@ for (i in 0 until listOfPlots.size) {
            val plotDouble = listOfPlots[i].toDouble()
            if (plotDouble < minBValue!!) {
                Log.e("sdfkfjs", "fjksdfskdf")

                val WarningDialog = SweetAlertDialog(this@FetchFarmerDetailsActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "${resources.getString(R.string.area_less_warning)}  ${minBValue.toString()}"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    Log.e("Next Screen", lkjs.toString())
                    WarningDialog.cancel()
                }.show()

                lkjs = 1
                Log.e("Next Screen", lkjs.toString())
                break@first
            }
        }


        /**
         * If all data is present the sending the data through API.
         */
        Log.e("Next Screen !", lkjs.toString())
        Log.e("Next Screen !", linearList.childCount.toString())
        if (lkjs == 0 && listOfPlots.size == linearList.childCount){
            Log.e("insidesetdata", "fjksdfskdf")
            Log.e("plotList", plotList.size.toString())
            Log.e("plots", linearList.childCount.toString())
            Log.e("STATUS","Success")
            sendData()
        }
//        else{
//            plotNumberList.clear()
//            plotAreaList.clear()
//            plotList.clear()
//
//            val WarningDialog = SweetAlertDialog(this@AssamFarmerOnboardingActivity, SweetAlertDialog.WARNING_TYPE)
//
//            WarningDialog.titleText = resources.getString(R.string.warning)
//            WarningDialog.contentText = resources.getString(R.string.area_cannot_warning)
//            WarningDialog.confirmText = resources.getString(R.string.ok)
//            WarningDialog.setCancelClickListener {
//                Log.e("Next Screen", lkjs.toString())
//                WarningDialog.cancel()
//            }.show()
//        }
    }

    /**
     * Sending all the data through API
     */
    private fun sendData() {
        val intSelectButton: Int = radioGroup.checkedRadioButtonId
        radioButton = findViewById(intSelectButton)

        Log.e("Entered sendData", "Entered sendData $intSelectButton ${radioButton.text}")

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()

        var guardian = edtGuardian.text.toString().trim()
       var farmer_name = farmerName.text.toString().trim()
        var mobile = farmerNumber.text.toString().toLong()
        var aadhar = edtAadhar.text.toString().trim()
//        var mobileAccess = farmerDetails?.survey_id!!.mobile_access
//        var relationship = farmerDetails?.survey_id!!.mobile_reln_owner
        var txtUniqueID = farmerDetails?.survey_id!!.farmer_uniqueId
        var txtSurveyID = farmerDetails?.survey_id!!.farmer_survey_id
        var gender = radioButton.text.toString();
        var org = orgID

        val plot_details_arrayList = ArrayList<PlotDetailsModel>()
        var plot: String
        var number: String
        var otherNumber: String

        for (i in 0 until listOfPlots.size) {
            plot = plotAreaList[i]
            otherNumber = listOfPlots[i]
            number = plotNumberList[i]

            val plot_details = PlotDetailsModel(plot, number, otherNumber, unit)
            plot_details_arrayList.add(plot_details)
        }

        if(relationship == "--Select--"){
            relationship = "NA"
        }


        val farmerInfo = FarmerInfoModelNew(farmer_name,mobile_access ,relationship , mobile, txtUniqueID.toString(),txtSurveyID.toString(),
            plot_details_arrayList, gender, guardian, 1, edtAadhar.text.toString())

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.addExistingPlots("Bearer $token", farmerInfo).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.e("SENDDATA","${response.code()} ${ gender.toString()}")
                Log.e("SENDDATA","${response.body()}")


                if (response.code() == 200) {
                    val jsonObject = JSONObject(response.body()!!.string())
                    FarmerId = jsonObject.getString("FarmerId")
                    FarmerUniqueID = jsonObject.getString("FarmerUniqueID")
                    Log.e("SENDDATA","${response.code()}")
                    Log.e("SENDDATA","$jsonObject")

                    lkjs == 0
                    nextScreen()
                } else if (response.code() == 500) {
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("SENDDATA", it) }
                progress.dismiss()
            }
        })

    }

    /**
     * If sending data is successful the going to the next screen.
     */
    private fun nextScreen() {
       try {
           progress.dismiss()
//           existingPlots = plotList.size
           Log.i("plot_number","plot_number $existingPlots")
           val intent = Intent(this, ExistingLocationActivity::class.java).apply {
               putExtra("total_plot", listOfPlots.size)
               putExtra("unique_id", farmerDetails!!.survey_id!!.farmer_uniqueId.toString())
               putExtra("FarmerId", FarmerId)
               putExtra("area_unit", unit)
               putExtra("existing_plot", existingPlots+1)
               putExtra("area_value", areaValue)
               putStringArrayListExtra("areaAcres", plotAreaList)
               putStringArrayListExtra("areaHectare", listOfPlots)

               putExtra("state", state)
               putExtra("state_id", state_id)

               Log.e("state_id", state_id)
           }
           startActivity(intent)
       }catch (e:Exception){
           Log.e("SENDDATA","THIS is Error $e")
       }
    }

    /**
     * Dynamically adding the view to add the areas of the plots.
     */
    private fun addView(position: Int) {
        if (position != 0) {

            close = 1
            val plotView = layoutInflater.inflate(R.layout.row_add_unique_assam, null, false)
            var unitNmae = plotView.findViewById<View>(R.id.area_unit_name) as TextView
            var plotName = plotView.findViewById<View>(R.id.assam_plot_number) as TextView
            var areaBigha = plotView.findViewById<View>(R.id.assam_area_bigha) as EditText
            var areaHectare = plotView.findViewById<View>(R.id.assam_area_hector) as EditText
            var closeButton = plotView.findViewById<View>(R.id.assam_image_close) as ImageView


            /**
             * Adding the area with some validation.
             */

            areaBigha.filters = arrayOf(
                com.pnpawd.userapp.utils.DecimalDigitsInputFilter(
                    3,
                    2,
                    maxBValue!!
                )
            )

            /**
             * Adding a listener to the area of land for calculation & filling the data in area in acres.
             */
            areaBigha.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    areaHectare.setText(acresCalculation(areaBigha.text.toString()))
                }
            })

            Log.i("plot_number","plot_number in add $existingPlots")
            Log.i("plot_number","plot_number in add $i")
            var j: Int = i++
            plotName.text = j.toString()
            unitNmae.text = unit

            /**
             * Will close the dynamically added view.
             */
//            closeButton.setOnClickListener(View.OnClickListener {
//                try {
//
//                    Log.e("childCount_", linearList.childCount.toString())
//                    Log.e("plotName", j.toString())
//
//                    if( linearList.childCount != j){
//                        Log.e("childCount_if", linearList.childCount.toString())
//
//                    }
//                    else{
//                        try{
//                        Log.e("childCount_else", linearList.childCount.toString())
//
//                        var count: Int = linearList.childCount - 1
//                        plotNum.setText(count.toString())
//
//                        if (count == 0) {
//                            Log.e("count_if", count.toString())
//                            clear = true
//                            close = 0
//                            plotNum.isEnabled = true
//
//                            removeView(plotView)
//                        } else {
//                            Log.e("count_else", count.toString())
//                            removeView(plotView)
//                        }
//                        i = 1
//                    }catch (e:Exception){
//                        Log.e("EXX","ERRO $e")
//                    }
//                    }
//
//                }
//                catch (_: java.lang.Exception){
//                }
//            })


            closeButton.setOnClickListener(View.OnClickListener {
                try {
                    Log.e("childCount_", linearList.childCount.toString())
                    Log.e("plotName", j.toString())
                    removeView(plotView)
                    var count = linearList.childCount
                    if (linearList.childCount >0){
                        editablePlot = false;
                    }else{
                        i = existingPlots +1
                        plotNum.isEnabled = true
                        editablePlot = true
                    }
                    plotNum.setText(linearList.childCount.toString())
                }catch (e:Exception){
                    Log.e("EXX","ERRO $e")
                }
            })


            linearList.addView(plotView)
            addView(position - 1)
        }
    }


    /**
     * Clicking of x icon to remove the view is here.
     */
    private fun removeView(view: View) {
        try {
            linearList.removeView(view)
        }catch (e:Exception){
            Log.e("EXX","ERROR $e")
        }
    }

    /**
     * Area in acres calculation is don her.
     */
    private fun acresCalculation(acres: String): String {
        return if(acres.isNotEmpty()){
            val value = acres.toDouble()
            val calculated = (value * areaValue!!).toString()
            Log.e("Area Bigha", calculated)
            calculated
        } else{
            "0.0"
        }
    }
    ////// Function to search for details

    private fun searchForDetails(text:String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
//        progress.contentText = resources.getString(R.string.loading)
        progress.setCancelable(false)
        progress.show()

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        retIn.getExistingPlotDetails("Bearer $token", text).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.e("statusCode", response.code().toString() + "  $token")
                Log.e("statusCode", response.body().toString())
                if (response.code() == 200) {
                    if (response.body() != null) {
                        //// parsing JSON
                        val jsonResponse = try {
                            response.body()?.string()?.let { JSONObject(it) }
                        } catch (e: JSONException) {
                            Log.e("statusCode", "Error parsing JSON: ${e.message}")
                            null // Handle the case when JSON parsing fails
                        }

                        ////
                        var surveyId = jsonResponse?.optJSONObject("survey_id");
                        Log.e("statusCode", "Data $jsonResponse")
                        Log.e("statusCode", "Data ${jsonResponse?.optJSONObject("survey_id")}")
                        Log.e("statusCode", "Data ${jsonResponse?.optJSONArray("plots")}")
                        progress.dismiss()
                        /// Check if Survey is null
                        if(surveyId == null){
                           //// Show waring when no Data found
                            showWaringDialog("No Data Found","Ok");
                        }else{
                            var surveyResponse : ExistingPlotsDetailsModel;
                            val gson = Gson()
                            try {
                                surveyResponse = gson.fromJson(jsonResponse.toString(), ExistingPlotsDetailsModel::class.java)
                                Log.e("statusCode", "Saving response to model ${surveyResponse.survey_id?.farmer_name.toString()}")
                            }catch (e :Exception){
                                Log.e("statusCode", "ERROR $e")
                                return
                            }

                            try {
                                plotList= surveyResponse.plots;
                                farmerDetails = surveyResponse;
                            }catch (e: Exception){
                                Log.i("ASSIGN","$e")
                            }

                            val courseAdapter = GridRVAdapter(courseList = plotList, this@FetchFarmerDetailsActivity)
                            plotGRV.adapter = courseAdapter
                            var name = surveyResponse.survey_id?.farmer_name;
                            var num = surveyResponse.survey_id?.mobile;
                            farmerName.setText(name.toString())
                            farmerNumber.setText(num.toString())
                            plotIdTv.text = surveyResponse.survey_id?.farmer_uniqueId.toString()

                            baseValue(surveyResponse.survey_id?.state_id);
                            plotNum.isEnabled = true;
                            plotGRV.visibility = View.VISIBLE

//                            Check If Any Data is Null and if its null let user fill it again
                            if (farmerDetails?.survey_id?.gender.isNullOrEmpty()){
                                findViewById<LinearLayout>(R.id.gender_ll).visibility = View.VISIBLE
                            }else{
                                Log.e("Check Gender"," C ${farmerDetails?.survey_id?.gender}")
                                when(farmerDetails?.survey_id?.gender.toString()){
                                    "MALE" -> radioGroup.check(R.id.gender_radioMale)
                                    "FEMALE" ->radioGroup.check(R.id.gender_radioFemale)
                                    else -> radioGroup.check(R.id.gender_radioOther)
                                }

                            }
                            Log.e("Last PLOT"," C ${farmerDetails?.latest_plot}")
                            try {
                                if (farmerDetails?.latest_plot != null){
                                    existingPlots = farmerDetails!!.latest_plot!!.toInt()
                                }
                                i = existingPlots +1
                            }catch (e:Exception){
                                Log.e("Last PLOT"," ${farmerDetails?.latest_plot} $e")
                            }


                            if (farmerDetails?.survey_id?.guardian_name.isNullOrEmpty()){
                                findViewById<LinearLayout>(R.id.guardian_linearlayout).visibility = View.VISIBLE
                            }else{
                                edtGuardian.setText(farmerDetails?.survey_id?.guardian_name.toString())
                            }

                            if (farmerDetails?.survey_id?.aadhaar.isNullOrEmpty()){
                                findViewById<LinearLayout>(R.id.aadhar_ll).visibility = View.VISIBLE
                            }else{
                                edtAadhar.setText(farmerDetails?.survey_id?.aadhaar.toString())
                            }






                        }

                    }
                    else {

                    }
                }
                else{
                    progress.dismiss()
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FetchFarmerDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })

    }


    /**
     * Getting the base value, min, max, unit value from API.
     */
    private fun baseValue(stateId: Int?) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.baseValue("Bearer $token", stateId.toString()).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val value = stringResponse.getJSONObject("value")

                        val stateResponse = stringResponse.getJSONObject("state")

                        bValue = value.optDouble("value")
                        state = stateResponse.getString("name")
                        unit = stateResponse.optString("units")
                        areaValue = stateResponse.getString("base_value").toDouble()
                        minBValue = stateResponse.getString("min_base_value").toDouble()
                        maxBValue = stateResponse.getString("max_base_value").toDouble()
                        orgAPI()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FetchFarmerDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * Getting the organization data from API.
     */
    private fun orgAPI() {
        val organizationModel = OrganizationModel(userId.toInt())

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.org(organizationModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("list")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("company")

                        orgID = id
                        relationshipAPI()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FetchFarmerDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Getting the relationship data from API.
     */
    private fun relationshipAPI() {
        relationshipNameList.clear()
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.relationship("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("relationshipowner")

                    relationshipNameList.add("--Select--")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("name")

                        realtionshipIDList.add(id)
                        relationshipNameList.add(name)
                    }

                    relationshipSpinner()
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@FetchFarmerDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

//
private fun relationshipSpinner() {
    if (owner_spinner != null) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, relationshipNameList)
        owner_spinner.adapter = adapter

        owner_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                relationship = relationshipNameList[position]
                Log.e("owner_spinner", relationshipNameList[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

   fun showWaringDialog(contentTxt :String, confirmTxt :String){
       val WarningDialog = SweetAlertDialog(this@FetchFarmerDetailsActivity, SweetAlertDialog.WARNING_TYPE)

       WarningDialog.titleText = resources.getString(R.string.warning)
       WarningDialog.contentText = contentTxt
       WarningDialog.confirmText = confirmTxt
       WarningDialog.showCancelButton(false)
       WarningDialog.setCancelable(false)
       WarningDialog.setConfirmClickListener {
           WarningDialog.cancel()
       }.show()
    }
}