import SwiftUI
import Foundation

struct TransactionView: View {
    var isLoggedIn: Bool
    
    // 검색 텍스트 상태
    @State private var searchText = ""
    
    // 설정 모달 표시 상태
    @State private var showingSettings = false
    
    // 사기 주의 안내 팝업 표시 상태
    @State private var showFraudWarning = false
    
    // 활성화된 필터 카테고리
    @State private var selectedCategory: TransactionCategory = .all
    
    // 활성화된 필터 구분
    @State private var selectedFilter: TransactionFilter = .all
    
    // 선택된 토큰 추적
    @State private var selectedTokens = Set<String>()
    
    // UserData 액세스
    @EnvironmentObject private var userData: UserData
    
    // 카테고리 헤더 뷰
    func categoryHeaderView(title: String) -> some View {
        HStack {
            Text(title)
                .font(.system(size: 15, weight: .bold))
                .padding(.vertical, 10)
                .padding(.horizontal, 16)
            Spacer()
        }
        .background(Color(UIColor.systemGray6))
    }
    
    // 총 보유자산 계산 프로퍼티
    var totalAssets: Double {
        // 1. UserData에서 USD 잔액 가져오기 ($10,000 고정값)
        let usdTransaction = userData.transactions.first(where: { $0.type == "USD" })
        let usdBalance = usdTransaction?.totalUSD ?? 0.0
        
        // 2. 티커들의 USD 가치 합산
        let tickerAssetsValue = userData.transactions.filter { $0.type != "USD" }.reduce(0) { $0 + $1.totalUSD }
        
        // 3. USD 잔액과 티커 자산 가치 합산하여 총 보유자산 계산
        return usdBalance + tickerAssetsValue
    }
    
    // 티커 데이터는 표시 형식만 하드코딩으로 유지하고, 실제 데이터는 UserData에서 가져옴
    // 11개 티커 + USD의 고정된 표시 형식이지만 데이터는 UserData에서 가져옴
    var transactions: [Transaction] {
        // UserData에서 데이터 가져옴
        return userData.transactions
    }
    
    // 필터링된 트랜잭션 목록
    var filteredTransactions: [Transaction] {
        let categoryFiltered: [Transaction]
        switch selectedCategory {
        case .all:
            categoryFiltered = userData.transactions
        case .usd:
            categoryFiltered = userData.transactions.filter { $0.category == .usd }
        case .patent:
            categoryFiltered = userData.transactions.filter { $0.category == .patent }
        case .businessModel:
            categoryFiltered = userData.transactions.filter { $0.category == .businessModel }
        }
        
        let filterApplied: [Transaction]
        switch selectedFilter {
        case .all:
            filterApplied = categoryFiltered
        case .held:
            filterApplied = categoryFiltered.filter { $0.amount > 0 }
        }
        
        if searchText.isEmpty {
            return filterApplied
        }
        
        let normalizedSearchText = searchText.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
        
        return filterApplied.filter { transaction in
            let matchesType = transaction.type.lowercased().contains(normalizedSearchText)
            let matchesUSD = normalizedSearchText == "usd" && transaction.type == "USD"
            let matchesAmount: Bool
            if let searchNumber = Double(normalizedSearchText) {
                matchesAmount = abs(Double(transaction.amount) - searchNumber) < 0.1 ||
                abs(transaction.totalUSD - searchNumber) < 0.1 ||
                abs(transaction.priceUSD - searchNumber) < 0.1
            } else {
                matchesAmount = false
            }
            return matchesType || matchesUSD || matchesAmount
        }
    }
    
    // USD 값 포맷팅
    func formattedUSDValue(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 0
        let formattedValue = formatter.string(from: NSNumber(value: value)) ?? "\(Int(value))"
        return "$\(formattedValue) USD"
    }
    
    // UserDefaults 키
    private let fraudWarningDismissedKey = "fraudWarningDismissedUntil"
    
