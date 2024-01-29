package com.pnpawd.userapp.models

data class ExistingPlotsDetailsModel(
    val success: Boolean,
    val survey_id: SurveyDetails?,
    val plots: List<PlotDetails>,
    val latest_plot: String?
)

data class SurveyDetails(
    val id: Int,
    val farmer_uniqueId: Int,
    val aadhaar: String,
    val farmer_survey_id: String,
    val farmer_name: String,
    val mobile_access: String,
    val mobile_reln_owner: String,
    val mobile: Long,
    val gender: String?,
    val guardian_name: String,
    val country_id: Int,
    val country: String,
    val state_id: Int,
    val state: String,
    val district_id: Int,
    val district: String,
    val taluka_id: Int,
    val taluka: String,
    val panchayat_id: Int?,
    val panchayat: String?,
    val village_id: Int?,
    val village: String?,
)

data class PlotDetails(
    val farmer_uniqueId: Int,
    val plot_no: String
)
