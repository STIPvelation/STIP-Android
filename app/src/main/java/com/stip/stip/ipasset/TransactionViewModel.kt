package com.stip.stip.ipasset

import androidx.lifecycle.ViewModel
import com.stip.stip.ipasset.model.Filter
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.ipasset.model.TransactionHistory
import com.stip.stip.ipasset.model.TransactionHistory.Status
import com.stip.stip.ipasset.model.WithdrawalDestination
import com.stip.stip.ipasset.model.WithdrawalStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor() : ViewModel() {
    private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    private val _filter = MutableStateFlow(Filter.ALL)
    val filter = _filter.asStateFlow()

    private val _transactionHistories = MutableStateFlow<List<TransactionHistory>>(value = emptyList())
    val transactionHistories = _transactionHistories.combine(filter) { histories, filter ->
        when (filter) {
            Filter.ALL -> histories
            Filter.DEPOSIT -> histories.filter { it.status == Status.DEPOSIT_COMPLETED }
            Filter.WITHDRAW -> histories.filter { it.status == Status.WITHDRAWAL_COMPLETED }
            else -> histories
        }
            .groupBy {
                simpleDateFormat.format(Date(it.timestamp))
            }
            .flatMap { (_, itemsInGroup) ->
                itemsInGroup.mapIndexed { index, item ->
                    item.copy(heading = if (index == 0) simpleDateFormat.format(Date(item.timestamp)) else null)
                }
            }
    }

    private val _withdrawalStatus = MutableStateFlow<WithdrawalStatus?>(value = null)
    val withdrawalStatus = _withdrawalStatus.asStateFlow()

    fun fetchWithdrawalStatus(ipAsset: IpAsset, accountNumber: String) {
        // TODO: 실제 API에서 출금 상태 정보 가져오기
        // val response = transactionRepository.getWithdrawalStatus(ipAsset.currencyCode)
        
        _withdrawalStatus.value = WithdrawalStatus(
            pendingAmount = 0.0,
            availableAmount = 0.0,
            withdrawalLimit = 0.0,
            currencyCode = ipAsset.currencyCode,
            fee = 1.0,
            withdrawalDestination = if (ipAsset.isUSD) {
                WithdrawalDestination.BankAccount("")
            } else {
                WithdrawalDestination.Wallet("")
            }
        )
    }

    fun fetchTransactionHistory(ipAsset: IpAsset) {
        // TODO: 실제 API에서 거래 내역 가져오기
        // val response = transactionRepository.getTransactionHistory(ipAsset.currencyCode)
        // val transactionList = response.map { transaction ->
        //     TransactionHistory(
        //         id = transaction.id,
        //         memberNumber = transaction.memberNumber,
        //         amount = transaction.amount,
        //         timestamp = transaction.timestamp,
        //         usdAmount = transaction.usdAmount,
        //         currencyCode = transaction.currencyCode,
        //         status = transaction.status,
        //         ticker = transaction.ticker
        //     )
        // }
        
        // 임시 빈 데이터 사용
        _transactionHistories.value = emptyList()
    }

    companion object {
        // 마지막 생성된 거래내역 전체 리스트를 static하게 저장
        var lastTransactionList: List<TransactionHistory> = emptyList()
    }

    fun onFilterChange(filter: Filter) {
        _filter.value = filter
    }
}
