import SwiftUI
import Foundation

// MARK: - USD Detail View
struct USDDetailView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var userData = UserData.shared
    let usdAmount: Int
    @State private var selectedFilter: USDTransactionType = .all
    
    // USD 보유량은 UserData의 transactions에서 가져옴
    private var totalUSDBalance: Double {
        // UserData의 USD 트랜잭션에서 총액 가져오기
        let usdTransaction = userData.transactions.first(where: { $0.type == "USD" })
        return usdTransaction?.totalUSD ?? 0.0
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
                // USD Icon
                ZStack {
                    Circle()
                        .fill(LinearGradient(gradient: Gradient(colors: [
                            Color(red: 62/255, green: 180/255, blue: 232/255),
                            Color(red: 48/255, green: 198/255, blue: 252/255)
                        ]), startPoint: .topLeading, endPoint: .bottomTrailing))
                        .frame(width: 80, height: 80)
                        .shadow(color: Color(red: 48/255, green: 198/255, blue: 232/255).opacity(0.3), radius: 4, x: 0, y: 2)
                    
                    Text("$")
                        .font(.system(size: 36, weight: .bold))
                        .foregroundColor(.white)
                }
                .padding(.top, 20)
                
                // USD Value
                Text("\(String(format: "%.2f", totalUSDBalance)) USD")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(Color(red: 40/255, green: 40/255, blue: 60/255))
                
                // KRW Value
                Text("≈ 13,218,000 KRW")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(red: 100/255, green: 100/255, blue: 120/255))
                    .padding(.bottom, 20)
                
                // Deposit & Withdraw Buttons
                HStack(spacing: 12) {
                    NavigationLink(destination: KRWDepositView()) {
                        HStack(spacing: 8) {
                            Image(systemName: "arrow.down")
                                .font(.system(size: 16))
                            Text("입금")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                        .cornerRadius(12)
                    }
                    
                    NavigationLink(destination: USDWithdrawalView()) {
                        HStack(spacing: 8) {
                            Image(systemName: "arrow.up")
                                .font(.system(size: 16))
                            Text("출금")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                        .frame(maxWidth: .infinity)
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
                    HStack(spacing: 8) {
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
                            TransactionHistoryRow(
                                transactionId: transaction.id,
                                date: transaction.date,
                                time: transaction.time,
                                type: transaction.type.typeDisplayName,
                                isDeposit: transaction.type == .deposit,
                                amount: transaction.amount,
                                usdValue: transaction.usdValue
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

// Using USDTransactionType from UserData.swift

// Function to filter transactions
private func filterTransaction(_ transaction: USDTransactionInfo, filter: USDTransactionType) -> Bool {
    if filter == .all { return true }
    return transaction.type == filter
}

// Filter Pill Component
struct FilterPill: View {
    let filter: USDTransactionType
    let isSelected: Bool
    let action: () -> Void
    
    // 배경색 계산
    private var backgroundColor: SwiftUI.Color {
        isSelected ? SwiftUI.Color(hex: "30C6E8") : SwiftUI.Color.white
    }
    
    // 텍스트색 계산
    private var textColor: SwiftUI.Color {
        isSelected ? .white : SwiftUI.Color(hex: "555555")
    }
    
    // 그림자 설정
    private var shadowRadius: CGFloat {
        isSelected ? 4 : 2
    }
    
    private var shadowY: CGFloat {
        isSelected ? 2 : 1
    }
    
    var body: some View {
        Button(action: action) {
            pillContent
        }
    }
    
    private var pillContent: some View {
        Text(filter.displayName)
            .font(.system(size: 14, weight: isSelected ? .semibold : .regular))
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .frame(minWidth: 60) // Set minimum width for consistent sizing
            .background(
                Capsule()
                    .fill(backgroundColor)
                    .shadow(color: Color.black.opacity(isSelected ? 0.1 : 0.05), 
                            radius: shadowRadius, x: 0, y: shadowY)
            )
            .foregroundColor(textColor)
            .animation(.easeInOut(duration: 0.2), value: isSelected)
    }
}

// Transaction History Row for USD Detail View
struct TransactionHistoryRow: View {
    let transactionId: Int
    let date: String
    let time: String
    let type: String
    let isDeposit: Bool
    let amount: String
    let usdValue: String
    
    // 기본 뷰
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
    
    // 날짜 및 시간 섹션
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
    
    // 거래 유형 섹션 (모던 UI)
    private var typeSection: some View {
        NavigationLink(destination: WithdrawalDetailView(transactionId: transactionId)) {
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
    
    // Amount
    private var amountSection: some View {
        VStack(alignment: .trailing, spacing: 4) {
            Text(usdValue)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.black)
            
            Text(amount)
                .font(.system(size: 13))
                .foregroundColor(Color.gray)
        }
        .frame(width: 80, alignment: .trailing)
    }
}

// Preview
struct USDDetailView_Previews: PreviewProvider {
    static var previews: some View {
        USDDetailView(usdAmount: 0)
    }
}
