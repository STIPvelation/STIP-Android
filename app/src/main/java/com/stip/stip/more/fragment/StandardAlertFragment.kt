package com.stip.stip.more.fragment

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.stip.stip.R
import android.content.SharedPreferences


class StandardAlertFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_more_price_alert_standard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("alert_pref", Context.MODE_PRIVATE)

        val activeSwitches = listOf(
            Triple(R.id.switch_login_alert, "login_alert_enabled", true),
            Triple(R.id.switch_transaction_alert, "transaction_alert_enabled", true),
            Triple(R.id.switch_deposit_withdrawal_alert, "deposit_withdrawal_alert_enabled", true),
            Triple(R.id.switch_order_alert, "order_alert_enabled", true),
            Triple(R.id.switch_staking_alert, "staking_alert_enabled", true)
        )

        val disabledSwitches = listOf(
            R.id.switch_ip_toon_alert,
            R.id.switch_auction_alert,
            R.id.switch_new_listing_alert,
            R.id.switch_ip_swap_alert
        )

        // ✅ 활성 알림 스위치 세팅
        for ((viewId, prefKey, defaultValue) in activeSwitches) {
            view.findViewById<SwitchCompat>(viewId)?.let { switch ->
                setupSwitch(sharedPref, switch, prefKey, defaultValue)
            }
        }

        // ❌ 준비중 스위치 - 비활성화 대신 클릭만 막고 토스트 표시
        for (id in disabledSwitches) {
            view.findViewById<SwitchCompat>(id)?.apply {
                isChecked = false
                isEnabled = true
                setOnCheckedChangeListener { _, _ ->
                    isChecked = false
                    Toast.makeText(requireContext(), "해당 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ✅ 알림 설정으로 이동
        view.findViewById<View>(R.id.notification_settings_layout)?.setOnClickListener {
            val intent = android.content.Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            }
            startActivity(intent)
        }
    }

    private fun setupSwitch(
        sharedPref: SharedPreferences,
        switch: SwitchCompat,
        key: String,
        defaultValue: Boolean
    ) {
        val isEnabled = sharedPref.getBoolean(key, defaultValue)
        switch.isChecked = isEnabled
        switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            sharedPref.edit().putBoolean(key, isChecked).apply()
            // TODO: 서버 연동 필요 시 여기에 추가
        }
    }
}
