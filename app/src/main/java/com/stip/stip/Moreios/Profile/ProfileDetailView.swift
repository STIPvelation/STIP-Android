import SwiftUI
import PhotosUI

// 사용할 sheet의 종류를 정의하는 enum
fileprivate enum ActiveSheet: Identifiable {
    case pinVerification
    case profileEdit
    
    var id: Int {
        switch self {
        case .pinVerification: return 0
        case .profileEdit: return 1
        }
    }
}

struct ProfileDetailView: View {
    // MARK: - Properties
    
    // EnvironmentKey를 통해 제공되는 UserData 사용
    @EnvironmentObject private var userData: UserData
    
    // 로그인 상태 관리 (TickerOrderView와 동일한 키 사용)
    @AppStorage("isLoggedIn") private var isLoggedIn: Bool = false
    
    // 이 View에서만 사용하는 상태 변수들
    @State private var isShowingImagePicker = false
    @State private var showLogoutAlert = false
    @State private var showDeleteAccountAlert = false
    @State private var showDeleteAccountConfirmAlert = false
    @State private var navigateToAuthSelection = false
    @State private var activeSheet: ActiveSheet? = nil
    @State private var pinError = false
    @State private var showWithdrawalInfoPopup = false
    
    // MARK: - Body
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                profileHeaderSection
                
                Divider()
                
                accountInfoSection
                
                Spacer(minLength: 24)
                
