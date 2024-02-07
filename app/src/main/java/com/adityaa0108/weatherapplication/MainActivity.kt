package com.adityaa0108.weatherapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.adityaa0108.weatherapplication.POJO.WeatherApp
import com.adityaa0108.weatherapplication.databinding.ActivityMainBinding
import com.adityaa0108.weatherapplication.utilities.ApiInterface
import com.adityaa0108.weatherapplication.utilities.ApiUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

   //05e0b30d193515b51bed487db08e24e0
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val binding:ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)

   }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            binding.mainLayout.visibility = View.GONE
        getCurrentLocation()
        searchCity()
    }


    private fun getCurrentLocation(){
        //check whether the permission to use the device location is granted or not to the app
        if(checkPermissions()){
            if(isLocationEnabled()){ //check whether the device location is enabled or not
                //final Latitude and longitude code here
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                   requestPermissions()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){
                    task->
                    val location:Location? = task.result
                    if(location == null){
                             Toast.makeText(this,"Location not Received",Toast.LENGTH_SHORT).show()
                    }
                    else{
                       //fetch weather here
                              fetchWeatherData(location.latitude.toString(),location.longitude.toString())
                    }
                }

            }
            else{
                //settings open here
                Toast.makeText(this,"Turn on Location",Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }
        }
        else {
            //request permission here if not granted
            requestPermissions()


        }
    }

    private fun isLocationEnabled():Boolean{
        //this function is used to check whether the device location is enabled or not, it will return true if enabled
        //else it will return false
        val locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        const val API_KEY = "05e0b30d193515b51bed487db08e24e0"
    }
    private fun checkPermissions():Boolean{
        //this function will check whether the app has all permission access to use the device location
        //if permission is granted to the device then it return true else false
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                return true
            }
            return false
        }
        private fun requestPermissions(){
            //if app has not granted the permission to use the device location then permission is granted
            //in below code
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.
            ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_LOCATION)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            //After granting of the permission , its result is displayed to the user
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"Granted",Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
            else{
                Toast.makeText(applicationContext,"Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun searchCity(){
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchCityWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
               return true
            }

        })
    }

    private fun fetchWeatherData(latitude:String,longitude:String){
        binding.progressBar.visibility = View.VISIBLE
        val flag = true
        ApiUtilities.getApiInterface()?.getWeatherData(latitude,longitude, API_KEY)?.enqueue(object :
        Callback<WeatherApp>{
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                  if(response.isSuccessful && responseBody != null){
                      binding.mainLayout.visibility = View.VISIBLE
                      setDataOnViews(responseBody,flag)
                  }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(applicationContext,"Error",Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun setDataOnViews(responseBody:WeatherApp,flag:Boolean){
        val temperature = responseBody.main.temp.toString()
        val humidity = responseBody.main.humidity
        val windSpeed = responseBody.wind.speed
        val sunRise = responseBody.sys.sunrise.toLong()
        val sunSet = responseBody.sys.sunset.toLong()
        val seaLevel = responseBody.main.pressure
        val condition = responseBody.weather.firstOrNull()?.main?:"unknown"

        // Log.d("TAG", "onResponse: $temperature")
        if(flag == true) {
            binding.temperature.text = "${kelvinToCelsius(temperature.toDouble())} °C"
        }else{
            binding.temperature.text = "$temperature °C"
        }
        binding.humidity.text = "$humidity %"
        binding.windSpeed.text = "$windSpeed m/s"
        binding.condition.text = condition
        binding.sunrise.text = "${time(sunRise)}"
        binding.sunset.text = "${time(sunSet)}"
        binding.sea.text = "$seaLevel hPA"
        binding.weather.text = condition
        binding.date.text = date()
        binding.cityName.text = responseBody.name
    }


    private fun fetchCityWeatherData(cityName:String){
        //loader visible
       val flag = false
       ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, API_KEY,"metric")?.
       enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>){
               val responseBody = response.body()
                if(response.isSuccessful && responseBody!=null){
                             setDataOnViews(responseBody,flag)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(applicationContext,"Not a Valid CityName",Toast.LENGTH_SHORT).show()
            }

        })


    }
    private fun date():String{
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timestamp: Long):String{
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }
    private fun kelvinToCelsius(temp:Double):Double{
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()
    }


}


