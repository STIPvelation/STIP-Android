import SwiftUI
import UIKit
import PhotosUI
import WebKit

// Import for relocated AuctionView
// No special import needed - we're in the same module

// MARK: - 메인 뷰
struct MoreView: View {
    // 기존 AuthenticationManager 대신 UserData 모델 사용
    @ObservedObject private var userData = UserData.shared
    
    // 로그인 상태 받기
    var isLoggedIn: Bool
    
    // 프로필 이미지 표시를 위한 상태
    @State private var isShowingImagePicker = false
    
    // 화면 크기에 따른 반응형 디자인을 위한 환경 변수
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    
    // State to control navigation to notifications page
    @State private var showNotifications = false

    var body: some View {
        Group {
            if isLoggedIn {
                ScrollView {
                    VStack(spacing: 0) {
                        // 1. 프로필 섹션
                        NavigationLink(destination: ProfileDetailView()) {
                            profileSection
                        }
                        .buttonStyle(PlainButtonStyle())
                        .padding(.vertical, 8)
                        .background(Color.white)
                        
                        // 위에 구분선 추가
                        Rectangle()
                            .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                            .frame(height: 4)
                        
                        // 2. 빠른 메뉴 섹션
                        quickActionsMenu
                            .padding(.vertical, 8)
                            .background(Color.white)
                            
                        // 섹션 구분선 추가
                        Rectangle()
                            .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                            .frame(height: 4)

                        // 3. 아이콘 그리드 섹션
                        iconGridMenu
                            .padding(.vertical, 12)
                            .background(Color.white)
                        
                        // 섹션 구분선 추가
                        Rectangle()
                            .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                            .frame(height: 4)
                        
                        // 4. 로고 섹션
                        Image("ic_logo_color")
                            .resizable()
                            .scaledToFit()
                            .frame(height: 40)
                            .padding(.vertical, 16)
                            .frame(maxWidth: .infinity)
                            .background(Color.white)
                        
                        // 로고 아래에 구분선 추가
                        Rectangle()
                            .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                            .frame(height: 4)
                        
                        // 5. 정책 및 약관 - 수평 스크롤 레이아웃
                        VStack(spacing: 16) {
                            // 수평 스크롤 카드 뷰
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    // 1. 정책 및 약관
                                    NavigationLink(destination: PolicyListView()) {
                                        PolicyCardView(title: "정책 및 약관", iconName: "doc.text")
                                    }
                                    
                                    // 2. ESG 정책
                                    NavigationLink(destination: PolicyDetailView(policyTitle: "ESG 정책")) {
                                        PolicyCardView(title: "ESG 정책", iconName: "leaf.fill")
                                    }
                                    
                                    // 3. 스타트업 및 중소기업 지원 정책
                                    NavigationLink(destination: PolicyDetailView(policyTitle: "스타트업 및 중소기업 지원 정책")) {
                                        PolicyCardView(title: "스타트업 지원 정책", iconName: "building.2.fill")
                                    }
                                    
                                    // 4. 자금세탁방지(AML)및 내부통제 정책
                                    NavigationLink(destination: PolicyDetailView(policyTitle: "자금세탁방지(AML) 및 내부통제 정책")) {
                                        PolicyCardView(title: "AML 정책", iconName: "lock.shield.fill")
                                    }
                                    
                                    // 5. DIP 가치평가 기준 정책
                                    NavigationLink(destination: PolicyDetailView(policyTitle: "DIP 가치평가 기준 정책")) {
                                        PolicyCardView(title: "DIP 평가 기준", iconName: "chart.bar.fill")
                                    }
                                    
                                    // 6. 수수료 투명성 정책
                                    NavigationLink(destination: PolicyDetailView(policyTitle: "수수료 투명성 정책")) {
                                        PolicyCardView(title: "수수료 정책", iconName: "creditcard.fill")
                                    }
                                    
                                    // 7. STIPvelation 외부 링크
                                    Link(destination: URL(string: "https://stipvelation.com")!) {
                                        PolicyCardView(title: "STIPvelation", iconName: "arrow.up.right.circle.fill", isExternalLink: true)
                                    }
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                            }
                            
                            HStack {
                                Text("앱 버전 1.00")
                                Spacer()
                            }
                            .padding(.vertical, 12)
                        }
                        .padding(.horizontal, 24)
                        .padding(.top, 16)
                        .background(Color.white)
                        
                        // 공백 추가하여 고객센터 아래로 이동
                        Spacer()
                            .frame(height: 30)
                        
                        // 6. 고객센터 버튼
                        NavigationLink(destination: CustomerSupportView()) {
                            Text("고객센터")
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity, minHeight: 44)
                            
                        }
                        .buttonStyle(BorderlessButtonStyle())
                        .foregroundColor(.primary)
                        .background(Color(red: 244/255, green: 247/255, blue: 252/255))
                        .cornerRadius(8)
                        .padding(.horizontal, 16)
                        .padding(.bottom, 30) 
                      
                    }
                }
            } else {
                VStack(spacing: 20) {
                    Text("로그인이 필요한 서비스입니다")
                        .font(.title2)
                    
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
            }
        }
        .background(Color.white)
        .navigationTitle("더보기")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            // Toolbar items removed as requested
        }

    }

    // MARK: - UI 컴포넌트 (private var로 분리)
    
    // MARK: - 프로필 섹션 (모던한 디자인)    
    private var profileSection: some View {
        ZStack {
            // 배경 디자인
            RoundedRectangle(cornerRadius: 16)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color.white,
                            Color(hex: "F8FBFF")
                        ]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .strokeBorder(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color.white.opacity(0.5),
                                    Color(red: 48/255, green: 198/255, blue: 232/255).opacity(0.2)
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1.5
                        )
                )

            // 콘텐츠
            HStack(spacing: horizontalSizeClass == .compact ? 16 : 20) {
                // 프로필 이미지 버튼
                Button(action: {
                    isShowingImagePicker = true
                }) {
                    ZStack {
                        if let profileImage = userData.profileImage {
                            Image(uiImage: profileImage)
                                .resizable()
                                .scaledToFill()
                                .frame(width: horizontalSizeClass == .compact ? 60 : 70, 
                                       height: horizontalSizeClass == .compact ? 60 : 70)
                                .clipShape(Circle())
                        } else {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            Color(hex: "E8F7FC"),
                                            Color(hex: "F0F8FC")
                                        ]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: horizontalSizeClass == .compact ? 60 : 70, 
                                       height: horizontalSizeClass == .compact ? 60 : 70)
                                
                            Image(systemName: "person.fill")
                                .font(.system(size: horizontalSizeClass == .compact ? 26 : 30, weight: .medium))
                                .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                        }
                        
                        // 프로필 이미지 변경 버튼 표시
                        Circle()
                            .strokeBorder(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        Color(hex: "30C6E8"),
                                        Color(hex: "30C6E8").opacity(0.7)
                                    ]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 2.5
                            )
                            .frame(width: horizontalSizeClass == .compact ? 60 : 70, 
                                   height: horizontalSizeClass == .compact ? 60 : 70)
                        
                        // 편집 버튼 아이콘
                        Circle()
                            .fill(Color(hex: "30C6E8"))
                            .frame(width: 22, height: 22)
                            .overlay(
                                Image(systemName: "pencil")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                            )
                            .shadow(color: Color.black.opacity(0.15), radius: 4, x: 0, y: 2)
                            .offset(x: horizontalSizeClass == .compact ? 20 : 24, y: horizontalSizeClass == .compact ? 20 : 24)
                    }
                }
                .sheet(isPresented: $isShowingImagePicker) {
                    ImagePicker(selectedImage: $userData.profileImage)
                }
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 3)
                
                // 사용자 정보
                VStack(alignment: .leading, spacing: 4) {
                    Text(userData.userName)
                        .font(.system(size: horizontalSizeClass == .compact ? 20 : 22, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                        .lineLimit(1)
                    
                    // 사용자 ID 배지
                    HStack(spacing: 6) {
                        Text("ID")
                            .font(.system(size: horizontalSizeClass == .compact ? 12 : 13, weight: .semibold))
                            .foregroundColor(Color.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(
                                Capsule()
                                    .fill(Color(hex: "30C6E8"))
                            )
                        
                        Text(userData.userId)
                            .font(.system(size: horizontalSizeClass == .compact ? 13 : 14))
                            .foregroundColor(Color(hex: "6E7491"))
                            .lineLimit(1)
                    }
                    
                    // 공개 상태 표시
                    HStack(spacing: 5) {
                        Circle()
                            .fill(Color.green)
                            .frame(width: 8, height: 8)
                        
                        Text("활동 중")
                            .font(.system(size: horizontalSizeClass == .compact ? 12 : 13))
                            .foregroundColor(Color(hex: "6E7491"))
                    }
                }
                
                Spacer()
                
                // 오른쪽 화살표 대신 버튼
                Image(systemName: "chevron.right")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Color(hex: "A0A5BA"))
                    .padding(12)
                    .background(Color(hex: "F5F8FC"))
                    .clipShape(Circle())
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
        .padding(.horizontal, 16)
        .frame(minHeight: 76)
    }
    
    // 2. 빠른 메뉴 UI (모던한 디자인)
    private var quickActionsMenu: some View {
        HStack(spacing: 16) {
            // 시세알림 버튼
            NavigationLink(destination: PriceAlertSettingsView()) {
                MenuItemCard(
                    title: "시세알림",
                    imageName: "bell.badge.fill",
                    gradientColors: [Color(hex: "4BD964"), Color(hex: "25C13F")],
                    iconTint: .white
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            // 모드설정 버튼
            NavigationLink(destination: ModeSettingsView()) {
                MenuItemCard(
                    title: "모드설정",
                    imageName: "slider.horizontal.3",
                    gradientColors: [Color(hex: "5ACDFA"), Color(hex: "0B93E9")],
                    iconTint: .white
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            // 보안/인증 버튼
            NavigationLink(destination: SecurityAuthView()) {
                MenuItemCard(
                    title: "보안/인증",
                    imageName: "lock.shield.fill",
                    gradientColors: [Color(hex: "8E89D6"), Color(hex: "5856D6")],
                    iconTint: .white
                )
            }
            .buttonStyle(PlainButtonStyle())
        }
        .padding(.horizontal, 16)
    }
    
    // 모던한 메뉴 아이템 카드
    private struct MenuItemCard: View {
        let title: String
        let imageName: String
        let gradientColors: [Color]
        let iconTint: Color
        
        var body: some View {
            VStack(spacing: 12) {
                // 아이콘 배경
                ZStack {
                    // 그라데이션 배경
                    RoundedRectangle(cornerRadius: 16)
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: gradientColors),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 64, height: 64)
                        .shadow(color: gradientColors[0].opacity(0.3), radius: 8, x: 0, y: 4)
                    
                    // 아이콘
                    Image(systemName: imageName)
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundColor(iconTint)
                }
                
                // 타이틀
                Text(title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color(hex: "232D3F"))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
    }
    
    // 3. 아이콘 그리드 UI (모던 디자인)
    private var iconGridMenu: some View {
        VStack(spacing: 24) {
            // 섹션 제목
            Text("IP 엔터테인먼트")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(Color(hex: "232D3F"))
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, 12)
            
            // 지그재그 2x2 그리드 레이아웃
            VStack(spacing: 16) {
                HStack(spacing: 16) {
                    // IP웹툰
                    NavigationLink(destination: destinationView(for: "IP웹툰")) {
                        ModernIPCard(
                            title: "IP웹툰",
                            imageName: "doc.richtext.fill",
                            gradientColors: [Color(hex: "529AF8"), Color(hex: "3478F6")],
                            description: "웹툰을 즐기세요"
                        )
                    }
                    
                    // IP옥션
                    NavigationLink(destination: destinationView(for: "IP옥션")) {
                        ModernIPCard(
                            title: "IP옥션",
                            imageName: "tag.fill",
                            gradientColors: [Color(hex: "5AD975"), Color(hex: "4CD964")],
                            description: "경매로 IP를 획득하세요"
                        )
                    }
                }
                
                HStack(spacing: 16) {
                    // IP스왑
                    NavigationLink(destination: destinationView(for: "IP스왑")) {
                        ModernIPCard(
                            title: "IP스왑",
                            imageName: "arrow.2.squarepath",
                            gradientColors: [Color(hex: "7E7BD8"), Color(hex: "5856D6")],
                            description: "기술 스왑을 시작하세요"
                        )
                    }
                    
                    // IP기부
                    NavigationLink(destination: destinationView(for: "IP기부")) {
                        ModernIPCard(
                            title: "IP기부",
                            imageName: "gift.fill",
                            gradientColors: [Color(hex: "FFCC00"), Color(hex: "FFA400")],
                            description: "기부로 IP를 공유하세요"
                        )
                    }
                }
            }
            .padding(.horizontal, 20)
        }
        .padding(.vertical, 20)
    }
    
    // 모던한 IP 카드
    private struct ModernIPCard: View {
        let title: String
        let imageName: String
        let gradientColors: [Color]
        let description: String
        
        var body: some View {
            VStack(alignment: .leading, spacing: 16) {
                // 상단 아이콘
                HStack {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: gradientColors),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 46, height: 46)
                            .shadow(color: gradientColors[0].opacity(0.3), radius: 6, x: 0, y: 3)
                        
                        Image(systemName: imageName)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(.white)
                    }
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color(hex: "A0A5BA"))
                }
                
                // 제목 및 설명
                VStack(alignment: .leading, spacing: 6) {
                    Text(title)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                    
                    Text(description)
                        .font(.system(size: 13))
                        .foregroundColor(Color(hex: "6E7491"))
                        .lineLimit(2)
                }
            }
            .padding(18)
            .frame(maxWidth: .infinity)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.06), radius: 10, x: 0, y: 4)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                gradientColors[0].opacity(0.4),
                                gradientColors[1].opacity(0.2)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 1
                    )
            )
            .contentShape(Rectangle())
        }
    }
    
    // MARK: - Logged Out View
    private var loggedOutView: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 위에 구분선 추가
            Rectangle()
                .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                .frame(height: 4)
            
            // 정책 및 약관 목록
            VStack(alignment: .leading, spacing: 20) {
                // 로그인 버튼 - 입출금 내역과 동일한 스타일
                NavigationLink(destination: LoginView()) {
                    Text("로그인")
                        .padding(.vertical, 12)
                        .foregroundColor(.primary)
                }
                
                // 입출금 내역 버튼
                NavigationLink(destination: EmptyView()) {
                    Text("입출금 내역")
                        .padding(.vertical, 12)
                        .foregroundColor(.primary)
                }
            }
            .font(.system(size: 16))
            .padding(.horizontal, 24)
            .padding(.top, 16)
            .background(Color.white)
            
            Spacer()
        }
        .background(Color.white)
    }
    
    // 각 아이콘에 맞는 화면으로 이동하기 위한 헬퍼 함수
    @ViewBuilder
    private func destinationView(for label: String) -> some View {
        switch label {
        case "IP웹툰": MoreWebtoonView()
        case "IP옥션": AuctionView()
        case "IP스왑": SwapView()
        case "IP기부": IPDonationView()
        default: EmptyView()
        }
    }
}

