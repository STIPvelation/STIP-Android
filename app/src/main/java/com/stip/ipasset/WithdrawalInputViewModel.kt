package com.stip.stip.ipasset

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.stip.stip.R
import com.stip.stip.ipasset.model.WithdrawalInputState
import com.stip.stip.ipasset.model.WithdrawalStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WithdrawalInputViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application = application) {
    private val _isKeypadVisible = MutableStateFlow(false)
    val withdrawalStatus = savedStateHandle.getStateFlow<WithdrawalStatus?>("withdrawalStatus", null)

    val isKeypadVisible = _isKeypadVisible.asStateFlow()
    private val _withdrawalAmount = MutableStateFlow("")
    val withdrawalAmount = _withdrawalAmount.asStateFlow()
    val withdrawalState = withdrawalAmount.combine(withdrawalStatus.filterNotNull()) { withdrawalAmount, withdrawalInfo ->
        val amount = withdrawalAmount.toDoubleOrNull() ?: 0.0
        val fee = withdrawalStatus.value?.fee ?: 0.0

        when {
            amount < 1.0 -> WithdrawalInputState(isEnabled = false)
            withdrawalAmount.isEmpty() -> WithdrawalInputState(isEnabled = false)
            amount > withdrawalInfo.availableAmount + fee -> WithdrawalInputState(
                isEnabled = false,
                errorMessage = application.getString(R.string.withdrawal_error_available_amount_exceeded)
            )

            amount > withdrawalInfo.withdrawalLimit -> WithdrawalInputState(
                isEnabled = false,
                errorMessage = application.getString(R.string.withdrawal_error_limit_exceeded)
            )

            else -> WithdrawalInputState(isEnabled = true)
        }
    }

    fun onNumber(value: String) {
        _withdrawalAmount.update {
            if (it.isEmpty()) {
                if (value == "0") it else it.plus(value)
            } else {
                it.plus(value)
            }
        }
    }

    fun onDelete() {
        _withdrawalAmount.update {
            it.dropLast(1)
        }
    }

    fun hideKeypad() {
        _isKeypadVisible.value = false
    }

    fun showKeypad() {
        _isKeypadVisible.value = true
    }
}
