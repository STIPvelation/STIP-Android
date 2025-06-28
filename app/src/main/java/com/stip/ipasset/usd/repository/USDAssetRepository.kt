package com.stip.ipasset.usd.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stip.ipasset.usd.model.USDAssetData

/**
 * USD 자산 데이터 저장소 - 싱글톤 패턴 사용
 */
class USDAssetRepository private constructor() {

    // 라이브 데이터로 자산 데이터 관리
    private val _assetData = MutableLiveData<USDAssetData>(USDAssetData())
    val assetData: LiveData<USDAssetData> = _assetData

    // 현재 자산 데이터 가져오기
    fun getCurrentAssetData(): USDAssetData {
        return _assetData.value ?: USDAssetData()
    }

    // 자산 데이터 업데이트
    fun updateAssetData(newData: USDAssetData) {
        _assetData.postValue(newData)
    }

    // 출금 실행 - 잔액과 출금가능 금액 업데이트
    fun processWithdrawal(amount: Double): Boolean {
        val currentData = getCurrentAssetData()
        
        // 출금가능 금액보다 많이 출금할 수 없음
        if (amount > currentData.withdrawable) {
            return false
        }
        
        // 출금 처리 - 잔액 및 출금가능 금액 감소
        val newBalance = currentData.balance - amount
        val newWithdrawable = currentData.withdrawable - amount
        
        // 수수료는 동일하게 유지
        val updatedData = currentData.copy(
            balance = newBalance,
            withdrawable = newWithdrawable
        )
        
        updateAssetData(updatedData)
        return true
    }

    companion object {
        // 싱글톤 인스턴스
        @Volatile
        private var INSTANCE: USDAssetRepository? = null

        fun getInstance(): USDAssetRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = USDAssetRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
