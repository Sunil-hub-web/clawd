package com.pnpawd.userapp.models

import android.util.Log

class PipeReportModel (id: String, farmer_uniqueId: String, lat: String, farmer_plot_uniqueid: String, lng: String, plot_no: String, pipe_no: String, distance: String, farmer_name: String,
    reason: String, reason_id: String, area_in_acers: String,state :String,district:String,taluka:String,vilageName :String,aadharNum :String, mobileNum :String) {
    private var id: String
    private var farmer_uniqueId: String
    private var lat: String
    private var farmer_plot_uniqueid: String
    private var lng: String
    private var plot_no: String
    private var pipe_no: String
    private var distance: String
    private var farmer_name: String
    private var reason: String
    private var reason_id: String
    private var area_in_acers: String
    private var state: String
    private var district: String
    private var taluka: String
    private var vilageName: String
    private var aadharNum: String
    private var mobileNum: String

    init {
        this.id = id
        this.farmer_uniqueId = farmer_uniqueId
        this.lat = lat
        this.farmer_plot_uniqueid = farmer_plot_uniqueid
        this.lng = lng
        this.plot_no = plot_no
        this.pipe_no = pipe_no
        this.distance = distance
        this.farmer_name = farmer_name
        this.reason = reason
        this.reason_id = reason_id
        this.area_in_acers = area_in_acers
        this.state = state
        this.district = district
        this.taluka = taluka
        this.vilageName = vilageName
        this.aadharNum = aadharNum
        this.mobileNum = mobileNum
    }

    fun getId(): String {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getFarmerId(): String {
        return farmer_uniqueId
    }

    fun getState(): String {
        return state
    }
    fun getDistrict(): String {
        return district
    }
    fun getTaluka(): String {
        return taluka
    }

    fun getVillage(): String {
        return vilageName
    }

    fun getAadhar(): String {
        return aadharNum
    }

    fun getMobile(): String {
        return mobileNum
    }
    fun setFarmerId(farmer_uniqueId: String) {
        this.farmer_uniqueId = farmer_uniqueId
    }

    fun getLat(): String {
        return lat
    }

    fun setLat(lat: String) {
        this.lat = lat
    }

    fun getLng(): String {
        return lng
    }

    fun setLng(lng: String) {
        this.lng = lng
    }

    fun getFarmerPlotUniqueId(): String {
        return farmer_plot_uniqueid
    }

    fun setFarmerPlotUniqueId(farmer_plot_uniqueid: String) {
        this.farmer_plot_uniqueid = farmer_plot_uniqueid
    }

    fun getPlotNo(): String {
        return plot_no
    }

    fun setPlotNo(plot_no: String) {
        this.plot_no = plot_no
    }

    fun getPipeNo(): String {
        return pipe_no
    }

    fun setPipeNo(pipe_no: String) {
        this.pipe_no = pipe_no
    }

    fun getDistance(): String {
        return distance
    }

    fun setDistance(distance: String) {
        this.distance = distance
    }

    fun getFarmerName(): String {
        return farmer_name
    }

    fun getFarmerFirstName(): String {
        val separate = farmer_name.split(" ".toRegex())
        var firstName = separate[0]
        Log.i("NAMEE","NAME $firstName")
        return firstName
    }

    fun setFarmerName(farmer_name: String) {
        this.farmer_name = farmer_name
    }

    fun getReason(): String {
        return reason
    }

    fun setReason(reason: String) {
        this.reason = reason
    }

    fun getReasonID(): String {
        return reason_id
    }

    fun setReasonID(reason_id: String) {
        this.reason_id = reason_id
    }

    fun getAreaInAcers(): String {
        return area_in_acers
    }

    fun setAreaInAcers(area_in_acers: String) {
        this.area_in_acers = area_in_acers
    }

}