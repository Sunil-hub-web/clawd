package com.pnpawd.userapp.cropdata

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.CropInfoModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import kotlinx.android.synthetic.main.activity_crop.current_year_nitrogen
import kotlinx.android.synthetic.main.activity_crop.current_year_phosphorous
import kotlinx.android.synthetic.main.activity_crop.current_year_potassium
//import kotlinx.android.synthetic.main.activity_crop.current_year_water_management
//import kotlinx.android.synthetic.main.activity_crop.current_year_yield
import kotlinx.android.synthetic.main.activity_crop.last_year_nitrogen
import kotlinx.android.synthetic.main.activity_crop.last_year_phosphorous
import kotlinx.android.synthetic.main.activity_crop.last_year_potassium
//import kotlinx.android.synthetic.main.activity_crop.last_year_water_management
import kotlinx.android.synthetic.main.activity_crop.last_year_yield
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


/**
 * This activity will be called multiple time.
 * Because the sub-plots will be filled one after the other.
 * So this activity is in a kind of loop till the user does not fill all the sub-plots data.
 */

class CropActivity : AppCompatActivity() {
    private lateinit var progress: SweetAlertDialog

    private var filled: Boolean = false
    private var lastData: Boolean = false

    private var farmer_id: String = ""
    private var state_id: String = ""
    private var onboardingDate: String = ""
    private var state: String = ""
    var token: String = ""
    var UNIQURID: String = ""


    var org: String = ""
    private var total_number: Int = 0
    private var totalSubPlots: Int = 0
    private var subPlotPosition: Int = 0
    private var farmerUniquePosition: Int = 0
    private var seasonLastPosition: Int = 0
    private var seasonCurrentPosition: Int = 0

    private var season_day: Int= 0
    private var season_year: Int= 0
    private var end_of_date: Int = 0
    private var season_month: Int= 0
    private var transplant_day: Int= 0
    private var transplant_year: Int= 0
    private var preparation_day: Int= 0
    private var transplant_month: Int= 0
    private var preparation_date_interval: Int = 0
    private var transplantation_date_interval: Int = 0
    private var preparation_year: Int = 0
    private var preparation_month: Int = 0
    private var water_current_day: Int = 0
    private var water_current_year: Int = 0
    private var water_current_month: Int = 0
    var areaValue: Double? = 0.0
    var maxBValue: Double = 0.0

    var IDList = ArrayList<Int>()
    var FarmerUniqueList = ArrayList<String>()
    var PlotIdList = ArrayList<String>()
    var PlotNoList = ArrayList<String>()
    var AcresAreaList = ArrayList<String>()
    var AWDPlot = ArrayList<String>()
    var AWDAcres = ArrayList<String>()
    var PlotAreaList = ArrayList<String>()
    var FarmerPlotUniqueID = ArrayList<String>()
    var SeasonIDList = ArrayList<Int>()
    var SeasonNameList = ArrayList<String>()
    var update = ArrayList<String>()


    lateinit var txtArea: TextView
    lateinit var nursery_date: TextView
    lateinit var preparation_date: TextView
    lateinit var transplanting_date: TextView
    lateinit var edtSub_plot: TextView
    lateinit var edtPlotArea: EditText
    lateinit var edtFarmer_name: TextView
    lateinit var txtunique_id: TextView
    lateinit var edtMobile_nmuber: EditText
    lateinit var search: ImageView

    private lateinit var txtAreaChooseText: TextView
    private lateinit var txtFertilizer: TextView
    private lateinit var txtNitrogen: TextView
    private lateinit var txtPhosphorous: TextView
    private lateinit var txtPotassium: TextView
    private lateinit var txtYield: TextView


    lateinit var first: LinearLayout
    lateinit var second: LinearLayout
    lateinit var mobile_number_layout: LinearLayout
    lateinit var first_unique_view: LinearLayout
    lateinit var second_unique_view: LinearLayout

    lateinit var plot_ID: Spinner
    private lateinit var season_last_year_spinner: Spinner
    private lateinit var season_current_year_spinner: Spinner
    private lateinit var txtVariety_last_year: EditText
    private lateinit var txtVariety_current_year: EditText

    private lateinit var txtSeason_last: TextView
    private lateinit var txtSeason_current: TextView
    private lateinit var txtVariety_last: TextView
    private lateinit var txtVariety_current: TextView
    private lateinit var nitrogen_last_year: EditText
    private lateinit var nitrogen_current_year: EditText
    private lateinit var phosphorous_last_year: EditText
    private lateinit var phosphorous_current_year: EditText
    private lateinit var potassium_last_year: EditText
    private lateinit var potassium_current_year: EditText
    private lateinit var awd_plot_area: EditText
    private lateinit var awd_area_acers: TextView

    private lateinit var yield_last_year: EditText

    private lateinit var aadharNumber: EditText
    private lateinit var mobileNumber: EditText
    private lateinit var pattaNumber: EditText
    private lateinit var daagNumber: EditText
    private lateinit var khathaNumber: EditText
    private lateinit var patthadharNumber: EditText
    private lateinit var khatianNumber: EditText