                actionButtonsSection
                    .padding(.bottom, 40)
            }
            .padding(.horizontal, 20)
            .frame(maxWidth: .infinity)
        }
        .background(Color.white)
        .edgesIgnoringSafeArea(.bottom)
        .navigationTitle("회원 정보")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $activeSheet) { item in
            sheetView(for: item)
        }
        .fullScreenCover(isPresented: $navigateToAuthSelection) {
            AuthSelectionView() // 프로젝트 내에 정의된 뷰를 사용
        }
        .overlay {
            alertOverlay
        }
    }
    
    // MARK: - View Components
    
    private var profileHeaderSection: some View {
        ZStack {
            // 배경 그라디언트
            RoundedRectangle(cornerRadius: 16)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color(hex: "30C6E8").opacity(0.1),
                            Color(hex: "30C6E8").opacity(0.05)
                        ]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .strokeBorder(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color(hex: "30C6E8").opacity(0.4),
                                    Color(hex: "30C6E8").opacity(0.1)
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1
                        )
                )
                .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
                
            HStack(spacing: 24) {
                // 프로필 이미지
                Button(action: {
                    isShowingImagePicker = true
                }) {
                    ZStack(alignment: .bottomTrailing) {
                        // 프로필 이미지
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            Color(hex: "30C6E8").opacity(0.2),
                                            Color(hex: "30C6E8").opacity(0.1)
                                        ]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 100, height: 100)
                            
                            Group {
                                if let profileImage = userData.profileImage {
                                    Image(uiImage: profileImage)
                                        .resizable()
                                        .scaledToFill()
                                } else {
                                    Image(systemName: "person.circle.fill")
                                        .resizable()
                                        .scaledToFit()
                                        .foregroundColor(.gray)
                                }
                            }
                            .frame(width: 96, height: 96)
                            .clipShape(Circle())
                            .shadow(radius: 2)
                        }
                        .overlay(
                            Circle()
                                .strokeBorder(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            Color(hex: "30C6E8").opacity(0.8),
                                            Color(hex: "30C6E8").opacity(0.4)
                                        ]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    ),
                                    lineWidth: 2
                                )
                        )
                        
                        // 카메라 버튼
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            Color(hex: "30C6E8"),
                                            Color(hex: "30C6E8").opacity(0.8)
                                        ]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 34, height: 34)
                                .shadow(color: Color(hex: "30C6E8").opacity(0.3), radius: 4, x: 0, y: 2)
                            
                            Image(systemName: "camera.fill")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(.white)
                        }
                        .offset(x: 6, y: 6)
                    }
                }
                .sheet(isPresented: $isShowingImagePicker) {
                    ImagePicker(selectedImage: Binding<UIImage?>(get: {
                        return self.userData.profileImage
                    }, set: { newValue in
                        self.userData.profileImage = newValue
                    }))
                }
                
                // 사용자 정보
                VStack(alignment: .leading, spacing: 8) {
                    Text(userData.userName)
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                    
                    HStack(spacing: 8) {
                        // ID 뱃지
                        Text("ID")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [
                                                Color(hex: "30C6E8"),
                                                Color(hex: "30C6E8").opacity(0.8)
                                            ]),
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        )
                                    )
                            )
                        
                        Text(userData.userId)
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(Color(hex: "6E7491"))
                    }
                    
                    // 활동 상태
                    HStack(spacing: 6) {
                        Circle()
                            .fill(Color(hex: "4CD964"))
                            .frame(width: 8, height: 8)
                        
                        Text("활동 중")
                            .font(.system(size: 14))
                            .foregroundColor(Color(hex: "4CD964"))
                    }
                }
                
                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 24)
        }
        .padding(.horizontal, 4)
    }
    
    private var accountInfoSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            HStack(spacing: 12) {
                // 섹션 타이틀 영역
                HStack(spacing: 10) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color(hex: "30C6E8"),
                                    Color(hex: "30C6E8").opacity(0.7)
                                ]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: 3, height: 18)
                    
                    Text("계정 정보")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                }
                
                Spacer()
                
                // 수정 버튼
                Button(action: {
                    activeSheet = .pinVerification
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "pencil")
                            .font(.system(size: 12))
                        
                        Text("수정")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .foregroundColor(Color(hex: "30C6E8"))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .strokeBorder(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        Color(hex: "30C6E8").opacity(0.7),
                                        Color(hex: "30C6E8").opacity(0.3)
                                    ]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1.5
                            )
                    )
                }
            }
            
            // 정보 컬럼
            VStack(spacing: 0) {
                // 각 정보 항목들
                infoRow(title: "여권 영문 이름", value: userData.passportName)
                infoRow(title: "휴대폰 번호", value: userData.phoneNumber)
                infoRow(title: "생년월일", value: userData.birthDate)
                infoRow(title: "이메일", value: userData.email)
                infoRow(title: "인증은행", value: "신한은행 110-123-456789")
                infoRow(title: "주소", value: userData.address)
                infoRow(title: "직업", value: userData.occupation)
                infoRow(title: "직장명", value: userData.companyName)
                infoRow(title: "직장주소", value: userData.companyAddress)
            }
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 4)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(Color(hex: "F0F2F5"), lineWidth: 1)
            )
        }
        .padding(.horizontal, 2)
    }
    
    private var actionButtonsSection: some View {
        VStack(spacing: 20) {
            // 로그아웃 버튼 - 그라디언트 스타일
            Button(action: {
                showLogoutAlert = true
            }) {
                Text("로그아웃")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(hex: "30C6E8"),
                                Color(hex: "30C6E8").opacity(0.9)
                            ]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(12)
                    .shadow(color: Color(hex: "30C6E8").opacity(0.2), radius: 8, x: 0, y: 4)
            }
            
            // 회원 탈퇴 버튼 - 세련된 링크 스타일
            Button(action: {
                showWithdrawalInfoPopup = true
            }) {
                HStack(spacing: 6) {
                    Image(systemName: "person.fill.xmark")
                        .font(.system(size: 12))
                    
                    Text("회원 탈퇴")
                        .font(.system(size: 15))
                }
                .foregroundColor(Color(hex: "8A8D9F"))
                .padding(.top, 8)
            }
        }
        .padding(.vertical, 10)
    }
    
    @ViewBuilder
    private var alertOverlay: some View {
        ZStack {
            if showLogoutAlert {
                GeometryReader { geometry in
                    ZStack {
                        // 배경 후지
                        Color.black.opacity(0.4)
                            .ignoresSafeArea()
                            .onTapGesture {}  // 배경을 탭해도 닫히지 않게 함
                        
                        VStack(spacing: 16) {
                            // 상단 아이콘
                            ZStack {
                                Circle()
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [
                                                Color(hex: "30C6E8").opacity(0.1),
                                                Color(hex: "30C6E8").opacity(0.05)
                                            ]),
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                                    .frame(width: 76, height: 76)
                                
                                Image(systemName: "power")
                                    .font(.system(size: 28, weight: .medium))
                                    .foregroundColor(Color(hex: "30C6E8"))
                            }
                            .padding(.top, 32)
                            
                            // 제목
                            Text("로그아웃")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(Color(hex: "232D3F"))
                                .padding(.top, 8)
                            
                            // 안내 메시지
                            Text("정말로 로그아웃 하시겠습니까?")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "6E7491"))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 8)
                            
                            // 버튼 영역
                            HStack(spacing: 12) {
                                // 취소 버튼
                                Button {
                                    showLogoutAlert = false
                                } label: {
                                    Text("취소")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(Color(hex: "6E7491"))
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 54)
                                        .background(
                                            RoundedRectangle(cornerRadius: 12)
                                                .fill(Color(hex: "F5F7FA"))
                                        )
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .strokeBorder(Color(hex: "E1E5EE"), lineWidth: 1)
                                        )
                                }
                                
                                // 확인 버튼
                                Button {
                                    // 로그아웃 처리: isLoggedIn 상태를 false로 설정
                                    isLoggedIn = false
                                    UserDefaults.standard.set(false, forKey: "isLoggedIn")
                                    print("로그아웃 처리: isLoggedIn 설정 = \(isLoggedIn)")
                                    
                                    // 로그아웃 알림창 닫고 AuthSelection 화면으로 이동
                                    showLogoutAlert = false
                                    navigateToAuthSelection = true
                                } label: {
                                    Text("확인")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 54)
                                        .background(
                                            LinearGradient(
                                                gradient: Gradient(colors: [
                                                    Color(hex: "30C6E8"),
                                                    Color(hex: "30C6E8").opacity(0.9)
                                                ]),
                                                startPoint: .leading,
                                                endPoint: .trailing
                                            )
                                        )
                                        .cornerRadius(12)
                                        .shadow(color: Color(hex: "30C6E8").opacity(0.25), radius: 8, x: 0, y: 4)
                                }
                            }
                            .padding(.horizontal, 24)
                            .padding(.top, 10)
                            .padding(.bottom, 28)
                        }
                        .frame(width: geometry.size.width * 0.85)
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(Color.white)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .strokeBorder(Color(hex: "F0F2F5"), lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.1), radius: 16)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }

            if showDeleteAccountAlert {
                GeometryReader { geometry in
                    ZStack {
                        // 배경 후지
                        Color.black.opacity(0.4)
                            .ignoresSafeArea()
                            .onTapGesture {}  // 배경을 탭해도 닫히지 않게 함
                        
                        VStack(spacing: 16) {
                            // 상단 아이콘
                            ZStack {
                                Circle()
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [
                                                Color(hex: "FF5F5F").opacity(0.1),
                                                Color(hex: "FF5F5F").opacity(0.05)
                                            ]),
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                                    .frame(width: 76, height: 76)
                                
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .font(.system(size: 28, weight: .medium))
                                    .foregroundColor(Color(hex: "FF5F5F"))
                            }
                            .padding(.top, 32)
                            
                            // 제목
                            Text("회원 탈퇴")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(Color(hex: "232D3F"))
                                .padding(.top, 8)
                            
                            // 안내 메시지
                            Text("회원 탈퇴시 24시간 동안\n서비스 재가입이 제한됩니다.\n\n그래도 탈퇴 하시겠습니까?")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "6E7491"))
                                .multilineTextAlignment(.center)
                                .lineSpacing(4)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 8)
                            
                            // 버튼 영역
                            HStack(spacing: 12) {
                                // 취소 버튼
                                Button {
                                    showDeleteAccountAlert = false
                                } label: {
                                    Text("취소")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(Color(hex: "6E7491"))
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 54)
                                        .background(
                                            RoundedRectangle(cornerRadius: 12)
                                                .fill(Color(hex: "F5F7FA"))
                                        )
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .strokeBorder(Color(hex: "E1E5EE"), lineWidth: 1)
                                        )
                                }
                                
                                // 확인 버튼
                                Button {
                                    showDeleteAccountAlert = false
                                    showDeleteAccountConfirmAlert = true
                                } label: {
                                    Text("확인")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 54)
                                        .background(
                                            LinearGradient(
                                                gradient: Gradient(colors: [
                                                    Color(hex: "FF5F5F"),
                                                    Color(hex: "FF5F5F").opacity(0.9)
                                                ]),
                                                startPoint: .leading,
                                                endPoint: .trailing
                                            )
                                        )
                                        .cornerRadius(12)
                                        .shadow(color: Color(hex: "FF5F5F").opacity(0.25), radius: 8, x: 0, y: 4)
                                }
                            }
                            .padding(.horizontal, 24)
                            .padding(.top, 10)
                            .padding(.bottom, 28)
                        }
                        .frame(width: geometry.size.width * 0.85)
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(Color.white)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .strokeBorder(Color(hex: "F0F2F5"), lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.1), radius: 16)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
            
            if showWithdrawalInfoPopup {
                GeometryReader { geometry in
                    ZStack {
                        Color.black.opacity(0.4)
                            .ignoresSafeArea()
                            .onTapGesture {}  // 배경을 탭해도 닫히지 않게 함
                        
                        VStack(spacing: 16) {
                            // 제목
                            Text("회원탈퇴 안내")
                                .font(.system(size: 18, weight: .semibold))
                                .padding(.top, 30)
                            
                            ScrollView {
                                VStack(alignment: .leading, spacing: 12) {
                                    // 안내 메시지
                                    Text("회원 탈퇴 전, 보유한 IP 자산이 있다면 반드시 필요한 조치를 완료해 주세요.")
                                        .font(.system(size: 14))
                                        .foregroundColor(Color(UIColor.darkText))
                                    
                                    Text("탈퇴가 처리되면 보유 중인 IP 자산 및 관련 데이터가 모두 삭제되며, 복구할 수 없습니다. 또한, STIP IP 거래소 계정과 연결된 외부 서비스(예: 제3자 연동 서비스)도 자동 해지되며, 이후 이용이 불가능합니다.")
                                        .font(.system(size: 14))
                                        .foregroundColor(Color(UIColor.darkText))
                                    
                                    Text("회원 탈퇴 후에는 IP 자산 및 거래 내역을 포함한 모든 정보가 삭제되며, STIP IP 거래소 내 개인 서비스에 대한 이용 동의 역시 철회됩니다.")
                                        .font(.system(size: 14))
                                        .foregroundColor(Color(UIColor.darkText))
                                    
                                    Text("보유한 IP 자산이 있으면 탈퇴가 가능하니 참고해 주세요.")
                                        .font(.system(size: 14))
                                        .foregroundColor(Color(UIColor.darkText))
                                    
                                    Text("기타 유의사항")
                                        .font(.system(size: 16, weight: .medium))
                                        .padding(.top, 8)
                                    
                                    // 유의사항 목록
                                    VStack(alignment: .leading, spacing: 10) {
                                        HStack(alignment: .top, spacing: 4) {
                                            Text("•")
                                                .font(.system(size: 14))
                                            Text("실명 확인된 계좌 정보가 없을 경우, 서비스 이용료 지급이 제한될 수 있습니다.")
                                                .font(.system(size: 14))
                                                .fixedSize(horizontal: false, vertical: true)
                                        }
                                        
                                        HStack(alignment: .top, spacing: 4) {
                                            Text("•")
                                                .font(.system(size: 14))
                                            Text("탈퇴 후 24시간 동안 STIP IP 거래소 재가입이 불가능합니다.")
                                                .font(.system(size: 14))
                                                .fixedSize(horizontal: false, vertical: true)
                                        }
                                        
                                        HStack(alignment: .top, spacing: 4) {
                                            Text("•")
                                                .font(.system(size: 14))
                                            Text("1년 내 3회 이상 탈퇴와 재가입을 반복하면, 1년간 가입이 제한될 수 있습니다. 단, 고객센터를 통해 정당한 사유가 인정될 경우 제한이 신청이 가능합니다.")
                                                .font(.system(size: 14))
                                                .fixedSize(horizontal: false, vertical: true)
                                        }
                                    }
                                    .padding(.leading, 4)
                                }
                                .padding(.horizontal, 24)
                            }
                            .frame(maxHeight: 380)
                            
                            // 확인 버튼
                            Button {
                                showWithdrawalInfoPopup = false
                                showDeleteAccountAlert = true
                            } label: {
                                Text("확인")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 50)
                                    .background(Color(red: 48/255, green: 198/255, blue: 232/255)) // #30C6E8
                                    .cornerRadius(8)
                            }
                            .padding(.horizontal, 24)
                            .padding(.bottom, 24)
                        }
                        .frame(width: geometry.size.width * 0.85)
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.15), radius: 10)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
            
            if showDeleteAccountConfirmAlert {
                GeometryReader { geometry in
                    ZStack {
                        // 배경 후지
                        Color.black.opacity(0.4)
                            .ignoresSafeArea()
                            .onTapGesture {}  // 배경을 탭해도 닫히지 않게 함
                        
                        VStack(spacing: 16) {
                            // 상단 아이콘
                            ZStack {
                                Circle()
                                    .fill(
                                        LinearGradient(
                                            gradient: Gradient(colors: [
                                                Color(hex: "FF5F5F").opacity(0.15),
                                                Color(hex: "FF5F5F").opacity(0.08)
                                            ]),
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                                    .frame(width: 76, height: 76)
                                
                                Image(systemName: "xmark.shield.fill")
                                    .font(.system(size: 28, weight: .medium))
                                    .foregroundColor(Color(hex: "FF5F5F"))
                            }
                            .padding(.top, 32)
                            
                            // 제목
                            Text("탈퇴 최종 확인")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(Color(hex: "232D3F"))
                                .padding(.top, 8)
                            
                            // 안내 메시지
                            Text("정말로 탈퇴 하시겠습니까?")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "6E7491"))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 4)
                                
                            Text("탈퇴하면 저장된 모든 정보가 삭제됩니다.")
                                .font(.system(size: 15, weight: .semibold))
                                .foregroundColor(Color(hex: "FF5F5F"))
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 4)
                            
                            // 버튼 영역
                            HStack(spacing: 12) {
                                // 취소 버튼
                                Button {
                                    showDeleteAccountConfirmAlert = false
                                } label: {
                                    Text("취소")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(Color(hex: "6E7491"))
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 54)
                                        .background(
                                            RoundedRectangle(cornerRadius: 12)
                                                .fill(Color(hex: "F5F7FA"))
                                        )
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .strokeBorder(Color(hex: "E1E5EE"), lineWidth: 1)
                                        )
                                }
                                
                                // 확인 버튼
                                Button {
                                    // 탈퇴 완료 후 로그인 화면으로 이동
                                    showDeleteAccountConfirmAlert = false
                                    navigateToAuthSelection = true
                                } label: {
                                    Text("탈퇴")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 54)
                                        .background(
                                            LinearGradient(
                                                gradient: Gradient(colors: [
                                                    Color(hex: "FF5F5F"),
                                                    Color(hex: "FF5F5F").opacity(0.9)
                                                ]),
                                                startPoint: .leading,
                                                endPoint: .trailing
                                            )
                                        )
                                        .cornerRadius(12)
                                        .shadow(color: Color(hex: "FF5F5F").opacity(0.25), radius: 8, x: 0, y: 4)
                                }
                            }
                            .padding(.horizontal, 24)
                            .padding(.top, 10)
                            .padding(.bottom, 28)
                        }
                        .frame(width: geometry.size.width * 0.85)
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(Color.white)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .strokeBorder(Color(hex: "F0F2F5"), lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.1), radius: 16)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
            }
        }
    }
    
    @ViewBuilder
    private func sheetView(for item: ActiveSheet) -> some View {
        switch item {
        case .pinVerification:
            PinVerificationView(onComplete: { success in // 프로젝트 내에 정의된 PinVerificationView 사용
                activeSheet = nil
                if success {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        activeSheet = .profileEdit
                    }
                } else {
                    pinError = true
                }
            })
            
        case .profileEdit:
            NavigationView {
                ProfileEditView() // 프로젝트 내에 정의된 ProfileEditView 사용
            }
        }
    }
    
    // MARK: - Helper Methods & Views
    
    private func infoRow(title: String, value: String) -> some View {
        VStack(spacing: 0) {
            HStack(alignment: .center, spacing: 16) {
                // 타이틀 영역
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(Color(hex: "30C6E8").opacity(0.1))
                        .frame(width: 4, height: 16)
                        .padding(.leading, 2)
                    
                    Text(title)
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(Color(hex: "6E7491"))
                        .padding(.leading, 14)
                }
                .frame(width: 110, alignment: .leading)
                
                Spacer()
                
                // 값 영역
                Text(value.isEmpty ? "-" : value)
                    .font(.system(size: 16))
                    .foregroundColor(Color(hex: "232D3F"))
                    .multilineTextAlignment(.trailing)
                    .frame(alignment: .trailing)
            }
            .padding(.vertical, 14)
            .padding(.horizontal, 16)
            
            // 구분선
            Divider()
                .padding(.leading, 16)
        }
        .background(Color.white.opacity(0.01)) // 터치 감지를 위한 공간
    }
    
    // 모던한 스타일의 정보 행 함수
    private func modernInfoRow(title: String, value: String, isLast: Bool = false) -> some View {
        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: 16) {
                // 제목
                Text(title)
                    .font(.system(size: 15))
                    .foregroundColor(Color(hex: "6E7491"))
                    .frame(width: 100, alignment: .leading)
                
                // 값
                Text(value.isEmpty ? "-" : value)
                    .font(.system(size: 16))
                    .foregroundColor(Color(hex: "232D3F"))
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.vertical, 14)
            .padding(.horizontal, 16)
            
            // 구분선 (마지막 항목이 아니면 표시)
            if !isLast {
                Divider()
                    .padding(.leading, 16)
            }
        }
    }
    
    private var profileImageView: some View {
        Group {
            if let profileImage = userData.profileImage {
                Image(uiImage: profileImage)
                    .resizable()
                    .scaledToFill()
            } else {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .scaledToFit()
                    .foregroundColor(.gray)
            }
        }
        .frame(width: 100, height: 100)
        .clipShape(Circle())
        .shadow(radius: 2)
    }
}



// MARK: - Preview Provider

struct ProfileDetailView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ProfileDetailView()

        }
    }
}
