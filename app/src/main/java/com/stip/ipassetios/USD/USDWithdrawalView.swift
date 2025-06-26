import SwiftUI
import UIKit

struct USDWithdrawalView: View {
    @ObservedObject private var userData = UserData.shared
    @Environment(\.presentationMode) var presentationMode
    
    @State private var withdrawalAmount: String = ""
    @State private var showingConfirmation = false
    @State private var navigateToConfirmation = false
    @State private var isAmountFieldFocused: Bool = false // @FocusState 대신 @State 사용
    @State private var showingLimitInfo = false // 출금한도 정보 팝업을 보여주기 위한 상태
    @State private var showingAvailableInfo = false // 출금가능 정보 팝업을 보여주기 위한 상태
    @State private var showingLimitExceededAlert = false // 출금한도 초과 알림 팝업
    
    // 수수료 정보
    let fixedFee: Double = 1.00
    
    private var totalWithFee: Double {
        guard let amountValue = Double(withdrawalAmount), !amountValue.isNaN else {
            return fixedFee
        }
        return amountValue + fixedFee
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                customNavigationBar
                
                ScrollView {
                    VStack(spacing: 24) {
                        accountInfoSection
                        withdrawalAmountSection
                        feeInfoSection
                        Button(action: {
                            // 키보드가 열려 있으면 닫기
                            if isAmountFieldFocused {
                                isAmountFieldFocused = false
                            }
                            
                            let validationResult = isValidWithdrawal()
                            if validationResult.isValid {
                                // 최종 확인 화면으로 넘어가기
                                self.navigateToConfirmation = true
                            } else if validationResult.isLimitExceeded {
                                // 출금한도 초과 알림 표시
                                self.showingLimitExceededAlert = true
                            }
                        }) {
                            Text("출금신청")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 18)
                                .background(Color(hex: "30C6E8"))
                                .cornerRadius(16)
                                .shadow(color: Color(hex: "30C6E8").opacity(0.2), radius: 6, x: 0, y: 3)
                        }
                        .disabled(!isValidWithdrawal().isValid)
                        .opacity(isValidWithdrawal().isValid ? 1.0 : 0.7)
                        .padding(.vertical, 24)
                    }
                    .padding(.horizontal, 24)
                }
                .padding(.top, 10)
                .background(Color.white.edgesIgnoringSafeArea(.all)) // 전체 배경 흰색
            }
            .navigationBarHidden(true)
            .navigationDestination(isPresented: $navigateToConfirmation) {
                USDWithdrawalConfirmationView(withdrawalAmount: withdrawalAmount, fixedFee: fixedFee)
            }
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    self.isAmountFieldFocused = true
                }
            }
            .sheet(isPresented: $showingLimitInfo) {
                WithdrawalLimitInfoView(isPresented: $showingLimitInfo)
            }
            .sheet(isPresented: $showingAvailableInfo) {
                AvailableWithdrawalInfoView(isPresented: $showingAvailableInfo)
            }
            .alert(isPresented: $showingLimitExceededAlert) {
                Alert(
                    title: Text("출금 한도 초과"),
                    message: Text("일일 출금 가능 금액을 초과하였습니다. 남은 출금 가능 금액: \(formatNumber(userData.dailyWithdrawalLimit - userData.dailyWithdrawalUsed)) USD"),
                    dismissButton: .default(Text("확인"))
                )
            }
        }
    }
    
    // MARK: - Subviews
    
    var customNavigationBar: some View {
        ZStack {
            HStack {
                Spacer()
            }
            .padding(.horizontal, 24)
            
            Text("USD 출금하기")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(Color(hex: "232D3F"))
        }
        .padding(.vertical, 16)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
    }
    
    var accountInfoSection: some View {
        VStack(spacing: 0) {
            // 실명계좌 row
            HStack(alignment: .center) {
                // Left side with icon and label
                HStack(spacing: 10) {
                    Image(systemName: "building.columns.fill")
                        .font(.system(size: 16))
                        .foregroundColor(Color(hex: "30C6E8"))
                        .frame(width: 20, height: 20)
                    
                    Text("실명계좌")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                }
                .frame(width: 120, alignment: .leading)
                
                Spacer()
                
                Text(maskAccountNumber(userData.bankAccountNumber))
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .padding(.vertical, 16)
            
            // 출금가능 row
            HStack(alignment: .center) {
                // Left side with icon, label and info button
                HStack(spacing: 10) {
                    Image(systemName: "dollarsign.circle.fill")
                        .font(.system(size: 16))
                        .foregroundColor(Color(hex: "30C6E8"))
                        .frame(width: 20, height: 20)
                    
                    Text("출금가능")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                    
                    // 출금가능 정보 아이콘
                    Button(action: {
                        showingAvailableInfo = true
                    }) {
                        Image(systemName: "exclamationmark.circle.fill")
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "30C6E8"))
                    }
                }
                .frame(width: 120, alignment: .leading)
                
                Spacer()
                
                Text("\(formatUSD(userData.transactions.first(where: { $0.type == "USD" })?.amount ?? 0)) USD")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .padding(.vertical, 16)
            
            // 출금한도 row
            HStack(alignment: .center) {
                // Left side with icon, label and info button
                HStack(spacing: 10) {
                    Image(systemName: "chart.line.uptrend.xyaxis")
                        .font(.system(size: 16))
                        .foregroundColor(Color(hex: "30C6E8"))
                        .frame(width: 20, height: 20)
                    
                    Text("출금한도")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(Color(hex: "505060"))
                    
                    // 출금한도 정보 아이콘
                    Button(action: {
                        showingLimitInfo = true
                    }) {
                        Image(systemName: "exclamationmark.circle.fill")
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "30C6E8"))
                    }
                }
                .frame(width: 120, alignment: .leading)
                
                Spacer()
                
                Text("\(formatNumber(userData.dailyWithdrawalLimit - userData.dailyWithdrawalUsed)) USD")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .padding(.vertical, 16)
        }
        .padding(20)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
    }
    
    // 출금 금액 입력 섹션 (수정된 버전)
    var withdrawalAmountSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 10) {
                Image(systemName: "creditcard")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(hex: "30C6E8"))
                
                Text("출금금액")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .padding(.bottom, 5)
            
            HStack {
                // 새로 만든 FocusableTextField 사용
                FocusableTextField(text: $withdrawalAmount, placeholder: "최소 1", isFocused: isAmountFieldFocused, onDone: {
                    // 키보드 완료 버튼 눌렸을 때 처리
                    isAmountFieldFocused = false
                })
                
                Spacer()
                
                Text("USD")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(Color(hex: "30C6E8"))
            }
            .padding()
            .frame(height: 64)
            // 하단 라인 제거하고 배경 단순화
            .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isAmountFieldFocused ? Color(hex: "30C6E8").opacity(0.5) : Color(hex: "F0F0F5"), lineWidth: 1)
            )
            .contentShape(Rectangle())
            .onTapGesture {
                isAmountFieldFocused = true
            }
    
        }
        .padding(20)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
    }

    var feeInfoSection: some View {
        // ... (이전 코드와 동일)
        VStack(spacing: 20) {
            VStack(spacing: 16) {
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "percent").font(.system(size: 14)).foregroundColor(Color(hex: "30C6E8"))
                        Text("수수료 (부가세 포함)").font(.system(size: 15, weight: .medium)).foregroundColor(Color(hex: "505060"))
                    }
                    Spacer()
                    Text("1.00 USD").font(.system(size: 15, weight: .semibold)).foregroundColor(Color(hex: "232D3F"))
                }
                
                Divider().background(Color(hex: "F0F0F5")).padding(.horizontal, -20)
                
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "sum").font(.system(size: 14)).foregroundColor(Color(hex: "30C6E8"))
                        Text("총출금 (수수료 포함)").font(.system(size: 15, weight: .medium)).foregroundColor(Color(hex: "505060"))
                    }
                    Spacer()
                    Text("\(formatNumber(max(0, totalWithFee))) USD").font(.system(size: 18, weight: .bold)).foregroundColor(Color(hex: "30C6E8"))
                }
            }
            
            HStack(spacing: 6) {
                Image(systemName: "exclamationmark.circle.fill").font(.system(size: 12)).foregroundColor(Color(hex: "888899"))
                Text("출금 신청 이후 취소가 불가합니다.").font(.system(size: 12)).foregroundColor(Color(hex: "888899"))
            }
        }
        .padding(20)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
    }

    var submitButton: some View {
        // ... (이전 코드와 동일)
        Button(action: {
            // 키보드가 열려 있으면 닫기
            if isAmountFieldFocused {
                isAmountFieldFocused = false
            }
            
            let validationResult = isValidWithdrawal()
            if validationResult.isValid {
                // 최종 확인 화면으로 넘어가기
                self.navigateToConfirmation = true
            } else if validationResult.isLimitExceeded {
                // 출금한도 초과 알림 표시
                self.showingLimitExceededAlert = true
            }
        }) {
            Text("출금신청")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 18)
                .background(Color(hex: "30C6E8"))
                .cornerRadius(16)
                .shadow(color: Color(hex: "30C6E8").opacity(0.2), radius: 6, x: 0, y: 3)
        }
        .background(Color.white)
        .disabled(!isValidWithdrawal().isValid)
        .opacity(isValidWithdrawal().isValid ? 1.0 : 0.7)
        .padding(.vertical, 24)
    }
    
    // MARK: - Helper Functions

    func maskAccountNumber(_ accountNumber: String) -> String {
        // ... (이전 코드와 동일)
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
    
    func formatUSD(_ value: Int) -> String {
        // ... (이전 코드와 동일)
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        return numberFormatter.string(from: NSNumber(value: value)) ?? "\(value)"
    }
    
    func isValidWithdrawal() -> (isValid: Bool, isLimitExceeded: Bool) {
        // 입력된 금액에서 콤마 제거 후 Double로 변환
        let cleanAmount = withdrawalAmount.replacingOccurrences(of: ",", with: "")
        guard !cleanAmount.isEmpty, let amountValue = Double(cleanAmount), amountValue > 0 else {
            return (false, false)
        }
        
        // 1. USD 잔액 검사
        let availableBalance = Double(userData.transactions.first(where: { $0.type == "USD" })?.amount ?? 0)
        if amountValue + fixedFee > availableBalance {
            return (false, false) // 잔액 부족
        }
        
        // 2. 일일 출금 한도 검사
        let dailyLimit = userData.dailyWithdrawalLimit
        let usedToday = userData.dailyWithdrawalUsed
        let remaining = dailyLimit - usedToday
        
        // 출금하려는 금액이 남은 한도를 초과하는지 검사
        if amountValue + fixedFee > remaining {
            return (false, true) // 한도 초과
        }
        
        return (true, false) // 유효한 출금
    }
    
    // 숫자에 3자리마다 콤마를 추가하는 함수
    func formatNumber(_ number: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSNumber(value: number)) ?? String(format: "%.2f", number)
    }
}