    private lateinit var incompleteData: LinearLayout
    private lateinit var assamLayout: LinearLayout
    private lateinit var telanganaLayout: LinearLayout
    private lateinit var bengalLayout: LinearLayout

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    lateinit var next: Button
    lateinit var back: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!
        org =  sharedPreference.getString("company_id","")!!

        progress = SweetAlertDialog(this@CropActivity, SweetAlertDialog.PROGRESS_TYPE)

        nursery_date = findViewById(R.id.date_irrigation)
        preparation_date = findViewById(R.id.preparation_date)
        transplanting_date = findViewById(R.id.transplanting_date)
        edtSub_plot = findViewById(R.id.crop_sub_plots)
        edtPlotArea = findViewById(R.id.crop_plot_area)
        edtFarmer_name = findViewById(R.id.crop_farmer_name)
        txtunique_id = findViewById(R.id.txt_unique_id)

        edtMobile_nmuber = findViewById(R.id.crop_mobile)
        plot_ID = findViewById(R.id.crop_plot_unique_id)

        txtArea = findViewById(R.id.auto_area)
        txtSeason_last = findViewById(R.id.first_season_last)
        txtSeason_current = findViewById(R.id.first_season_current)
        txtVariety_last = findViewById(R.id.first_variety_last)
        txtVariety_current = findViewById(R.id.first_variety_current)
        season_last_year_spinner = findViewById(R.id.season_last_year)
        season_current_year_spinner = findViewById(R.id.current_year_crop)
        txtVariety_last_year = findViewById(R.id.variety_last_year)
        txtVariety_current_year = findViewById(R.id.variety_current_year)
        nitrogen_last_year = findViewById(R.id.last_year_nitrogen)
        nitrogen_current_year = findViewById(R.id.current_year_nitrogen)
        phosphorous_last_year = findViewById(R.id.last_year_phosphorous)
        phosphorous_current_year = findViewById(R.id.current_year_phosphorous)
        potassium_last_year = findViewById(R.id.last_year_potassium)
        potassium_current_year = findViewById(R.id.current_year_potassium)
        yield_last_year = findViewById(R.id.last_year_yield)

        next = findViewById(R.id.crop_submit)
        back = findViewById(R.id.crop_back)

        first = findViewById(R.id.first_layout)
        second = findViewById(R.id.second_layout)
        mobile_number_layout = findViewById(R.id.mobile_number_layout)
        first_unique_view = findViewById(R.id.first_unique_view)
        second_unique_view = findViewById(R.id.second_unique_view)
        search = findViewById(R.id.crop_search)


        awd_plot_area = findViewById(R.id.crop_plot_area_awd)
        awd_area_acers = findViewById(R.id.auto_area_awd)
        txtAreaChooseText = findViewById(R.id.plot_area_of_crop)
        txtFertilizer = findViewById(R.id.fetilizer_txt)
        txtNitrogen = findViewById(R.id.nitrogen_txt)
        txtPhosphorous = findViewById(R.id.phosphorous_txt)
        txtPotassium = findViewById(R.id.potassium_txt)
        txtYield = findViewById(R.id.yield_txt)

        incompleteData = findViewById(R.id.incomplete_data)
        aadharNumber = findViewById(R.id.incomplete_aadhar_number)
        mobileNumber = findViewById(R.id.incomplete_mobile)
        pattaNumber = findViewById(R.id.incomplete_patta_number)
        daagNumber = findViewById(R.id.incomplete_daag_number)
        khathaNumber = findViewById(R.id.incomplete_khatha_number)
        patthadharNumber = findViewById(R.id.incomplete_pattadhar_number)
        khatianNumber = findViewById(R.id.incomplete_khatian_number)

        assamLayout = findViewById(R.id.incomplete_assam_linear)
        telanganaLayout = findViewById(R.id.incomplete_telangana_linear)
        bengalLayout = findViewById(R.id.incomplete_bengal_linear)

        radioGroup = findViewById(R.id.incomplete_radioGroup)

//        assignText("Ha")



        /**
         * Getting some data's from previous screen.
         */
        val bundle = intent.extras
        if (bundle != null){
            preparation_date_interval = bundle.getInt("preparation_date_interval")
            transplantation_date_interval = bundle.getInt("transplantation_date_interval")
            transplant_day = bundle.getInt("transplantation_day")
            transplant_month = bundle.getInt("transplantation_month")
            transplant_year = bundle.getInt("transplantation_year")
            totalSubPlots = bundle.getInt("total_sub_plots")
            total_number = bundle.getInt("total_number")
            edtSub_plot.text = (total_number + 1).toString()

            edtFarmer_name.text = bundle.getString("farmer_name")
            edtMobile_nmuber.setText(bundle.getString("mobile_number"))
            farmer_id = bundle.getString("unique_id").toString()
            end_of_date = bundle.getInt("cropdata_end_days")
        }
        else{
            Log.e("preparation_date_interval", "Bundle issue")
        }


