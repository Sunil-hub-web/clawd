package com.pnpawd.userapp.models

data class BenefitModel(
    val farmer_id: String,
    val farmer_uniqueId: String,
    val plot_no: String,
    val seasons: String,
    val benefit: String,
    val total_plot_area: String,
    val benefit_id: String
)