// 미리보기
// 출금한도 정보 뷰
struct WithdrawalLimitInfoView: View {
    @Binding var isPresented: Bool
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 0) {
                // 팝업 헤더
                HStack {
                    Text("출금한도 안내")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                    
                    Spacer()
                    
                    Button(action: {
                        isPresented = false
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color(hex: "9098A0"))
                            .padding(8)
                            .background(Circle().fill(Color(hex: "F5F9FF")))
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)
                .padding(.bottom, 16)
                
                Divider()
                    .background(Color(hex: "F0F0F5"))
                    .padding(.horizontal, 20)
                
                // 내용
                VStack(spacing: 20) {
                    // 한도 정보 테이블
                    VStack(spacing: 16) {
                        limitInfoRow(title: "1회 출금 한도", value: "100,000 USD", icon: "speedometer")
                        limitInfoRow(title: "1일 출금 한도", value: "500,000 USD", icon: "calendar")
                    }
                    .padding(20)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Color(hex: "F8FAFF")))
                    
                    // 참고 사항
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: "info.circle.fill")
                            .font(.system(size: 16))
                            .foregroundColor(Color(hex: "30C6E8"))
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("출금 대기 금액은 1일 한도에서 차감됩니다.")
                                .font(.system(size: 15))
                                .foregroundColor(Color(hex: "505060"))
                                .lineSpacing(4)
                        }
                        Spacer()
                    }
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)
                
                Spacer()
                
                // 확인 버튼
                Button(action: {
                    isPresented = false
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
                .padding(.horizontal, 24)
                .padding(.bottom, 24)
            }
            .frame(width: UIScreen.main.bounds.width * 0.9)
            .background(
                RoundedRectangle(cornerRadius: 24)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.15), radius: 16, x: 0, y: 5)
            )
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    // 한도 정보 행 컴포넌트
    private func limitInfoRow(title: String, value: String, icon: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(.white)
                .frame(width: 32, height: 32)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [Color(hex: "30C6E8"), Color(hex: "2BAED8")]),
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .cornerRadius(8)
            
            Text(title)
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "505060"))
            
            Spacer()
            
            Text(value)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(Color(hex: "232D3F"))
        }
    }
}

