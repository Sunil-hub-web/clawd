package com.pnpawd.userapp.network

import WeatherResponseModel
import com.pnpawd.userapp.models.*
import com.pnpawd.userapp.models.WeatherForecastModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {
    @Headers("Accept: application/json")
    @GET("check/version")
    fun checkVersion(@Query("version") version: String): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("dashboard/setting")
    fun moduleAccess(@Body stateIdModel: StateIdModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/cropdata/setting")
    fun dateInterval(@Header( "Authorization") token: String): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("register")
    fun register(@Body userModel: UserModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("validate")
    fun verifyNumber(@Body mobileVerifyModel: MobileVerifyModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("validate")
    fun verifyEmail(@Body emailVerifyModel: EmailVerifyModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("login")
    fun login(@Body loginModel: LoginModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("generateuniqueId")
    fun uniqueID(@Query("version") version: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/plotuniqueid/list")
    fun plotUniqueId(@Header( "Authorization") token: String, @Query("data") mobile: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/subplots")
    fun subPlotId(@Header( "Authorization") token: String, @Query("farmer_uniqueId") farmer_uniqueId: String) : Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/crop/subplots")
    fun cropSubPlotId(@Header( "Authorization") token: String, @Query("farmer_uniqueId") farmer_uniqueId: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/cropdata/plot/detail")
    fun plotDetails(@Header( "Authorization") token: String, @Query("plot_id") plot_id: String,
                  @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String, @Query("plot_no") plot_no: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/farmer/benefits/detail")
    fun farmerPlotDetails(@Header( "Authorization") token: String, @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String,
                    @Query("farmer_uniqueId") farmer_uniqueId: String, @Query("plot_no") plot_no: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("cropvariety")
    fun cropVariety(@Query("state_id") state_id: String, @Query("season_id") season_id: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/farmer/cropdata")
    fun sendCropData(@Header( "Authorization") token: String, @Body cropInfoModel: CropInfoModel): Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/farmer")
    fun farmerInfo(@Header( "Authorization") token: String, @Body farmerInfoModel: FarmerInfoModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("V1/onboarding/data")
    fun addExistingPlots(@Header( "Authorization") token: String, @Body farmerInfoModel: FarmerInfoModelNew): Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/storeimage")
    fun imageUpload(@Header( "Authorization") token: String, @Part farmer_id: MultipartBody.Part, @Part farmer_unique_id: MultipartBody.Part,
                    @Part image: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/farmer/benefits/images")
    fun benefitImageUpload(@Header( "Authorization") token: String, @Part farmer_id: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part,
                           @Part plot_no: MultipartBody.Part, @Part farmer_benefit_id: MultipartBody.Part, @Part image: MultipartBody.Part): Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/farmer/benefits")
    fun sendBenefitData(@Header( "Authorization") token: String, @Body benefitModel: BenefitModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/farmer/benefits/check")
    fun checkBenefitData(@Header( "Authorization") token: String, @Body benefitCheckModel: BenefitCheckModel): Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("relationshipowner")
    fun relationship(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("base/value")
    fun baseValue(@Header( "Authorization") token: String, @Query("state_id") state_id: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("State")
    fun state() : Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("organization")
    fun org(@Body organizationModel: OrganizationModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("districts/{id}")
    fun district(@Header( "Authorization") token: String, @Path("id") id: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("districtsid")
    fun newDistrict(@Header( "Authorization") token: String, @Body districtModel: DistrictModel) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("taluka/{id}")
    fun taluka(@Header( "Authorization") token: String, @Path("id") id: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("villagepanchayat/{id}")
    fun panchayat(@Header( "Authorization") token: String, @Path("id") id: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("village/{id}")
    fun village(@Header( "Authorization") token: String, @Path("id") id: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("get/seasons")
    fun seasons(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("get/benefits")
    fun benefits(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/farmer/location/info")
    fun farmerLocation(@Header( "Authorization") token: String, @Body farmerLocationModel: FarmerLocationModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("V1/onboarding/store/location")
    fun existingFarmerLocation(@Header( "Authorization") token: String, @Body farmerLocationModel: FarmerLocationModel): Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/farmer/plot")
    fun plotInfo(@Header( "Authorization") token: String, @Part farmer_id: MultipartBody.Part, @Part farmer_unique_id: MultipartBody.Part,
                 @Part plot_no: MultipartBody.Part, @Part land_ownership: MultipartBody.Part,  @Part actual_owner_name: MultipartBody.Part,
                 @Part patta_number: MultipartBody.Part, @Part daag_number: MultipartBody.Part,  @Part khatha_number: MultipartBody.Part,
                 @Part pattadhar_number: MultipartBody.Part, @Part khatian_number: MultipartBody.Part, @Part sign_affidavit: MultipartBody.Part,
                 @Part survey_no: MultipartBody.Part, @Part check_carbon_credit: MultipartBody.Part, @Part area_other_awd: MultipartBody.Part,
                 @Part area_acre_awd: MultipartBody.Part, @Part signature: MultipartBody.Part): Call<ResponseBody>

//    Existing
    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/onboarding/store/plot")
    fun existingPlotInfo(@Header( "Authorization") token: String, @Part farmer_id: MultipartBody.Part, @Part farmer_unique_id: MultipartBody.Part,
                 @Part plot_no: MultipartBody.Part, @Part land_ownership: MultipartBody.Part,  @Part actual_owner_name: MultipartBody.Part,
                 @Part patta_number: MultipartBody.Part, @Part daag_number: MultipartBody.Part,  @Part khatha_number: MultipartBody.Part,
                 @Part pattadhar_number: MultipartBody.Part, @Part khatian_number: MultipartBody.Part, @Part sign_affidavit: MultipartBody.Part,
                 @Part survey_no: MultipartBody.Part, @Part check_carbon_credit: MultipartBody.Part, @Part area_other_awd: MultipartBody.Part,
                 @Part area_acre_awd: MultipartBody.Part, @Part signature: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/farmer/images")
    fun lastScreen(@Header( "Authorization") token: String, @Part screen: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part, @Part date_survey: MultipartBody.Part,
                   @Part time_survey: MultipartBody.Part, @Part plotowner_sign: MultipartBody.Part, @Part farmer_photo: MultipartBody.Part, @Part aadhaar_photo: MultipartBody.Part,
                   @Part others_photo: MultipartBody.Part,): Call<ResponseBody>


//    @Multipart
//    @Headers("Accept: application/json")
//    @POST("V1/farmer")
//    fun lastScreen(@Header( "Authorization") token: String, @Part screen: MultipartBody.Part, @Part farmer_id: MultipartBody.Part, @Part date_survey: MultipartBody.Part,
//                   @Part time_survey: MultipartBody.Part, @Part farmer_photo: MultipartBody.Part, @Part aadhaar_photo: MultipartBody.Part,
//                   @Part others_photo: MultipartBody.Part, @Part plotowner_sign: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/onboarding/data")
    fun existingLastScreen(@Header( "Authorization") token: String, @Part screen: MultipartBody.Part, @Part farmer_id: MultipartBody.Part, @Part date_survey: MultipartBody.Part,
                   @Part time_survey: MultipartBody.Part, @Part farmer_photo: MultipartBody.Part, @Part aadhaar_photo: MultipartBody.Part,
                   @Part others_photo: MultipartBody.Part, @Part plotowner_sign: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/farmer/plot/images")
    fun plotImageUpload(@Header( "Authorization") token: String, @Part farmer_id: MultipartBody.Part, @Part farmer_unique_id: MultipartBody.Part,
                    @Part sr: MultipartBody.Part, @Part image: MultipartBody.Part): Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/farmer")
    fun farmerSubmit(@Header( "Authorization") token: String, @Body submitModel: SubmitModel): Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("termandconditions")
    fun tnc() : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/user/profile")
    fun userDetails(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("app/termcondition")
    fun tncData() : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("privacy/policy")
    fun privacyData() : Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/user/profile/update")
    fun postUserDetails(@Header( "Authorization") token: String, @Body profileModel: ProfileModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("generate/user/otp")
    fun generateOTP(@Body mobileVerifyModel: MobileVerifyModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("validate/user/otp")
    fun verifyOTP(@Body verifyOtpModel: VerifyOtpModel): Call<ResponseBody>





// Reports
    @Headers("Accept: application/json")
    @GET("V1/user/farmer/count")
    fun formCount(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/status/onboarding/list")
    fun registrationReportList(@Header( "Authorization") token: String, @Body statusModel: StatusModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("")
    fun registrationReportList(@Header( "Authorization") token: String, @Url url: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("")
    fun registrationReportPagination(@Header( "Authorization") token: String,@Url url: String, @Body statusModel: StatusModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/user/farmer/registration/search")
    fun registrationReportQuery(@Header( "Authorization") token: String, @Query("uniqueid") uniqueid: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/pipe/installtion/list")
    fun searchPipeReport(@Header( "Authorization") token: String, @Query("farmer_uniqueId") farmer_uniqueId: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/farmer/registration/detail")
    fun onBoardingReportDetail(@Header( "Authorization") token: String, @Body farmerIDModel: FarmerIDModel) : Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/user/farmer/registration/update/img")
    fun reUploadImage(@Header( "Authorization") token: String, @Part farmer_unique_id: MultipartBody.Part,  @Part farmer_id: MultipartBody.Part,
                      @Part plotno: MultipartBody.Part, @Part image: MultipartBody.Part) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/user/farmer/registration/edit/{unique_id}/{plot_no}")
    fun rejectReason(@Header( "Authorization") token: String, @Path("unique_id")unique_id : String, @Path("plot_no")plot_no : String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/farmer/registration/update")
    fun submitNumberRejectReason(@Header("Authorization") token: String, @Body reuploadModel: SurveyNumberReUploadModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/farmer/registration/update")
    fun submitAreaRejectReason(@Header("Authorization") token: String, @Body reuploadModel: AreaReUploadModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/farmer/registration/update")
    fun submitNameRejectReason(@Header("Authorization") token: String, @Body reuploadModel: NameReUploadModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/status/cropdata/list")
    fun cropReportList(@Header( "Authorization") token: String, @Body statusModel: StatusModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("")
    fun cropReportPagination(@Header( "Authorization") token: String,@Url url: String, @Body statusModel: StatusModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/user/farmer/cropdata/search")
    fun cropReportQuery(@Header( "Authorization") token: String, @Query("uniqueid") uniqueid: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/farmer/cropdata/detail")
    fun cropReportDetail(@Header( "Authorization") token: String, @Body farmerIDModel: FarmerIDModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/status/benefit/list")
    fun benefitReportList(@Header( "Authorization") token: String, @Body statusModel: StatusModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("")
    fun benefitReportPagination(@Header( "Authorization") token: String,@Url url: String, @Body statusModel: StatusModel) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/user/farmer/benefit/search")
    fun benefitReportQuery(@Header( "Authorization") token: String, @Query("uniqueid") uniqueid: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/user/farmer/benefit/detail")
    fun benefitReportDetail(@Header( "Authorization") token: String, @Body farmerIDModel: FarmerIDModel) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/aeration/list")
    fun aeriationReportList(@Header( "Authorization") token: String) : Call<ResponseBody>





    // Pipe Installation
    @Headers("Accept: application/json")
    @GET("V1/check/pipe/data")
    fun checkPipeData(@Header( "Authorization") token: String, @Query("farmer_uniqueId") farmer_uniqueId: String,
                      @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String, @Query("plot_no") plot_no: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/farmer/pipe/plot/detail")
    fun farmerPipeDetails(@Header( "Authorization") token: String, @Body farmerUniqueIdModel: FarmerUniqueIdModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/pipe/installation")
    fun sendPipeData(@Header( "Authorization") token: String, @Body pipeLocationModel: PipeLocationModel) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/number/pipe/check")
    fun getPipeQty(@Header( "Authorization") token: String, @Body pipeQtyModel: PipeQtyModel) : Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/pipe/location")
    fun sendPipeLastData(@Header( "Authorization") token: String, @Part farmer_id: MultipartBody.Part, @Part farmer_plot_uniqueid: MultipartBody.Part,
                         @Part farmer_unique_id: MultipartBody.Part, @Part plot_no: MultipartBody.Part, @Part lat: MultipartBody.Part,  @Part lng: MultipartBody.Part,
                         @Part pipe_no: MultipartBody.Part, @Part distance: MultipartBody.Part, @Part installing_pipe: MultipartBody.Part,
                         @Part images: MultipartBody.Part): Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/check/pipe/image/data")
    fun checkPipeStatus(@Header( "Authorization") token: String, @Query("farmer_uniqueId") farmer_uniqueId: String,
                        @Query("plot_no") plot_no: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/check/polyon/nearby")
    fun polygonNearby(@Header( "Authorization") token: String, @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String,
                        @Query("lat") lat: Double,  @Query("lng") lng: Double) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @POST("V1/check/coordinates")
    fun checkCoordinates(@Header( "Authorization") token: String, @Body checkPolygonModel: CheckPolygonModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/get/pipe/threshold")
    fun threshold(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/pipe/installtion/list")
    fun pipeReportList(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("V1/polygon/reject/list")
    fun polygonReportList(@Header( "Authorization") token: String) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/pipeinstallation/polygon")
    fun pipeGetPolygon(@Header( "Authorization") token: String, @Body farmerUniqueIdModel: FarmerUniqueIdModel) : Call<ResponseBody>


    @Headers("Accept: application/json")
    @POST("V1/update/pipeinstallation")
    fun updatePipePolygon(@Header( "Authorization") token: String, @Body updatePolygonModel: UpdatePolygonModel) : Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/update/pipe/image")
    fun pipeReUploadImage(@Header( "Authorization") token: String, @Part farmer_uniqueId: MultipartBody.Part,  @Part farmer_plot_uniqueid: MultipartBody.Part,
                      @Part pipe_no: MultipartBody.Part, @Part plot_no: MultipartBody.Part, @Part lat: MultipartBody.Part, @Part lng: MultipartBody.Part, @Part distance: MultipartBody.Part,
                      @Part pipe_img_id: MultipartBody.Part, @Part images: MultipartBody.Part) : Call<ResponseBody>



    // Aeriation Event
    @Headers("Accept: application/json")
    @GET("V1/pipe/unique/plot")
    fun aeriationPltList(@Header( "Authorization") token: String, @Query("farmer_uniqueId") farmer_uniqueId: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/pipe/plot/pipeno")
    fun aeriationPipeNumber(@Header( "Authorization") token: String, @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/check/aeration/ploygon")
    fun aeriationPloygonList(@Header( "Authorization") token: String, @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String,
                             @Query("pipe_no") pipe_no: String) : Call<ResponseBody>



    @Headers("Accept: application/json")
    @GET("V1/check/aeration/data")
    fun aeriationPloygonList(@Header( "Authorization") token: String, @Query("farmer_plot_uniqueid") farmer_plot_uniqueid: String,
                             @Query("pipe_no") pipe_no: String, @Query("aeration_no") aeration_no: String) : Call<ResponseBody>



    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/store/aeration")
    fun sendAeriationData(@Header( "Authorization") token: String, @Part pipe_installation_id: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part,
                 @Part farmer_plot_uniqueid: MultipartBody.Part, @Part plot_no: MultipartBody.Part,  @Part aeration_no: MultipartBody.Part,
                 @Part pipe_no: MultipartBody.Part): Call<ResponseBody>



    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/store/aeration/image")
    fun sendAeriationImage(@Header( "Authorization") token: String, @Part pipe_installation_id: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part,
                          @Part farmer_plot_uniqueid: MultipartBody.Part, @Part plot_no: MultipartBody.Part,  @Part aeration_no: MultipartBody.Part,
                          @Part pipe_no: MultipartBody.Part, @Part image: MultipartBody.Part): Call<ResponseBody>


    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/update/aeration/image")
    fun sendAeriationReportImage(@Header( "Authorization") token: String, @Part pipe_installation_id: MultipartBody.Part, @Part farmer_uniqueId: MultipartBody.Part,
                           @Part farmer_plot_uniqueid: MultipartBody.Part, @Part plot_no: MultipartBody.Part,  @Part aeration_no: MultipartBody.Part,
                           @Part pipe_no: MultipartBody.Part, @Part surveyImage: List<MultipartBody.Part>): Call<ResponseBody>


    @GET("2.5/weather/")
    fun weatherResponse(@Query("lat") lat: String, @Query("lon") lon: String, @Query("appid") appid: String): Call<WeatherResponseModel>

    @GET("2.5/forecast")
    fun weatherForecastResponse(@Query("lat") lat: String, @Query("lon") lon: String, @Query("appid") appid: String): Call<WeatherForecastModel>




    /*** Add More Plots Screen*/
    @Headers("Accept: application/json")
    @GET("V1/search/surveyid/list")
    fun getExistingPlotDetails(@Header( "Authorization") token: String,@Query("data") data: String) :Call<ResponseBody>

}