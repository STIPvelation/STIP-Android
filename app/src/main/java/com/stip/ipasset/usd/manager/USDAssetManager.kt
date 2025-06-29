package com.stip.ipasset.usd.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.stip.dummy.AssetDummyData
import com.stip.dummy.FeeAndLimitsDummyData
import com.stip.dummy.USDTransactionDummyData
import com.stip.ipasset.model.IpAsset
import com.stip.ipasset.usd.model.USDTransaction

/**
 * USD 자산 관리 싱글톤 클래스
 * - 더미 데이터를 활용하여 여러 화면 간 데이터 공유
 */
class USDAssetManager private constructor() {

    // USD 잔액 데이터
    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    // USD 출금가능 금액
    private val _withdrawableAmount = MutableLiveData<Double>()
    val withdrawableAmount: LiveData<Double> = _withdrawableAmount

    // USD 출금한도
    private val _withdrawalLimit = MutableLiveData<Double>()
    val withdrawalLimit: LiveData<Double> = _withdrawalLimit

    // USD 출금 수수료
    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double> = _fee

    // 트랜잭션 데이터
    private val _transactions = MutableLiveData<List<USDTransaction>>()
    val transactions: LiveData<List<USDTransaction>> = _transactions

    init {
        // 데이터 초기화
        refreshData()

        // 출금 한도 설정 (1,000,000으로 고정 - 모든 티커 공통)
        _withdrawalLimit.value = 1000000.0

        // 출금 수수료 설정
        val fee = FeeAndLimitsDummyData.getWithdrawalFee("USD")
        _fee.value = fee

        // 트랜잭션 데이터 로드
        loadTransactions()
    }

    /**
     * 트랜잭션 데이터 로드
     */
    private fun loadTransactions() {
        val allTransactions = USDTransactionDummyData.getAllTransactions()
        _transactions.value = allTransactions
    }

    /**
     * 출금 처리
     */
    fun processWithdrawal(amount: Double): Boolean {
        // 출금가능 금액보다 많이 출금할 수 없음
        if (amount > _withdrawableAmount.value ?: 0.0) {
            return false
        }

        // 잔액 및 출금가능 금액 업데이트
        val currentBalance = _balance.value ?: 0.0
        val currentWithdrawable = _withdrawableAmount.value ?: 0.0
        val currentWithdrawalLimit = _withdrawalLimit.value ?: 500000.0

        // 잔액, 출금가능금액, 출금한도 모두 감소
        _balance.value = currentBalance - amount
        _withdrawableAmount.value = currentWithdrawable - amount
        _withdrawalLimit.value = currentWithdrawalLimit - amount
        
        // 더미 데이터 업데이트는 실제 API 연동에서 구현
        // 현재 더미 데이터의 Asset 객체는 불변(immutable) 속성을 가지고 있어 직접 수정 불가
        // 실제 구현 시 서버 API를 통해 잔액 업데이트 필요
        android.util.Log.d("USDAssetManager", "Note: Asset dummy data not updated due to immutability")

        // 로그 출력으로 현재 잔액 상태 확인
        android.util.Log.d("USDAssetManager", "Withdrawal completed: amount=$amount, new balance=${_balance.value}, new withdrawable=${_withdrawableAmount.value}, new limit=${_withdrawalLimit.value}")
        
        // 새 트랜잭션은 실제 API 연동 시 추가
        // 여기서는 기존 데이터만 유지
        
        return true
    }
    
    /**
     * 데이터 새로고침
     * - 더미 데이터에서 최신 정보를 다시 가져와 LiveData를 업데이트
     * - 화면 초기화 또는 데이터 갱신이 필요한 경우 호출
     */
    fun refreshData() {
        try {
            // 최신 자산 정보 로드 - 반드시 값이 존재하도록 확인
            val usdAsset = AssetDummyData.getAssetByCode("USD") ?: run {
                // USD 자산이 없으면 직접 기본 값 설정
                AssetDummyData.getDefaultAssets().firstOrNull { it.ticker == "USD" } ?: 
                IpAsset(id = "1", name = "US Dollar", ticker = "USD", balance = 10000.0, value = 10000.0)
            }
            
            // 로그 출력으로 디버깅
            android.util.Log.d("USDAssetManager", "Retrieved USD asset: $usdAsset")
            
            // 잔액 업데이트 - 기존 값 유지 (출금 후 반영을 위해)
            if (_balance.value == null) {
                _balance.value = usdAsset.balance
                android.util.Log.d("USDAssetManager", "Set initial balance: ${usdAsset.balance}")
            } else {
                android.util.Log.d("USDAssetManager", "Preserving existing balance: ${_balance.value}")
            }
            
            // 출금 가능 금액 업데이트 - 항상 최신 잔액을 반영하도록 수정
            val withdrawableAmountValue = _balance.value ?: 10000.0
            _withdrawableAmount.value = withdrawableAmountValue
            android.util.Log.d("USDAssetManager", "Updated withdrawable amount: $withdrawableAmountValue")
            
            // 출금 한도 업데이트
            // 이미 값이 설정되어 있으면 그 값 유지, 아니면 500,000 USD로 설정
            if (_withdrawalLimit.value == null) {
                _withdrawalLimit.value = 500000.0
                android.util.Log.d("USDAssetManager", "Set initial withdrawal limit: 500000.0")
            }
            
            // 수수료 업데이트
            val fee = FeeAndLimitsDummyData.getWithdrawalFee("USD") ?: 1.0
            _fee.value = fee
            
            // 트랜잭션 데이터 다시 로드
            loadTransactions()
            
            // 비동기 딜레이 후 LiveData 값을 다시 설정하여 UI업데이트 강제 (관찰자 재활성화)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                // 같은 값이라도 새 객체로 설정하여 LiveData 갱신 트리거
                val currentBalance = _balance.value
                val currentWithdrawable = _withdrawableAmount.value
                val currentLimit = _withdrawalLimit.value
                
                _balance.value = currentBalance
                _withdrawableAmount.value = currentWithdrawable
                _withdrawalLimit.value = currentLimit
                
                android.util.Log.d("USDAssetManager", "Refreshed UI values - Balance: $currentBalance, Withdrawable: $currentWithdrawable, Limit: $currentLimit")
            }, 300) // 300ms 딜레이
            
        } catch (e: Exception) {
            android.util.Log.e("USDAssetManager", "Error refreshing data", e)
            // 오류 발생 시 기본값 설정
            _balance.value = 10000.0
            _withdrawableAmount.value = 10000.0
            _withdrawalLimit.value = 500000.0
            _fee.value = 1.0
        }
    }

    companion object {
        @Volatile
        private var instance: USDAssetManager? = null

        fun getInstance(): USDAssetManager {
            return instance ?: synchronized(this) {
                instance ?: USDAssetManager().also { instance = it }
            }
        }
    }
}
