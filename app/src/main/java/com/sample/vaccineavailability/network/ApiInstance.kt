package com.sample.vaccineavailability.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiInstance {
    private const val BASE_URL = "https://cdn-api.co-vin.in/api/v2/"

    private val clientBuilder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addNetworkInterceptor(StethoInterceptor())


    private fun retrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .baseUrl(BASE_URL)
            .build()
    }

    val checkVaccineAvailabilityService: SessionService by lazy {
        retrofit().create(SessionService::class.java)
    }
}