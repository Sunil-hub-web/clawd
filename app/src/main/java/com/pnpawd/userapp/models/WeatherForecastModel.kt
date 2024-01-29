package com.pnpawd.userapp.models

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class WeatherForecastModel (
    val cod: String,
    val message: Long,
    val cnt: Long,
    val list: List<ListElement>,
    val city: City
)

@Serializable
data class City (
    val id: Long,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Long,
    val timezone: Long,
    val sunrise: Long,
    val sunset: Long
)

@Serializable
data class Coord (
    val lat: Double,
    val lon: Double
)

@Serializable
data class ListElement (
    val dt: Long,
    val main: MainClass,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Long,
    val pop: Double,
    val rain: Rain,
    val sys: Sys,
    val dt_txt: String
)

@Serializable
data class Clouds (
    val all: Long
)

@Serializable
data class MainClass (
    val temp: Double,

    @SerialName("feels_like")
    val feelsLike: Double,

    @SerialName("temp_min")
    val tempMin: Double,

    @SerialName("temp_max")
    val tempMax: Double,

    val pressure: Long,

    @SerialName("sea_level")
    val seaLevel: Long,

    @SerialName("grnd_level")
    val grndLevel: Long,

    val humidity: Long,

    @SerialName("temp_kf")
    val tempKf: Double
)

@Serializable
data class Rain (
    @SerialName("3h")
    val the3H: Double
)

@Serializable
data class Sys (
    val pod: Pod
)

@Serializable
enum class Pod(val value: String) {
    @SerialName("d") D("d"),
    @SerialName("n") N("n");
}

@Serializable
data class Weather (
    val id: Long,
    val main: String,
    val description: String,
    val icon: String
)


@Serializable
data class Wind (
    val speed: Double,
    val deg: Long,
    val gust: Double
)
