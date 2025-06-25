package com.stip.stip.ipasset

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.stip.stip.R
import com.stip.stip.databinding.ActivityTransactionBinding
import com.stip.stip.ipasset.model.IpAsset
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityTransactionBinding.inflate(layoutInflater)

        val ipAsset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(IpAsset.NAME, IpAsset::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(IpAsset.NAME)
        }
            ?: run {
                finish()
                return
            }

        val navHost = NavHostFragment.create(
            R.navigation.nav_graph_ip_asset,
            bundleOf(IpAsset.KEY to ipAsset)
        )

        supportFragmentManager.commit {
            replace(R.id.fragment_container_view, navHost)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navController = navHost.navController

                    if (!navController.popBackStack()) {
                        finish()
                    }
                }
            }
        )

        setContentView(viewBinding.root)
    }
}
