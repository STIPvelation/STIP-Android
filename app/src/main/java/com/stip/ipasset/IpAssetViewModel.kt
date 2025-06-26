package com.stip.stip.ipasset

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stip.stip.ipasset.model.Filter
import com.stip.stip.ipasset.model.IpAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IpAssetViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _ipAssets = MutableStateFlow<List<IpAsset>>(emptyList())
    val ipAssets: StateFlow<List<IpAsset>> = _ipAssets.asStateFlow()

    private val _filteredIpAssets = MutableStateFlow<List<IpAsset>>(emptyList())
    val filteredIpAssets: StateFlow<List<IpAsset>> = _filteredIpAssets.asStateFlow()

    private val _totalIpAssets = MutableStateFlow(0L)
    val totalIpAssets: StateFlow<Long> = _totalIpAssets.asStateFlow()

    private val _filter = MutableStateFlow(Filter.ALL)
    val filter: StateFlow<Filter> = _filter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentQuery: String = ""

    init {
        loadIpAssets()
    }

    fun refreshIpAssets() {
        viewModelScope.launch {
            _isLoading.value = true
            loadIpAssets()
        }
    }

    private fun loadIpAssets() {
    viewModelScope.launch {
        try {
            if (_isLoading.value) {
                delay(1000) // 중복 요청 방지를 위한 딜레이 유지
            }
            
            // USD는 기본 디폴트 값으로 유지
            val ipAssets = mutableListOf<IpAsset>(
                IpAsset(
                    id = 0L,
                    currencyCode = "USD",
                    amount = 0L, // 실제 값은 API에서 갱신될 것임
                    usdValue = 0L
                )
            )
            
            // TODO: 실제 API에서 나머지 자산 데이터 불러오기
            // 예시: 
            // val response = assetRepository.getUserAssets()
            // ipAssets.addAll(response.assets.map { it.toIpAsset() })
            
            _ipAssets.value = ipAssets
            _totalIpAssets.value = 0L // 실제 값은 API에서 갱신될 것임
            
            filterAndUpdateIpAssets(currentQuery)
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: 에러 처리 추가 (예: 에러 메시지 표시)
        } finally {
            _isLoading.value = false
        }
    }
}

    fun filterIpAssets(query: String) {
        currentQuery = query
        filterAndUpdateIpAssets(query)
    }

    private fun filterAndUpdateIpAssets(query: String) {
        val assets = when (_filter.value) {
            Filter.ALL -> _ipAssets.value
            Filter.OWNED -> _ipAssets.value.filter { it.amount > 0 }
            Filter.DEPOSIT -> _ipAssets.value.filter { it.amount > 0 && it.currencyCode == "USD" }
            Filter.WITHDRAW -> _ipAssets.value.filter { it.amount == 0L }
            Filter.REFUND -> _ipAssets.value.filter { it.amount > 0 }
            Filter.PENDING -> _ipAssets.value
            Filter.PROCESSING -> _ipAssets.value
        }

        val filtered = if (query.isNotEmpty()) {
            assets.filter { it.currencyCode.contains(query, ignoreCase = true) }
        } else {
            assets
        }

        _filteredIpAssets.value = filtered
        updateTotalAssets(filtered)
    }

    private fun updateTotalAssets(ipAssets: List<IpAsset>) {
        _totalIpAssets.value = ipAssets.sumOf { it.usdValue }
    }
}