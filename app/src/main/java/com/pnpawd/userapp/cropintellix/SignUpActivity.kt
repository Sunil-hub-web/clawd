package com.pnpawd.userapp.cropintellix

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.pnpawd.userapp.BuildConfig
import com.pnpawd.userapp.R
import com.pnpawd.userapp.SignInActivity
import com.pnpawd.userapp.models.MobileVerifyModel
import com.pnpawd.userapp.models.UserModel
import com.pnpawd.userapp.network.ApiClient
import com.pnpawd.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    lateinit var edtName: EditText
    lateinit var edtMobile_number: EditText
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var edtConfirm_password: EditText
    lateinit var edtCode: EditText
    lateinit var signUp: Button
    lateinit var signIn: TextView
    lateinit var email_verify: TextView
    lateinit var mobile_verify:TextView

    var name: String = ""
    var mobile_number: String = ""
    var username: String = ""
    var password: String = ""
    var confirm_password: String = ""
    var code: String = ""
    var fcmToken: String = ""

    private lateinit var state_spinner: Spinner
    var stateIDList = java.util.ArrayList<Int>()
    var stateNameList = java.util.ArrayList<String>()
    private var statePosition: Int = 0
    var state: String = ""
    var state_id: String = ""

    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        progress = SweetAlertDialog(this@SignUpActivity, SweetAlertDialog.PROGRESS_TYPE)

        state_spinner = findViewById(R.id.account_state_spinner)

        edtName = findViewById(R.id.account_name)
        edtMobile_number = findViewById(R.id.account_mobile_no)
        edtEmail = findViewById(R.id.account_email)
        edtPassword = findViewById(R.id.account_password)
        edtConfirm_password = findViewById(R.id.account_confirm_password)
        edtCode = findViewById(R.id.account_company_name)

        signUp = findViewById(R.id.accountSignUp)
        signIn = findViewById(R.id.signIn)

        email_verify = findViewById(R.id.email_verify)
        mobile_verify = findViewById(R.id.mobile_verify)


        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val release = java.lang.Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
        Log.e("version", versionName + versionCode + release.toString())

        val deviceName = Build.MODEL // returns model name
        val deviceManufacturer = Build.MANUFACTURER // returns manufacturer
        Log.e("version", deviceName + deviceManufacturer)


        signIn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        })


        edtMobile_number.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                edtEmail.setText(p0)
            }

            override fun afterTextChanged(p0: Editable?) {
                if (edtMobile_number.text.length < 10) {
                    mobile_verify.text = "Not a valid mobile number"
                    mobile_verify.setTextColor(Color.parseColor("#FF1E00"))
                } else {
                    mobile_verify.text = "Checking Availability"
                    mobile_verify.setTextColor(Color.parseColor("#FF1E00"))
                    checkMobileNumber(edtMobile_number.text.toString())
                }

                if (p0.toString().length == 1 && p0.toString().startsWith("0") || p0.toString().startsWith("1")
                    || p0.toString().startsWith("2")  || p0.toString().startsWith("3") || p0.toString().startsWith("4")
                    || p0.toString().startsWith("5"))
                {
                    p0!!.clear()
                }
            }

        })


        signUp.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this@SignUpActivity, SweetAlertDialog.WARNING_TYPE)

            if (edtName.text == null){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.name_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtMobile_number.text == null){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtEmail.text == null){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.email_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtPassword.text == null){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.password_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtPassword.text.length < 6){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.password_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtConfirm_password.text == null){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.confirm_empty_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtConfirm_password.text.length < 6){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.confirm_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtConfirm_password.text.toString() != edtPassword.text.toString()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.password_mismatch_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (edtCode.text.isEmpty()){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.company_code_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (statePosition == 0){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.state_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else{
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.wait)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                name = edtName.text.toString()
                mobile_number = edtMobile_number.text.toString()
                username = edtEmail.text.toString()
                password = edtPassword.text.toString()
                confirm_password = edtConfirm_password.text.toString()
                code = edtCode.text.toString()
                state_id = stateIDList[statePosition].toString()
                state = stateNameList[statePosition]



                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }

// Get new FCM registration token
                    fcmToken = task.result
                    Log.e("FCM_TOKEN", fcmToken)
                })

                signup(name, mobile_number, username, password, code, versionCode, versionName, release, deviceName, deviceManufacturer, state_id, state, fcmToken)
            }

        })

        getState()
    }

    private fun getState() {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.wait)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()


        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.state().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.body() != null) {
                    stateIDList.add(0)
                    stateNameList.add("--Select--")

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("state")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("name")

                        stateIDList.add(id)
                        stateNameList.add(name)
                    }
                    progress.dismiss()
                    stateSpinner()
                }
                else{
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@SignUpActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun stateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stateNameList)
        state_spinner.adapter = adapter
        state_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                statePosition = position
                Log.e("statePosition", statePosition.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun checkMobileNumber(number: String) {
        val mobileVerifyModel = MobileVerifyModel(number)

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.verifyNumber(mobileVerifyModel).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, t.message, Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        mobile_verify.text = "Mobile Number Available"
                        mobile_verify.setTextColor(Color.parseColor("#06c238"))

                    }
                }
                else if(response.code() == 422){
                    mobile_verify.text = "Mobile Number Already Used"
                    mobile_verify.setTextColor(Color.parseColor("#FF1E00"))

                }
            }
        })
    }

    private fun signup(
        name: String,
        mobile: String,
        username: String,
        password: String,
        code: String,
        versionCode: Int,
        versionName: String,
        release: Double,
        deviceName: String,
        deviceManufacturer: String,
        state_id: String,
        state: String,
        fcmToken: String
    ){
        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val registerInfo = UserModel(name, mobile, username, password, code, "", versionCode.toString(), versionName, release, deviceName, deviceManufacturer, state_id, state, fcmToken)

        retIn.register(registerInfo).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, t.message, Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    progress.dismiss()
                    Toast.makeText(this@SignUpActivity, "Registration success!", Toast.LENGTH_SHORT).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        this@SignUpActivity.finish()
                    }, 2000)
                } else if(response.code() == 403){
                    progress.dismiss()

                    val WarningDialog = SweetAlertDialog(this@SignUpActivity, SweetAlertDialog.WARNING_TYPE)

                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Wrong Company Code"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                }
                else if(response.code() == 422){
                    progress.dismiss()

                    if (response.errorBody() != null) {
                        val stringResponse = JSONObject(response.errorBody()!!.string())
                        val jsonObject = stringResponse.optString("mobile")

                        Log.e("statusCode422", response.code().toString())
                        val WarningDialog = SweetAlertDialog(this@SignUpActivity, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = jsonObject
                        WarningDialog.confirmText = resources.getString(R.string.ok)
                        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                    }
                }
            }
        })
    }


}
