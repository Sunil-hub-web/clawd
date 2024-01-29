package com.pnpawd.userapp.models

data class FarmerInfoModel(
    val farmer_name: String,
    val mobile_access: String,
    val mobile_reln_owner: String,
    val mobile: Long,
    val farmer_uniqueId: String,
    val no_of_plots: String,
    var plot_detail: ArrayList<PlotDetailsModel>,
    val gender: String,
    val guardian_name: String,
    val organization_id: Int,
    val aadhar: String
)

data class FarmerInfoModelNew(
    val farmer_name: String,
    val mobile_access: String,
    val mobile_reln_owner: String,
    val mobile: Long,
    val farmer_uniqueId: String,
    val farmer_survey_id: String,
    var plot_detail: ArrayList<PlotDetailsModel>,
    val gender: String,
    val guardian_name: String,
    val organization_id: Int,
    val aadhar: String
)