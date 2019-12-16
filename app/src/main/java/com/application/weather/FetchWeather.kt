package com.application.weather

import android.location.Location
import android.os.AsyncTask
import java.net.URL

class FetchWeather(val location: Location, val apply: (w: Weather) -> Unit, val exception: (e: String) -> Unit) :
    AsyncTask<String, Void, String>() {

    private val API: String = "be0186378e46cf7864c6a5c6d9cd80d2"

    override fun doInBackground(vararg params: String?): String? {
        var response: String?
        try {
            response =
                URL("https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
        } catch (e: Exception) {
            response = null
        }
        return response
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result == null) {
            exception("Unable to load weather")
            return
        }

        apply(Weather(result));
    }
}