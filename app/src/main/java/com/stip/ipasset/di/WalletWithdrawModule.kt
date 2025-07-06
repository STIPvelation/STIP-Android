package com.stip.ipasset.di

import android.util.Log
import com.stip.stip.api.RetrofitClient
import com.stip.ipasset.api.WalletWithdrawService
import com.stip.ipasset.repository.WalletWithdrawRepository
import com.stip.ipasset.repository.WalletWithdrawRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 출금 관련 DI 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WalletWithdrawModule {

    /**
     * 출금 리포지토리 구현체 바인딩
     */
    @Binds
    @Singleton
    abstract fun bindWalletWithdrawRepository(
        walletWithdrawRepositoryImpl: WalletWithdrawRepositoryImpl
    ): WalletWithdrawRepository

    companion object {
        /**
         * 출금 API 서비스 제공 (엔진 서버 + 토큰 포함)
         */
        @Provides
        @Singleton
        fun provideWalletWithdrawService(): WalletWithdrawService {
            return try {
                Log.d("WalletWithdrawModule", "🔧 엔진 서버 WalletWithdrawService 생성 시작")
                Log.d("WalletWithdrawModule", "🌐 엔진 서버 URL: http://34.64.197.80:5000/")
                
                // 엔진 서버 서비스 사용 (토큰 자동 포함됨)
                val service = RetrofitClient.createEngineService(WalletWithdrawService::class.java)
                
                Log.d("WalletWithdrawModule", "✅ 엔진 서버 WalletWithdrawService 생성 성공")
                service
            } catch (e: Exception) {
                Log.e("WalletWithdrawModule", "❌ 엔진 서버 WalletWithdrawService 생성 실패", e)
                throw e
            }
        }
    }
} 