    private var shouldShowFraudWarning: Bool {
        // UserDefaults에서 저장된 날짜 가져오기
        let dismissedUntil = UserDefaults.standard.object(forKey: fraudWarningDismissedKey) as? Date
        
        // 저장된 날짜가 없거나, 저장된 날짜가 현재보다 이전이라면 표시
        if let dismissDate = dismissedUntil {
            return Date() > dismissDate
        }
        return true // 기본적으로 표시
    }
    
    // 하루동안 보지 않기 처리
    private func dismissForOneDay() {
        // 현재로부터 24시간 후
        let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: Date())!
        UserDefaults.standard.set(tomorrow, forKey: fraudWarningDismissedKey)
        showFraudWarning = false
    }
    
    // 임시 숨기기 처리
    private func dismissTemporarily() {
        showFraudWarning = false
    }
    
    var body: some View {
        Group {
            if isLoggedIn {
                VStack(spacing: 0) {
                    // 헤더 타이틀 제거 (navigationTitle로 대체)
                    
                    // 자산 표시 카드 (USD)
                    VStack(spacing: 22) {
                        VStack(alignment: .center, spacing: 8) {
                            Text("총 보유자산")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Color(red: 100/255, green: 100/255, blue: 120/255))
                            
                            Text(formattedUSDValue(totalAssets))
                                .font(.system(size: 24, weight: .bold))
                                .padding(.horizontal, 6)
                                .foregroundColor(Color(red: 40/255, green: 40/255, blue: 60/255))
                        }
                        .frame(maxWidth: .infinity)
                        
                        NavigationLink(destination: KRWDepositView()) {
                            HStack(spacing: 10) {
                                Image(systemName: "plus.circle.fill")
                                    .font(.system(size: 16))
                                Text("KRW 입금")
                                    .font(.system(size: 16, weight: .semibold))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                            .cornerRadius(12)
                            .shadow(color: Color(red: 48/255, green: 198/255, blue: 232/255).opacity(0.3), radius: 5, x: 0, y: 3)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 24)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.white)
                            .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 5)
                    )
                    .padding(.bottom, 12) // 상하 여백 줄임
                    
                    // 검색창 및 필터 (한 줄에 배치)
                    HStack(spacing: 12) {
                        Spacer() // 왼쪽 공간을 채워 오른쪽으로 이동
                        // 티커명 검색 - 너비 축소
                        HStack {
                            Image(systemName: "magnifyingglass").font(.system(size: 16)).foregroundColor(Color(red: 130/255, green: 130/255, blue: 150/255))
                            TextField("티커명 검색", text: $searchText).font(.system(size: 16))
                        }
                        .padding(.vertical, 14).padding(.horizontal, 16)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color(red: 245/255, green: 246/255, blue: 250/255)))
                        .frame(width: 180) // 검색창 너비 축소
                        
                        // 전체/보유중 필터 - 옆으로 이동
                        ZStack {
                            RoundedRectangle(cornerRadius: 15).stroke(Color.gray.opacity(0.3), lineWidth: 1).frame(width: 150, height: 36)
                            HStack(spacing: 0) {
                                if selectedFilter == .all {
                                    RoundedRectangle(cornerRadius: 14)
                                        .fill(Color(hex: "30C6E8").opacity(0.1))
                                        .frame(width: 75, height: 34)
                                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "30C6E8"), lineWidth: 1))
                                        .padding(.leading, 1)
                                    Spacer()
                                } else {
                                    Spacer()
                                    RoundedRectangle(cornerRadius: 14)
                                        .fill(Color(hex: "30C6E8").opacity(0.1))
                                        .frame(width: 75, height: 34)
                                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "30C6E8"), lineWidth: 1))
                                        .padding(.trailing, 1)
                                }
                            }
                            .frame(width: 150)
                            HStack(spacing: 0) {
                                Button(action: { selectedFilter = .all }) {
                                    Text(TransactionFilter.all.displayName)
                                        .font(.system(size: 14, weight: selectedFilter == .all ? .bold : .regular))
                                        .foregroundColor(selectedFilter == .all ? Color(hex: "30C6E8") : .gray)
                                        .frame(width: 75, height: 36)
                                }
                                Button(action: { selectedFilter = .held }) {
                                    Text(TransactionFilter.held.displayName)
                                        .font(.system(size: 14, weight: selectedFilter == .held ? .bold : .regular))
                                        .foregroundColor(selectedFilter == .held ? Color(hex: "30C6E8") : .gray)
                                        .frame(width: 75, height: 36)
                                }
                            }
                            
                        }
                        .frame(width: 150, height: 36)
                        
                        Spacer() // 나머지 공간 채우기
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 8)
                    .padding(.bottom, 4)
                    .frame(maxWidth: .infinity)
                    
                    // 거래 내역 목록 (티커)
                    ScrollView {
                        VStack(spacing: 0) {
                            if filteredTransactions.isEmpty {
                                Text("검색 결과가 없습니다")
                                    .font(.system(size: 16))
                                    .foregroundColor(.gray)
                                    .padding(.top, 40)
                                    .frame(maxWidth: .infinity)
                            } else {
                                VStack(spacing: 0) {
                                    ForEach(filteredTransactions) { transaction in
                                        TransactionRow(transaction: transaction, selectedTokens: $selectedTokens)
                                        
                                        if transaction.id != filteredTransactions.last?.id {
                                            Divider() // 구분선 여백 제거
                                        }
                                    }
                                }
                                .background(Color.white)
                                .cornerRadius(12)
                                .padding(.top, 8)
                            }
                        }
                        .padding(.bottom, 20)
                    }
                    .padding(.top, 4)
                    
                    
                }
                .navigationTitle("입출금")
                .navigationBarTitleDisplayMode(.inline)
                .sheet(isPresented: $showingSettings) { Text("설정 화면").font(.title).padding() }
                // 출금/입금 상세페이지 표시를 위한 modifier 적용
                .withTransactionDetailNavigation()
                // 전기통신금융사기 주의 안내 팝업
                .overlay {
                    if showFraudWarning {
                        FraudWarningView(
                            isPresented: $showFraudWarning,
                            onDismissTemporarily: dismissTemporarily,
                            onDismissForOneDay: dismissForOneDay
                        )
                    }
                }
                .onAppear {
                    // 탭에 돌아왔을 때 팝업 표시 쥼 확인
                    showFraudWarning = shouldShowFraudWarning
                }
            } else {
                VStack(spacing: 20) {
                    Text("로그인이 필요한 서비스입니다").font(.title2)
                    NavigationLink(destination: LoginView()) {
                        Text("로그인하기")
                            .font(.headline)
                            .foregroundColor(.white)
                            .padding()
                            .frame(maxWidth: .infinity)
                            .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                            .cornerRadius(8)
                            .padding(.horizontal, 40)
                    }
                }
                .navigationTitle("입출금").navigationBarTitleDisplayMode(.inline)
            }
        }
    }
}

