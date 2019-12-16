package com.application.weather

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class Weather(val result: String) {

    val jsonObj = JSONObject(result)
    val main = jsonObj.getJSONObject("main")
    val sys = jsonObj.getJSONObject("sys")
    val wind = jsonObj.getJSONObject("wind")
    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

    val updatedAt: Long = jsonObj.getLong("dt")
    val updatedAtText = "Updated at: " + SimpleDateFormat(
        "dd/MM/yyyy hh:mm a",
        Locale.ENGLISH
    ).format(Date(updatedAt * 1000))
    val temp = main.getString("temp") + "°C"
    val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
    val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
    val pressure = main.getString("pressure")
    val humidity = main.getString("humidity")
    val sunrise: Long = sys.getLong("sunrise")
    val sunset: Long = sys.getLong("sunset")
    val windSpeed = wind.getString("speed")
    val weatherDescription = weather.getString("description")
    val address = jsonObj.getString("name") + ", " + sys.getString("country")
}
