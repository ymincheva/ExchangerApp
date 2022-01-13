package com.exchanger.network

import com.exchanger.helper.EndPoints
import com.exchanger.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET(EndPoints.LATEST_URL)
    suspend fun convertCurrency(
        @Query("access_key") access_key: String/*,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double*/
    ) : Response<ApiResponse>

}