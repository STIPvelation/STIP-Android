import SwiftUI
import Foundation

// MARK: - Ticker Deposit Page
struct TiickerDepositPage: View {
    let transaction: Transaction
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var userData = UserData.shared
    @State private var selectedFilter: USDTransactionType = .all
    
    // Calculate total USD balance from all transactions
    private var totalUSDBalance: Double {
        var balance: Double = 0.0
        
        for transaction in userData.usdTransactions {
            let amount = extractUSDAmount(from: transaction.usdValue)
            
            switch transaction.type {
            case .deposit:
                balance += amount
            case .withdraw, .exchange:
                // Both completed withdrawals and pending withdrawal requests reduce balance
                balance -= amount
            default:
                // Other transaction types don't affect balance
                break
            }
        }
        
        return max(0.0, balance) // Ensure balance doesn't go negative
    }
    
    // Calculate total KRW balance from all transactions
    private var totalKRWBalance: Double {
        var balance: Double = 0.0
        
        for transaction in userData.usdTransactions {
            let amount = extractKRWAmount(from: transaction.amount)
            
            switch transaction.type {
            case .deposit:
                balance += amount
            case .withdraw:
                balance -= amount
            default:
                // Other transaction types don't affect balance
                break
            }
        }
        
        return max(0.0, balance) // Ensure balance doesn't go negative
    }
    
    // Helper function to extract USD amount from string (e.g., "0.37 USD" -> 0.37)
    private func extractUSDAmount(from usdString: String) -> Double {
        let components = usdString.components(separatedBy: " ")
        guard let firstComponent = components.first else { return 0 }
        
        // Replace commas with empty string and convert to Double
        let numberString = firstComponent.replacingOccurrences(of: ",", with: "")
        return Double(numberString) ?? 0.0
    }
    
    // Helper function to extract KRW amount from string (e.g., "500 KRW" -> 500)
    private func extractKRWAmount(from krwString: String) -> Double {
        let components = krwString.components(separatedBy: " ")
        guard let firstComponent = components.first else { return 0 }
        
        // Replace commas with empty string and convert to Double
        let numberString = firstComponent.replacingOccurrences(of: ",", with: "")
        return Double(numberString) ?? 0.0
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            ZStack {
                Text("총 보유")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(Color(red: 40/255, green: 40/255, blue: 60/255))
                    .frame(maxWidth: .infinity)
                
                HStack {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(Color(red: 40/255, green: 40/255, blue: 60/255))
                    }
                    Spacer()
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
            .padding(.bottom, 12)
            
            // USD Amount Card
            VStack(spacing: 8) {
                // Ticker Icon
                ZStack {
                    Circle()
                        .fill(tokenGradient(for: transaction.type))
                        .frame(width: 80, height: 80)
                        .shadow(color: tokenColor(for: transaction.type).opacity(0.3), radius: 4, x: 0, y: 2)
                    
                    Text(tokenLogoText(for: transaction.type))
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(.white)
                }
                .padding(.top, 20)
                
                // Ticker Value
                Text("\(transaction.amount) \(transaction.type)")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(Color(red: 40/255, green: 40/255, blue: 60/255))
                
                // USD Value
                Text(transaction.type.lowercased() == "jwv" && transaction.amount == 100 ? 
                     "1345 USD" : 
                     "\(String(format: "%.2f", transaction.totalUSD)) USD")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(red: 100/255, green: 100/255, blue: 120/255))
                    .padding(.bottom, 20)
                
                // Deposit & Withdraw Buttons
                HStack(spacing: 12) {
                    NavigationLink(destination: TiickerDepositPageDetail(transaction: transaction)) {
                        HStack {
                            Spacer()
                            HStack(spacing: 8) {
                                Image(systemName: "arrow.down")
                                    .font(.system(size: 16))
                                Text("입금")
                                    .font(.system(size: 16, weight: .semibold))
                            }
                            Spacer()
                        }
                        .foregroundColor(.white)
                        .padding(.vertical, 16)
                        .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                        .cornerRadius(12)
                    }
                    
                    NavigationLink(destination: TickerWithdrawalView(
                        selectedTicker: transaction.type,
                        tickerBalance: Double(transaction.amount), // Converting Int to Double
                        maxWithdrawalLimit: 1000000.0
                    )) {
                        HStack {
                            Spacer()
                            HStack(spacing: 8) {
                                Image(systemName: "arrow.up")
                                    .font(.system(size: 16))
                                Text("출금")
                                    .font(.system(size: 16, weight: .semibold))
                            }
                            Spacer()
                        }
                        .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                        .padding(.vertical, 16)
                        .background(Color.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(red: 48/255, green: 198/255, blue: 232/255), lineWidth: 1)
                        )
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 5)
            )
            .padding(.horizontal, 20)
            
            // Transaction History Section with modern filter bar
            VStack(alignment: .trailing, spacing: 16) {
                // Filter pills - center aligned
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 10) {
                        Spacer() // Left spacer for center alignment
                        
                        ForEach(USDTransactionType.orderedCases, id: \.self) { filter in
                            FilterPill(filter: filter, isSelected: selectedFilter == filter) {
                                withAnimation {
                                    selectedFilter = filter
                                }
                            }
                        }
                        
                        Spacer() // Right spacer for center alignment
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.top, 24)
                .padding(.bottom, 10)
                
                ScrollView {
                    VStack(spacing: 0) {
                        // Pre-filter transactions to simplify the ForEach expression
                        let filteredTransactions = userData.usdTransactions.filter { transaction in
                            filterTransaction(transaction, filter: selectedFilter)
                        }
                        
                        // Use the pre-filtered array in ForEach
                        ForEach(filteredTransactions, id: \.id) { transaction in
                            // Create TransactionHistoryRow with transaction properties
                            
                            TickerTransactionHistoryRow(
                                transactionId: transaction.id,
                                date: transaction.date,
                                time: transaction.time,
                                type: transaction.type.typeDisplayName,
                                isDeposit: transaction.type == .deposit,
                                amount: transaction.amount,
                                usdValue: transaction.usdValue,
                                ticker: self.transaction
                            )
                            
                            if transaction.id < userData.usdTransactions.count - 1 {
                                Divider()
                                    .padding(.horizontal, 20)
                            }
                        }
                    }
                    .background(Color.white)
                    .cornerRadius(12)
                    .padding(.horizontal, 2)
                    .padding(.bottom, 20)
                }
            }
            
