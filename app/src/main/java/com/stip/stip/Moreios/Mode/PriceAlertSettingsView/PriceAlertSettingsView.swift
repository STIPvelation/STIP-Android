import SwiftUI

// MARK: - 디자인 시스템 확장

private extension Color {
    static let alertAccentStart = Color(red: 48/255, green: 198/255, blue: 232/255)
    static let alertAccentEnd = Color(red: 81/255, green: 145/255, blue: 240/255)
    static let alertBackground = Color(UIColor.systemGroupedBackground)
    static let alertCard = Color(UIColor.secondarySystemGroupedBackground)
    static let alertDivider = Color(UIColor.separator).opacity(0.5)
    static let alertTextPrimary = Color(UIColor.label)
    static let alertTextSecondary = Color(UIColor.secondaryLabel)
}

// 토글 활성화 시 사용할 커스텀 색상 정의 (#30C6E8)
let activeToggleColor = Color.alertAccentStart

// '표준' 탭의 알림 설정 데이터 모델
struct AlertSettings {
    var isLoginAlertOn: Bool = false
    var isTradeAlertOn: Bool = false
    var isDepositWithdrawalAlertOn: Bool = false
    var isScheduledOrderAlertOn: Bool = false
    var isEventReceptionOn: Bool = false
    var isIpWebtoonAlertOn: Bool = false
    var isIpAuctionAlertOn: Bool = false
    var isIpListingAlertOn: Bool = false
}

// '시세' 탭의 알림 설정 데이터 모델
struct MarketAlertSettings {
    var isRestModeOn: Bool = false
    var isOwnedIpAlertOn: Bool = false
    var isDesignatedAlertOn: Bool = false
    var isRiseFallAlertOn: Bool = false
    var isHighLowAlertOn: Bool = false
    var isIpCrossCompareAlertOn: Bool = false
    var isNewForfeitedUserAlertOn: Bool = false
    var isForfeitureUserAlertOn: Bool = false
}


// 메인 뷰: 시세 알림 설정 화면
struct PriceAlertSettingsView: View {
    @State private var selectedTab = 0
    // '준비중' 알림창 표시를 위한 상태 변수
    @State private var showPreparingAlert = false
    @Environment(\.colorScheme) private var colorScheme
    
    // 각 탭의 상태를 저장하는 변수
    @State private var standardSettings = AlertSettings()
    @State private var marketSettings = MarketAlertSettings() // '시세' 탭 상태 변수 추가
    
    let tabs = ["표준", "시세"]

    var body: some View {
        ZStack {
            // 배경 그라디언트
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.alertBackground.opacity(0.7),
                    Color.alertBackground.opacity(0.4)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // 커스텀 세그먼트 컬러
                ZStack {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.alertCard)
                        .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: 2)
                    
                    HStack(spacing: 0) {
                        ForEach(0..<tabs.count, id: \.self) { index in
                            Button(action: {
                                withAnimation(.spring()) {
                                    selectedTab = index
                                }
                            }) {
                                Text(tabs[index])
                                    .font(.system(size: 15, weight: selectedTab == index ? .semibold : .medium))
                                    .foregroundColor(selectedTab == index ? .alertAccentStart : .alertTextSecondary)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                                    .background(
                                        Group {
                                            if selectedTab == index {
                                                RoundedRectangle(cornerRadius: 6)
                                                    .fill(Color.alertCard)
                                                    .shadow(color: Color.alertAccentStart.opacity(0.15), radius: 3, x: 0, y: 2)
                                                    .padding(3)
                                            }
                                        }
                                    )
                            }
                        }
                    }
                    .padding(3)
                }
                .frame(height: 50)
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 8)

                // 선택된 탭에 따라 다른 뷰를 보여줌
                if selectedTab == 0 {
                    StandardAlertsView(settings: $standardSettings, showPreparingAlert: $showPreparingAlert)
                        .transition(.opacity)
                } else {
                    MarketAlertsView(settings: $marketSettings, showPreparingAlert: $showPreparingAlert)
                        .transition(.opacity)
                }
                Spacer()
            }
        }
        .navigationTitle("시세 알림")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("시세 알림")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.alertTextPrimary)
            }
        }
        .alert("준비중", isPresented: $showPreparingAlert) {
            Button("확인", role: .cancel) { }
        } message: {
            Text("준비중")
        }
    }
}

// '표준' 탭의 컨텐츠 뷰
struct StandardAlertsView: View {
    @Binding var settings: AlertSettings
    @Binding var showPreparingAlert: Bool

