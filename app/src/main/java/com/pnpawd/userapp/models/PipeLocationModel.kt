package com.pnpawd.userapp.models

data class PipeLocationModel(
    val farmer_id: String,
    val farmer_uniqueId: String,
    val plot_no: String,
    val latitude: String,
    val longitude: String,
    val state: String,
    val district: String,
    val taluka: String,
    val village: String,
    val acers_units: String,
    val plot_area: String,
    val ranges: ArrayList<LocationModel>,
    val farmer_plot_uniqueid: String,
    val polygon_date_time: String
)
