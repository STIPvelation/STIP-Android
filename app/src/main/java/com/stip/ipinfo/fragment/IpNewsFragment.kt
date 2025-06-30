package com.stip.stip.ipinfo.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.stip.stip.MainActivity
import com.stip.stip.R
import com.stip.stip.ScrollableToTop
import com.stip.stip.databinding.FragmentIpNewsBinding
import com.stip.stip.ipinfo.DipListingInfoFragment
import com.stip.stip.ipinfo.adapter.DipNewsAdapter
import com.stip.stip.ipinfo.adapter.IpToonAdapter
import com.stip.stip.ipinfo.adapter.NewsPageAdapter
import com.stip.stip.ipinfo.model.DipNewsItem
import com.stip.stip.ipinfo.model.IpToonItem
import com.stip.stip.ipinfo.model.NewsItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class IpNewsFragment : Fragment(), ScrollableToTop {

    private lateinit var mainViewModel: com.stip.stip.MainViewModel

    private var _binding: FragmentIpNewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var newsPageAdapter: NewsPageAdapter
    private var timeUpdateJob: Job? = null
    private var currentBreakingNews: NewsItem? = null
    private val switchHandler = Handler(Looper.getMainLooper())
    private var currentNewsIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.stip.stip.MainViewModel::class.java]
        mainViewModel.memberInfo.observe(viewLifecycleOwner) { memberInfo ->
            // TODO: 회원정보를 UI에 반영하는 코드 작성
        }

        super.onViewCreated(view, savedInstanceState)

        newsPageAdapter = NewsPageAdapter(this) { newsItem -> openLink(newsItem.link) }
        binding.viewpagerNews.adapter = newsPageAdapter
        binding.viewpagerNews.isSaveEnabled = false

        binding.viewpagerNews.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDotIndicator(position)
            }
        })

        binding.textSingleNewsSwiper.isSelected = true
        binding.textSingleNewsSwiper.setOnClickListener {
            currentBreakingNews?.let { openLink(it.link) }
        }

        binding.navigateNextDipListingInfo.setOnClickListener {
            (requireActivity() as? MainActivity)?.hideHeaderAndTabs()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DipListingInfoFragment())
                .addToBackStack(null)
                .commit()
        }

        // NewsRepository에서 미리 캐싱된 뉴스가 있으면 바로 표시, 아니면 비동기로 로딩
        val cached = com.stip.stip.ipinfo.NewsRepository.getCachedNews()
        if (cached != null) {
            displayNews(cached)
        } else {
            lifecycleScope.launch {
                val news = com.stip.stip.ipinfo.NewsRepository.getNews()
                displayNews(news)
            }
        }

        setupDipNewsRecyclerView()
        startAutoUpdateTime()
    }

    private val switchRunnable = object : Runnable {
        override fun run() {
            val currentList = com.stip.stip.ipinfo.NewsRepository.getCachedNews() ?: return
            if (currentList.isNotEmpty()) {
                currentBreakingNews = currentList[currentNewsIndex]
                binding.textSingleNewsSwiper.text = currentBreakingNews?.title ?: ""
                currentNewsIndex = (currentNewsIndex + 1) % currentList.size
            }
            switchHandler.postDelayed(this, 4000)
        }
    }

    private fun updateDotIndicator(position: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index == position) R.drawable.ic_dot_active else R.drawable.ic_dot_inactive
            )
        }
    }

    private fun displayNews(newsList: List<NewsItem>) {
        val newsPages = newsList.chunked(5)
        newsPageAdapter.submitList(newsPages)

        currentBreakingNews = newsList.firstOrNull()
        binding.textSingleNewsSwiper.text = currentBreakingNews?.title ?: ""
        switchHandler.removeCallbacks(switchRunnable)
        switchHandler.post(switchRunnable)
    }

    private fun openLink(link: String?) {
        if (link == null) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error_open_link_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun parsePubDate(pubDate: String?): Long? {
        if (pubDate.isNullOrBlank()) return null
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.ENGLISH).parse(pubDate)?.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun calculateRelativeTime(pubDate: String?): String {
        val publishedTime = parsePubDate(pubDate) ?: return getString(R.string.time_just_now)
        val now = System.currentTimeMillis()
        val diffMillis = now - publishedTime
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)

        val pubCal = Calendar.getInstance().apply { timeInMillis = publishedTime }
        val nowCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        return when {
            diffMinutes < 1 -> getString(R.string.time_just_now)
            diffMinutes < 60 -> getString(R.string.time_minutes_ago, diffMinutes.toInt())
            isSameDay(pubCal, nowCal) -> getString(R.string.time_hours_ago, diffHours.toInt())
            isSameDay(pubCal, yesterdayCal) -> getString(R.string.time_yesterday, formatTime(pubCal))
            else -> formatTime(pubCal)
        }
    }

    private fun formatTime(cal: Calendar): String {
        return SimpleDateFormat("a h:mm", Locale.KOREAN).format(cal.time)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun setupDipNewsRecyclerView() {
        val context = requireContext()

        val dipNewsItems = listOf(
            DipNewsItem(
                logoResId = null,
                name = "ETIP AI",
                description = context.getString(R.string.dip_listing_info_etipai),
                date = "방금 전"
            ),
            DipNewsItem(
                logoResId = null,
                name = "ETIP 제조업",
                description = context.getString(R.string.dip_listing_info_etip1),
                date = "10분 전"
            ),
            DipNewsItem(
                logoResId = null,
                name = "ETIP 반도체",
                description = context.getString(R.string.dip_listing_info_etipsct),
                date = "20분 전"
            ),
            DipNewsItem(
                logoResId = null,
                name = "ETIP 전기차",
                description = context.getString(R.string.dip_listing_info_patent),
                date = "1시간 전"
            ),
            DipNewsItem(
                logoResId = null,
                name = "ETIP 신재생",
                description = context.getString(R.string.dip_listing_info_etipre),
                date = "어제"
            )
        )

        binding.recyclerViewDipNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = DipNewsAdapter(dipNewsItems)
        }
    }

    private fun startAutoUpdateTime() {
        timeUpdateJob?.cancel()
        timeUpdateJob = lifecycleScope.launch {
            while (isActive) {
                delay(60_000)
                com.stip.stip.ipinfo.NewsRepository.getCachedNews()?.let { original ->
                    val updated = original.map {
                        it.copy(date = calculateRelativeTime(it.pubDate))
                    }
                    displayNews(updated)
                }
            }
        }
    }

    override fun scrollToTop() {
        _binding?.scrollView?.smoothScrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timeUpdateJob?.cancel()
        switchHandler.removeCallbacks(switchRunnable)
    }

    companion object {
        @JvmStatic
        fun newInstance(): IpNewsFragment = IpNewsFragment()
    }
}