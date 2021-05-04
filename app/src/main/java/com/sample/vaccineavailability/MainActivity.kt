package com.sample.vaccineavailability


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.facebook.stetho.Stetho
import com.sample.vaccineavailability.databinding.ActivityMainBinding
import com.sample.vaccineavailability.network.*
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var statesSpinnerData = ArrayList<String>()
    private var states = ArrayList<State>()
    private var districtsSpinnerData = ArrayList<String>()
    private var districts = ArrayList<District>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Stetho.initializeWithDefaults(this)


        //adapters
        val adapter1 = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item,
            statesSpinnerData)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val adapter2 = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item,
            districtsSpinnerData)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val minAgeLimits = resources.getStringArray(R.array.min_age_list)
        val adapter3 = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item,
            minAgeLimits
        )
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.minAge.setAdapter(adapter3)

        //get states.
        ApiInstance.checkVaccineAvailabilityService.getStates()
            .enqueue(object: retrofit2.Callback<RespStates> {
                override fun onFailure(call: Call<RespStates>, t: Throwable) {
                    //todo: add error message and handle UI
                }

                override fun onResponse(call: Call<RespStates>, response: Response<RespStates>) {
                    if (response.isSuccessful) {
                        if(states.isNotEmpty()) states.clear()
                        states.addAll((response.body() as RespStates).states)

                        if (statesSpinnerData.isNotEmpty()) statesSpinnerData.clear()
                        statesSpinnerData.addAll((response.body() as RespStates).states.map { it.name })
                        adapter1.notifyDataSetChanged()

                        //check from prefs and select.
                    }
                }

            })

        binding.states2.setAdapter(adapter1)
        binding.states2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                states.find { it.name.toLowerCase() == p0.toString().toLowerCase() }?.let {
                    Log.e("id", it.id.toString())
                    ApiInstance.checkVaccineAvailabilityService.getDistrictsByStates(it.id)
                        .enqueue(object: retrofit2.Callback<RespDistrict> {
                            override fun onFailure(call: Call<RespDistrict>, t: Throwable) {
                                //todo: handle error here
                            }

                            override fun onResponse(
                                call: Call<RespDistrict>,
                                response: Response<RespDistrict>
                            ) {
                                if(response.isSuccessful) {
                                    if(districts.isNotEmpty()) districts.clear()
                                    districts.addAll((response.body() as RespDistrict).districts)

                                    if (districtsSpinnerData.isNotEmpty()) districtsSpinnerData.clear()
                                    districtsSpinnerData.addAll((response.body() as RespDistrict).districts.map { it.name })
                                    adapter2.notifyDataSetChanged()
                                }
                            }

                        })
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        binding.districts2.setAdapter(adapter2)
        binding.districts2.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val selectedDistrict = p0?.toString()
                districts.find { it.name.toLowerCase() == selectedDistrict?.toLowerCase()}?.let {
                    Log.e("district id", it.id.toString())
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        binding.checkNow.setOnClickListener {
            val sharedPref = getSharedPreferences("Info", Context.MODE_PRIVATE) ?: return@setOnClickListener
            with (sharedPref.edit()) {
                if(binding.pincode.text.isNotEmpty())  {
                    putString("pincode", binding.pincode.text.toString())
                } else {
                    putString("state", binding.states2.text.toString())
                    putString("district", binding.districts2.text.toString())
                    putInt("stateId", states.find { it.name.toLowerCase() ==
                            binding.states2.text.toString().toLowerCase()}?.id ?: -1)
                    putInt("districtId", districts.find { it.name.toLowerCase() ==
                            binding.districts2.text.toString().toLowerCase()}?.id ?: -1)
                }

                putInt("minAgeLimit", binding.minAge.text.toString().toInt())
                apply()
            }
            val checkNow =
                OneTimeWorkRequestBuilder<VaccineAvailabilityWorker>()
                    .build()
            WorkManager.getInstance(this)
                .enqueue(checkNow)

            WorkManager.getInstance(this)
                .getWorkInfoByIdLiveData(checkNow.id)
                .observe(this, Observer {
                    if(it.state.isFinished) {
                        binding.result.text = if(it.outputData.getBoolean("Availability", false)){
                            "Slots available on ".plus(it.outputData.getString("Dates"))
                        } else {
                            "No Slots available!"
                        }
                    }
                })
        }

        binding.schedule.setOnClickListener {
            //schedule periodic request.
        val checkVaccineAvailibilityWorkerRequest =
            PeriodicWorkRequest.Builder(VaccineAvailabilityWorker::class.java,
                30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(VaccineAvailabilityWorker::class.java.name,
                ExistingPeriodicWorkPolicy.REPLACE,
                checkVaccineAvailibilityWorkerRequest)
        }
    }

}