// MARK: - 재사용 가능한 컴포넌트

// 아이콘 그리드 버튼
struct IconGridButton: View {
    let imageName: String
    let label: String
    let iconColor: Color
    let size: CGFloat?
    
    init(imageName: String, label: String, iconColor: Color, size: CGFloat? = nil) {
        self.imageName = imageName
        self.label = label
        self.iconColor = iconColor
        self.size = size
    }
    
    var body: some View {
        NavigationLink(destination: destinationView(for: label)) {
            HStack(spacing: 16) {
                ZStack {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(iconColor.opacity(0.15))
                        .frame(width: 52, height: 52)
                    
                    Image(systemName: imageName)
                        .font(.system(size: 22, weight: .medium))
                        .foregroundColor(iconColor)
                }
                .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: 1)
                
                Text(label)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.primary)
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color(UIColor.systemGray3))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    // 각 아이콘에 맞는 화면으로 이동하기 위한 헬퍼 함수
    @ViewBuilder
    func destinationView(for label: String) -> some View {
        switch label {
        case "IP웹툰": MoreWebtoonView()
        case "IP옥션": AuctionView()
        case "IP스왑": SwapView()
        case "IP기부": IPDonationView()
        default: EmptyView()
        }
    }
}

// MARK: - 이동할 화면들 (임시 Placeholder)
// 각 NavigationLink의 목적지가 되는 임시 뷰들입니다.
struct MyProfileView: View { var body: some View { Text("내 프로필 화면") } }
// PriceAlertSettingsView is now implemented in a separate file
// ModeSettingsView is now implemented in a separate file
// SecurityAuthView is now implemented in a separate file
// PolicyView는 이미 프로젝트에 존재하는 뷰를 사용합니다.
// 임시로 사용하기 위한 다른 뷰 이름

