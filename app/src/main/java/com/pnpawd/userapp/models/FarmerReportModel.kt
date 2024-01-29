package com.pnpawd.userapp.models

class FarmerReportModel(id: String, farmer_uniqueId: String, date_survey: String, time_survey: String) {
    private var id: String
    private var farmer_uniqueId: String
    private var date_survey: String
    private var time_survey: String

    init {
        this.id = id
        this.farmer_uniqueId = farmer_uniqueId
        this.date_survey = date_survey
        this.time_survey = time_survey
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

    fun setFarmerId(farmer_uniqueId: String) {
        this.farmer_uniqueId = farmer_uniqueId
    }

    fun getDate(): String {
        return date_survey
    }

    fun setDate(date_survey: String) {
        this.date_survey = date_survey
    }

    fun getTime(): String {
        return time_survey
    }

    fun setTime(time_survey: String) {
        this.time_survey = time_survey
    }

}