package com.sample.vaccineavailability.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//todo: rename to something good maybe.
interface SessionService {
    @GET("appointment/sessions/public/calendarByDistrict")
    fun listAvailableCentersByDist(@Query("district_id") distId: Int,
                             @Query("date") date: String): Call<Centers>

    @GET("appointment/sessions/public/calendarByPin")
    fun listAvailableCentersByPin(@Query("pincode") pincode: String,
                             @Query("date") date: String): Call<Centers>

    @GET("admin/location/states")
    fun getStates(): Call<RespStates>

    @GET("admin/location/districts/{stateId}")
    fun getDistrictsByStates(@Path("stateId") stateId: Int): Call<RespDistrict>
}