    private func preparingBinding(for keyPath: WritableKeyPath<AlertSettings, Bool>) -> Binding<Bool> {
        return Binding<Bool>(
            get: { settings[keyPath: keyPath] },
            set: { _ in showPreparingAlert = true }
        )
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // 시세알림 섹션
                VStack(alignment: .leading, spacing: 8) {
                    AlertSectionHeaderView(title: "일반 알림", icon: "bell.fill")
                    
                    // 시세알림 버튼
                    Button(action: { showPreparingAlert = true }) {
                        HStack {
                            AlertRow(title: "시세알림", subtitle: "수신 및 배지, 사운드, 배너 등의 알림 설정.", iconName: "bell.badge")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(Color.alertTextSecondary.opacity(0.7))
                                .padding(.trailing, 4)
                        }
                        .padding(.vertical, 6)
                        .padding(.horizontal, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.alertCard)
                                .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
                        )
                    }
                    .buttonStyle(AlertScaleButtonStyle())
                    
                    Divider()
                        .padding(.vertical, 6)
                    
                    // 로그인 알림 및 기타 알림
                    VStack(spacing: 4) {
                        AlertToggleCard(title: "로그인 알림", subtitle: "새로운 로그인 발생 시 알림.", iconName: "person.badge.key.fill", isOn: preparingBinding(for: \.isLoginAlertOn))
                        AlertToggleCard(title: "거래 알림", subtitle: "매수/매도 체결 완료 시 알림.", iconName: "arrow.2.squarepath", isOn: preparingBinding(for: \.isTradeAlertOn))
                        AlertToggleCard(title: "입출금 알림", subtitle: "입출금 완료 시 알림.", iconName: "yensign.circle", isOn: preparingBinding(for: \.isDepositWithdrawalAlertOn))
                        AlertToggleCard(title: "예약주문 알림", subtitle: "설정한 가격에 도달하여 예약 주문이 실행된 경우 알림.", iconName: "timer", isOn: preparingBinding(for: \.isScheduledOrderAlertOn))
                    }
                    
                    Divider()
                        .padding(.vertical, 6)
                    
                    // 이벤트 및 IP 관련 알림
                    VStack(spacing: 4) {
                        AlertToggleCard(title: "이벤트 등 안내 수신", subtitle: "혜택 및 이벤트 등 알림 수신 동의.", iconName: "gift", isOn: preparingBinding(for: \.isEventReceptionOn))
                        AlertToggleCard(title: "IP웹툰 알림", subtitle: "새로운 IP웹툰의 알림 설정.", iconName: "book", isOn: preparingBinding(for: \.isIpWebtoonAlertOn))
                        AlertToggleCard(title: "IP옥션 알림", subtitle: "IP옥션이 열리기 1시간전 알림.", iconName: "hammer", isOn: preparingBinding(for: \.isIpAuctionAlertOn))
                        AlertToggleCard(title: "IP신규 상장", subtitle: "신규 상장이 되는 지식재산의 알림.", iconName: "star", isOn: preparingBinding(for: \.isIpListingAlertOn))
                    }
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.alertCard.opacity(0.8))
                        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
            .padding(.bottom, 20)
        }
    }
}

// 섹션 헤더 뷰
struct AlertSectionHeaderView: View {
    let title: String
    let icon: String
    
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(.alertAccentStart)
                .font(.system(size: 16, weight: .medium))
            
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(.alertTextPrimary)
        }
        .padding(.bottom, 6)
    }
}

// 토글 카드 뷰
struct AlertToggleCard: View {
    let title: String
    let subtitle: String
    let iconName: String
    @Binding var isOn: Bool

    var body: some View {
        Button(action: {
            withAnimation(.spring(response: 0.2)) {
                isOn.toggle()
            }
        }) {
            HStack {
                AlertRow(title: title, subtitle: subtitle, iconName: iconName)
                Spacer()
                Toggle("", isOn: $isOn)
                    .labelsHidden()
                    .tint(activeToggleColor)
                    .scaleEffect(0.85)
            }
            .contentShape(Rectangle())
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.alertCard)
                    .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
            )
        }
        .buttonStyle(AlertScaleButtonStyle())
    }
}


// '시세' 탭의 컨텐츠 뷰
struct MarketAlertsView: View {
    @Binding var settings: MarketAlertSettings
    @Binding var showPreparingAlert: Bool

