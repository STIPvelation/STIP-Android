package com.stip.stip.ipasset.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment // Ensure correct import
import com.stip.stip.R // Import your R class
import com.stip.stip.databinding.FragmentPhoneFraudAlertDialogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Consider inheriting directly from DialogFragment if BaseDialogFragment doesn't add crucial functionality here
class PhoneFraudAlertDialogFragment : DialogFragment() { // Changed inheritance for clarity

    private lateinit var viewBinding: FragmentPhoneFraudAlertDialogBinding // Use lateinit

    // SharedPreferences constants
    private val PREFS_NAME = "app_prefs_phone_fraud" // Using a more specific name is good practice
    private val KEY_LAST_SUPPRESS_DATE = "last_suppress_date_phone_fraud" // Specific key

    // Date formatter for storing/comparing YYYY-MM-DD
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentPhoneFraudAlertDialogBinding.inflate(inflater, container, false)

        // --- Check if dialog should be shown ---
        // It's generally better to do this check *before* showing the dialog,
        // but within the DialogFragment lifecycle, we check here before setting up listeners.
        // If you control showing the dialog (e.g., fragmentManager.show()),
        // perform this check *before* calling show().
        if (!shouldShowDialog(requireContext())) {
            // Dismiss immediately if shouldn't show today
            // Using dismissAllowingStateLoss is safer if the check happens early in lifecycle
            dismissAllowingStateLoss()
            // Return the inflated view anyway, but it won't be fully shown
            // Or potentially return a placeholder/empty view if dismiss is immediate
            // return View(context) // Alternative: Return an empty view to avoid listener setup
        }

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Proceed with setup only if the dialog should be shown (redundant check if dismissed in onCreateView)
        // 수정된 코드 (56번째 줄)
        if (isStateSaved) {
            return
        }

        setupListeners()
        setupDialogAppearance()

        // Set texts or other initial states if needed
        viewBinding.title.text = getString(R.string.phone_fraud_alert_title)
    }

    private fun setupListeners() {
        // Left Button ("Don't show again today")
        viewBinding.btnCancel.setOnClickListener {
            setDontShowAgainToday(requireContext()) // Save today's date
            dismiss()
        }

        // Right Button ("Confirm")
        viewBinding.btnConfirm.setOnClickListener {
            // Add any specific confirmation action here if needed
            dismiss()
        }
    }

    private fun setupDialogAppearance() {
        // Optional: Set dialog window properties like size, gravity etc.
        // This can also be done via styles applied to the DialogFragment theme
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Example: dialog?.window?.setGravity(Gravity.BOTTOM)
    }


    // --- "Don't show again today" Logic ---

    private fun getCurrentDateString(): String {
        return dateFormat.format(Date())
    }

    // Function to check if the dialog should be displayed
    private fun shouldShowDialog(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSuppressDate = sharedPreferences.getString(KEY_LAST_SUPPRESS_DATE, null)
        val currentDate = getCurrentDateString()

        // Show the dialog IF no date is stored OR the stored date is NOT today's date
        val show = lastSuppressDate != currentDate
        // Log.d(TAG, "Should show dialog? Last suppress: $lastSuppressDate, Current: $currentDate, Show: $show") // Optional logging
        return show
    }

    // Function to save today's date when "Don't show again" is clicked
    private fun setDontShowAgainToday(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentDate = getCurrentDateString()
        editor.putString(KEY_LAST_SUPPRESS_DATE, currentDate)
        editor.apply()
        // Log.d(TAG, "Setting don't show again for date: $currentDate") // Optional logging
    }


    companion object {
        const val TAG = "PhoneFraudAlertFrag" // Define a TAG for logging/finding the fragment

        // --- Static check function ---
        // Allows checking *before* creating/showing the fragment instance
        fun shouldShow(context: Context): Boolean {
            // Create a temporary instance of the logic holder to check
            // Or make the check logic truly static if possible (depends on context needs)
            val checker = PhoneFraudAlertDialogFragment() // Temporary instance for logic access
            return checker.shouldShowDialog(context.applicationContext) // Use application context
        }


        fun newInstance(): PhoneFraudAlertDialogFragment {
            return PhoneFraudAlertDialogFragment()
        }
    }
}