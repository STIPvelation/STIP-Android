package com.stip.stip.iphome.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.stip.stip.R

class TradingChartFragment : Fragment() {

    private var tradingViewWebView: WebView? = null
    private var ticker: String? = null

    companion object {
        private const val ARG_TICKER = "ticker"

        fun newInstance(ticker: String?): TradingChartFragment {
            return TradingChartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TICKER, ticker)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val passedTicker = arguments?.getString(ARG_TICKER)
        ticker = formatTicker(passedTicker)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_trading_chart, container, false)
        tradingViewWebView = view.findViewById(R.id.tradingViewWebView)
        tradingViewWebView?.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
            }
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            loadTradingViewChart(ticker ?: "NASDAQ:AAPL")
        }
        return view
    }



    fun updateTicker(newTicker: String?) {
        if (newTicker.isNullOrBlank()) return
        ticker = formatTicker(newTicker)
        loadTradingViewChart(ticker!!)
    }

    private fun formatTicker(raw: String?): String {
        if (raw.isNullOrBlank()) return "NASDAQ:AAPL"
        return "${raw.uppercase()}/USD"
    }

    private fun loadTradingViewChart(symbol: String) {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                html, body { margin:0; padding:0; height:100%; overflow:hidden; }
                #tv_chart_container { width:100%; height:100%; }
              </style>
            </head>
            <body>
              <div id="tv_chart_container"></div>
              <script src="https://s3.tradingview.com/tv.js"></script>
              <script>
                new TradingView.widget({
                  "container_id": "tv_chart_container",
                  "width": "100%",
                  "height": "100%",
                  "symbol": "$symbol",
                  "interval": "D",
                  "timezone": "Asia/Seoul",
                  "theme": "light",
                  "style": "1",
                  "locale": "en",
                  "toolbar_bg": "#f1f3f6",
                  "enable_publishing": false,
                  "hide_legend": false,
                  "save_image": false,
                  "studies": [],
                  "show_popup_button": false
                });

                // 실시간 체결 반영용 함수 (데모)
                window.updateChart = function(price, volume, side, timestamp) {
                  // 실제 TradingView Charting Library에서는 데이터피드에 push 필요
                  // 이 데모에선 단순히 콘솔 및 바 색상 효과만 예시
                  var color = side === 'buy' ? '#FF4B4B' : '#3B7CFF';
                  document.body.style.transition = 'background 0.2s';
                  document.body.style.background = color;
                  setTimeout(function() {
                    document.body.style.background = '#fff';
                  }, 200);
                  console.log('실시간 체결:', price, volume, side, timestamp);
                }
              </script>
            </body>
            </html>
        """.trimIndent()

        tradingViewWebView?.loadDataWithBaseURL(
            "https://s3.tradingview.com/",
            html,
            "text/html",
            "utf-8",
            null
        )
    }

    override fun onDestroyView() {
        tradingViewWebView?.destroy()
        tradingViewWebView = null
        super.onDestroyView()
    }
}