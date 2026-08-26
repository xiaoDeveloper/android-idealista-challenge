package com.xiao.idealistachallenge.data.remote

import retrofit2.http.GET

const val IDEALISTA_BASE_URL = "https://idealista.github.io/android-challenge/"

interface IdealistaApi {

    @GET("list.json")
    suspend fun listAds(): List<PropertyAdDto>

    @GET("detail.json")
    suspend fun getDetails(): PropertyDetailsDto
}
