package com.application.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_ID = 42

    var location: Location? = null

    private lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.GONE

        println("App started")

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocation()

        val activity = this

        findViewById<EditText>(R.id.search).addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                println("a: $s")

                FetchWeatherSearch(s.toString(), activity::apply, activity::exception).execute()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        findViewById<Button>(R.id.gps).setOnClickListener {
            findViewById<EditText>(R.id.search).setText("")
            reload()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&

            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            LOCATION_PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            location = locationResult.lastLocation
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val request = LocationRequest()
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request.interval = 0
        request.fastestInterval = 0
        request.numUpdates = 1

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.requestLocationUpdates(
            request, locationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (!checkPermissions()) {
            exception("We need permission to your location")

            requestPermissions()
        }

        if (!isLocationEnabled()) {
            exception("Turn on location")
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        locationClient.lastLocation.addOnCompleteListener(this) {
            requestNewLocationData()

            reload()
        }
    }

    private fun reload() {
        if (location == null) {
            getLocation()
            return
        }

        Handler().postDelayed(
            {
                FetchWeather(location!!, this::apply, this::exception).execute()
            },
            1000
        )
    }

    private fun exception(error: String) {
        println("ERROR:: $error")
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    private fun apply(weather: Weather) {
        findViewById<TextView>(R.id.address).text = weather.address
        findViewById<TextView>(R.id.updated_at).text = weather.updatedAtText
        findViewById<TextView>(R.id.status).text = weather.weatherDescription.capitalize()
        findViewById<TextView>(R.id.temp).text = weather.temp
        findViewById<TextView>(R.id.temp_min).text = weather.tempMin
        findViewById<TextView>(R.id.temp_max).text = weather.tempMax
        findViewById<TextView>(R.id.sunrise).text =
            SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(weather.sunrise * 1000))
        findViewById<TextView>(R.id.sunset).text =
            SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(weather.sunset * 1000))
        findViewById<TextView>(R.id.wind).text = weather.windSpeed
        findViewById<TextView>(R.id.pressure).text = weather.pressure
        findViewById<TextView>(R.id.humidity).text = weather.humidity

        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
    }
}
