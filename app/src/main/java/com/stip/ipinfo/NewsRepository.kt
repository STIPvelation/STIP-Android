package com.stip.stip.ipinfo

import android.util.Log
import com.stip.stip.ipinfo.model.NewsItem
import com.prof18.rssparser.RssParserBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object NewsRepository {
    private var cachedNews: List<NewsItem>? = null
    private var isLoading = false
    private val rssScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val rssSources = listOf(
        "https://www.e-patentnews.com/rss/rss_news.php" to 10,
        "https://ipwatchdog.com/feed" to 5,
        "https://www.epo.org/en/news-events/news/feed" to 5
    )

    suspend fun getNews(force: Boolean = false): List<NewsItem> {
        if (!force && cachedNews != null) return cachedNews!!
        return loadRssNews()
    }

    fun preloadNews() {
        if (cachedNews != null || isLoading) return
        isLoading = true
        rssScope.launch {
            try {
                loadRssNews()
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadRssNews(): List<NewsItem> = withContext(Dispatchers.IO) {
        val parser = RssParserBuilder(callFactory = OkHttpClient(), charset = Charsets.UTF_8).build()
        try {
            val deferredNews = rssSources.map { (url, limit) ->
                async {
                    try {
                        val channel = parser.getRssChannel(url)
                        channel.items.sortedByDescending { it.pubDate }.take(limit).map {
                            NewsItem(
                                title = it.title.orEmpty(),
                                date = calculateRelativeTime(it.pubDate),
                                link = it.link.orEmpty(),
                                pubDate = it.pubDate.orEmpty()
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("RSS", "Failed to parse $url", e)
                        emptyList()
                    }
                }
            }
            val combinedNews = deferredNews.awaitAll().flatten()
                .sortedByDescending { parsePubDate(it.pubDate) }
                .take(20)
            cachedNews = combinedNews
            return@withContext combinedNews
        } catch (e: Exception) {
            Log.e("RSS", "Loading failed", e)
            return@withContext emptyList()
        }
    }

    fun getCachedNews(): List<NewsItem>? = cachedNews

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
        val publishedTime = parsePubDate(pubDate) ?: return "방금 전"
        val now = System.currentTimeMillis()
        val diffMillis = now - publishedTime
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val pubCal = Calendar.getInstance().apply { timeInMillis = publishedTime }
        val nowCal = Calendar.getInstance()
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        return when {
            diffMinutes < 1 -> "방금 전"
            diffMinutes < 60 -> "${diffMinutes.toInt()}분 전"
            isSameDay(pubCal, nowCal) -> "${diffHours.toInt()}시간 전"
            isSameDay(pubCal, yesterdayCal) -> "어제 ${formatTime(pubCal)}"
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
}
