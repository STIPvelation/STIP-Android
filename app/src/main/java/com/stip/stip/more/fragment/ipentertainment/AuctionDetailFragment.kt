package com.stip.stip.more.fragment.ipentertainment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentAuctionDetailBinding
import com.stip.stip.more.fragment.ipentertainment.adapter.AuctionImageAdapter
import com.stip.stip.more.fragment.ipentertainment.data.AuctionModel
import com.stip.stip.more.fragment.ipentertainment.data.IpType
import com.stip.stip.more.fragment.ipentertainment.util.formatRemainingTime
import java.util.*

class AuctionDetailFragment : Fragment() {
    private var _binding: FragmentAuctionDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()
    
    // 전달된 경매 아이템 정보 (나중에 navArgs로 대체 예정)
    private lateinit var auction: AuctionModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuctionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 현재는 arguments로 전달된 데이터를 직접 가져옴 (나중에 navArgs로 변경 예정)
        arguments?.let { bundle ->
            // 테스트를 위한 임시 데이터
            auction = AuctionModel(
                id = bundle.getString("id") ?: "",
                title = bundle.getString("title") ?: "IP 경매 제목",
                description = bundle.getString("description") ?: "상세 설명",
                imageUrl = bundle.getString("imageUrl") ?: "",
                startPrice = bundle.getLong("startPrice", 5000),
                currentPrice = bundle.getLong("currentPrice", 7500),
                endTime = Date(bundle.getLong("endTime", System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)),
                bidCount = bundle.getInt("bidCount", 5),
                ipType = try {
                    IpType.valueOf(bundle.getString("ipType") ?: IpType.PATENT.name)
                } catch (e: Exception) {
                    IpType.PATENT
                },
                isFeatured = bundle.getBoolean("isFeatured", false),
                registrationNumber = bundle.getString("registrationNumber") ?: "제 10-2023-1234567호",
                viewCount = bundle.getInt("viewCount", 42)
            )
        } ?: run {
            // arguments가 null인 경우를 대비한 기본 데이터
            auction = AuctionModel(
                id = "default_id",
                title = "샘플 특허 경매",
                description = "이것은 샘플 경매 상세 설명입니다.",
                imageUrl = "",
                startPrice = 5000,
                currentPrice = 7500,
                endTime = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000),
                bidCount = 5,
                ipType = IpType.PATENT,
                isFeatured = false,
                registrationNumber = "제 10-2023-1234567호",
                viewCount = 42
            )
        }
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {


        
        // 이미지 ViewPager 어댑터 설정
        try {
            val imageAdapter = AuctionImageAdapter(auction.imageUrl)
            binding.imageViewPager.adapter = imageAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // IP 타입 태그 배경색 설정
        val tagBackgroundColor = when (auction.ipType) {
            IpType.PATENT -> R.color.ip_patent
            IpType.TRADEMARK -> R.color.ip_trademark
            IpType.COPYRIGHT -> R.color.ip_copyright
            IpType.DESIGN -> R.color.ip_design
            IpType.CHARACTER -> R.color.ip_character
            IpType.FRANCHISE -> R.color.ip_franchise
            IpType.MUSIC, IpType.MOVIE, IpType.OTHER -> R.color.ip_other
        }
        binding.ipTypeTag.setBackgroundColor(resources.getColor(tagBackgroundColor, null))
        binding.ipTypeTag.text = auction.ipType.displayName
        
        // 기본 정보 설정
        // Title text removed as requested to avoid duplicate headers
        binding.registrationNumberText.text = auction.registrationNumber
        
        // 등록일과 만료일 설정
        val dateFormat = java.text.SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
        binding.registrationDateText.text = dateFormat.format(auction.registrationDate)
        binding.expiryDateText.text = dateFormat.format(auction.expiryDate)
        
        // 가격 정보
        binding.startingPriceText.text = formatPrice(auction.startPrice)
        binding.currentPriceText.text = formatPrice(auction.currentPrice)
        binding.bottomCurrentPriceText.text = formatPrice(auction.currentPrice)
        
        // 남은 시간 및 입찰 참여자
        val remainingTimeText = formatRemainingTime(auction.endTime)
        binding.remainingTimeText.text = "남은 시간: $remainingTimeText"
        binding.bidCountText.text = "${auction.bidCount}명 참여"
        
        // 상세 정보
        binding.descriptionText.text = auction.description
        
        // 권리 관련 정보 (실제 데이터는 서버에서 받아와야 함)
        setupRightsIncluded()
        
        // 경매 통계 정보
        binding.viewCountText.text = "${auction.viewCount}명 조회"
    }
    
    private fun setupListeners() {
        // 툴바 관련 코드 삭제 - 사용하지 않음
        
        // 입찰 버튼
        binding.bidButton.setOnClickListener {
            showBidDialog()
        }
    }
    
    private fun showBidDialog() {
        // 입찰 다이얼로그 표시
        BidDialogFragment.newInstance(auction) { newBidAmount ->
            // 입찰 성공 시 currentPrice와 bidCount는 immutable이므로 직접 변경할 수 없음
            // 대신 새로운 auction 객체 생성 (copy 함수 사용)
            auction = auction.copy(
                currentPrice = newBidAmount,
                bidCount = auction.bidCount + 1
            )
            
            // UI 업데이트
            binding.currentPriceText.text = formatPrice(auction.currentPrice)
            binding.bottomCurrentPriceText.text = formatPrice(auction.currentPrice)
            binding.bidCountText.text = "${auction.bidCount}명 참여"
        }.show(childFragmentManager, "bid_dialog")
    }
    
    private fun setupRightsIncluded() {
        // 관련 권리 정보 표시 (실제로는 서버에서 받아온 데이터 사용)
        // 이 부분은 데이터 구조에 따라 달라질 수 있음
    }
    
    private fun formatPrice(price: Long): String {
        // Convert to dollar-based value by removing three zeros
        val dollarValue = price / 1000
        return String.format("$%,d", dollarValue)
    }
    
    override fun onResume() {
        super.onResume()
        // Set the header title and back button
        viewModel.updateHeaderTitle("IP옥션")
        viewModel.updateNavigationIcon(R.drawable.ic_arrow_back)
        viewModel.updateNavigationClickListener {
            // Use activity's supportFragmentManager instead of parentFragmentManager to avoid fragment not attached error
            activity?.supportFragmentManager?.popBackStack()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
