import SwiftUI
import UIKit

// MARK: - 티커 출금 확인 뷰
struct TickerWithdrawalConfirmationView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var userData = UserData.shared
    
    // 출금 정보 (이전 화면에서 전달)
    var selectedTicker: String
    var withdrawalAmount: Double
    var fee: Double = 1.00 // 고정 수수료 1.00
    var toAddress: String // 받는 주소
    
    @State private var isLoading = false
    @State private var showAlert = false
    @State private var alertType: AlertType = .none
    @State private var errorMessage = ""
    
    // Alert 타입 정의
    enum AlertType {
        case none
        case success
        case failure
    }
    
    init(selectedTicker: String, withdrawalAmount: Double, toAddress: String) {
        self.selectedTicker = selectedTicker
        self.withdrawalAmount = withdrawalAmount
        self.toAddress = toAddress
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // 1. 출금정보 확인 섹션
                    confirmationSection
                    
                    // 2. 총 출금수량 확인 섹션
                    totalWithdrawalSection
                    
                    // 3. 주의사항 섹션
                    noticeSection
                    
                    // 4. 출금하기 버튼
                    submitButton
                        .padding(.vertical, 24)
                }
                .padding(.horizontal, 24)
            }
            .padding(.top, 10)
            .background(Color.white.edgesIgnoringSafeArea(.all))
            // 연결된 NavigationView에서 페이지 최대 2개까지 팝해서 홈으로 돌아가기
            .alert(isPresented: $showAlert) {
                switch alertType {
                case .success:
                    return Alert(
                        title: Text("출금신청 완료"),
                        message: Text("출금신청이 정상적으로 처리되었습니다."),
                        dismissButton: .default(Text("출금 내역 상세")) {
                            // 티커 상세 페이지로 이동
                            navigateToTickerDetail()
                        }
                    )
                case .failure:
                    return Alert(
                        title: Text("출금신청 실패"),
                        message: Text(errorMessage),
                        dismissButton: .default(Text("확인"))
                    )
                case .none:
                    return Alert(title: Text(""), message: nil, dismissButton: .default(Text("확인")))
                }
            }
        }
        .navigationTitle("티커 출금신청")
        // 뒤로가기 버튼은 유지하되 아이콘이 표시되지 않도록 빈 컨텐츠로 수정
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { presentationMode.wrappedValue.dismiss() }) {
                    Color.clear
                        .frame(width: 0, height: 0)
                }
                .accessibility(label: Text("뒤로가기"))
            }
        }
    }
    
    // MARK: - UI Components
    
    // 1. 출금정보 확인 섹션
    var confirmationSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("출금 정보")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(Color(hex: "232D3F"))
            
            VStack(spacing: 18) {
                // 티커 정보
                HStack {
                    Text("티커")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                    
                    Spacer()
                    
                    Text(selectedTicker)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(hex: "232D3F"))
                }
                
                Divider().background(Color(hex: "F0F0F5"))
                
                // 출금수량
                HStack {
                    Text("출금수량")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                    
                    Spacer()
                    
                    Text("\(formatNumber(withdrawalAmount, minFraction: 2, maxFraction: 2)) \(selectedTicker)")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(hex: "232D3F"))
                }
                
                Divider().background(Color(hex: "F0F0F5"))
                
                // 출금수수료
                HStack {
                    Text("출금수수료")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                    
                    Spacer()
                    
                    Text("\(formatNumber(fee, minFraction: 2, maxFraction: 2)) \(selectedTicker)")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(hex: "232D3F"))
                }
                
                Divider().background(Color(hex: "F0F0F5"))
                
                // 출금 주소
                VStack(alignment: .leading, spacing: 12) {
                    Text("출금 주소")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                    
                    HStack {
                        Text(toAddress)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(Color(hex: "232D3F"))
                            .lineLimit(1)
                            .truncationMode(.middle)
                        
                        Spacer()
                        
                        Button(action: {
                            UIPasteboard.general.string = toAddress
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "doc.on.doc.fill")
                                    .font(.system(size: 14))
                                
                                Text("복사")
                                    .font(.system(size: 14, weight: .medium))
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(Color(hex: "30C6E8").opacity(0.15))
                            .cornerRadius(8)
                        }
                        .foregroundColor(Color(hex: "30C6E8"))
                    }
                }
            }
            .padding(24)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 4)
        }
    }
    
    // 2. 총 출금수량 확인 섹션
    var totalWithdrawalSection: some View {
        VStack(spacing: 20) {
            HStack {
                Text("총 출금수량")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
                
                Spacer()
                
                // 출금액 + 수수료 계산
                let totalAmount = withdrawalAmount + fee
                Text("\(formatNumber(totalAmount, minFraction: 2, maxFraction: 2))")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(Color(hex: "30C6E8"))
                
                Text(" \(selectedTicker)")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color(hex: "505060"))
            }
            
            HStack {
                Text("출금수량 + 수수료 포함")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8A8A8A"))
                Spacer()
            }
        }
        .padding(24)
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color.white, Color(hex: "F6FDFF")]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
    
    // 3. 주의사항 섹션
    var noticeSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(Color(hex: "FF6B6B").opacity(0.15))
                        .frame(width: 32, height: 32)
                    
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color(hex: "FF6B6B"))
                }
                
                Text("주의사항")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            
            VStack(alignment: .leading, spacing: 12) {
                noticeItem(icon: "xmark.circle.fill", text: "출금 신청 후에는 취소가 불가능합니다.")
                noticeItem(icon: "exclamationmark.circle.fill", text: "네트워크(Polygon)를 반드시 확인해주세요. 다른 네트워크로 출금 시 복구가 불가능합니다.")
                noticeItem(icon: "clock.fill", text: "출금 소요시간은 네트워크 상황에 따라 달라질 수 있습니다.")
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color(hex: "FFF5F5"), Color(hex: "FFF9F9")]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "FFDADA"), lineWidth: 1)
        )
        .shadow(color: Color(hex: "FF6B6B").opacity(0.05), radius: 8, x: 0, y: 4)
    }
    
    // 주의사항 항목 컴포넌트
    private func noticeItem(icon: String, text: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "FF6B6B"))
                .frame(width: 18)
            
            Text(text)
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(Color(hex: "232D3F"))
                .fixedSize(horizontal: false, vertical: true)
                .lineSpacing(2)
            
            Spacer()
        }
    }
    
    // 4. 출금하기 버튼
    var submitButton: some View {
        Button(action: {
            requestWithdrawal()
        }) {
            Group {
                if isLoading {
                    HStack(spacing: 10) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        Text("처리중...")
                            .font(.system(size: 18, weight: .bold))
                    }
                } else {
                    Text("출금 신청 확인")
                        .font(.system(size: 18, weight: .bold))
                }
            }
            .frame(maxWidth: .infinity, minHeight: 60)
            .foregroundColor(.white)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color(hex: "30C6E8"), Color(hex: "2D9CDB")]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(16)
            .shadow(color: Color(hex: "30C6E8").opacity(0.3), radius: 12, x: 0, y: 5)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.white.opacity(0.2), lineWidth: 1)
            )
        }
        .disabled(isLoading)
        .padding(.vertical, 8)
    }
    
    // MARK: - Helper Functions
    
    // 출금 신청 처리
    private func requestWithdrawal() {
        isLoading = true
        
        // 1. 티커 잔액 업데이트 (출금 수량만큼 차감)
        updateTickerBalance()
        
        // 2. 일일 출금한도 업데이트 (출금 금액만큼 사용량 증가)
        updateDailyWithdrawalLimit()
        
        // API 호출 시뮬레이션 (성공/실패 처리)
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            isLoading = false
            
            // 출금 완료 팝업 표시
            // 메인 스레드에서 상태 변경 확실히 적용
            DispatchQueue.main.async {
                self.alertType = .success
                self.showAlert = true
                print("\n\n출금 완료 팝업 표시: showAlert=\(self.showAlert), alertType=\(self.alertType)")
            }
            
            // 실패 시 예시
            // self.errorMessage = "네트워크 오류가 발생했습니다."
            // self.alertType = .failure
            // self.showAlert = true
            
            print("\n\n출금처리완료") // 디버깅용 로그
        }
    }
    
    // 티커 출금 상세 페이지로 이동
    private func navigateToTickerDetail() {
        // 현재 화면 닫기
        presentationMode.wrappedValue.dismiss()
        
        // 0.3초 뒤에 티커 출금 상세 페이지로 이동
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            NotificationCenter.default.post(
                name: NSNotification.Name("ShowTickerWithdrawalDetail"), 
                object: nil,
                userInfo: [
                    "selectedTicker": selectedTicker,
                    "withdrawalAmount": withdrawalAmount,
                    "fee": fee,
                    "toAddress": toAddress
                ]
            )
        }
    }
    
    // 티커 잔액 업데이트
    private func updateTickerBalance() {
        // 티커 트랜잭션에서 해당 티커를 찾아 잔액 업데이트
        if let index = userData.transactions.firstIndex(where: { $0.type == selectedTicker }) {
            let oldTransaction = userData.transactions[index]
            let currentAmount = oldTransaction.amount
            
            // 총 차감액 (출금액 + 수수료)
            let totalDeduction = withdrawalAmount + fee
            let newAmount = max(0, currentAmount - Int(totalDeduction))
            
            // 새 트랜잭션 생성
            let newTransaction = Transaction(
                type: oldTransaction.type,
                amount: newAmount,
                priceUSD: oldTransaction.priceUSD,
                tokenPrice: oldTransaction.tokenPrice,
                totalUSD: oldTransaction.priceUSD * Double(newAmount),
                contractAddress: oldTransaction.contractAddress,
                category: oldTransaction.category
            )
            
            // 변경된 트랜잭션으로 교체
            userData.transactions[index] = newTransaction
        }
    }
    
    // 일일 출금한도 업데이트
    private func updateDailyWithdrawalLimit() {
        // 티커의 USD 가치를 계산하여 일일 출금한도에서 차감
        if let ticker = userData.transactions.first(where: { $0.type == selectedTicker }) {
            // 티커의 USD 가격
            let tickerUsdPrice = ticker.priceUSD
            
            // 출금한 티커의 USD 가치 계산 (출금액 + 수수료)
            let totalDeduction = withdrawalAmount + fee
            let usdValueOfWithdrawal = tickerUsdPrice * totalDeduction
            
            // 일일 출금한도에서 사용량 증가
            userData.dailyWithdrawalUsed += usdValueOfWithdrawal
        }
    }
    
    // 홈 화면으로 이동
    private func navigateToHome() {
        // 여기에 홈 화면으로 이동하는 로직 구현
        // 예: rootNavigationController로 popToRoot 등
    }
    
    // 주의사항 텍스트 스타일
    private func noticeText(_ content: String) -> some View {
        Text("• " + content)
            .font(.system(size: 14))
            .foregroundColor(Color(hex: "505060"))
            .fixedSize(horizontal: false, vertical: true)
    }
    
    // 숫자 포맷팅 (천단위 구분 및 소수점 처리)
    private func formatNumber(_ number: Double, minFraction: Int = 0, maxFraction: Int = 2) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = minFraction
        formatter.maximumFractionDigits = maxFraction
        return formatter.string(from: NSNumber(value: number)) ?? "\(number)"
    }
}

// MARK: - Preview
struct TickerWithdrawalConfirmationView_Previews: PreviewProvider {
    static var previews: some View {
        TickerWithdrawalConfirmationView(
            selectedTicker: "KATV",
            withdrawalAmount: 100.0,
            toAddress: "0x71C7656EC7ab88b0d40B5f76F"
        )
    }
}



// 사용자 지정 뷰 크기 (width, height)
struct SizedBox: View {
    var width: CGFloat?
    var height: CGFloat?
    
    var body: some View {
        Rectangle()
            .fill(Color.clear)
            .frame(width: width, height: height)
    }
}
