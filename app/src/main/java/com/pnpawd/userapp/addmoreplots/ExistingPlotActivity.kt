package com.pnpawd.userapp.addmoreplots

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.ImageRecyclerView
import com.pnpawd.userapp.cropintellix.DashboardActivity
import com.pnpawd.userapp.cropintellix.TNCActivity
import com.pnpawd.userapp.models.LandRecordsModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.utils.Common
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.pnpawd.userapp.SessionManager
import com.pnpawd.userapp.ViewCameraDetail
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ExistingPlotActivity : AppCompatActivity() {
    lateinit var ownerLayout: LinearLayout
    var arrayList = ArrayList<String>()
    lateinit var uri: Uri

    var imageFileName: String = ""
    lateinit var currentPhotoPath: String
    lateinit var photoPath: File
    var count :Int = 0
    var rotate = 0

    private var imageModel: ArrayList<LandRecordsModel> = ArrayList<LandRecordsModel>()
    val ownership = arrayOf("--Select--", "Own", "Leased")
    private var unique_id: String = ""
    var plot_number: Int = 0
    var total_plot: Int = 0
    var farmerId: String = ""
    var latitude: String = ""
    var longitude: String = ""
    var state_ID: String = ""
    var areaValue: Double = 0.0
    //    var leased: Boolean = false
    var plotAreaList = ArrayList<String>()
    //    var leasedList = ArrayList<String>()
    var plotList = ArrayList<String>()


    private lateinit var imageRecyclerView: ImageRecyclerView
    private lateinit var recyclerView: RecyclerView

    private lateinit var txtPercent: TextView
    private lateinit var txtUniqueID: TextView
    private lateinit var txtPlot_Number: TextView
    private lateinit var txtAreaChooseText: TextView
    private lateinit var txtLandArea: TextView
    private lateinit var txtLandAreaUnit: TextView
    private lateinit var txtAutoAcres: TextView
    private lateinit var editAreaChoose: EditText
    private lateinit var txtAutoAreaChoose: TextView
    private lateinit var edtSurvey_Number: EditText
    private lateinit var edtOwner_Name: EditText
    private lateinit var edtPattaName: EditText
    private lateinit var edtDaagNumber: EditText
    private lateinit var edtKhathaNumber: EditText
    private lateinit var edtPattadharNumber: EditText
    private lateinit var edtKhatianNumber: EditText

    private lateinit var txtclick: LinearLayout
    private lateinit var llAssam: LinearLayout
    private lateinit var llTelangana: LinearLayout
    private lateinit var llBengal: LinearLayout


    private lateinit var back: Button
    private lateinit var next: Button

    var isLeased: Boolean = false
    var affinityPath: String = ""
    var carbonCreditPath: String = ""
    var token: String = ""
    var unit: String = ""
    var relationship: String = ""
    var ownerSpinnerPosition: Int = 0

    var existingPlots:Int? = 0;

    val watermark: Common = Common()

    private lateinit var perProgressBar: CircularProgressBar
    private lateinit var cardview: CardView
    lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_existing_plot)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        sessionManager = SessionManager(this@ExistingPlotActivity)
        /**
         * Getting some data's from previous screen.
         */
        val bundle = intent.extras
        if (bundle != null) {
            total_plot = bundle.getInt("total_plot")
            plot_number = bundle.getInt("plot_number")
            existingPlots = bundle.getInt("existing_plot")
            Log.i("plot_number","plot_number $plot_number in existing plot activity")
            unit = bundle.getString("area_unit")!!
            areaValue = bundle.getDouble("area_value")
//            leasedList = bundle.getStringArrayList("leasedList")!!
            plotList = bundle.getStringArrayList("areaHectare")!!
            plotAreaList = bundle.getStringArrayList("areaAcres")!!
            state_ID = bundle.getString("state_id")!!
            unique_id = bundle.getString("unique_id")!!
            farmerId = bundle.getString("FarmerId")!!
            latitude = bundle.getString("latitude")!!
            longitude = bundle.getString("longitude")!!
        } else {
            Log.e("total_plot", "Nope")
        }

        txtLandArea = findViewById(R.id.area_land_ex)
        perProgressBar = findViewById(R.id.circularProgressBar_ex)
        cardview = findViewById(R.id.progresscard_ex)

        val ownership_spinner = findViewById<Spinner>(R.id.ownership_ex)
        next = findViewById(R.id.plot_next_ex)
        back = findViewById(R.id.plot_back_ex)

        txtPlot_Number = findViewById(R.id.plot_number_ex)
        txtPlot_Number.text = existingPlots.toString()

        txtAreaChooseText = findViewById(R.id.area_choosen_text_ex)

        txtPercent = findViewById(R.id.percentage_ex)
        txtUniqueID = findViewById(R.id.plot_id_ex)
        txtUniqueID.text = unique_id

        txtclick = findViewById(R.id.clickHere_ex)
        txtLandAreaUnit = findViewById(R.id.land_area_unit_name_ex)
        txtAutoAcres = findViewById(R.id.auto_area_ex)
        editAreaChoose = findViewById(R.id.area_choosen_ex)
        txtAutoAreaChoose = findViewById(R.id.area_choosen_acres_ex)

        edtSurvey_Number = findViewById(R.id.survey_number_ex)
        edtOwner_Name = findViewById(R.id.owner_name_ex)

        edtPattaName = findViewById(R.id.patta_number_ex)
        edtDaagNumber = findViewById(R.id.daag_number_ex)
        edtKhathaNumber = findViewById(R.id.khatha_number_ex)
        edtPattadharNumber = findViewById(R.id.pattadhar_number_ex)
        edtKhatianNumber = findViewById(R.id.khatian_number_ex)

        recyclerView = findViewById(R.id.crop_camera_capture_ex)

        ownerLayout = findViewById(R.id.owner_name_linearlayout)
        llTelangana = findViewById(R.id.telangana_linear)
        llBengal = findViewById(R.id.bengal_linear)
        llAssam = findViewById(R.id.assam_linear)


        /**
         * Calling the below few lines as we are using the same activity multiple times.
         */
        txtLandArea.text = plotList[plot_number - 1]
        txtAutoAcres.text = plotAreaList[plot_number - 1]
        txtLandAreaUnit.text = unit


        /**
         * Showing the unit based on the state selected.
         */
        val AreaChoosenString = getString(R.string.area_choosen)
        txtAreaChooseText.text = AreaChoosenString + " ($unit)"


        /**
         * Getting token from the shared preference
         */
        token = sharedPreference.getString("token", "")!!


        /**
         * Assign the adapter to ownership spinner.
         * Also if index 2nd is selected we enable the leased option.
         */
        if (ownership_spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ownership)
            ownership_spinner.adapter = adapter

            ownership_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("LongLogTag")
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    Log.i("Mobile access item position", position.toString())

                    if (position == 2) {
                        ownerLayout.visibility = View.VISIBLE
                        ownerSpinnerPosition = position
                        isLeased = true
                        relationship = ownership[ownerSpinnerPosition]
                    } else {
                        ownerSpinnerPosition = position
                        isLeased = false
                        relationship = ownership[ownerSpinnerPosition]
                        ownerLayout.visibility = View.GONE
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        /**
         * Assigning layout manager ot recycler view.
         */
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        imageRecyclerView = ImageRecyclerView(imageModel)
        recyclerView.adapter = imageRecyclerView


        /**
         * on button click start taking pictures.
         */
        txtclick.setOnClickListener {

            val intent = Intent(this@ExistingPlotActivity, ViewCameraDetail::class.java)
            intent.putExtra("clickimage", "existingploatimage")
            startActivity(intent)

//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            if (intent.resolveActivity(packageManager) != null) {
//
//                try {
//                    photoPath = createImageFile()
//
//// Continue only if the File was successfully created
//                    uri = FileProvider.getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", photoPath)
//                } catch (_: IOException) { }
//
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                resultLauncher.launch(intent)
//            }
        }


        /**
         * Area in AWD checking if the area is greater than the area present in the area in acres.
         * Added a listener on it.-
         */
        editAreaChoose.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if(editAreaChoose.text.toString().isNotEmpty()){
                    var status = checkValue()
                    if(status){
                        txtAutoAreaChoose.text = acresCalculation(editAreaChoose.text.toString())
                    }
                    else{
                        showWarning()
                    }
                }
            }
        })


        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })


        /**
         * Checking if all required data are present or not after clicking next button.
         */
        next.setOnClickListener {
            val WarningDialog = SweetAlertDialog(this@ExistingPlotActivity, SweetAlertDialog.WARNING_TYPE)

            if (ownerSpinnerPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.land_ownership_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (isLeased && edtOwner_Name.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.owner_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (editAreaChoose.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.area_awd_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (!checkValue()) {
                showWarning()
            } else if (arrayList.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.preview_image_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else {
                cardview.visibility = View.VISIBLE

                perProgressBar.apply {
                    progressMax = 100f
                    setProgressWithAnimation(0f, 1000)
                    progressBarWidth = 5f
                    backgroundProgressBarWidth = 2f
                    progressBarColor = Color.GREEN
                }

                txtPercent.text = "0 %"

                back.isEnabled = false
                next.isEnabled = false

                checkData(arrayList)
            }
        }

        stateFieldsVisible(state_ID)
    }


    private fun showWarning() {
        val WarningDialog = SweetAlertDialog(this@ExistingPlotActivity, SweetAlertDialog.WARNING_TYPE)
        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = resources.getString(R.string.calculate_warning)
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
    }

    /**
     * Checking value 1 is greater than value 2.
     */
    private fun checkValue(): Boolean {
        var AreaLand = txtLandArea.text
        var AreasChoose = editAreaChoose.text

        Log.e("AreaLand", AreaLand.toString())
        Log.e("AreasChoose", AreasChoose.toString())

        var value1 = AreaLand.toString()
        var value2 = AreasChoose.toString()

        var isZero = value2.toDouble() == 0.0
        return value1.toDouble() >= value2.toDouble() &&  !isZero
    }

    /**
     * Checking which state it is current entry for.
     */
    private fun stateFieldsVisible(stateId: String) {
        if(stateId == "29"){
            llAssam.visibility = View.VISIBLE
        } else if(stateId == "36"){
            llTelangana.visibility = View.VISIBLE

        } else if(stateId == "37"){
            llBengal.visibility = View.VISIBLE

        }
    }


    /**
     * Calculating the area of land to get the area in acres
     */
    private fun acresCalculation(acres: String): String {
        return if(acres.isNotEmpty()){
            val value = acres.toDouble()
            val calculated = (value * areaValue).toString()
            Log.e("Area Bigha", calculated)
            calculated
        } else{
            "0.0"
        }
    }

    /**
     * The data of the last entry will be carried to the next screen.
     * As we are sending the data on the submit screen.
     */
    private fun checkData(arrayList: ArrayList<String>){
        if (total_plot == 1){
            if(ownerSpinnerPosition == 2){
                val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putBoolean("Leased", true)
                editor.commit()
            }
            else{}

            cardview.visibility = View.VISIBLE

            val intent = Intent(this, ExistingTNCActivity::class.java).apply {
                putStringArrayListExtra("imageList", arrayList)
                putExtra("plot_number", txtPlot_Number.text.toString())
                putExtra("plot", txtLandArea.text.toString())
                putExtra("relationship", relationship)
                putExtra("owner_name", edtOwner_Name.text.toString())
                putExtra("unique_id", unique_id)
                putExtra("survey_number", edtSurvey_Number.text.toString())
                putExtra("FarmerId", farmerId)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("area_other_awd", editAreaChoose.text.toString())
                putExtra("area_acre_awd", txtAutoAreaChoose.text.toString())
                putExtra("patta_number", edtPattaName.text.toString())
                putExtra("daag_number", edtDaagNumber.text.toString())
                putExtra("khatha_number", edtKhathaNumber.text.toString())
                putExtra("pattadhar_number", edtPattadharNumber.text.toString())
                putExtra("khatian_number", edtKhatianNumber.text.toString())
                putExtra("khatian_number", edtKhatianNumber.text.toString())
            }
            startActivity(intent)
        }
        else{
            sendData(arrayList)
        }

    }


    /**
     * Sending the data the the back-end through API.
     * Sending in multipart.
     */
    private fun sendData(arrayList: ArrayList<String>) {
        perProgressBar.apply {
            progressMax = 100f
            setProgressWithAnimation(50f, 1000)
            progressBarWidth = 5f
            backgroundProgressBarWidth = 2f
            progressBarColor = Color.GREEN
        }

        txtPercent.text = "50 %"


        val tncCC = 0
        val ownership = relationship

        val FarmerId: RequestBody = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
        val UniqueID: RequestBody = txtUniqueID.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val AreaOther: RequestBody = editAreaChoose.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val AreaAcres: RequestBody = txtAutoAreaChoose.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val PlotNumber: RequestBody = txtPlot_Number.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val Ownership: RequestBody = ownership.toRequestBody("text/plain".toMediaTypeOrNull())
        val SurveyNumber: RequestBody = edtSurvey_Number.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val TNCCarbonCredit: RequestBody = tncCC.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val farmerID: MultipartBody.Part = createFormData("farmer_id", null, FarmerId)
        val farmerUniqueID: MultipartBody.Part = createFormData("farmer_unique_id", null, UniqueID)
        val areaOther: MultipartBody.Part = createFormData("area_other_awd", null, AreaOther)
        val areaAcres: MultipartBody.Part = createFormData("area_acre_awd", null, AreaAcres)
        val plotNumber: MultipartBody.Part = createFormData("plot_no", null, PlotNumber)
        val landOwnership: MultipartBody.Part = createFormData("land_ownership", null, Ownership)
        val surveyNumber: MultipartBody.Part = createFormData("survey_no", null, SurveyNumber)
        val tncCarbonCredit: MultipartBody.Part = createFormData("check_carbon_credit", null, TNCCarbonCredit)

        val OwnerName: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var ownerNameBody: MultipartBody.Part = createFormData("actual_owner_name", null, OwnerName)

        if (edtOwner_Name.text.isNotEmpty()){
            val requestOwnerName: RequestBody = edtOwner_Name.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            ownerNameBody = createFormData("actual_owner_name", null, requestOwnerName)
        }

        val PattaNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var pattaNumber: MultipartBody.Part = createFormData("patta_number", null, PattaNumber)
        if(edtPattaName.text.isNotEmpty()){
            val requestPattaNumber: RequestBody = edtPattaName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            pattaNumber = createFormData("patta_number", null, requestPattaNumber)
        }

        val DaagNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var daagNumber: MultipartBody.Part = createFormData("daag_number", null, DaagNumber)
        if(edtDaagNumber.text.isNotEmpty()){
            val requestDaagNumber: RequestBody = edtDaagNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            daagNumber = createFormData("daag_number", null, requestDaagNumber)
        }

        val KhathaNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var khathaNumber: MultipartBody.Part = createFormData("khatha_number", null, KhathaNumber)
        if(edtKhathaNumber.text.isNotEmpty()){
            val requestKhathaNumber: RequestBody = edtKhathaNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            khathaNumber = createFormData("khatha_number", null, requestKhathaNumber)
        }

        val PattadharNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var pattadharNumber: MultipartBody.Part = createFormData("pattadhar_number", null, PattadharNumber)
        if(edtPattadharNumber.text.isNotEmpty()){
            val requestPattadharNumber: RequestBody = edtPattadharNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            pattadharNumber = createFormData("pattadhar_number", null, requestPattadharNumber)
        }

        val KhatianNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var khatianNumber: MultipartBody.Part = createFormData("khatian_number", null, KhatianNumber)
        if(edtKhatianNumber.text.isNotEmpty()){
            val requestPattadharNumber: RequestBody = edtKhatianNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            khatianNumber = createFormData("khatian_number", null, requestPattadharNumber)
        }


        val AffinityDummy: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var affinityBody: MultipartBody.Part = createFormData("sign_affidavit", null, AffinityDummy)

        if (affinityPath != "") {
            val affinityPathFile = File(affinityPath)
            val requestFile: RequestBody = affinityPathFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            affinityBody = createFormData("sign_affidavit", affinityPathFile.name, requestFile)
        }

        val CreditDummy: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var creditBody: MultipartBody.Part = createFormData("sign_carbon_credit", null, CreditDummy)

        if (carbonCreditPath != "") {
            val creditPathFile = File(carbonCreditPath)
            val requestFile: RequestBody = creditPathFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            creditBody = createFormData("sign_carbon_credit", creditPathFile.name, requestFile)
        }


        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        Log.e("SENDDATA","UNIQYE ${txtUniqueID.text}")
        retIn.existingPlotInfo("Bearer $token", farmerID, farmerUniqueID, plotNumber, landOwnership, ownerNameBody, pattaNumber, daagNumber,
            khathaNumber, pattadharNumber, khatianNumber, affinityBody, surveyNumber, tncCarbonCredit, areaOther, areaAcres, creditBody)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        Log.e("Success", response.body().toString())
                        upLoadSingleImage(arrayList, "Bearer $token")
                    } else {
                        cardview.visibility = View.GONE
                        back.isEnabled = true
                        next.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    cardview.visibility = View.GONE
                    back.isEnabled = true
                    next.isEnabled = true
                }
            })
    }

    /**
     * Using a recursion function to send the data.
     * Sending the image one by one to the API.
     * Sending in multi part.
     */
    private fun upLoadSingleImage(arrayList: ArrayList<String>, token: String) {
        if (arrayList.isEmpty()) {
            repeatScreen()
        } else {

            val currentUrl: String = arrayList[0]
            val file = File(currentUrl)

            val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val farmer_id = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmer_unique_id = txtUniqueID.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val sr = plot_number.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val body: MultipartBody.Part = createFormData("image", file.name, requestFile)
            val farmerID: MultipartBody.Part = createFormData("farmer_id", null, farmer_id)
            val farmerUniqueID: MultipartBody.Part =
                createFormData("farmer_unique_id", null, farmer_unique_id)
            val Sr: MultipartBody.Part = createFormData("sr", null, sr)

            val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
            retIn.plotImageUpload("Bearer $token", farmerID, farmerUniqueID, Sr, body)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() == 200) {
                            arrayList.removeAt(0)

                            perProgressBar.apply {
                                progressMax = 100f
                                setProgressWithAnimation(75f, 1000)
                                progressBarWidth = 5f
                                backgroundProgressBarWidth = 2f
                                progressBarColor = Color.GREEN
                            }

                            txtPercent.text = "75 %"
                            upLoadSingleImage(arrayList, token)
                        }

                        else{
                            back.isEnabled = true
                            next.isEnabled = true
                            cardview.visibility = View.GONE
                            Toast.makeText(this@ExistingPlotActivity, "Please click images again", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        cardview.visibility = View.GONE
                        back.isEnabled = true
                        next.isEnabled = true
                        Toast.makeText(this@ExistingPlotActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    /**
     * If entries are present we call the same screen.
     * We also carry some data to show.
     */
    private fun repeatScreen() {
        perProgressBar.apply {
            progressMax = 100f
            setProgressWithAnimation(100f, 1000)
            progressBarWidth = 5f
            backgroundProgressBarWidth = 2f
            progressBarColor = Color.GREEN
        }

        txtPercent.text = "100 %"

        if (ownerSpinnerPosition == 2) {
            val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putBoolean("Leased", true)
            editor.apply()
        }

        if (total_plot != 1) {
            cardview.visibility = View.GONE
            back.isEnabled = true
            next.isEnabled = true

            val SuccessDialog = SweetAlertDialog(this@ExistingPlotActivity, SweetAlertDialog.SUCCESS_TYPE)

            SuccessDialog.titleText = resources.getString(R.string.success)
            SuccessDialog.contentText = "${resources.getString(R.string.plot_number)} $plot_number  ${resources.getString(R.string.submitted_successfully)}"
            SuccessDialog.confirmText = resources.getString(R.string.ok)
            SuccessDialog.showCancelButton(false)
            SuccessDialog.setCancelable(false)
            SuccessDialog.setConfirmClickListener {
                SuccessDialog.cancel()

                cardview.visibility = View.GONE

                val intent = Intent(this, ExistingPlotActivity::class.java).apply {
                    putExtra("total_plot", total_plot - 1)
                    putExtra("plot_number", plot_number + 1)
                    putStringArrayListExtra("areaHectare", plotList)
                    putStringArrayListExtra("areaAcres", plotAreaList)
                    putExtra("area_unit", unit)
                    putExtra("state_id", state_ID)
                    putExtra("area_value", areaValue)
                    putExtra("unique_id", unique_id)
                    putExtra("FarmerId", farmerId)
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("existing_plot", existingPlots!! +1)
                }
                startActivity(intent)
            }.show()

        } else {
            cardview.visibility = View.GONE
            back.isEnabled = true
            next.isEnabled = true
            nextScreen()

        }
    }

    private fun nextScreen() {
        val SuccessDialog = SweetAlertDialog(this@ExistingPlotActivity, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Farmer Onboarded successfully. "
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


    /**
     * Creating image file in the directory.
     */
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
// Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

// Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        Log.i("imagepath", currentPhotoPath)
        return image
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
//                val source = ImageDecoder.createSource(this.contentResolver, uri)
//                val image = ImageDecoder.decodeBitmap(source)
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

                val stampImage = watermark.addWatermark(application.applicationContext, image, "#$unique_id | P$plot_number | $timeStamp | $latitude | $longitude")
                count += 1

                try {
                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")

                    val outStream = FileOutputStream(outFile)
                    stampImage.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    //val data = LandRecordsModel(count, stampImage, rotate)
                    //imageModel.add(data)
                    imageRecyclerView.notifyDataSetChanged()

                    val imagePath = outFile.absolutePath
                    arrayList.add(imagePath)


                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onBackPressed() {
    }

//    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
//        var image = image
//        return if (maxHeight > 0 && maxWidth > 0) {
//            val width = image.width
//            val height = image.height
//            val ratioBitmap = width.toFloat() / height.toFloat()
//            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
//            var finalWidth = maxWidth
//            var finalHeight = maxHeight
//            if (ratioMax > ratioBitmap) {
//                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
//            } else {
//                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
//            }
//            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
//            image
//        } else {
//            image
//        }
//    }

    override fun onStart() {
        super.onStart()

        if (!sessionManager.getEXISTINGPLOATIMAGE().equals("DEFAULT")){

            count += 1
            val data = sessionManager.getEXISTINGPLOATIMAGE()?.let { LandRecordsModel(count, it, rotate) }
            if (data != null) {
                imageModel.add(data)
            }
            imageRecyclerView.notifyDataSetChanged()

            val imagePath = sessionManager.getEXISTINGPLOATIMAGE()
            if (imagePath != null) {
                arrayList.add(imagePath)
            }

            Log.d("userimagedetails","userimage1"+imageModel.toString())
            Log.d("userimagedetails","userimage2"+imageModel.toString())
        }

    }

}