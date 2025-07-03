package com.stip.stip.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 클라이언트 객체
 * API 서버와의 통신을 담당하는 인스턴스를 생성하고 관리
 */
object RetrofitClient {
    private const val BASE_URL = "https://backend.stipvelation.com/"
    private const val ENGINE_URL = "http://34.64.197.80:5000/"
    private const val TIMEOUT = 30L // 30초 타임아웃
    
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder().apply {
                    header("Content-Type", "application/json")
                    header("Accept", "application/json")
                    method(original.method, original.body)
                }.build()
                chain.proceed(request)
            }
            .build()
    }
    
    private val authOkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = com.stip.stip.signup.utils.PreferenceUtil.getString(com.stip.stip.signup.Constants.PREF_KEY_AUTH_TOKEN_VALUE)
                val request = original.newBuilder().apply {
                    header("Content-Type", "application/json")
                    header("Accept", "application/json")
                    if (token.isNotBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                    android.util.Log.d("APII", "${original.method} : ${original.url} ${original.body} Token : ${token}")
                    method(original.method, original.body)
                }.build()
                chain.proceed(request)
            }
            .build()
    }
    
    // 인증이 필요없는 API 서비스용 Retrofit 인스턴스
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // 인증이 필요한 API 서비스용 Retrofit 인스턴스
    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // 엔진 서버용 OkHttpClient
    private val engineOkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder().apply {
                    header("Content-Type", "application/json")
                    header("Accept", "application/json")
                    method(original.method, original.body)
                }.build()
                chain.proceed(request)
            }
            .build()
    }
    
    // 엔진 서버용 Retrofit 인스턴스
    private val engineRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ENGINE_URL)
            .client(engineOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API 서비스 인스턴스 생성 (인증 불필요)
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
    
    // API 서비스 인스턴스 생성 (인증 필요)
    fun <T> createAuthService(serviceClass: Class<T>): T {
        return authRetrofit.create(serviceClass)
    }
    
    // 엔진 서버 API 서비스 인스턴스 생성
    fun <T> createEngineService(serviceClass: Class<T>): T {
        return engineRetrofit.create(serviceClass)
    }
}
