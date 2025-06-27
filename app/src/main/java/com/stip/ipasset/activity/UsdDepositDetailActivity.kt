package com.stip.ipasset.activity

import android.os.Bundle
import android.os.Build
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.stip.stip.databinding.ActivityUsdDepositDetailBinding
import com.stip.ipasset.usd.model.USDDepositTransaction

class UsdDepositDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUsdDepositDetailBinding
    private var transaction: USDDepositTransaction? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsdDepositDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get transaction data from intent
        transaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("transaction", USDDepositTransaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("transaction")
        }
        
        // Setup back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Set title
        binding.tvTitle.text = "USD 입금 상세"
        
        // Populate UI with transaction data
        displayTransactionDetails()
    }
    
    private fun displayTransactionDetails() {
        transaction?.let { tx ->
            // Display transaction details in the UI
            binding.tvTxIdValue.text = tx.id.toString()
            binding.tvAmount.text = tx.getFormattedUsdAmount()
            binding.tvKrwAmount.text = tx.getFormattedKrwAmount()
            binding.tvDate.text = "${tx.getFormattedDate()} ${tx.getFormattedTime()}"
            binding.tvStatus.text = "USD 입금"
            
            // Setup confirm button
            binding.btnConfirm.setOnClickListener {
                finish()
            }
        }
    }
}
