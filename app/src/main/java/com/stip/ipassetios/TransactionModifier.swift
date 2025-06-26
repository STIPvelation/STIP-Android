import SwiftUI

struct TransactionDetailViewModifier: ViewModifier {
    @State private var showUSDWithdrawalDetail = false
    @State private var showTickerWithdrawalDetail = false
    
    // USD 출금 정보
    @State private var usdWithdrawalAmount: Double = 0.0
    @State private var accountNumber: String = ""
    @State private var bankName: String = ""
    
    // 티커 출금 정보
    @State private var selectedTicker: String = ""
    @State private var tickerWithdrawalAmount: Double = 0.0
    @State private var tickerFee: Double = 0.0
    @State private var toAddress: String = ""
    
    func body(content: Content) -> some View {
        content
            .onAppear {
                setupNotifications()
            }
            .onDisappear {
                removeNotifications()
            }
            .fullScreenCover(isPresented: $showUSDWithdrawalDetail) {
                NavigationView {
                    USDWithdrawalDetailView(
                        withdrawalAmount: usdWithdrawalAmount,
                        accountNumber: accountNumber,
                        bankName: bankName
                    )
                }
            }
            .fullScreenCover(isPresented: $showTickerWithdrawalDetail) {
                NavigationView {
                    TickerWithdrawalDetailView(
                        selectedTicker: selectedTicker,
                        withdrawalAmount: tickerWithdrawalAmount,
                        fee: tickerFee,
                        toAddress: toAddress
                    )
                }
            }
    }
    
    // 알림 설정
    private func setupNotifications() {
        // USD 출금 상세 알림 수신
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name("ShowUSDWithdrawalDetail"),
            object: nil,
            queue: .main
        ) { notification in
            if let userInfo = notification.userInfo,
               let amount = userInfo["withdrawalAmount"] as? Double,
               let account = userInfo["accountNumber"] as? String,
               let bank = userInfo["bankName"] as? String {
                self.usdWithdrawalAmount = amount
                self.accountNumber = account
                self.bankName = bank
                self.showUSDWithdrawalDetail = true
            }
        }
        
        // 티커 출금 상세 알림 수신
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name("ShowTickerWithdrawalDetail"),
            object: nil,
            queue: .main
        ) { notification in
            if let userInfo = notification.userInfo,
               let ticker = userInfo["selectedTicker"] as? String,
               let amount = userInfo["withdrawalAmount"] as? Double,
               let fee = userInfo["fee"] as? Double,
               let address = userInfo["toAddress"] as? String {
                self.selectedTicker = ticker
                self.tickerWithdrawalAmount = amount
                self.tickerFee = fee
                self.toAddress = address
                self.showTickerWithdrawalDetail = true
            }
        }
    }
    
    // 알림 제거
    private func removeNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
}

// ViewModifier를 쉽게 사용할 수 있는 View Extension
extension View {
    func withTransactionDetailNavigation() -> some View {
        self.modifier(TransactionDetailViewModifier())
    }
}
