package com.pnpawd.userapp.models

class PlotDetailsModel  (area_in_hectare: String, sr: String, area_in_other: String, area_in_other_unit: String) {
    private var area_in_hectare: String
    private var sr: String
    private var area_in_other: String
    private var area_in_other_unit: String

    init {
        this.area_in_hectare = area_in_hectare
        this.sr = sr
        this.area_in_other = area_in_other
        this.area_in_other_unit = area_in_other_unit
    }

    fun getAreaInHectare(): String {
        return area_in_hectare
    }

    fun setAreaInHectare(area_in_hectare: String) {
        this.area_in_hectare = area_in_hectare
    }

    fun getSr(): String {
        return sr
    }

    fun setSr(sr: String) {
        this.sr = sr
    }

    fun getAreaInOther(): String {
        return area_in_other
    }

    fun setAreaInOther(area_in_other: String) {
        this.area_in_other = area_in_other
    }

    fun getAreaInOtherUnit(): String {
        return area_in_other_unit
    }

    fun setAreaInOtherUnit(area_in_other_unit: String) {
        this.area_in_other_unit = area_in_other_unit
    }
}