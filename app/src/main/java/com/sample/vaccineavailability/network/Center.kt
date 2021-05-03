package com.sample.vaccineavailability.network

import com.google.gson.annotations.SerializedName


data class Centers (
    @SerializedName("centers")
    val centers: List<Center>
)

data class Center(
    @SerializedName("center_id")
    val centerId: Long,
    @SerializedName("name")
    val centerName: String,
    @SerializedName("state_name")
    val stateName: String,
    @SerializedName("district_name")
    val distName: String,
    @SerializedName("block_name")
    val blockName: String,
    @SerializedName("pincode")
    val pincode: Int,
    @SerializedName("lat")
    val lat: Int,
    @SerializedName("long")
    val long: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("sessions")
    val sessions: List<Session>
)

data class Session (
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("available_capacity")
    val availibility: Int,
    @SerializedName("min_age_limit")
    val ageLimit: Int,
    @SerializedName("vaccine")
    val vaccine: String
)

data class RespStates (
    @SerializedName("states")
    val states: List<State>,
    @SerializedName("ttl")
    val total: Int
)

data class State(
    @SerializedName("state_id")
    val id: Int,
    @SerializedName("state_name")
    val name: String
)

data class RespDistrict (
    @SerializedName("districts")
    val districts: List<District>,
    @SerializedName("ttl")
    val total: Int
)

data class District(
    @SerializedName("district_id")
    val id: Int,
    @SerializedName("district_name")
    val name: String
)