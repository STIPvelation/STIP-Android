package com.stip.ipasset.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stip.stip.databinding.ActivityUsdDepositDetailBinding
import com.stip.stip.ipasset.model.USDDepositTransaction

class UsdDepositDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUsdDepositDetailBinding
    private var transaction: USDDepositTransaction? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsdDepositDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get transaction data from intent
        transaction = intent.getParcelableExtra("transaction")
        
        // Setup back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
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
