package com.pnpawd.userapp.models

class AeriationReportModel(id: String, farmer_uniqueId: String, pipe_no: String, farmer_plot_uniqueid: String, pipe_installation_id: String, aeration_no: String, plot_no: String, farmer_name : String,
        reasons: String) {
    private var id: String
    private var farmer_uniqueId: String
    private var pipe_no: String
    private var farmer_plot_uniqueid: String
    private var pipe_installation_id: String
    private var aeration_no: String
    private var plot_no: String
    private var farmer_name: String
    private var reasons: String

    init {
        this.id = id
        this.farmer_uniqueId = farmer_uniqueId
        this.pipe_no = pipe_no
        this.farmer_plot_uniqueid = farmer_plot_uniqueid
        this.pipe_installation_id = pipe_installation_id
        this.aeration_no = aeration_no
        this.plot_no = plot_no
        this.farmer_name = farmer_name
        this.reasons = reasons
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

    fun getPipeNo(): String {
        return pipe_no
    }

    fun setPipeNo(pipe_no: String) {
        this.pipe_no = pipe_no
    }

    fun getFarmerPlotUniqueId(): String {
        return farmer_plot_uniqueid
    }

    fun setFarmerPlotUniqueId(farmer_plot_uniqueid: String) {
        this.farmer_plot_uniqueid = farmer_plot_uniqueid
    }

    fun getPipeInstallationId(): String {
        return pipe_installation_id
    }

    fun setPipeInstallationId(pipe_installation_id: String) {
        this.pipe_installation_id = pipe_installation_id
    }

    fun getAerationNo(): String {
        return aeration_no
    }

    fun setAerationNo(aeration_no: String) {
        this.aeration_no = aeration_no
    }


    fun getPlotNo(): String {
        return plot_no
    }

    fun setPlotNo(plot_no: String) {
        this.plot_no = plot_no
    }

    fun getFarmerName(): String {
        return farmer_name
    }

    fun setFarmerName(farmer_name: String) {
        this.farmer_name = farmer_name
    }

    fun getReason(): String {
        return reasons
    }

    fun setReason(reasons: String) {
        this.reasons = reasons
    }

}