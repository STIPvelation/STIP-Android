package com.stip.stip.iptransaction.api

import com.stip.stip.iptransaction.model.DipHoldingitem
import com.stip.stip.iptransaction.model.IpInvestmentItem
import com.stip.stip.iptransaction.model.MyIpHoldingsSummaryItem
import com.stip.stip.iptransaction.model.UnfilledOrder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface IpTransactionApi {
    @GET("api/ip/transactions")
    fun getIpTransactions(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("types") types: List<String>? = null
    ): Call<List<IpInvestmentItem>>

    @GET("api/ip/holdings")
    fun getIpHoldings(): Call<List<DipHoldingitem>>

    @GET("api/ip/holdings/summary")
    fun getIpHoldingsSummary(): Call<MyIpHoldingsSummaryItem>

    @GET("api/ip/unfilled")
    fun getUnfilledOrders(): Call<List<UnfilledOrder>>
    
    // 손익 정보 API
    @GET("api/ip/profit/detail")
    fun getProfitLossItems(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("profitType") profitType: String? = null
    ): Call<List<com.stip.stip.iptransaction.model.ProfitLossItem>>
    
    @GET("api/ip/profit/summary")
    fun getProfitLossSummary(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("profitType") profitType: String? = null
    ): Call<com.stip.stip.iptransaction.model.ProfitLossSummary>
    
    @GET("api/ip/profit/chart")
    fun getProfitLossChartData(
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("profitType") profitType: String? = null
    ): Call<com.stip.stip.iptransaction.model.ProfitLossChartData>
}

object IpTransactionService {
    private const val BASE_URL = "https://backend.stipvelation.com/"
    private const val X_API_KEY: String = "AIzaSyAM4J1XFF6SAkXeY78ONDyRtgo3mhk78kE"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("X_API_Key", X_API_KEY)
            
            // JWT 토큰이 있으면 헤더에 추가
            val token = com.stip.stip.signup.utils.PreferenceUtil.getToken()
            if (token != null && token.isNotEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }
            
            val request = builder.method(original.method, original.body).build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
        
    val ipTransactionApi: IpTransactionApi = retrofit.create(IpTransactionApi::class.java)
    
    // IP 거래 내역 조회 (투자 기록)
    fun getIpTransactions(
        filterTypes: List<String>? = null,
        startDate: String? = null,
        endDate: String? = null,
        callback: (List<IpInvestmentItem>?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getIpTransactions(startDate, endDate, filterTypes).enqueue(
            object : retrofit2.Callback<List<IpInvestmentItem>> {
                override fun onResponse(
                    call: Call<List<IpInvestmentItem>>,
                    response: retrofit2.Response<List<IpInvestmentItem>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<IpInvestmentItem>>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
    
    // IP 보유 현황 조회
    fun getIpHoldings(
        callback: (List<DipHoldingitem>?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getIpHoldings().enqueue(
            object : retrofit2.Callback<List<DipHoldingitem>> {
                override fun onResponse(
                    call: Call<List<DipHoldingitem>>,
                    response: retrofit2.Response<List<DipHoldingitem>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<DipHoldingitem>>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
    
    // IP 보유 현황 요약 조회
    fun getIpHoldingsSummary(
        callback: (MyIpHoldingsSummaryItem?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getIpHoldingsSummary().enqueue(
            object : retrofit2.Callback<MyIpHoldingsSummaryItem> {
                override fun onResponse(
                    call: Call<MyIpHoldingsSummaryItem>,
                    response: retrofit2.Response<MyIpHoldingsSummaryItem>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<MyIpHoldingsSummaryItem>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
    
    // 미체결 주문 조회
    fun getUnfilledOrders(
        callback: (List<UnfilledOrder>?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getUnfilledOrders().enqueue(
            object : retrofit2.Callback<List<UnfilledOrder>> {
                override fun onResponse(
                    call: Call<List<UnfilledOrder>>,
                    response: retrofit2.Response<List<UnfilledOrder>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<UnfilledOrder>>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
    
    // 손익 상세 정보 조회
    fun getProfitLossItems(
        year: Int,
        month: Int,
        profitType: String? = null,
        callback: (List<com.stip.stip.iptransaction.model.ProfitLossItem>?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getProfitLossItems(year, month, profitType).enqueue(
            object : retrofit2.Callback<List<com.stip.stip.iptransaction.model.ProfitLossItem>> {
                override fun onResponse(
                    call: Call<List<com.stip.stip.iptransaction.model.ProfitLossItem>>,
                    response: retrofit2.Response<List<com.stip.stip.iptransaction.model.ProfitLossItem>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<com.stip.stip.iptransaction.model.ProfitLossItem>>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
    
    // 손익 요약 정보 조회
    fun getProfitLossSummary(
        year: Int,
        month: Int,
        profitType: String? = null,
        callback: (com.stip.stip.iptransaction.model.ProfitLossSummary?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getProfitLossSummary(year, month, profitType).enqueue(
            object : retrofit2.Callback<com.stip.stip.iptransaction.model.ProfitLossSummary> {
                override fun onResponse(
                    call: Call<com.stip.stip.iptransaction.model.ProfitLossSummary>,
                    response: retrofit2.Response<com.stip.stip.iptransaction.model.ProfitLossSummary>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<com.stip.stip.iptransaction.model.ProfitLossSummary>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
    
    // 손익 차트 데이터 조회
    fun getProfitLossChartData(
        year: Int,
        month: Int,
        profitType: String? = null,
        callback: (com.stip.stip.iptransaction.model.ProfitLossChartData?, Throwable?) -> Unit
    ) {
        ipTransactionApi.getProfitLossChartData(year, month, profitType).enqueue(
            object : retrofit2.Callback<com.stip.stip.iptransaction.model.ProfitLossChartData> {
                override fun onResponse(
                    call: Call<com.stip.stip.iptransaction.model.ProfitLossChartData>,
                    response: retrofit2.Response<com.stip.stip.iptransaction.model.ProfitLossChartData>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body(), null)
                    } else {
                        callback(null, Exception("API 호출 실패: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<com.stip.stip.iptransaction.model.ProfitLossChartData>, t: Throwable) {
                    callback(null, t)
                }
            }
        )
    }
}