struct MorePolicyView: View { var body: some View { Text("정책 및 약관 화면") } }
struct TransactionStatusView: View { var body: some View { Text("입출금현황 화면") } }
struct MoreWebtoonView: View { 
    var body: some View { 
        IPWebtoonView()
    } 
}


struct SwapView: View { 
    var body: some View { 
        IPSwapDetailView() 
    } 
}
struct DonateView: View { var body: some View { Text("IP기부 화면") } }


// MARK: - 미리보기
#Preview {
    MoreView(isLoggedIn: true)
}


// MARK: - Placeholder Destination Views

// LoginView는 실제 프로젝트의 파일을 사용합니다
struct MoreMemberInfoView: View { var body: some View { Text("회원 정보 화면") } }
struct MorePriceAlertView: View { var body: some View { Text("시세 알림 화면") } }
struct MoreModeSettingView: View { var body: some View { Text("모드 설정 화면") } }
struct MoreSecurityView: View { var body: some View { Text("보안/인증 화면") } }
struct PolicyView: View { var body: some View { Text("이용약관 및 정책 화면") } }
struct PrivacyView: View { var body: some View { Text("개인정보 처리방침 화면") } }
struct YouthPolicyView: View { var body: some View { Text("소비자 보호정책 화면") } }
struct TransactionStatusInfoView: View { var body: some View { Text("입출금 이용안내 화면") } }
struct ValuationPolicyView: View { var body: some View { Text("IP 가치평가 안내 화면") } }
struct FeePolicyView: View { var body: some View { Text("수수료 안내 화면") } }


