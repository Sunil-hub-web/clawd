package com.pnpawd.userapp.models

data class FarmerLocationModel(
    val farmer_id: String,
    val farmer_uniqueId: String,
    val country: String,
    val state_id: String,
    val district_id: String,
    val taluka_id: String,
    val panchayat_id: String,
    val village_id: String,
    val remarks: String,
    val latitude: String,
    val longitude: String
)