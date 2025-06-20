package com.stip.stip.ipasset // TODO: 실제 ViewModel 파일이 위치한 패키지 경로로 변경하세요

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stip.stip.ipasset.model.TransactionHistory // TODO: TransactionHistory 모델의 정확한 경로 확인
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// TODO: 실제 데이터 Repository나 DataSource를 주입받도록 수정해야 합니다. (예: Hilt, Koin 사용)
// class TransactionDetailViewModel(private val repository: YourRepository) : ViewModel() {
class TransactionDetailViewModel : ViewModel() { // 우선 간단한 구조로 시작

    // 상세 거래 내역 데이터를 담을 StateFlow. 초기값은 null.
    // 외부에서는 읽기 전용 StateFlow(transactionDetails)만 접근 가능.
    private val _transactionDetails = MutableStateFlow<TransactionHistory?>(null)
    val transactionDetails: StateFlow<TransactionHistory?> = _transactionDetails

    /**
     * Transaction ID를 받아서 해당 거래의 상세 내역을 불러오는 함수
     * @param id 상세 내역을 조회할 거래의 ID (Long 타입)
     */
    fun fetchTransactionDetailsById(id: Long) {
        viewModelScope.launch {
            try {
                Log.d("TransactionDetailVM", "Fetching details for ID: $id")

                // 전체 거래내역 리스트에서 id로 찾아서 상세 반환
                val allList = com.stip.stip.ipasset.TransactionViewModel.lastTransactionList
                val transaction = allList.firstOrNull { it.id == id }
                _transactionDetails.value = transaction
                android.util.Log.d("TransactionDetailVM", "Loaded transaction detail: $transaction")

            } catch (e: Exception) {
                Log.e("TransactionDetailVM", "Error fetching details for ID $id", e)
                _transactionDetails.value = null
            }
        }
    }
}