// MARK: - Previews

#Preview("Logged In") {
    MoreView(isLoggedIn: true)
}

#Preview("Logged Out") {
    MoreView(isLoggedIn: false)
}

// MARK: - Policy Card View
struct PolicyCardView: View {
    var title: String
    var iconName: String
    var isExternalLink: Bool = false
    
    var body: some View {
        VStack(spacing: 12) {
            // Icon with colored background
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 240/255, green: 245/255, blue: 255/255),
                                Color(red: 230/255, green: 240/255, blue: 250/255)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 48, height: 48)
                
                Image(systemName: iconName)
                    .font(.system(size: 22, weight: .medium))
                    .foregroundColor(isExternalLink ? 
                              Color(red: 48/255, green: 143/255, blue: 232/255) :
                              Color(red: 48/255, green: 176/255, blue: 199/255))
            }
            
            Text(title)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.primary)
                .lineLimit(2)
                .multilineTextAlignment(.center)
                .frame(height: 40)
        }
        .frame(width: 100, height: 120)
        .padding(.vertical, 12)
        .padding(.horizontal, 8)
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

// MARK: - ImagePicker
struct ImagePicker: UIViewControllerRepresentable {
    @Binding var selectedImage: UIImage?
    @Environment(\.presentationMode) private var presentationMode
    
    func makeUIViewController(context: Context) -> PHPickerViewController {
        var configuration = PHPickerConfiguration()
        configuration.filter = .images
        configuration.selectionLimit = 1
        
        let picker = PHPickerViewController(configuration: configuration)
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {
        // 업데이트 필요 없음
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            parent.presentationMode.wrappedValue.dismiss()
            
            guard let provider = results.first?.itemProvider, provider.canLoadObject(ofClass: UIImage.self) else { return }
            
            provider.loadObject(ofClass: UIImage.self) { image, error in
                DispatchQueue.main.async {
                    self.parent.selectedImage = image as? UIImage
                }
            }
        }
    }
}
