package com.pnpawd.userapp.models

class CropReportModel(plot_no: String, farmer_uniqueId: String, area_in_acers: String, season: String) {
    private var plot_no: String
    private var farmer_uniqueId: String
    private var area_in_acers: String
    private var season: String

    init {
        this.plot_no = plot_no
        this.farmer_uniqueId = farmer_uniqueId
        this.area_in_acers = area_in_acers
        this.season = season
    }

    fun getPlotNo(): String {
        return plot_no
    }

    fun setPlotNo(plot_no: String) {
        this.plot_no = plot_no
    }

    fun getFarmerId(): String {
        return farmer_uniqueId
    }

    fun setFarmerId(farmer_uniqueId: String) {
        this.farmer_uniqueId = farmer_uniqueId
    }

    fun getArea(): String {
        return area_in_acers
    }

    fun setArea(area_in_acers: String) {
        this.area_in_acers = area_in_acers
    }

    fun getSeason(): String {
        return season
    }

    fun setSeason(season: String) {
        this.season = season
    }

}