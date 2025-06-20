package com.stip.stip.iptransaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.ScrollableToTop
import com.stip.stip.iptransaction.adapter.IpInvestmentAdapter
import com.stip.stip.databinding.FragmentIpInvestmentBinding
import com.stip.stip.iptransaction.api.IpTransactionService
import com.stip.stip.iptransaction.model.IpInvestmentItem
import com.stip.stip.signup.utils.PreferenceUtil

class IpInvestmentFragment : Fragment(), ScrollableToTop {

    private lateinit var mainViewModel: com.stip.stip.MainViewModel


    private var _binding: FragmentIpInvestmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var ipInvestmentAdapter: IpInvestmentAdapter

    companion object {
        @JvmStatic
        fun newInstance() = IpInvestmentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpInvestmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.stip.stip.MainViewModel::class.java]
        mainViewModel.memberInfo.observe(viewLifecycleOwner) { memberInfo ->
            // TODO: 회원정보를 UI에 반영하는 코드 작성
        }

        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupScrollSync()
        loadInvestmentData()

        binding.imageViewFilterIcon.setOnClickListener {
            navigateToInvestmentFilter()
        }

        binding.buttonIpSearch.setOnClickListener {
            Toast.makeText(requireContext(), "준비중", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.setFragmentResultListener("investmentFilterResult", viewLifecycleOwner) { _, bundle ->
            val types = bundle.getStringArrayList("filterTypes") ?: arrayListOf()
            val startDate = bundle.getString("filterStartDate")
            val endDate = bundle.getString("filterEndDate")
            val periodLabel = bundle.getString("filterPeriodLabel") ?: ""
            binding.textViewFilterLabel.text = "$periodLabel 내역보기"
            loadInvestmentData(types, startDate, endDate)
        }
    }

    private fun setupRecyclerView() {
        ipInvestmentAdapter = IpInvestmentAdapter()
        binding.recyclerViewInvestmentList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ipInvestmentAdapter
            val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            ContextCompat.getDrawable(requireContext(), R.drawable.recycler_divider)?.let {
                divider.setDrawable(it)
                addItemDecoration(divider)
            }
        }
    }

    private fun setupScrollSync() {
        binding.headerScrollView.bindSyncTarget(binding.scrollableContent)
        binding.scrollableContent.bindSyncTarget(binding.headerScrollView)
    }

    private fun loadInvestmentData(
        filterTypes: List<String>? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        // 로그인 여부 확인
        if (PreferenceUtil.getToken().isNullOrEmpty()) {
            // 로그인 필요 상태 표시
            binding.recyclerViewInvestmentList.visibility = View.GONE
            binding.noDataText.visibility = View.VISIBLE
            binding.noDataText.text = "로그인이 필요합니다."
            return
        }
        
        // 로딩 상태 표시
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewInvestmentList.visibility = View.GONE
        binding.noDataText.visibility = View.GONE
        
        // API 호출
        IpTransactionService.getIpTransactions(filterTypes, startDate, endDate) { data, error ->
            // UI 스레드에서 처리
            requireActivity().runOnUiThread {
                binding.progressBar.visibility = View.GONE
                
                if (error != null) {
                    // 오류 처리
                    binding.noDataText.visibility = View.VISIBLE
                    binding.noDataText.text = "데이터를 불러오는 중 오류가 발생했습니다."
                    return@runOnUiThread
                }
                
                // 데이터 표시
                if (data != null && data.isNotEmpty()) {
                    showInvestmentData(data)
                } else {
                    showEmptyState()
                }
            }
        }
    }
    
    private fun showEmptyState() {
        binding.recyclerViewInvestmentList.visibility = View.GONE
        binding.noDataText.visibility = View.VISIBLE
        binding.noDataText.text = "거래 내역이 없습니다."
        ipInvestmentAdapter.updateData(emptyList())
    }
    
    private fun showInvestmentData(data: List<IpInvestmentItem>) {
        if (data.isNotEmpty()) {
            binding.recyclerViewInvestmentList.visibility = View.VISIBLE
            binding.noDataText.visibility = View.GONE
            ipInvestmentAdapter.updateData(data)
        } else {
            showEmptyState()
        }
    }

    private fun navigateToInvestmentFilter() {
        val filterFragment = InvestmentFilterFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, filterFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun scrollToTop() {
        binding.recyclerViewInvestmentList.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}