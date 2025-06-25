import SwiftUI

struct USDWithdrawalConfirmationView: View {
    @ObservedObject private var userData = UserData.shared
    @Environment(\.presentationMode) var presentationMode
    
    // 완료 팝업을 보여주기 위한 상태
    @State private var showingCompletionAlert = false
    
    let withdrawalAmount: String
    let fixedFee: Double
    
    var body: some View {
        VStack(spacing: 0) {
            // 커스텀 네비게이션 바
            Text("USD 출금하기")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(Color(hex: "232D3F"))
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, 16)
                .padding(.bottom, 16)
                .background(Color.white)
                .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
            
            // 메인 콘텐츠
            ScrollView {
                VStack(spacing: 24) {
                    // 안내 메시지
                    HStack {
                        Text("확인을 진행하면 출금신청이 완료되고, 취소가 불가능합니다.")
                            .font(.system(size: 16))
                            .foregroundColor(Color(hex: "505060"))
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .background(Color(hex: "F8FAFF"))
                    .cornerRadius(12)
                    
                    // 출금신청 확인 타이틀
                    Text("USD 출금신청 확인")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(Color(hex: "30C6E8"))
                        .padding(.vertical, 8)
                        .frame(maxWidth: .infinity)
                    
                    // 출금 상세정보 카드
                    VStack(spacing: 0) {
                        HStack {
                            Text("출금계좌")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "505060"))
                            Spacer()
                            Text(maskAccountNumber(userData.bankAccountNumber))
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color(hex: "232D3F"))
                        }
                        .padding(.vertical, 16)
                        
                        Divider().background(Color(hex: "F0F0F5"))
                        
                        HStack {
                            Text("출금금액")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "505060"))
                            Spacer()
                            Text("\(formatNumber(Double(cleanAmount(withdrawalAmount)) ?? 0)) USD")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color(hex: "232D3F"))
                        }
                        .padding(.vertical, 16)
                    }
                    .padding(.horizontal, 20)
                    .background(Color.white)
                    .cornerRadius(12)
                    .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 4)
                    
                    // 참고 사항
                    VStack(alignment: .leading, spacing: 12) {
                        Text("한 번 더 확인해 주세요")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color(hex: "232D3F"))
                        
                        Text("본인 명의의 STIP 계정을 타인의 요청이나 지시에 따라 대여할 경우 범죄 처벌 대상이 될 수 있으며, 실명 인증된 계정을 공유하면 개인정보 보호를 침해하게 됩니다. 또한, 출금 전산 시간대별 23:30~00:30)에는 출금 서비스 이용이 원활하지 않을 수 있으며, VPN을 사용하여 접속할 경우 출금이 보류되거나 제한될 수 있습니다.")
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "505060"))
                            .lineSpacing(4)
                        
                        Text("특정 거래는 정밀한 점검 출금이 지연될 수도 있습니다. 이후의, 당사의 이상 거래 감지 결과에 따라 출금 신청 시 거절 및 일출금자 확인을 위한 서류 제출을 요청할 수 있으며, 이에 따라 출금이 지연될 수 있습니다.")
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "505060"))
                            .lineSpacing(4)
                        
                        Text("서류 검토 결과에 따라 출금이 승인되지 않을 수도 있으므로 유의하시기 바랍니다.")
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "505060"))
                            .lineSpacing(4)
                    }
                    .padding(20)
                    .background(Color(hex: "F8FAFF"))
                    .cornerRadius(12)
                    
                    // 최종 확인 버튼
                    Button(action: {
                        // 최종 출금 요청 처리
                        completeWithdrawal()
                    }) {
                        Text("확인")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 18)
                            .background(
                                LinearGradient(
                                    gradient: Gradient(colors: [Color(hex: "30C6E8"), Color(hex: "2BAED8")]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .cornerRadius(16)
                            .shadow(color: Color(hex: "30C6E8").opacity(0.3), radius: 8, x: 0, y: 4)
                    }
                }
                .padding(24)
            }
        }
        .background(Color.white.edgesIgnoringSafeArea(.all))
        .navigationBarHidden(true)
        .alert(isPresented: $showingCompletionAlert) {
            Alert(
                title: Text("출금신청 완료"),
                message: Text("출금신청 이 성공적으로 처리되었습니다."),
                dismissButton: .default(Text("확인")) {
                    // USD 상세 페이지로 이동
                    navigateToUSDDetail()
                }
            )
        }
    }
    
    // MARK: - Helper Functions
    
    // USD 출금 상세 페이지로 이동
    private func navigateToUSDDetail() {
        // 현재 화면 닫기
        self.presentationMode.wrappedValue.dismiss()
        
        // 0.3초 뒤에 USD 출금 상세 페이지로 이동
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            // 출금 정보를 포함하여 이벤트 발생
            NotificationCenter.default.post(
                name: NSNotification.Name("ShowUSDWithdrawalDetail"), 
                object: nil,
                userInfo: [
                    "withdrawalAmount": Double(self.withdrawalAmount) ?? 0.0,
                    "accountNumber": self.userData.bankAccountNumber,
                    "bankName": self.userData.bankName
                ]
            )
        }
    }
    
    // 홈 화면으로 이동 (이전 기능 유지)
    private func navigateToHome() {
        self.presentationMode.wrappedValue.dismiss()
    }
    
    private func maskAccountNumber(_ accountNumber: String) -> String {
        let components = accountNumber.split(separator: "-")
        if components.count >= 3 {
            let bankCode = components[0]
            let middle = components[1]
            let accountNumber = components[2]
            
            if accountNumber.count >= 4 {
                let lastFourDigits = accountNumber.suffix(4)
                let maskedPart = String(repeating: "*", count: accountNumber.count - 4)
                return "\(userData.bankName) \(bankCode)-\(middle)-\(maskedPart)\(lastFourDigits)"
            }
        }
        return accountNumber
    }
    
    private func formatNumber(_ number: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSNumber(value: number)) ?? String(format: "%.2f", number)
    }
    
    private func cleanAmount(_ amount: String) -> String {
        return amount.replacingOccurrences(of: ",", with: "")
    }
    
    private func completeWithdrawal() {
        // 출금 금액 (콤마 제거 후) 파싱
        let cleanedAmount = cleanAmount(withdrawalAmount)
        if let amountValue = Double(cleanedAmount) {
            // 출금 가능 금액에서 출금 금액 차감 (수수료 포함)
            let totalDeduction = amountValue + fixedFee
            
            // 1. UserData에서 USD 잔액을 찾아 업데이트
            if let index = userData.transactions.firstIndex(where: { $0.type == "USD" }) {
                let oldTransaction = userData.transactions[index]
                let currentAmount = oldTransaction.amount
                let newAmount = max(0, currentAmount - Int(totalDeduction))
                
                // 새로운 Transaction 객체 생성 (immutable한 amount 속성 대신 바뀐 값으로 새로 생성)
                let newTransaction = Transaction(
                    type: oldTransaction.type,
                    amount: newAmount,
                    priceUSD: oldTransaction.priceUSD,
                    tokenPrice: oldTransaction.tokenPrice,
                    totalUSD: Double(newAmount),  // USD의 경우 1:1 비율이라 totalUSD는 amount과 동일
                    contractAddress: oldTransaction.contractAddress,
                    category: oldTransaction.category
                )
                
                // 변경된 Transaction으로 교체
                userData.transactions[index] = newTransaction
            }
            
            // 2. 일일 출금한도 업데이트 (사용한 출금금액 증가)
            userData.dailyWithdrawalUsed += totalDeduction
            
            // 실제 서비스에서는 API 호출을 통해 서버에 출금 요청을 보내는 로직이 추가될 수 있음
            // API 호출 코드...
        }
        
        // 완료 팝업 보여주기
        self.showingCompletionAlert = true
    }
}

// Using existing Color(hex:) extension from the project
