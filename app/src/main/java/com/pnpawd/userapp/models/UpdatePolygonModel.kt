package com.pnpawd.userapp.models

data class UpdatePolygonModel (
    val farmer_plot_uniqueid: String,
    val updated_poly_area: Double,
    val polygon_date_time: String,
    val updated_polygon: ArrayList<LocationModel>,
    )