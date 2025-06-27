package com.stip.ipasset.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stip.stip.databinding.ActivityUsdWithdrawalDetailBinding
import com.stip.stip.ipasset.model.USDWithdrawalTransaction

class UsdWithdrawalDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUsdWithdrawalDetailBinding
    private var transaction: USDWithdrawalTransaction? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsdWithdrawalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get transaction data from intent
        transaction = intent.getParcelableExtra("transaction")
        
        // Setup back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        
        // Set title
        binding.tvTitle.text = "USD 출금 상세"
        
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
            binding.tvStatus.text = "USD 출금"
            
            // Additional withdrawal-specific info could be added here
            // If we have views for these in the layout, we can uncomment and use them
            // binding.tvRecipientAddress?.text = tx.recipientAddress ?: "-"
            // binding.tvFee?.text = String.format("%.2f USD", tx.fee)
            
            // Setup confirm button
            binding.btnConfirm.setOnClickListener {
                finish()
            }
        }
    }
}
