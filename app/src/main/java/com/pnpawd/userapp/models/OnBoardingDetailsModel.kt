package com.pnpawd.userapp.models

class OnBoardingDetailsModel (plot_no: String, area_in_acers: String, land_ownership: String, actual_owner_name: String, survey_no: String, status: String, unique_id: String, base_vale: Double) {
    private var plot_no: String
    private var area_in_acers: String
    private var land_ownership: String
    private var actual_owner_name: String
    private var survey_no: String
    private var status: String
    private var unique_id: String
    private var base_vale: Double

    init {
        this.plot_no = plot_no
        this.area_in_acers = area_in_acers
        this.land_ownership = land_ownership
        this.actual_owner_name = actual_owner_name
        this.survey_no = survey_no
        this.status = status
        this.unique_id = unique_id
        this.base_vale = base_vale
    }

    fun getPlotNo(): String {
        return plot_no
    }

    fun setPlotNo(plot_no: String) {
        this.plot_no = plot_no
    }

    fun getArea(): String {
        return area_in_acers
    }

    fun setArea(area_in_acers: String) {
        this.area_in_acers = area_in_acers
    }

    fun getOwnership(): String {
        return land_ownership
    }

    fun setOwnership(land_ownership: String) {
        this.land_ownership = land_ownership
    }

    fun getOwnerName(): String {
        return actual_owner_name
    }

    fun setOwnerName(actual_owner_name: String) {
        this.actual_owner_name = actual_owner_name
    }

    fun getSurveyNo(): String {
        return survey_no
    }

    fun setSurveyNo(survey_no: String) {
        this.survey_no = survey_no
    }

    fun getStatus(): String {
        return status
    }

    fun setStstus(status: String) {
        this.status = status
    }



    fun getUniqueID(): String {
        return unique_id
    }

    fun setUniqueID(unique_id: String) {
        this.unique_id = unique_id
    }


    fun getBaseValue(): Double {
        return base_vale
    }

    fun setBaseValue(base_vale: Double) {
        this.base_vale = base_vale
    }

}