// 전기통신금융사기 주의 안내 팝업
struct FraudWarningView: View {
    @Binding var isPresented: Bool
    @State private var checkbox1 = true
    @State private var checkbox2 = true
    @State private var checkbox3 = true
    
    // 버튼 클릭 동작 콜백
    var onDismissTemporarily: () -> Void
    var onDismissForOneDay: () -> Void
    
    var body: some View {
        ZStack {
            // 배경 (어두운 반투명 오버레이)
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture {
                    // 배경 탭으로는 닫히지 않도록 함
                }
            
            // 팝업 컨테이너
            VStack(spacing: 20) {
                // 제목
                Text("전기통신금융사기 주의 안내")
                    .font(.system(size: 18, weight: .bold))
                    .padding(.top, 40)
                
                // 설명 텍스트
                Text("최근 디지털IP(DIP) 자산을 이용한 전기 통신금융사기 피해(보이스피싱)가 많이 발생하고 있습니다.")
                    .font(.system(size: 14))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)
                
                VStack(alignment: .leading, spacing: 16) {
                    // 체크박스 1
                    HStack(alignment: .top, spacing: 8) {
                        Image(systemName: checkbox1 ? "checkmark.square.fill" : "square")
                            .foregroundColor(checkbox1 ? .green : .gray)
                            .onTapGesture {
                                checkbox1.toggle()
                            }
                        
                        Text("국내외 거래소 임직원 및 금융기관 담당자가 금융정보 요구 및 디지털IP (DIP) 인증을 유도했나요?")
                            .font(.system(size: 14))
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    
                    // 체크박스 2
                    HStack(alignment: .top, spacing: 8) {
                        Image(systemName: checkbox2 ? "checkmark.square.fill" : "square")
                            .foregroundColor(checkbox2 ? .green : .gray)
                            .onTapGesture {
                                checkbox2.toggle()
                            }
                        
                        Text("SNS, 데이팅 앱 등에서 친해진 외국인이 생소한 해외 거래 사이트를 소개하며 회원가입을 유도했나요?")
                            .font(.system(size: 14))
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    
                    // 체크박스 3
                    HStack(alignment: .top, spacing: 8) {
                        Image(systemName: checkbox3 ? "checkmark.square.fill" : "square")
                            .foregroundColor(checkbox3 ? .green : .gray)
                            .onTapGesture {
                                checkbox3.toggle()
                            }
                        
                        Text("투자 손실 금액을 보존해준다며 특정 사이트 가입, 앱 설치 및 자금 생성을 요구했나요?")
                            .font(.system(size: 14))
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
                .padding(.horizontal, 24)
                
                // 추가 경고 텍스트
                Text("위 항목 중 하나라도 해당된다면, 보이스피싱 사기일 수 있습니다.")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
                    .padding(.vertical, 10)
                
                VStack(alignment: .leading, spacing: 16) {
                    Text("조금이라도 의심이 든다면, 즉시 거래를 멈추시고 STIP 카톡채널 로 연락해 주세요.")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                        .padding(.vertical, 10)
                    
                    Text("누구나 피해자가 될 수 있는 보이스피싱, 각별히 주의하면 예방할 수 있습니다.")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                }
                .padding(.horizontal, 20)
                
                Spacer()
                
                HStack {
                    // 취소 버튼
                    Button(action: {
                        onDismissForOneDay() // 하루동안 보지 않기
                    }) {
                        Text("하루 동안 보지 않기")
                            .font(.system(size: 16))
                            .foregroundColor(.gray)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color(hex: "f0f0f0"))
                    }
                    .offset(y: -20) // Move the button slightly upward
                    
                    // 확인 버튼
                    Button(action: {
                        onDismissTemporarily() // 현재만 숨기기
                    }) {
                        Text("확인")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color(hex: "30C6E8"))
                    }
                    .offset(y: -20) // Move the button slightly upward
                }
                .padding(.horizontal, 16)
            }
            .background(Color.white)
            .padding(20)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}



// TransactionCategory is now defined in TransactionModels.swift

// TransactionFilter is now defined in TransactionModels.swift

// Transaction struct is now defined in TransactionModels.swift

struct CategoryFilterButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14, weight: isSelected ? .bold : .regular))
                .padding(.horizontal, 16).padding(.vertical, 8)
                .foregroundColor(isSelected ? .white : Color(red: 100/255, green: 100/255, blue: 120/255))
                .background(
                    Group {
                        if isSelected {
                            LinearGradient(gradient: Gradient(colors: [Color(red: 62/255, green: 180/255, blue: 232/255), Color(red: 48/255, green: 198/255, blue: 252/255)]), startPoint: .leading, endPoint: .trailing)
                        } else {
                            Color(red: 245/255, green: 246/255, blue: 250/255)
                        }
                    }
                )
                .cornerRadius(20)
                .shadow(color: isSelected ? Color(red: 48/255, green: 198/255, blue: 232/255).opacity(0.3) : Color.clear, radius: 4, x: 0, y: 2)
                .animation(.easeInOut(duration: 0.2), value: isSelected)
        }
    }
}

