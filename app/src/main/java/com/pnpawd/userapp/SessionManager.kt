package com.pnpawd.userapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(val context: Context) {

    var PRIVATE_MODE = 0
    private val PREF_NAME = "sharedcheckLogin"

    var sharedprefernce: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    var editor: SharedPreferences.Editor = sharedprefernce.edit()

    //var context: Context? = null


    private val AADHARIMAGE = "aadharimage"
    private val fRAMERiMAGE = "framerimage"
    private val OTHERIMAGE = "otherimage"

    private val AERATIONIMAGE1 = "aerationimage1"
    private val AERATIONIMAGE2 = "areationimage2"

    private val PLOATIMAGE = "ploatimage"
    private val EXISTINGPLOATIMAGE = "existingploatimage"

    private val LANDINFOIMAGE = "landInfoimage"

    private val FARMERBENEFIT1 = "farmerbenefit1"
    private val FARMERBENEFIT2 = "farmerbenefit2"



//    fun SessionManager(context: Context) {
//        this.context = context
//        sharedprefernce = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
//        editor = sharedprefernce.edit()
//    }

    fun setFARMERBENEFIT1(farmerbenefit1: String?) {
        editor.putString(FARMERBENEFIT1, farmerbenefit1)
        editor.commit()
    }

    fun getFARMERBENEFIT1(): String? {
        return sharedprefernce.getString(FARMERBENEFIT1, "DEFAULT")
    }

    fun setFARMERBENEFIT2(farmerbenefit2: String?) {
        editor.putString(FARMERBENEFIT2, farmerbenefit2)
        editor.commit()
    }

    fun getFARMERBENEFIT2(): String? {
        return sharedprefernce.getString(FARMERBENEFIT2, "DEFAULT")
    }

    fun setLANDINFOIMAGE(landinfoimage: String?) {
        editor.putString(LANDINFOIMAGE, landinfoimage)
        editor.commit()
    }

    fun getLANDINFOIMAGE(): String? {
        return sharedprefernce.getString(LANDINFOIMAGE, "DEFAULT")
    }

    fun setEXISTINGPLOATIMAGE(existingploatimage: String?) {
        editor.putString(EXISTINGPLOATIMAGE, existingploatimage)
        editor.commit()
    }

    fun getEXISTINGPLOATIMAGE(): String? {
        return sharedprefernce.getString(EXISTINGPLOATIMAGE, "DEFAULT")
    }


    fun setPLOATIMAGE(ploatimage: String?) {
        editor.putString(PLOATIMAGE, ploatimage)
        editor.commit()
    }

    fun getPLOATIMAGE(): String? {
        return sharedprefernce.getString(PLOATIMAGE, "DEFAULT")
    }

    fun setAERATIONIMAGE1(areationimage1: String?) {
        editor.putString(AERATIONIMAGE1, areationimage1)
        editor.commit()
    }

    fun getAERATIONIMAGE1(): String? {
        return sharedprefernce.getString(AERATIONIMAGE1, "DEFAULT")
    }

    fun setAERATIONIMAGE2(areationimage2: String?) {
        editor.putString(AERATIONIMAGE2, areationimage2)
        editor.commit()
    }

    fun getAERATIONIMAGE2(): String? {
        return sharedprefernce.getString(AERATIONIMAGE2, "DEFAULT")
    }

    fun setAADHARIMAGE(aadherImage: String?) {
        editor.putString(AADHARIMAGE, aadherImage)
        editor.commit()
    }

    fun getAADHARIMAGE(): String? {
        return sharedprefernce.getString(AADHARIMAGE, "DEFAULT")
    }

    fun setfRAMERiMAGE(framerImage: String?) {
        editor.putString(fRAMERiMAGE, framerImage)
        editor.commit()
    }

    fun getfRAMERiMAGE(): String? {
        return sharedprefernce.getString(fRAMERiMAGE, "DEFAULT")
    }

    fun setOTHERIMAGE(otherImage: String?) {
        editor.putString(OTHERIMAGE, otherImage)
        editor.commit()
    }

    fun getOTHERIMAGE(): String? {
        return sharedprefernce.getString(OTHERIMAGE, "DEFAULT")
    }

    fun logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear()
        editor.commit()
    }
}