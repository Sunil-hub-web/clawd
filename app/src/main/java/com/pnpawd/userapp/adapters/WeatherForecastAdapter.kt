package com.pnpawd.userapp.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.pnpawd.userapp.R
import com.pnpawd.userapp.models.ListElement
import com.pnpawd.userapp.models.WeatherForecastModel
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class WeatherForecastAdapter : RecyclerView.Adapter<WeatherForecastAdapter.ViewHolder>()  {
    private var weatherForecastList = ArrayList<ListElement>()

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view){
        var date: TextView = view.findViewById(R.id.forecast_date)
        var time: TextView = view.findViewById(R.id.forecast_time)
        var weatherIMG: ImageView = view.findViewById(R.id.forecast_image)
        var temperature: TextView = view.findViewById(R.id.weather_forecast_temperature)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.weather_forecast_list, parent, false)
        return ViewHolder(itemView)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = weatherForecastList[position]

        val date = model.dt_txt
        date.let {
            val formattedDate =  getDate(date)
            holder.date.text = formattedDate

            val formattedTime = getTime(date)
            holder.time.text = formattedTime
        }


        val iconUrl = "http://openweathermap.org/img/wn/${model.weather[0].icon}@4x.png"
        Picasso.get().load(iconUrl).into((holder.weatherIMG))

        val kelvin = model.main.temp
        val temperature = kelvin - 273.15
        holder.temperature.text = DecimalFormat("##").format(temperature)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(time: String): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(time, dateTimeFormatter)
        return dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(date: String): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDate.parse(date, dateTimeFormatter)
        val dayOfMonth = dateTime.dayOfMonth
        val month = dateTime.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        return "${dayOfMonth}${getOrdinalSuffix(dayOfMonth)} $month"
    }


    fun getOrdinalSuffix(day: Int): String {
        return when (day) {
            1, 21, 31 -> "st"
            2, 22 -> "nd"
            3, 23 -> "rd"
            else -> "th"
        }
    }



    override fun getItemCount(): Int {
        return weatherForecastList.size
    }


    fun setForecast(forecastList: WeatherForecastModel?) {
        if (forecastList != null) {
            this.weatherForecastList = forecastList.list as ArrayList<ListElement>
            notifyDataSetChanged()
        }

    }
}