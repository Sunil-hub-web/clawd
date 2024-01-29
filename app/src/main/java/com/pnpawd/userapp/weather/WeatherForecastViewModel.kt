package com.pnpawd.userapp.weather

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pnpawd.userapp.models.WeatherForecastModel
import com.pnpawd.userapp.network.ApiInterface
import com.pnpawd.userapp.network.WeatherApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherForecastViewModel : ViewModel() {
    val weatherForecast = MutableLiveData<WeatherForecastModel>()

    fun getWeatherForecast(lat: String, lng: String, appID: String){
        val retIn = WeatherApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.weatherForecastResponse(lat, lng, appID).enqueue(object : Callback<WeatherForecastModel> {
            override fun onResponse(call: Call<WeatherForecastModel>, response: Response<WeatherForecastModel>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        try {
                            weatherForecast.value = response.body()
                        }
                        catch (e: Exception){
                            Log.e("weather_exception", e.toString())
                        }

                    }
                }
            }
            override fun onFailure(call: Call<WeatherForecastModel>, t: Throwable) {
//                Toast.makeText(this@DashboardActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun observeWeatherForecast(): LiveData<WeatherForecastModel> {
        return weatherForecast
    }

}