package com.stip.stip.ipinfo.api

import com.stip.stip.ipinfo.model.FxRateResponse
import com.stip.stip.ipinfo.model.IpTrendResponse
import com.stip.stip.ipinfo.model.StipIndexResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface IpInfoApiService {
    @GET("api/v1/ip/trend/top")
    suspend fun getTopRisingIps(): Response<IpTrendResponse>
    
    @GET("api/v1/ip/index")
    suspend fun getStipIndices(): Response<StipIndexResponse>
    
    @GET("api/v1/fx/rates")
    suspend fun getFxRates(): Response<FxRateResponse>
    
    companion object {
        private const val BASE_URL = "https://backend.stipvelation.com/"
        
        fun create(): IpInfoApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IpInfoApiService::class.java)
        }
    }
}