            Spacer()
        }
        .navigationBarHidden(true)
        .background(Color.white.ignoresSafeArea())
    }
}

// Using the filter function from USDDetailView.swift
private func filterTransaction(_ transaction: USDTransactionInfo, filter: USDTransactionType) -> Bool {
    if filter == .all { return true }
    return transaction.type == filter
}

// Helper functions for token display
private func tokenLogoText(for tokenType: String) -> String {
    let components = tokenType.components(separatedBy: "/")
    if let firstPart = components.first, firstPart.count >= 2 {
        return String(firstPart.prefix(2))
    }
    return "$"
}

private func tokenColor(for tokenType: String) -> Color {
    if let firstPart = tokenType.components(separatedBy: "/").first {
        switch firstPart {
        case "JWV": return .orange
        case "MDM": return .green
        case "CDM": return .purple
        case "IJECT": return Color(red: 0.2, green: 0.5, blue: 0.8)
        case "WETALK": return .pink
        case "SLEEP": return Color(red: 0.1, green: 0.3, blue: 0.7)
        case "KCOT": return Color(red: 0.8, green: 0.3, blue: 0.2)
        case "MSK": return .indigo
        case "SMT": return Color(red: 0.5, green: 0.2, blue: 0.5)
        case "AXNO": return .brown
        case "KATV": return Color(red: 0.2, green: 0.6, blue: 0.4)
        default: return Color(red: 48/255, green: 198/255, blue: 232/255)
        }
    }
    return Color(red: 48/255, green: 198/255, blue: 232/255)
}

private func tokenGradient(for tokenType: String) -> LinearGradient {
    let color = tokenColor(for: tokenType)
    return LinearGradient(gradient: Gradient(colors: [color, color.opacity(0.7)]), startPoint: .topLeading, endPoint: .bottomTrailing)
}

// Custom Transaction History Row for Ticker Detail View
struct TickerTransactionHistoryRow: View {
    let transactionId: Int
    let date: String
    let time: String
    let type: String
    let isDeposit: Bool
    let amount: String
    let usdValue: String
    let ticker: Transaction // The ticker transaction from the parent view
    
    // Main view
    var body: some View {
        HStack(alignment: .center, spacing: 0) {
            dateTimeSection
            Spacer()
            typeSection
            Spacer()
            amountSection
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
    }
    
    // Date and time section
    private var dateTimeSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(date)
                .font(.system(size: 14))
                .foregroundColor(.black)
            
            Text(time)
                .font(.system(size: 13))
                .foregroundColor(Color.gray)
        }
        .frame(width: 80, alignment: .leading)
    }
    
    // Transaction type section with navigation to TickerDetailView
    private var typeSection: some View {
        // Right aligned - using HStack with spacer on left
        HStack {
            Spacer() // Push content to right
            
            NavigationLink(destination: TickerDetailView(transaction: ticker, isDeposit: isDeposit)) {
                HStack(spacing: 4) {
                    Circle()
                        .fill(isDeposit ? SwiftUI.Color(hex: "FF3B30").opacity(0.2) : SwiftUI.Color(hex: "007AFF").opacity(0.2))
                        .frame(width: 8, height: 8)
                    
                    Text(type)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(isDeposit ? SwiftUI.Color(hex: "FF3B30") : SwiftUI.Color(hex: "007AFF"))
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(isDeposit ? SwiftUI.Color(hex: "FF3B30").opacity(0.08) : SwiftUI.Color(hex: "007AFF").opacity(0.08))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(isDeposit ? SwiftUI.Color(hex: "FF3B30").opacity(0.2) : SwiftUI.Color(hex: "007AFF").opacity(0.2), lineWidth: 1)
                        )
                )
                .shadow(color: Color.black.opacity(0.03), radius: 4, x: 0, y: 2)
            }
        }
    }
    
    // Amount section
    private var amountSection: some View {
        VStack(alignment: .trailing, spacing: 2) {
            // Format as amount + ticker (e.g. "500 JWV") instead of KRW
            let amountValue = amount.components(separatedBy: " ").first ?? ""
            
            Text("\(amountValue) \(ticker.type)")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.black)
            
            Text(usdValue)
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
        .frame(width: 120, alignment: .trailing) // Increased width to accommodate longer text
    }
}

