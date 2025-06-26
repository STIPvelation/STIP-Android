import SwiftUI

struct USDWithdrawalDetailView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var userData = UserData.shared
    
    // 출금 정보
    let withdrawalAmount: Double
    let withdrawalDate: Date
    let accountNumber: String
    let bankName: String
    let status: String = "처리중" // 기본 상태
    
    init(withdrawalAmount: Double, accountNumber: String, bankName: String) {
        self.withdrawalAmount = withdrawalAmount
        self.withdrawalDate = Date() // 현재 시간을 출금 시간으로 설정
        self.accountNumber = accountNumber
        self.bankName = bankName
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
            
            Text("출금신청 상세")
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
                Text("$\(formatNumber(withdrawalAmount))")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(Color(hex: "232D3F"))
                
                Spacer()
            }
        }
    }
    
    // 출금 상세 정보 섹션
    var detailSection: some View {
        VStack(spacing: 24) {
            // 출금 정보 아이템들
            detailItem(title: "출금 수단", value: "USD")
            detailItem(title: "출금 금액", value: "$\(formatNumber(withdrawalAmount))")
            detailItem(title: "수수료", value: "$1.00") // 예시 수수료
            detailItem(title: "은행", value: bankName)
            detailItem(title: "계좌번호", value: maskAccountNumber(accountNumber))
            detailItem(title: "예상 출금 시간", value: "즉시 반영")
        }
    }
    
    // 상세 정보 아이템
    func detailItem(title: String, value: String) -> some View {
        HStack(alignment: .top) {
            Text(title)
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "6B7684"))
                .frame(width: 100, alignment: .leading)
            
            Spacer()
            
            Text(value)
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "232D3F"))
                .multilineTextAlignment(.trailing)
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
    
    // 계좌번호 마스킹 처리
    private func maskAccountNumber(_ accountNumber: String) -> String {
        let components = accountNumber.split(separator: "-")
        if components.count >= 3 {
            // 앞부분과 뒷부분만 보여주고 중간은 * 처리
            return "\(components[0])-****-\(components[components.count-1])"
        }
        
        // 일반적인 마스킹 처리 (뒷 4자리만 표시)
        if accountNumber.count > 4 {
            let lastFour = accountNumber.suffix(4)
            let maskedPart = String(repeating: "*", count: accountNumber.count - 4)
            return "\(maskedPart)\(lastFour)"
        }
        
        return accountNumber
    }
}



// MARK: - Preview
struct USDWithdrawalDetailView_Previews: PreviewProvider {
    static var previews: some View {
        USDWithdrawalDetailView(
            withdrawalAmount: 1000.0,
            accountNumber: "123-456-789",
            bankName: "국민은행"
        )
    }
}