        if (bundle != null) {
            PlotAreaList = bundle.getStringArrayList("plot_area")!!
            if (PlotAreaList.size != 0) {
                Log.e("PlotAreaList", PlotAreaList[total_number])
                edtPlotArea.setText(PlotAreaList[total_number])

                first_unique_view.visibility = View.GONE
                second_unique_view.visibility = View.VISIBLE
                transplant_day = bundle.getInt("transplantation_day")
                transplant_month = bundle.getInt("transplantation_month")
                transplant_year = bundle.getInt("transplantation_year")
                txtunique_id.text = bundle.getString("unique_list_id")
                PlotNoList = bundle.getStringArrayList("plot_no")!!
                AcresAreaList = bundle.getStringArrayList("area_acres")!!

                AWDPlot = bundle.getStringArrayList("awd_plot_area")!!
                AWDAcres = bundle.getStringArrayList("awd_acres_area")!!
                update = bundle.getStringArrayList("update")!!

                FarmerPlotUniqueID = bundle.getStringArrayList("farmer_plot_uniqueid")!!
                PlotIdList = bundle.getStringArrayList("plot_id")!!
                UNIQURID = txtunique_id.text.toString()

                edtSub_plot.text = PlotNoList[total_number]
                txtArea.text = AcresAreaList[total_number]
                awd_plot_area.setText(AWDPlot[total_number])
                awd_area_acers.text = AWDAcres[total_number]

                getFarmerDetails(PlotIdList[total_number], FarmerPlotUniqueID[total_number], PlotNoList[total_number])
            }

            state = bundle.getString("state").toString()
            state_id = bundle.getString("state_id").toString()
            Log.e("total_number", total_number.toString())
        }


        Log.e("transplant_day", transplant_day.toString())
        Log.e("transplant_month", transplant_month.toString())
        Log.e("transplant_year", transplant_year.toString())


        if (total_number > 0){
            Log.e("total_number", total_number.toString())
            Log.e("totalSubPlots", totalSubPlots.toString())
            mobile_number_layout.isEnabled = false
            edtMobile_nmuber.isEnabled = false

            plot_ID.isEnabled = false

            if(total_number + 1 == totalSubPlots){
                Log.e("total_number", total_number.toString())
                Log.e("totalSubPlots", totalSubPlots.toString())

                lastData = true
                next.text = "Submit"
            }
        }


        /**
         * Search function is here.
         * We search for data from here.
         */
        search.setOnClickListener {
            if (edtMobile_nmuber.text.isEmpty()){

            }
            else{
                getPlotUniqueId(edtMobile_nmuber.text.toString())
            }
        }


