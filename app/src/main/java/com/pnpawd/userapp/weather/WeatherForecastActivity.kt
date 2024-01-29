package com.pnpawd.userapp.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.adapters.WeatherForecastAdapter
import com.squareup.picasso.Picasso

class WeatherForecastActivity : AppCompatActivity() {
    private var description: String? = null
    private var temperature: String? = null
    private var longitude: String? = null
    private var windSpeed: String? = null
    private var latitude: String? = null
    private var iconUrl: String? = null
    private var humidity: String? = null
    private var appId: String? = null
    private var sunset: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var currentWeatherImg: ImageView
    private lateinit var txtTemperature: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtWindSpeed: TextView
    private lateinit var txtHumidity: TextView
    private lateinit var txtSunset: TextView

    private lateinit var viewModel: WeatherForecastViewModel
    private lateinit var weatherForecastAdapter: WeatherForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_forecast)

        val bundle = intent.extras
        if (bundle != null) {
            latitude = bundle.getString("latitude")
            longitude = bundle.getString("longitude")
            temperature = bundle.getString("current_temperature")
            iconUrl = bundle.getString("icon")
            appId = bundle.getString("appId")
            description = bundle.getString("description")
            windSpeed = bundle.getString("wind_speed")
            humidity = bundle.getString("humidity")
            sunset = bundle.getString("sunset")

            iconUrl?.let { Log.e("iconUrl", it) }
        }

        viewModel = ViewModelProviders.of(this)[WeatherForecastViewModel::class.java]

        recyclerView = findViewById(R.id.weather_forecast)
        txtTemperature = findViewById(R.id.forecast_temperature)
        currentWeatherImg = findViewById(R.id.weather_img)
        txtDescription = findViewById(R.id.forecast_description)
        txtWindSpeed = findViewById(R.id.forecast_wind_speed)
        txtHumidity = findViewById(R.id.forecast_humidity)
        txtSunset = findViewById(R.id.forecast_sunset)

        iconUrl.let {
            Picasso.get().load(iconUrl).into(currentWeatherImg)

        }

        temperature.let {
            txtTemperature.text = temperature
        }

        description.let {
            txtDescription.text = description?.uppercase()
        }

        windSpeed.let {
            txtWindSpeed.text = windSpeed
        }

        humidity.let {
            txtHumidity.text = humidity
        }

        sunset.let {
            txtSunset.text = sunset
        }

        prepareRecyclerView()
        weatherAPICall()
    }

    private fun prepareRecyclerView() {
        weatherForecastAdapter = WeatherForecastAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@WeatherForecastActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = weatherForecastAdapter
        }
    }

    private fun weatherAPICall() {
        latitude?.let {
            longitude?.let { it1 ->
                appId?.let { it2 ->
                    viewModel.getWeatherForecast(
                        it,
                        it1,
                        it2
                    )
                }
            }
        }

        viewModel.observeWeatherForecast()
            .observe(this@WeatherForecastActivity) { weatherForecast ->
                weatherForecastAdapter.setForecast(weatherForecast)
            }
    }

}