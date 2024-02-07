package com.adityaa0108.weatherapplication.utilities

import com.adityaa0108.weatherapplication.POJO.WeatherApp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun getCityWeatherData(
        @Query("q") city:String,
        @Query("appid") api_key:String,
        @Query("units") units:String,

    ): Call<WeatherApp>

    @GET("weather")
    fun getWeatherData(
        @Query("lat") latitude:String,
        @Query("lon") longitude:String,
        @Query("appid") api_key: String,
    ):Call<WeatherApp>

}