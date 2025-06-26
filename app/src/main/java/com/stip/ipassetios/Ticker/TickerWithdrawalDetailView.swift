import SwiftUI

struct TickerWithdrawalDetailView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var userData = UserData.shared
    
    // 출금 정보
    let selectedTicker: String
    let withdrawalAmount: Double
    let fee: Double
    let toAddress: String
    let withdrawalDate: Date
    let status: String = "처리중" // 기본 상태
    
    // USD 가격 계산 (UserData에서 가져오기)
    private var usdPrice: Double {
        if let transaction = userData.transactions.first(where: { $0.type == selectedTicker }) {
            return transaction.priceUSD
        }
        return 0.0
    }
    
    // 총 USD 가치
    private var totalUSDValue: Double {
        return withdrawalAmount * usdPrice
    }
    
    init(selectedTicker: String, withdrawalAmount: Double, fee: Double = 1.0, toAddress: String) {
        self.selectedTicker = selectedTicker
        self.withdrawalAmount = withdrawalAmount
        self.fee = fee
        self.toAddress = toAddress
        self.withdrawalDate = Date() // 현재 시간을 출금 시간으로 설정
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // 커스텀 네비게이션 바
            customNavigationBar
            
            ScrollView {
                VStack(spacing: 24) {
                    // 헤더 정보 (상태 및 금액)
                    headerSection
                    
                    Divider()
                        .padding(.vertical, 8)
                    
                    // 출금 상세 정보
                    detailSection
                }
                .padding(.horizontal, 24)
            }
            .padding(.top, 16)
        }
        .background(Color.white.edgesIgnoringSafeArea(.all))
        .navigationBarHidden(true)
    }
    
    // MARK: - UI Components
    
    // 커스텀 네비게이션 바
    var customNavigationBar: some View {
        HStack {
            Button(action: {
                presentationMode.wrappedValue.dismiss()
            }) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            
            Spacer()
            
            Text("\(selectedTicker) 출금 상세")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(Color(hex: "232D3F"))
            
            Spacer()
            
            // 우측 여백을 위한 투명 버튼
            Button(action: {}) {
                Image(systemName: "arrow.left")
                    .font(.system(size: 22))
                    .foregroundColor(.clear)
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
    }
    
    // 헤더 섹션 (상태 및 금액)
    var headerSection: some View {
        VStack(spacing: 16) {
            // 상태 표시
            HStack {
                Text(status)
                    .font(.system(size: 14, weight: .medium))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(Color(hex: "E9F7FC"))
                    .foregroundColor(Color(hex: "30C6E8"))
                    .cornerRadius(4)
                
                Spacer()
                
                // 날짜 표시
                Text(formatDate(withdrawalDate))
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "6B7684"))
            }
            
            // 금액 표시
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(formatNumber(withdrawalAmount)) \(selectedTicker)")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                    
                    Text("$\(formatNumber(totalUSDValue))")
                        .font(.system(size: 14))
                        .foregroundColor(Color(hex: "6B7684"))
                }
                
                Spacer()
            }
        }
    }
    
    // 출금 상세 정보 섹션
    var detailSection: some View {
        VStack(spacing: 24) {
            // 출금 정보 아이템들
            detailItem(title: "출금 수단", value: selectedTicker)
            detailItem(title: "출금 수량", value: formatNumber(withdrawalAmount))
            detailItem(title: "수수료", value: "\(formatNumber(fee)) \(selectedTicker)")
            detailItem(title: "총 출금액", value: "\(formatNumber(withdrawalAmount + fee)) \(selectedTicker)")
            detailItem(title: "출금 주소", value: maskAddress(toAddress), multiline: true)
            detailItem(title: "예상 출금 시간", value: "2~3일 (영업일 기준)")
        }
    }
    
    // 상세 정보 아이템
    func detailItem(title: String, value: String, multiline: Bool = false) -> some View {
        HStack(alignment: multiline ? .top : .center) {
            Text(title)
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "6B7684"))
                .frame(width: 100, alignment: .leading)
            
            Spacer()
            
            Text(value)
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "232D3F"))
                .multilineTextAlignment(.trailing)
                .lineLimit(multiline ? nil : 1)
        }
    }
    
    // MARK: - Helper Functions
    
    // 날짜 포맷팅
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy. MM. dd HH:mm"
        return formatter.string(from: date)
    }
    
    // 숫자 포맷팅 (천단위 구분 및 소수점 처리)
    private func formatNumber(_ number: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSNumber(value: number)) ?? "\(number)"
    }
    
    // 주소 마스킹 처리
    private func maskAddress(_ address: String) -> String {
        if address.count <= 15 {
            return address
        }
        
        // 앞 8자리와 뒤 8자리만 표시
        let prefix = String(address.prefix(8))
        let suffix = String(address.suffix(8))
        return "\(prefix)...\(suffix)"
    }
}

// MARK: - Preview
struct TickerWithdrawalDetailView_Previews: PreviewProvider {
    static var previews: some View {
        TickerWithdrawalDetailView(
            selectedTicker: "KATV",
            withdrawalAmount: 100.0,
            fee: 1.0,
            toAddress: "0x1234567890abcdef0123456789abcdef01234567"
        )
    }
}
