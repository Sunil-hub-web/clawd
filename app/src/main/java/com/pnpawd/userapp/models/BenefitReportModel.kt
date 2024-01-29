package com.pnpawd.userapp.models

class BenefitReportModel(total_plot_area: String, farmer_uniqueId: String, benefit: String, seasons: String) {
    private var total_plot_area: String
    private var farmer_uniqueId: String
    private var benefit: String
    private var seasons: String

    init {
        this.total_plot_area = total_plot_area
        this.farmer_uniqueId = farmer_uniqueId
        this.benefit = benefit
        this.seasons = seasons
    }

    fun getTotalPlot(): String {
        return total_plot_area
    }

    fun setTotalPlot(total_plot_area: String) {
        this.total_plot_area = total_plot_area
    }

    fun getFarmerId(): String {
        return farmer_uniqueId
    }

    fun setFarmerId(farmer_uniqueId: String) {
        this.farmer_uniqueId = farmer_uniqueId
    }

    fun getBenefit(): String {
        return benefit
    }

    fun setBenefit(benefit: String) {
        this.benefit = benefit
    }

    fun getSeason(): String {
        return seasons
    }

    fun setSeason(seasons: String) {
        this.seasons = seasons
    }

}