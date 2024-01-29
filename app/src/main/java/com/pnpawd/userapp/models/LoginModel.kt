package com.pnpawd.userapp.models

data class LoginModel(
    val mobile: String,
    val password: String,
    val versionCode: String,
    val versionName: String,
    val release: Double,
    val deviceName: String,
    val deviceManufacturer: String,
    val device_id: String,
    val fcm_token:String
)