struct TransactionRow: View {
    let transaction: Transaction
    @Binding var selectedTokens: Set<String>
    
    var isSelected: Bool { selectedTokens.contains(transaction.type) }
    
    func toggleSelection() {
        if isSelected {
            selectedTokens.remove(transaction.type)
        } else {
            selectedTokens.insert(transaction.type)
        }
    }
    
    var body: some View {
        Group {
            if transaction.type == "USD" {
                // USD일 경우 특정 페이지로 연결
                NavigationLink(destination: USDDetailView(usdAmount: Int(transaction.amount))) {
                    transactionRowContent
                }
                .buttonStyle(PlainButtonStyle())
            } else {
                // USD 이외의 모든 티커는 TiickerDepositPage로 연결
                NavigationLink(destination: TiickerDepositPage(transaction: transaction)) {
                    transactionRowContent
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
    }
    
    var transactionRowContent: some View {
        HStack(spacing: 16) {
            if transaction.type == "USD" {
                ZStack {
                    Circle().fill(LinearGradient(gradient: Gradient(colors: [Color(red: 62/255, green: 180/255, blue: 232/255), Color(red: 48/255, green: 198/255, blue: 252/255)]), startPoint: .topLeading, endPoint: .bottomTrailing)).frame(width: 36, height: 36).shadow(color: Color(red: 48/255, green: 198/255, blue: 232/255).opacity(0.3), radius: 4, x: 0, y: 2)
                    Text("$").font(.system(size: 24, weight: .bold)).foregroundColor(.white)
                }
            } else {
                ZStack {
                    Circle().fill(tokenGradient(for: transaction.type)).frame(width: 36, height: 36).shadow(color: tokenColor(for: transaction.type).opacity(0.3), radius: 4, x: 0, y: 2)
                    Text(tokenLogoText(for: transaction.type)).font(.system(size: 22, weight: .bold)).foregroundColor(.white)
                }
            }
            
            Text(cleanedTokenName(for: transaction.type)).font(.system(size: 20, weight: .bold)).foregroundColor(Color(red: 50/255, green: 50/255, blue: 70/255))
            Spacer()
            
            if transaction.type == "USD" {
                Text(formattedAmount(transaction.amount)).font(.system(size: 20, weight: .bold)).foregroundColor(Color(red: 50/255, green: 50/255, blue: 70/255)).padding(.trailing, 4)
            } else {
                VStack(alignment: .trailing, spacing: 4) {
                    Text(formattedAmount(transaction.amount)).font(.system(size: 18, weight: .semibold)).foregroundColor(Color(red: 50/255, green: 50/255, blue: 70/255))
                    Text("$\(String(format: "%.2f", transaction.totalUSD))").font(.system(size: 13, weight: .medium)).foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                }
            }
            
            Image(systemName: "chevron.right").font(.system(size: 14, weight: .medium)).foregroundColor(Color(red: 180/255, green: 180/255, blue: 200/255)).padding(.leading, 2)
        }
        .padding(.vertical, 10).padding(.horizontal, 16)
        .background(RoundedRectangle(cornerRadius: 12).fill(isSelected ? Color(hex: "30C6E8").opacity(0.1) : Color.white).shadow(color: Color.black.opacity(0.03), radius: 8, x: 0, y: 3))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(isSelected ? Color(hex: "30C6E8") : Color.clear, lineWidth: 1))
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 10)
        .padding(.vertical, 2)
        .contentShape(Rectangle())
    }
    
    func formattedAmount(_ amount: Int) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = ","
        return formatter.string(from: NSNumber(value: amount)) ?? "\(amount)"
    }
    
    func tokenLogoText(for tokenType: String) -> String {
        let components = tokenType.components(separatedBy: "/")
        if let firstPart = components.first, firstPart.count >= 2 {
            return String(firstPart.prefix(2))
        }
        return "$"
    }
    
    func cleanedTokenName(for tokenType: String) -> String {
        return tokenType.components(separatedBy: "/").first ?? tokenType
    }
    
    func tokenColor(for tokenType: String) -> Color {
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
    
    func tokenGradient(for tokenType: String) -> LinearGradient {
        let color = tokenColor(for: tokenType)
        return LinearGradient(gradient: Gradient(colors: [color, color.opacity(0.7)]), startPoint: .topLeading, endPoint: .bottomTrailing)
    }
}
