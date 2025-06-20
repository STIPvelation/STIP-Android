package com.stip.stip.more.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.stip.stip.R

class MarketPriceAlertFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_more_price_alert_market, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("alert_pref", Context.MODE_PRIVATE)

        // ✅ 휴식시간 제외 알림 스위치: 저장/불러오기 적용
        val switchDndRest = view.findViewById<SwitchCompat>(R.id.switch_dnd_rest)
        val isRestEnabled = sharedPref.getBoolean("dnd_rest_alert_enabled", false)
        switchDndRest?.isChecked = isRestEnabled

        switchDndRest?.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("dnd_rest_alert_enabled", isChecked).apply()
        }

        // ✅ 나머지 항목은 준비 중으로 안내
        val disabledSwitchIds = listOf(
            R.id.switch_owned_ip,
            R.id.switch_comp_designated,
            R.id.switch_comp_rise_fall,
            R.id.switch_comp_report_low,
            R.id.switch_comp_ip_cross,
            R.id.switch_comp_new_user,
            R.id.switch_comp_revoke_user
        )

        for (switchId in disabledSwitchIds) {
            view.findViewById<SwitchCompat>(switchId)?.apply {
                isChecked = false
                isEnabled = true
                setOnCheckedChangeListener { _, _ ->
                    isChecked = false
                    Toast.makeText(requireContext(), "해당 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