        awd_plot_area.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {

                if(awd_plot_area.text.toString() != ""){
                    var status = checkValue()
                    if(status){
                        awd_area_acers.text = acresCalculation(awd_plot_area.text.toString())
                    }
                    else{
                        showWarning()
                    }
                }
                else{
                    val WarningDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.calculate_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                }
            }
        })



        edtPlotArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                txtArea.text = acresCalculation(edtPlotArea.text.toString())
            }
        })



        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })

        /**
         * Initializing season calendar here
         */
        val seasonCalendar = Calendar.getInstance()
        seasonCalendar.add(Calendar.DAY_OF_MONTH, -1)


        /**
         * Initializing transplantation calendar here
         */
        val preparationCalendar = Calendar.getInstance()
        preparationCalendar.add(preparation_day,-1)


        /**
         * Date of nursery selection code is here.
         */
        nursery_date.setOnClickListener {
            val year = seasonCalendar[Calendar.YEAR]
            val month = seasonCalendar[Calendar.MONTH]
            val day = seasonCalendar[Calendar.DAY_OF_MONTH]

            val mDatePicker = DatePickerDialog(this@CropActivity,
                { view, yearOfYear, monthOfYear, dayOfMonth ->
// Display Selected date in textbox
                    nursery_date.text = StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(yearOfYear)
                    season_day = dayOfMonth
                    season_month = monthOfYear
                    season_year = yearOfYear
                }, year, month, day
            )

            mDatePicker.setTitle("Please select nursery date")
            mDatePicker.show()
        }


        /**
         * Date of land preparation selection code here
         */
        preparation_date.setOnClickListener(View.OnClickListener {
            val year = seasonCalendar[Calendar.YEAR]
            val month = seasonCalendar[Calendar.MONTH]
            val day = seasonCalendar[Calendar.DAY_OF_MONTH]

                val mDatePicker = DatePickerDialog(this@CropActivity,
                    { view, yearOfYear, monthOfYear, day ->
                        preparation_date.text = StringBuilder().append(day).append("/").append(monthOfYear + 1).append("/").append(yearOfYear)
                        preparation_day = day
                        preparation_month = monthOfYear
                        preparation_year = yearOfYear
                    }, year, month, day
                )

                mDatePicker.setTitle("Please select preparation date")
                mDatePicker.show()
        })


        /**
         * Date of transplantation selection code here
         */
        transplanting_date.setOnClickListener {
            if(preparation_date.text.isEmpty() && nursery_date.text.isEmpty()){

            }
            else{
                val year = transplant_year
                val month = transplant_month
                val day = transplant_day

                val mDatePicker = DatePickerDialog(
                    this@CropActivity,
                    { view, yearOfYear, monthOfYear, dayOfMonth ->
                        transplanting_date.text = StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(yearOfYear)
                        water_current_day = dayOfMonth
                        water_current_month = monthOfYear
                        water_current_year = yearOfYear

                    }, year, month, day
                )
                val calendar = Calendar.getInstance()
                calendar.set(transplant_year, transplant_month, transplant_day)

                mDatePicker.setTitle("Please select transplantation date")
                mDatePicker.datePicker.minDate = calendar.timeInMillis
                mDatePicker.show()
            }

            Log.e("farmerUniquePosition", farmerUniquePosition.toString())


//            if(farmerUniquePosition !=0){
//                val year = transplant_year
//                val month = transplant_month
//                val day = transplant_day
//
//                val mDatePicker = DatePickerDialog(
//                    this@CropActivity,
//                    { view, yearOfYear, monthOfYear, dayOfMonth ->
//                        transplanting_date.text = StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(yearOfYear)
//                        water_current_day = dayOfMonth
//                        water_current_month = monthOfYear
//                        water_current_year = yearOfYear
//
//                    }, year, month, day
//                )
//                val calendar = Calendar.getInstance()
//                calendar.set(transplant_year, transplant_month, transplant_day)
//
//                mDatePicker.setTitle("Please select date")
//                mDatePicker.datePicker.minDate = calendar.timeInMillis
//                mDatePicker.show()
//            }
//            else {
//                val year = seasonCalendar[Calendar.YEAR]
//                val month = seasonCalendar[Calendar.MONTH]
//                val day = seasonCalendar[Calendar.DAY_OF_MONTH]
//
//                val mDatePicker = DatePickerDialog(
//                    this@CropActivity,
//                    { view, yearOfYear, monthOfYear, dayOfMonth ->
//                        transplanting_date.text = StringBuilder().append(dayOfMonth).append("/").append(monthOfYear - 1).append("/").append(yearOfYear)
//                        water_current_day = dayOfMonth
//                        water_current_month = monthOfYear
//                        water_current_year = yearOfYear
//
//                    }, year, month, day
//                )
//
//                mDatePicker.setTitle("Please select date")
//                mDatePicker.datePicker.minDate = seasonCalendar.timeInMillis
//                mDatePicker.show()
//            }

        }


        /**
         * Sending data to the next screen.
         * i.e if there are more than 1 sub-plot then this screen will be called again.
         */
        next.setOnClickListener {
            if (filled) {
                if (lastData) {
                    homeScreen()
                } else {
                    val intent = Intent(this, CropActivity::class.java).apply {
                        putExtra("total_sub_plots", totalSubPlots)
                        putExtra("total_number", total_number + 1)
                        putExtra("farmer_name", edtFarmer_name.text.toString())
                        putExtra("mobile_number", edtMobile_nmuber.text.toString())
                        putStringArrayListExtra("plot_area", PlotAreaList)
                        putStringArrayListExtra("farmer_plot_uniqueid", FarmerPlotUniqueID)
                        putStringArrayListExtra("plot_no", PlotNoList)
                        putStringArrayListExtra("area_acres", AcresAreaList)
                        putStringArrayListExtra("plot_id", PlotIdList)
                        putStringArrayListExtra("awd_plot_area", AWDPlot)
                        putStringArrayListExtra("awd_acres_area", AWDAcres)
                        putStringArrayListExtra("update", update)
                        putExtra("unique_list_id", UNIQURID)
                        putExtra("state", state)
                        putExtra("state_id", state_id)
                        putExtra("unique_id", farmer_id)
                        putExtra("cropdata_end_days", end_of_date)
                        putExtra("preparation_date_interval", preparation_date_interval)
                        putExtra("transplantation_date_interval", transplantation_date_interval)
                        putExtra("transplantation_day", transplant_day)
                        putExtra("transplantation_month", transplant_month)
                        putExtra("transplantation_year", transplant_year)
                    }
                    startActivity(intent)
                }
            } else {

                /**
                 * Checking if all data is filled or not
                 * If not then showing error msg.
                 */

                val WarningDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.WARNING_TYPE)

                if (txtArea.text == "0.0") {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.area_in_acres_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (awd_area_acers.text == "0.0") {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.area_in_acres_awd_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (nursery_date.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.nursery_date_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (transplanting_date.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.ploughing_date_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (preparation_date.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.transplanting_date_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (current_year_nitrogen.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.nitrogen_current_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (last_year_nitrogen.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.nitrogen_last_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (last_year_phosphorous.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.phosphorous_last_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (current_year_phosphorous.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.phosphorous_current_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (last_year_potassium.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.potassium_last_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (current_year_potassium.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.potassium_current_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (last_year_yield.text.isEmpty()) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.yield_last_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else {
                    progress.progressHelper.barColor = Color.parseColor("#06c238")
                    progress.titleText = resources.getString(R.string.loading)
                    progress.contentText = resources.getString(R.string.data_send)
                    progress.setCancelable(false)
                    progress.show()


                    /**
                     * Calling submit fun to submit the data through API.
                     */
                    submitData()
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun assignText(value: String) {
        val fertilizer = getString(R.string.fertilizer_management)
        val y = getString(R.string.yield)

        txtFertilizer.text = "$fertilizer (Kg/ $value)"
        txtYield.text = "$y (Kg/ $value)"
    }


    /**
     * Submit fun to submit the data.
     * Also condition are present to check if there are more data.
     */
    private fun submitData() {
        val farmer_uniqueId = UNIQURID
        val plot_no = edtSub_plot.text.toString()
        val nursery = nursery_date.text.toString()
        val dt_transplanting= transplanting_date.text.toString()
        val dt_ploughing = preparation_date.text.toString()
        val nitrogen_last = nitrogen_last_year.text.toString()
        val nitrogen_current = nitrogen_current_year.text.toString()
        val phosphorous_last = phosphorous_last_year.text.toString()
        val phosphorous_current = phosphorous_current_year.text.toString()
        val potassium_last = potassium_last_year.text.toString()
        val potassium_current = potassium_current_year.text.toString()
        val yield_last = yield_last_year.text.toString()
        val season_last = SeasonNameList[seasonLastPosition]
        val season_Current = SeasonNameList[seasonCurrentPosition]
        val variety_last = txtVariety_last_year.text.toString()
        val variety_current = txtVariety_current_year.text.toString()
        val farmer_plot_uniqueid = FarmerPlotUniqueID[total_number]

        val area_in_acers = txtArea.text.toString()
        val area_in_other = edtPlotArea.text.toString()
        val area_acre_awd = awd_area_acers.text.toString()
        val area_other_awd = awd_plot_area.text.toString()
        val patta_number = pattaNumber.text.toString()
        val daag_number = daagNumber.text.toString()
        val khatha_number = khathaNumber.text.toString()
        val pattadhar_number = patthadharNumber.text.toString()
        val khatian_number = khatianNumber.text.toString()
        val organization_id = org

        val intSelectButton: Int = radioGroup.checkedRadioButtonId
        radioButton = findViewById(intSelectButton)
        val gender = radioButton.text.toString()

        val cropInfoModel = CropInfoModel(farmer_id, farmer_uniqueId, plot_no, nursery, dt_ploughing, dt_transplanting, area_in_acers, farmer_plot_uniqueid,
            season_last, season_Current, variety_last, variety_current, yield_last, "Nitrogen", nitrogen_last, nitrogen_current,
            "Phosphorous", phosphorous_last, phosphorous_current, "potassium", potassium_last, potassium_current,
            area_acre_awd, area_other_awd, patta_number, daag_number, khatha_number, pattadhar_number, khatian_number, organization_id, gender, area_in_other)

        Log.e("drop-data", cropInfoModel.toString())

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.sendCropData("Bearer $token", cropInfoModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    progress.dismiss()

                    if (lastData){
                        homeScreen()
                    }
                    else{
                        repeatScreen()
                    }
                }
                else if (response.code() == 422) {
                    progress.dismiss()

                    if (lastData){
                        homeScreen()
                    }
                    else{
                        repeatScreen()
                    }
                }
                else if (response.code() == 500) {
                    progress.dismiss()
                } else {
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }
        })
    }

    /**
     * If more sub-plots are present then this fun eill be called.
     * It will call same activity
     */
    private fun repeatScreen() {
        val SuccessDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = "Crop Details submitted successfully. "
        SuccessDialog.confirmText = " OK "
        SuccessDialog.showCancelButton(false)
        SuccessDialog.setCancelable(false)
        SuccessDialog.setConfirmClickListener { SuccessDialog.cancel()

            val intent = Intent(this, CropActivity::class.java).apply {
                putStringArrayListExtra("plot_area", PlotAreaList)
                putStringArrayListExtra("farmer_plot_uniqueid", FarmerPlotUniqueID)
                putStringArrayListExtra("plot_no", PlotNoList)
                putStringArrayListExtra("area_acres", AcresAreaList)
                putStringArrayListExtra("plot_id", PlotIdList)
                putStringArrayListExtra("awd_plot_area", AWDPlot)
                putStringArrayListExtra("awd_acres_area", AWDAcres)
                putStringArrayListExtra("update", update)
                putExtra("total_sub_plots", totalSubPlots)
                putExtra("total_number", total_number + 1)
                putExtra("farmer_name", edtFarmer_name.text.toString())
                putExtra("mobile_number", edtMobile_nmuber.text.toString())
                putExtra("unique_list_id", UNIQURID)
                putExtra("state", state)
                putExtra("state_id", state_id)
                putExtra("unique_id", farmer_id)
                putExtra("cropdata_end_days", end_of_date)
                putExtra("preparation_date_interval", preparation_date_interval)
                putExtra("transplantation_date_interval", transplantation_date_interval)
                putExtra("transplantation_day", transplant_day)
                putExtra("transplantation_month", transplant_month)
                putExtra("transplantation_year", transplant_year)
            }
            startActivity(intent)

        }.show()
    }

    /**
     * Once all the data is submit. Redirect to dashboard.
     */
    private fun homeScreen() {
        val SuccessDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.SUCCESS_TYPE)

        if(filled && lastData){
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            SuccessDialog.titleText = " Success "
            SuccessDialog.contentText = " Data submitted successfully. "
            SuccessDialog.confirmText = " OK "
            SuccessDialog.showCancelButton(false)
            SuccessDialog.setCancelable(false)
            SuccessDialog.setConfirmClickListener {
                SuccessDialog.cancel()

                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }.show()
        }
    }

    /**
     * After searching the number this fun will be called.
     * The func will give us the list of farmer unique ID.
     */
    private fun  getPlotUniqueId(mobile: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        IDList.clear()
        FarmerUniqueList.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plotUniqueId("Bearer $token", mobile).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        IDList.add(0)
                        FarmerUniqueList.add("--Select--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("list")

                        if (jsonArray != null) {
                            if (jsonArray.length() == 0) {
                                Log.e("length", jsonArray.length().toString())

                                IDList.clear()
                                FarmerUniqueList.clear()
                                PlotNoList.clear()
                                AcresAreaList.clear()
                                PlotIdList.clear()
                                AWDPlot.clear()
                                AWDAcres.clear()
                                update.clear()
                                PlotAreaList.clear()
                                SeasonIDList.clear()
                                SeasonNameList.clear()
                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.WARNING_TYPE)
                                WarningDialog.titleText = resources.getString(R.string.warning)
                                WarningDialog.contentText = "No data available for\n the given number"
                                WarningDialog.confirmText = " OK "
                                WarningDialog.showCancelButton(false)
                                WarningDialog.setCancelable(false)
                                WarningDialog.setConfirmClickListener {
                                    WarningDialog.cancel()
                                }.show()
                            } else if (jsonArray.length() > 0){
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val id = jsonObject.optString("id").toInt()
                                    val farmer_uniqueId = jsonObject.optString("farmer_uniqueId")

                                    IDList.add(id)
                                    FarmerUniqueList.add(farmer_uniqueId)
                                    preparation_date.text = ""
                                }

                                farmerUniqueIdSpinner()
                                progress.dismiss()
                            }
                        }
                    }
                    else {
                        progress.dismiss()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@CropActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


    private fun farmerUniqueIdSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerUniqueList)
        plot_ID.adapter = adapter
        plot_ID.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                farmerUniquePosition = position
                UNIQURID = FarmerUniqueList[farmerUniquePosition]

                getPlots()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Getting the details for the particulat plot selected.
     */
    private fun getPlots(){
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        PlotIdList.clear()
        PlotNoList.clear()
        AcresAreaList.clear()
        PlotAreaList.clear()
        FarmerPlotUniqueID.clear()
        AWDPlot.clear()
        AWDAcres.clear()
        update.clear()

        val plotUniqueIDName: String = FarmerUniqueList[farmerUniquePosition]

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.cropSubPlotId("Bearer $token", plotUniqueIDName).enqueue(object : Callback<ResponseBody> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.code() == 200) {
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("plotlist")

                    if (jsonArray != null) {
                        if (jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)

                                val plot_id = jsonObject.optInt("id")
                                val plot_no = jsonObject.optString("plot_no")
                                val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid")
                                val update_data = jsonObject.optString("update_data")


                                val jsonApproved = jsonObject.getJSONObject("apprv_farmer_plot")
                                val area_in_acers = jsonApproved.optString("area_in_other")
                                val area_in_other = jsonApproved.optString("area_in_acers")
                                val area_acre_awd = jsonApproved.optString("area_other_awd")
                                val area_other_awd = jsonApproved.optString("area_acre_awd")


                                PlotIdList.add(plot_id.toString())
                                PlotNoList.add(plot_no)
                                PlotAreaList.add(area_in_acers)
                                AcresAreaList.add(area_in_other)
                                AWDPlot.add(area_acre_awd)
                                AWDAcres.add(area_other_awd)
                                update.add(update_data)

                                FarmerPlotUniqueID.add(farmer_plot_uniqueid)
                        }

                            progress.dismiss()
                            edtSub_plot.text = PlotNoList[total_number]
                            getPlotDetails()
                        }
                    }

                    Log.e("PlotNoList_size", PlotNoList.size.toString())

                    if(PlotNoList.size <= 1){
                        lastData = true
                        next.text = "Submit"
                    }
                    else{
                        lastData = false
                        next.text = "Next"
                    }

                    progress.dismiss()
                }
                else{
                    Log.e("statusCode", response.code().toString())
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@CropActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


    private fun getPlotDetails() {
        totalSubPlots = PlotNoList.size
        txtArea.text = AcresAreaList[total_number]
        edtSub_plot.text = PlotNoList[subPlotPosition]
        edtPlotArea.setText(PlotAreaList[subPlotPosition])
        awd_plot_area.setText(AWDPlot[subPlotPosition])
        awd_area_acers.text = AWDAcres[subPlotPosition]


        val plotID: String = PlotIdList[total_number]
        val plotUniqueIDName: String = FarmerPlotUniqueID[total_number]
        val plotNumber: String = PlotNoList[subPlotPosition]

        getFarmerDetails(plotID, plotUniqueIDName, plotNumber)
    }

    private fun getFarmerDetails(plotID: String, plotUniqueIDName: String, plotNumber: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plotDetails("Bearer $token", plotID, plotUniqueIDName, plotNumber).enqueue(object : Callback<ResponseBody>{
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonObject2 = stringResponse.getJSONObject("farmer")
                        val farmer_name = jsonObject2.optString("farmer_name")
                        onboardingDate = jsonObject2.optString("date_survey")
                        state_id = jsonObject2.optString("state_id")
                        state = jsonObject2.optString("state")
                        edtFarmer_name.text = farmer_name

                        val jsonObject1 = stringResponse.getJSONObject("cropdetail")
                        val dt_ploughing = jsonObject1.optString("dt_ploughing")
                        val dt_transplanting = jsonObject1.optString("dt_transplanting")

                        val jsonObject4 = jsonObject1.getJSONObject("plot_crop_details")
                        val crop_season_lastyrs = jsonObject4.optString("crop_season_lastyrs")
                        val crop_season_currentyrs = jsonObject4.optString("crop_season_currentyrs")
                        val crop_variety_lastyrs = jsonObject4.optString("crop_variety_lastyrs")
                        val crop_variety_currentyrs = jsonObject4.optString("crop_variety_currentyrs")
                        val fertilizer_1_lastyrs = jsonObject4.optString("fertilizer_1_lastyrs")
                        val fertilizer_1_currentyrs = jsonObject4.optString("fertilizer_1_currentyrs")
                        val fertilizer_2_lastyrs = jsonObject4.optString("fertilizer_2_lastyrs")
                        val fertilizer_2_currentyrs = jsonObject4.optString("fertilizer_2_currentyrs")
                        val fertilizer_3_lastyrs = jsonObject4.optString("fertilizer_3_lastyrs")
                        val fertilizer_3_currentyrs = jsonObject4.optString("fertilizer_3_currentyrs")
                        val yeild_lastyrs = jsonObject4.optString("yeild_lastyrs")
                        val nursery = jsonObject4.optString("nursery")

                        val jsonObject3 = stringResponse.getJSONObject("plotdetail")
                        val area_other_awd = jsonObject3.optString("area_other_awd")
                        val area_in_other_unit = jsonObject3.optString("area_in_other_unit")


                        first.visibility = View.VISIBLE
                        second.visibility = View.GONE

                        txtSeason_last.text = crop_season_lastyrs
                        txtSeason_current.text = crop_season_currentyrs
                        txtVariety_last.text = crop_variety_lastyrs
                        txtVariety_current.text = crop_variety_currentyrs
                        nitrogen_last_year.setText(fertilizer_1_lastyrs)
                        nitrogen_current_year.setText(fertilizer_1_currentyrs)
                        phosphorous_last_year.setText(fertilizer_2_lastyrs)
                        phosphorous_current_year.setText(fertilizer_2_currentyrs)
                        potassium_last_year.setText(fertilizer_3_lastyrs)
                        potassium_current_year.setText(fertilizer_3_currentyrs)
                        yield_last_year.setText(yeild_lastyrs)

                        farmer_id = jsonObject1.optString("farmer_id")
                        nursery_date.text = nursery
                        preparation_date.text = dt_ploughing
                        transplanting_date.text = dt_transplanting


                        if(area_other_awd == "null"){
                            val AreaChoosenString = getString(R.string.plot_area)
                            txtAreaChooseText.text = "$AreaChoosenString (Ha)"
//                            assignText("Ha")
                        }
                        else{
                            val AreaChoosenString = getString(R.string.plot_area)
                            txtAreaChooseText.text = "$AreaChoosenString ($area_in_other_unit)"
                            assignText(area_in_other_unit)
                        }

                        viewsVisible(false)
                        filled = true

                        if(lastData && filled){
                            next.text = "Home"
                        }

                        viewChangeAWDArea()

                        val WarningDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.WARNING_TYPE)
                        WarningDialog.titleText = " Warning "
                        WarningDialog.contentText = " Data already submitted. "
                        WarningDialog.confirmText = " OK "
                        WarningDialog.showCancelButton(false)
                        WarningDialog.setCancelable(false)
                        WarningDialog.setConfirmClickListener {
                            WarningDialog.cancel()
                        }.show()
                    }
                }
                else if(response.code() == 422){
                    if (response.errorBody() != null) {

                        val stringResponse = JSONObject(response.errorBody()!!.string())
                        val jsonObject = stringResponse.getJSONObject("farmer")
                        state_id = jsonObject.optString("state_id")
                        state = jsonObject.optString("state")
                        onboardingDate = jsonObject.optString("date_survey")
                        val mobile = jsonObject.optString("mobile")
                        val aadhar = jsonObject.optString("aadhaar")
                        val gender = jsonObject.optString("gender")

                        val jsonObject1 = stringResponse.getJSONObject("plotdetail")
                        val area_other_awd = jsonObject1.optString("area_other_awd")
                        val area_in_other_unit = jsonObject1.optString("area_in_other_unit")
                        val patta = jsonObject1.optString("patta_number")
                        val daag = jsonObject1.optString("daag_number")
                        val khatha = jsonObject1.optString("khatha_number")
                        val pattadhar = jsonObject1.optString("pattadhar_number")
                        val khatian = jsonObject1.optString("khatian_number")

                        val jsonObject2 = stringResponse.getJSONObject("state")
                        areaValue = jsonObject2.optString("base_value").toDouble()
                        maxBValue = jsonObject2.optString("max_base_value").toDouble()


                        if(area_other_awd == "null"){
                            val AreaChoosenString = getString(R.string.plot_area)
                            txtAreaChooseText.text = "$AreaChoosenString (Ha)"
//                            assignText("Ha")
                        }
                        else{
                            val AreaChoosenString = getString(R.string.plot_area)
                            txtAreaChooseText.text = "$AreaChoosenString ($area_in_other_unit)"
                            assignText(area_in_other_unit)
                        }


                        nursery_date.text = ""
                        preparation_date.text = ""
                        transplanting_date.text = ""
                        farmer_id = jsonObject.optString("id")
                        edtFarmer_name.text = jsonObject.optString("farmer_name")

                        first.visibility = View.GONE
                        second.visibility = View.VISIBLE

                        viewsVisible(true)
                        viewIncomplete(aadhar, gender, mobile, patta, daag, khatha, pattadhar, khatian)
                        viewChangeAWDArea()
                        convertOnboardingDate(onboardingDate)
                        getSeason()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@CropActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * Converting onboarding date so that we can use it in the transplantation date.
     */
    private fun convertOnboardingDate(onboardingDate: String) {
        if(onboardingDate != "null"){
            val inputFormat = SimpleDateFormat("yyyy-M-d", Locale.US)
            val calendar: Calendar = Calendar.getInstance().apply {
                time = inputFormat.parse(onboardingDate)
            }

            transplant_day = calendar.get(Calendar.DAY_OF_MONTH)
            transplant_month = calendar.get(Calendar.MONTH)
            transplant_year = calendar.get(Calendar.YEAR)


            Log.e("transplant_day", transplant_day.toString())
            Log.e("transplant_month", transplant_month.toString())
            Log.e("transplant_year", transplant_year.toString())
        }
    }

    /**
     * Getting the season data from API.
     */
    private fun getSeason() {
        SeasonIDList.clear()
        SeasonNameList.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.seasons("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        SeasonIDList.add(0)
                        SeasonNameList.add("--Select--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("seasons")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.optString("id").toInt()
                            val seasons = jsonObject.optString("name")

                            SeasonIDList.add(id)
                            SeasonNameList.add(seasons)
                        }
                        seasonSpinner()
                    } else {
                        Log.e("statusCode", response.code().toString())
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@CropActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


    /**
     * Initializing he season spinner here
     * as well as
     */
    private fun seasonSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, SeasonNameList)

        season_last_year_spinner.adapter = adapter
        season_last_year_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(position != 0){
                    seasonLastPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        season_current_year_spinner.adapter = adapter
        season_current_year_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(position != 0){
                    seasonCurrentPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun viewsVisible(b: Boolean) {
        nursery_date.isEnabled = b

        preparation_date.isEnabled = b

        transplanting_date.isEnabled = b

        nitrogen_last_year.isEnabled = b
        nitrogen_current_year.isEnabled = b

        phosphorous_last_year.isEnabled = b
        phosphorous_current_year.isEnabled = b

        potassium_last_year.isEnabled = b
        potassium_current_year.isEnabled = b

        yield_last_year.isEnabled = b
    }


    private fun viewIncomplete(aadhar: String, gender: String, mobile: String, patta: String, daag: String, khatha: String, pattadhar: String, khatian: String) {
        if(update[total_number] == "1") {
            incompleteData.visibility = View.VISIBLE

            if(state_id == "29"){
                assamLayout.visibility = View.VISIBLE

                pattaNumber.setText(patta)
                daagNumber.setText(daag)
            } else if(state_id == "36"){
                telanganaLayout.visibility = View.VISIBLE

                khathaNumber.setText(khatha)
                patthadharNumber.setText(pattadhar)
            } else if(state_id == "37"){
                bengalLayout.visibility = View.VISIBLE

                khatianNumber.setText(khatian)
            }

            fillIncompleteData(aadhar, gender, mobile)
        }
        else{
            incompleteData.visibility = View.GONE
        }
    }

    private fun fillIncompleteData(aadhar: String, gender: String, mobile: String) {
        Log.e("GENDER", gender)
        if(aadhar != "null"){
            aadharNumber.isEnabled = false
            aadharNumber.setText(aadhar)
        }

        if(gender != "null") {
            radioGroup.setChildrenEnable(false)
        }

        if(mobile != "null"){
            mobileNumber.isEnabled = false
            mobileNumber.setText(mobile)
        }
    }

    fun RadioGroup.setChildrenEnable(enable: Boolean) {
        for (i in 0 until this.childCount) {
            this.getChildAt(i).isEnabled = enable
        }
    }

    private fun viewChangeAWDArea() {
        awd_plot_area.isEnabled = awd_plot_area.text.toString() == "0.0"
        edtPlotArea.isEnabled = edtPlotArea.text.toString() == "0.0"
    }



    /**
     * Area in acres calculation is done her.
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


    /**
     * Checking value 1 is greater than value 2.
     */
    private fun checkValue(): Boolean {
        if(edtPlotArea.text.isEmpty() || edtPlotArea.text.isNullOrBlank()){
            return false
        }
        else {

            var AreaLand = edtPlotArea.text
            var AreasChoose = awd_plot_area.text

            Log.e("AreaLand", AreaLand.toString())
            Log.e("AreasChoose", AreasChoose.toString())

            var value1 = AreaLand.toString()
            var value2 = AreasChoose.toString()

            return value1.toDouble() >= value2.toDouble()
        }
        return false
    }


    private fun showWarning() {
        val WarningDialog = SweetAlertDialog(this@CropActivity, SweetAlertDialog.WARNING_TYPE)
        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = resources.getString(R.string.calculate_warning)
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
    }


}