// 출금가능 정보 뷰
struct AvailableWithdrawalInfoView: View {
    @Binding var isPresented: Bool
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 0) {
                // 팝업 헤더
                HStack {
                    Text("출금가능 금액 안내")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                    
                    Spacer()
                    
                    Button(action: {
                        isPresented = false
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color(hex: "9098A0"))
                            .padding(8)
                            .background(Circle().fill(Color(hex: "F5F9FF")))
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)
                .padding(.bottom, 16)
                
                Divider()
                    .background(Color(hex: "F0F0F5"))
                    .padding(.horizontal, 20)
                
                // 내용
                VStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("회원님 상황 정보에 따라 1회 한도가 변동될 수 있으므로, 최대 한도를 제한할 수도 있습니다.")
                            .font(.system(size: 15))
                            .foregroundColor(Color(hex: "505060"))
                            .lineSpacing(4)
                        
                        Text("은행 계좌 상황 및 고객임의 여부에 따라 1회 한도가 변경될 수 있으며, 최대 출금을 제한할 수도 있습니다.")
                            .font(.system(size: 15))
                            .foregroundColor(Color(hex: "505060"))
                            .lineSpacing(4)
                    }
                    .padding(20)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Color(hex: "F8FAFF")))
                    
                    // 참고 사항
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: "info.circle.fill")
                            .font(.system(size: 16))
                            .foregroundColor(Color(hex: "30C6E8"))
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("출금 대기 중인 금액은 출금가능 금액에서 제외됩니다.")
                                .font(.system(size: 15))
                                .foregroundColor(Color(hex: "505060"))
                                .lineSpacing(4)
                        }
                        Spacer()
                    }
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)
                
                Spacer()
                
                // 확인 버튼
                Button(action: {
                    isPresented = false
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
                .padding(.horizontal, 24)
                .padding(.bottom, 24)
            }
            .frame(width: UIScreen.main.bounds.width * 0.9)
            .background(
                RoundedRectangle(cornerRadius: 24)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.15), radius: 16, x: 0, y: 5)
            )
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct USDWithdrawalView_Previews: PreviewProvider {
    static var previews: some View {
        USDWithdrawalView()
    }
}
