package com.stip.stip.iptransaction.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import com.stip.stip.R

/**
 * 티커 선택을 위한 다이얼로그 프래그먼트
 */
class TickerSelectionDialogFragment : DialogFragment() {

    // 선택된 티커를 전달하기 위한 콜백 인터페이스
    interface TickerSelectionListener {
        fun onTickerSelected(ticker: String)
    }

    private var listener: TickerSelectionListener? = null

    // 입출금에서 사용하는 티커 목록 ("전체" 옵션 및 알파벳순으로 정렬됨)
    companion object {
        val TICKERS = listOf(
            "전체",  // 모든 티커 선택
            "AXNO",
            "CDM",
            "IJECT",
            "JWV",
            "KATV",
            "KCOT",
            "MDM",
            "MSK",
            "SLEEP",
            "SMT",
            "WETALK"
        )

        fun newInstance(listener: TickerSelectionListener): TickerSelectionDialogFragment {
            return TickerSelectionDialogFragment().apply {
                this.listener = listener
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_ticker_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 다이얼로그 크기 설정
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 리스트뷰 설정
        val listView = view.findViewById<ListView>(R.id.listView_tickers)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, TICKERS)
        listView.adapter = adapter

        // 아이템 클릭 리스너 설정
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedTicker = TICKERS[position]
            listener?.onTickerSelected(selectedTicker)
            dismiss()
        }
    }
}
