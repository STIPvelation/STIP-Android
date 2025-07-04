package com.stip.stip.iptransaction.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stip.stip.iptransaction.repository.IpTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IpTransactionViewModel @Inject constructor(
    private val repository: IpTransactionRepository
) : ViewModel() {

    private val _cancelOrderResult = MutableLiveData<Result<Unit>>()
    val cancelOrderResult: LiveData<Result<Unit>> = _cancelOrderResult

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                val result = repository.cancelOrder(orderId)
                _cancelOrderResult.value = result
            } catch (e: Exception) {
                _cancelOrderResult.value = Result.failure(e)
            }
        }
    }
    
    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            try {
                val result = repository.cancelOrder(orderId)
                _cancelOrderResult.value = result
            } catch (e: Exception) {
                _cancelOrderResult.value = Result.failure(e)
            }
        }
    }
} 