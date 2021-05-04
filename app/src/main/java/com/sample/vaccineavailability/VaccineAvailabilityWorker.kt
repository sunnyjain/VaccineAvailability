package com.sample.vaccineavailability

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sample.vaccineavailability.network.ApiInstance
import com.sample.vaccineavailability.network.Center
import com.sample.vaccineavailability.network.Centers
import com.sample.vaccineavailability.util.NotificatonHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VaccineAvailabilityWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        return checkAvailability()

    }

    private fun checkAvailability(): Result {
        try {
            val list = ArrayList<Center>()
            val sharedPref = appContext.getSharedPreferences("Info", Context.MODE_PRIVATE)

            val distId = if (sharedPref.contains("districtId")) sharedPref.getInt("districtId", -1)
            else -1
            val pincode = if (sharedPref.contains("pincode")) sharedPref.getString("pincode", "")
            else ""

            val minAgeLimit = sharedPref.getInt("minAgeLimit", -1)

            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            for (i in 1..11) {
                val response = if (pincode != null && (pincode.isNotEmpty() || pincode.isNotBlank()))
                    ApiInstance.checkVaccineAvailabilityService
                        .listAvailableCentersByPin(
                            pincode,
                            simpleDateFormat.format(Date(calendar.timeInMillis))).execute()
                else
                    ApiInstance.checkVaccineAvailabilityService
                        .listAvailableCentersByDist(
                            distId,
                            simpleDateFormat.format(Date(calendar.timeInMillis))).execute()

                if (response.isSuccessful) {
                    calendar.add(Calendar.DATE, 7)
                    (response.body() as Centers).let {
                        list.addAll(it.centers)
                    }
                }
            }

            list.forEach { center ->
                Log.e("coming", "here")
                center.sessions.forEach { session ->
                    Log.e("session", session.availibility.toString().plus(" ").plus(minAgeLimit))
                    if (session.availibility > 0 && session.ageLimit == minAgeLimit) {
                        NotificatonHelper.onHandleEvent(
                            "ALERT!!",
                            "Vaccines available book slot now!!",
                            appContext
                        )
                        return Result.success(createOutputData(true, session.date))
                    }
                }
            }
            return Result.success(createOutputData(false, ""))

        } catch (ex: Exception) {
            Log.e("Process", "failed", ex)
            return Result.failure()
        }
    }

    private fun createOutputData(availability: Boolean, date: String): Data {
        return Data.Builder()
            .putBoolean("Availability", availability)
            .putString("Dates", date)
            .build()
    }
}