    // '준비중' 바인딩 헬퍼 함수
    private func preparingBinding(for keyPath: WritableKeyPath<MarketAlertSettings, Bool>) -> Binding<Bool> {
        return Binding<Bool>(
            get: { settings[keyPath: keyPath] },
            set: { _ in showPreparingAlert = true }
        )
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // 방해 차단 모드 섹션
                VStack(alignment: .leading, spacing: 8) {
                    AlertSectionHeaderView(title: "방해 차단 모드", icon: "moon.fill")
                    
                    AlertToggleCard(title: "휴식 모드", subtitle: "설정 시간 시세 알림 제한.", iconName: "moon.zzz.fill", isOn: preparingBinding(for: \.isRestModeOn))
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.alertCard.opacity(0.8))
                        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                
                // 보유IP 섹션
                VStack(alignment: .leading, spacing: 8) {
                    AlertSectionHeaderView(title: "보유IP", icon: "briefcase.fill")
                    
                    AlertToggleCard(title: "보유IP 알림", subtitle: "설정 기준 10% 구간 상승/하락 발생 시 알림.", iconName: "chart.line.uptrend.xyaxis", isOn: preparingBinding(for: \.isOwnedIpAlertOn))
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.alertCard.opacity(0.8))
                        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                
                // 종합 알림 섹션
                VStack(alignment: .leading, spacing: 8) {
                    AlertSectionHeaderView(title: "종합 알림", icon: "bell.and.waveform.fill")
                    
                    // 시세 동향 관련 알림
                    VStack(spacing: 4) {
                        AlertToggleCard(title: "지정 알림", subtitle: "설정 기준 10% 구간 상승/하락 발생 시 알림.", iconName: "bell.badge.fill", isOn: preparingBinding(for: \.isDesignatedAlertOn))
                        
                        AlertToggleCard(title: "상승/하락 알림", subtitle: "지정IP 단기간 상승 및 하락 발생 시 알림.", iconName: "arrow.up.arrow.down", isOn: preparingBinding(for: \.isRiseFallAlertOn))
                        
                        AlertToggleCard(title: "신고/저가 알림", subtitle: "지정한 날짜의 신고/저가 발생 시 알림.", iconName: "arrow.up.right.circle.fill", isOn: preparingBinding(for: \.isHighLowAlertOn))
                    }
                    
                    Divider()
                        .padding(.vertical, 6)
                    
                    // 교차 관련 알림
                    VStack(spacing: 4) {
                        AlertToggleCard(title: "IP교차 비교 알림", subtitle: "페어해둔 IP의 가격움직임 차이 알림.", iconName: "arrow.left.and.right.circle.fill", isOn: preparingBinding(for: \.isIpCrossCompareAlertOn))
                        
                        AlertToggleCard(title: "신규 실시권 사용자 알림", subtitle: "지정IP에서 신규 실시권 사용자 발생시 알림.", iconName: "person.badge.plus", isOn: preparingBinding(for: \.isNewForfeitedUserAlertOn))
                        
                        AlertToggleCard(title: "IP 실시권 포기자 알림", subtitle: "지정IP에서 실시권 사용 포기하는 유저 알림.", iconName: "person.badge.minus", isOn: preparingBinding(for: \.isForfeitureUserAlertOn))
                    }
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.alertCard.opacity(0.8))
                        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
            .padding(.bottom, 20)
        }
    }
}


// '시세알림' 상세 설정 화면 (Placeholder)
struct DetailAlertSettingsView: View {
    var body: some View {
        Text("시세알림 상세 설정")
            .navigationTitle("시세알림 상세")
    }
}


// MARK: - Reusable Views (재사용 가능한 뷰)

struct AlertRow: View {
    let title: String
    let subtitle: String
    let iconName: String?
    
    init(title: String, subtitle: String, iconName: String? = nil) {
        self.title = title
        self.subtitle = subtitle
        self.iconName = iconName
    }

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            if let iconName = iconName {
                Image(systemName: iconName)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.alertAccentStart)
                    .frame(width: 24, height: 24)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.alertTextPrimary)
                
                Text(subtitle)
                    .font(.system(size: 13))
                    .foregroundColor(.alertTextSecondary)
                    .lineLimit(2)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
        .padding(.vertical, 6)
    }
}

struct AlertToggleRow: View {
    let title: String
    let subtitle: String
    let iconName: String?
    @Binding var isOn: Bool
    
    init(title: String, subtitle: String, iconName: String? = nil, isOn: Binding<Bool>) {
        self.title = title
        self.subtitle = subtitle
        self.iconName = iconName
        self._isOn = isOn
    }

    var body: some View {
        HStack {
            AlertRow(title: title, subtitle: subtitle, iconName: iconName)
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(activeToggleColor)
                .scaleEffect(0.85)
                .animation(.spring(response: 0.2), value: isOn)
        }
        .contentShape(Rectangle())
    }
}

// 버튼 애니메이션 스타일
struct AlertScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(.spring(response: 0.2, dampingFraction: 0.6), value: configuration.isPressed)
    }
}


// MARK: - Preview (미리보기)

struct PriceAlertSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        // 미리보기에서도 정상적으로 보이도록 NavigationView로 감싸줍니다.
        NavigationView {
            PriceAlertSettingsView()
        }
    }
}
