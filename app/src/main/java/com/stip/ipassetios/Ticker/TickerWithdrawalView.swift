import SwiftUI
import UIKit


// MARK: - 티커 출금 뷰
struct TickerWithdrawalView: View {
    @ObservedObject private var userData = UserData.shared // 실제 사용 하는 싱글톤 인스턴스
    @Environment(\.presentationMode) var presentationMode // 뒤로가기를 위한 presentationMode
    
    @State private var withdrawalAmount: String = ""
    @State private var withdrawalAddress: String = "" // 출금 주소 입력 필드
    @State private var navigateToConfirmation = false
    @State private var isAmountFieldFocused: Bool = false
    @State private var isAddressFieldFocused: Bool = false // 주소 입력 필드 포커스 상태
    @State private var errorMessage: String? = nil
    @State private var showingLimitExceededAlert = false // 출금한도 초과 알림
    
    // 현재 선택된 티커 정보 (외부에서 주입)
    var selectedTicker: String // 티커 이름(필수)
    var tickerBalance: Double // 현재 티커 잔액
    var maxWithdrawalLimit: Double // 최대 출금 한도
    private let withdrawalFee: Double = 1.00 // 고정 수수료 1.00 티커
    
    // 명시적 초기화 추가
    init(selectedTicker: String, tickerBalance: Double, maxWithdrawalLimit: Double) {
        self.selectedTicker = selectedTicker
        self.tickerBalance = tickerBalance
        self.maxWithdrawalLimit = maxWithdrawalLimit
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                // 배경 그라디언트
                LinearGradient(gradient: Gradient(colors: [Color.white, Color(hex: "FAFBFF")]),
                               startPoint: .top, endPoint: .bottom)
                .ignoresSafeArea()
                
                ScrollView {
                    VStack(spacing: 16) {
                        withdrawalInfoSection
                        withdrawalAmountSection
                        withdrawalAddressSection
                        feeInfoSection
                        noticeSection
                        submitButton
                            .padding(.top, 24)
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 16)
                }
                .onTapGesture {
                    isAmountFieldFocused = false // 다른 곳 탭하면 키보드 닫기
                }
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text("\(selectedTicker) 출금하기")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "252A31"))
                }
                
                // 뒤로가기 버튼 아이콘 제거 (기능은 유지)
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { presentationMode.wrappedValue.dismiss() }) {
                        Color.clear
                            .frame(width: 0, height: 0)
                    }
                    .accessibility(label: Text("뒤로가기"))
                }
            }
            .navigationDestination(isPresented: $navigateToConfirmation) {
                if let amount = Double(withdrawalAmount.replacingOccurrences(of: ",", with: "")) {
                    TickerWithdrawalConfirmationView(
                        selectedTicker: selectedTicker,
                        withdrawalAmount: amount,
                        toAddress: withdrawalAddress // 사용자가 입력한 출금 주소 전달
                    )
                }
            }
            .alert("출금 한도 초과", isPresented: $showingLimitExceededAlert) {
                Button("확인") {}
            } message: {
                Text("일일 출금 가능 금액을 초과하였습니다.\n남은 출금 가능 금액: \(formatNumber(userData.dailyWithdrawalLimit - userData.dailyWithdrawalUsed)) USD")
            }
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    isAmountFieldFocused = true
                }
            }
        }
    }
    
    // MARK: - Subviews
    
    // 1. 출금 정보 섹션
    var withdrawalInfoSection: some View {
        VStack(spacing: 0) {
            // 출금가능 row
            HStack(alignment: .center) {
                Text("출금가능")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(Color(hex: "505060"))
                
                Spacer()
                
                Text("\(formatNumber(tickerBalance, minFraction: 2, maxFraction: 2)) \(selectedTicker)")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .padding(.vertical, 16)
            
            // 출금한도 row
            HStack(alignment: .center) {
                Text("출금한도")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(Color(hex: "505060"))
                
                Spacer()
                
                Text("\(formatNumber(maxWithdrawalLimit, maxFraction: 0)) \(selectedTicker)")
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
    
    // 2. 출금 수량 입력 섹션
    var withdrawalAmountSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 10) {
                Image(systemName: "creditcard")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(hex: "30C6E8"))
                
                Text("출금수량")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .padding(.bottom, 5)
            
            HStack {
                TextField("", text: $withdrawalAmount, onEditingChanged: { focused in
                    isAmountFieldFocused = focused
                    if !focused {
                        // 포커스가 해제된 때 유효성 업데이트
                        _ = isValidWithdrawal()
                    }
                })
                
                Spacer()
                
                Text(selectedTicker)
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(Color(hex: "30C6E8"))
            }
            .padding()
            .frame(height: 64)
            .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isAmountFieldFocused ? Color(hex: "30C6E8").opacity(0.5) : Color(hex: "F0F0F5"), lineWidth: 1)
            )
            .contentShape(Rectangle())
            .onTapGesture {
                isAmountFieldFocused = true
            }
            
            // 수량 선택 버튼
            HStack(spacing: 12) {
                ForEach([10, 25, 50, 100], id: \.self) { percentage in
                    Button(action: {
                        setAmount(percentage: Double(percentage))
                    }) {
                        Text("\(percentage)%")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(hex: "30C6E8"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(Color(hex: "30C6E8").opacity(0.1))
                            .cornerRadius(8)
                    }
                }
            }
        }
        .padding(20)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
    }
    
    // 3. 수수료 정보 섹션
    var feeInfoSection: some View {
        VStack(spacing: 16) {
            // 수수료 row
            HStack(alignment: .center) {
                Text("수수료 (부가세 포함)")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(Color(hex: "505060"))
                
                Spacer()
                
                Text("1.00 \(selectedTicker)")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "30C6E8")) // 푸른색으로 표시
            }
            
            // 부가세 row
            HStack(alignment: .center) {
                Text("총출금 (수수료 포함)")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(Color(hex: "505060"))
                
                Spacer()
                
                // 총 출금액(입력값 + 수수료) 계산
                Text(calculateTotalWithdrawal())
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "30C6E8")) // 푸른색으로 표시
            }
            
            // 오류 메시지 표시
            if let error = errorMessage {
                Text(error)
                    .font(.system(size: 14))
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 4)
            }
        }
        .padding(20)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
    }
    
    // 출금 주소 입력 섹션
    var withdrawalAddressSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 10) {
                Image(systemName: "link")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(hex: "30C6E8"))
                
                Text("출금 주소")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            
            // 출금 주소 입력 필드 - 가시성 개선
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.white)
                    .frame(height: 60)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(isAddressFieldFocused ? Color(hex: "30C6E8").opacity(0.5) : Color(hex: "F0F0F5"), lineWidth: 1)
                    )
                
                TextField("출금 주소를 입력해주세요", text: $withdrawalAddress)
                    .font(.system(size: 16))
                    .padding(.horizontal, 15)
                    .frame(height: 60)
                    .cornerRadius(8)
                    .accentColor(Color(hex: "30C6E8"))
            }
            .contentShape(Rectangle())
            .onTapGesture {
                isAddressFieldFocused = true
                isAmountFieldFocused = false
            }
        }
        .padding(20)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 4)
    }
    
    // 5. 유의사항 섹션
    var noticeSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(spacing: 10) {
                Image(systemName: "info.circle.fill")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Color(hex: "505060"))
                Text("입금 유의사항")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            
            VStack(alignment: .leading, spacing: 8) {
                noticeText("최소 출금 수량은 1 \(selectedTicker)입니다.")
                noticeText("네트워크(Polygon)를 반드시 확인해주세요. 다른 네트워크로 출금 시 복구가 불가능합니다.")
                noticeText("출금 소요시간은 네트워크 상황에 따라 달라질 수 있습니다.")
            }
            .padding(.leading, 26) // 아이콘 너비만큼 들여쓰기
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color(hex: "F8FAFF")))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "F0F0F5"), lineWidth: 1))
    }

    // 5. 출금신청 버튼
    var submitButton: some View {
        Button(action: {
            if isAmountFieldFocused {
                isAmountFieldFocused = false
            }
            
            let validationResult = isValidWithdrawal()
            if validationResult.isValid {
                self.navigateToConfirmation = true
            } else if validationResult.isLimitExceeded {
                self.showingLimitExceededAlert = true
            }
        }) {
            // 버튼 디자인은 동일하게 유지
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
        .opacity(isValidWithdrawal().isValid ? 1.0 : 0.5)
    }
    
    // MARK: - Helper Functions
    
    func noticeText(_ text: String) -> some View {
        HStack(alignment: .top, spacing: 6) {
            Text("•")
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "888899"))
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "505060"))
                .lineSpacing(4)
        }
    }
    
    func setAmount(percentage: Double) {
        // 100% 선택 시 수수료를 미리 차감
        var amount: Double
        if percentage == 100.0 {
            // 출금가능 최대값은 잔액에서 수수료를 미리 차감
            amount = tickerBalance - withdrawalFee
        } else {
            amount = tickerBalance * (percentage / 100.0)
        }
        
        // 0보다 작으면 0으로 설정
        if amount < 0 {
            amount = 0
        }
        
        // 정수로 변환하여 소수점 이하를 버림
        DispatchQueue.main.async {
            self.withdrawalAmount = String(Int(amount))
        }
    }
    
    func isValidWithdrawal() -> (isValid: Bool, isLimitExceeded: Bool) {
        let cleanAmount = withdrawalAmount.replacingOccurrences(of: ",", with: "")
        guard !cleanAmount.isEmpty, let amountValue = Double(cleanAmount) else {
            DispatchQueue.main.async {
                self.errorMessage = nil
            }
            return (false, false)
        }
        
        // 1. 티커 잔액 검사
        let fee = calculateFee()
        let maxWithdrawable = tickerBalance - fee
        
        // 최소 출금 수량 1과 최대 출금가능 금액 확인
        if amountValue < 1 {
            DispatchQueue.main.async {
                self.errorMessage = nil
            }
            return (false, false)
        } else if amountValue > maxWithdrawable {
            DispatchQueue.main.async {
                self.errorMessage = "출금가능 수량을 초과했습니다"
            }
            return (false, false)
        }
        
        // 2. 일일 출금한도 검사
        // 티커의 USD 가값 계산
        if let ticker = userData.transactions.first(where: { $0.type == selectedTicker }) {
            let tickerUsdPrice = ticker.priceUSD
            let withdrawalUsdValue = tickerUsdPrice * (amountValue + fee)
            
            // 일일 출금 가능 금액 검사
            let remainingLimit = userData.dailyWithdrawalLimit - userData.dailyWithdrawalUsed
            if withdrawalUsdValue > remainingLimit {
                DispatchQueue.main.async {
                    self.errorMessage = "일일 출금한도를 초과하였습니다"
                }
                return (false, true)
            }
        }
        
        // 유효한 출금
        DispatchQueue.main.async {
            self.errorMessage = nil
        }
        return (true, false)
    }
    
    func calculateFee() -> Double {
        return 1.00 // 고정 수수료 1.00
    }
    
    func calculateTotalWithdrawal() -> String {
        let cleanAmount = withdrawalAmount.replacingOccurrences(of: ",", with: "")
        if let amountValue = Double(cleanAmount) {
            // 고정 수수료 1.00 사용
            let fee = calculateFee()
            let totalAmount = amountValue + fee
            return "\(formatNumber(totalAmount, minFraction: 2, maxFraction: 2)) \(selectedTicker)"
        }
        return "\(formatNumber(1.00, minFraction: 2, maxFraction: 2)) \(selectedTicker)"
    }
    
    func formatNumber(_ number: Double, minFraction: Int = 0, maxFraction: Int = 2) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = minFraction
        formatter.maximumFractionDigits = maxFraction
        return formatter.string(from: NSNumber(value: number)) ?? "\(number)"
    }
}

// MARK: - Preview
struct TickerWithdrawalView_Previews: PreviewProvider {
    static var previews: some View {
        TickerWithdrawalView(
            selectedTicker: "KATV",
            tickerBalance: 50.0,
            maxWithdrawalLimit: 1000000.0
        )
    }
}

