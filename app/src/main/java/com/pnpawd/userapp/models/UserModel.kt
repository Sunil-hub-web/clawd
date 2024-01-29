package com.pnpawd.userapp.models

data class UserModel (
    val name: String,
    val mobile: String,
    val username: String,
    val password: String,
    val company_code: String,
    val email: String,
    val versionCode: String,
    val versionName: String,
    val release: Double,
    val deviceName: String,
    val deviceManufacturer: String,
    val state_id: String,
    val state: String,
    val fcm_token:String
)