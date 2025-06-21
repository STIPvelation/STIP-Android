import SwiftUI
import LocalAuthentication

// MARK: - 디자인 시스템 확장

private extension Color {
    // 보안 뷰 색상 테마
    static var securityAccentStart = Color(red: 48/255, green: 198/255, blue: 232/255)
    static var securityAccentEnd = Color(red: 81/255, green: 145/255, blue: 240/255)
    static var securityCard = Color.white
    static var securityTextPrimary = Color.black
    static var securityTextSecondary = Color.gray
}

// 메인 뷰: 보안/인증 설정 화면
struct SecurityAuthView: View {
    // --- UI 상태를 관리하기 위한 변수들 ---
    // @AppStorage를 사용하여 사용자의 설정을 앱 내에 영구적으로 저장합니다.
    @AppStorage("isFaceIdEnabled") private var isFaceIdEnabled = false
    @AppStorage("isOverseasLoginBlockEnabled") private var isOverseasLoginBlockEnabled = false
    
    // 알림창 표시를 위한 상태 변수
    @State private var showAlert = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""

    var body: some View {
        ZStack {
            // 흰색 배경
            Color.white
                .edgesIgnoringSafeArea(.all)
            
            ScrollView {
                VStack(spacing: 16) {
                    // PIN 비밀번호 변경 섹션
                    SecurityCardView {
                        NavigationLink(destination: PINChangeView()) {
                            HStack {
                                SecurityInfoRow(
                                    title: "PIN 비밀번호 변경",
                                    subtitle: "PIN 번호 분실시 처음부터 진행해야 합니다.",
                                    iconName: "lock.shield"
                                )
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(.securityTextSecondary)
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(SecurityScaleButtonStyle())
                    }
                    
                    // Face ID 사용 섹션
                    SecurityCardView {
                        SecurityToggleRow(
                            title: "Face ID 사용",
                            subtitle: "Face ID를 사용하여 본인 확인.",
                            iconName: "faceid",
                            isOn: $isFaceIdEnabled
                        )
                        .onChange(of: isFaceIdEnabled) { _, newValue in
                            if newValue {
                                authenticateWithBiometrics()
                            }
                        }
                    }
                    
                    // 해외 로그인 차단 섹션
                    SecurityCardView {
                        NavigationLink(destination: OverseasLoginBlockView()) {
                            HStack {
                                SecurityInfoRow(
                                    title: "해외 로그인 차단",
                                    subtitle: "국가별 로그인 허용/차단 설정",
                                    iconName: "globe"
                                )
                                Spacer()
                                HStack(spacing: 6) {
                                    Text(isOverseasLoginBlockEnabled ? "사용 중" : "꺼짐")
                                        .font(.system(size: 14, weight: .medium))
                                        .foregroundColor(isOverseasLoginBlockEnabled ? .securityAccentStart : .securityTextSecondary)
                                    
                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 14, weight: .semibold))
                                        .foregroundColor(.securityTextSecondary)
                                }
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(SecurityScaleButtonStyle())
                    }
                    
                    // 로그인 이력 섹션
                    SecurityCardView {
                        NavigationLink(destination: LoginHistoryView()) {
                            HStack {
                                SecurityInfoRow(
                                    title: "로그인 이력",
                                    subtitle: "로그인 이력 및 평소와 다른 IP로그인 체크",
                                    iconName: "list.clipboard"
                                )
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(.securityTextSecondary)
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(SecurityScaleButtonStyle())
                    }
                }
                .padding(.horizontal)
                .padding(.top, 16)
                .padding(.bottom, 24)
            }
        }
        .navigationTitle("보안 / 인증")
        .navigationBarTitleDisplayMode(.inline)
        .alert(alertTitle, isPresented: $showAlert) {
            Button("확인", role: .cancel) { }
        } message: {
            Text(alertMessage)
        }
    }
    
    // --- 생체 인증 (Face ID) 로직 ---
    private func authenticateWithBiometrics() {
        let context = LAContext()
        var error: NSError?

        // 1. 기기에서 생체 인증을 사용할 수 있는지 확인합니다.
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            let reason = "Face ID 인증을 통해 기능을 활성화합니다."
            
            // 2. 생체 인증을 요청합니다. 이 과정은 비동기적으로 실행됩니다.
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
                // 3. UI 업데이트는 메인 스레드에서 처리해야 합니다.
                DispatchQueue.main.async {
                    if success {
                        // 4a. 인증에 성공하면, 토글 상태를 '켬'으로 유지합니다.
                        print("Face ID 인증 성공")
                    } else {
                        // 4b. 인증에 실패하면, 토글을 다시 '끔' 상태로 되돌리고 사용자에게 알림을 표시합니다.
                        self.alertTitle = "인증 실패"
                        self.alertMessage = authenticationError?.localizedDescription ?? "Face ID 인증에 실패했습니다."
                        self.showAlert = true
                        self.isFaceIdEnabled = false
                    }
                }
            }
        } else {
            // 5. 기기에서 생체 인증을 지원하지 않거나 설정되지 않은 경우, 사용자에게 알림을 표시합니다.
            self.alertTitle = "사용 불가"
            self.alertMessage = error?.localizedDescription ?? "Face ID를 사용할 수 없는 기기입니다."
            self.showAlert = true
            self.isFaceIdEnabled = false
        }
    }
}

// MARK: - Destination Views (이동할 화면들)

// 모든 목적지 뷰는 이제 별도 파일로 분리되었습니다.
// (PINChangeView, OverseasLoginBlockView, LoginHistoryView)

// MARK: - Reusable Views (재사용 가능한 뷰)

fileprivate struct SecurityCardView<Content: View>: View {
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .padding(16)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.securityCard)
                    .shadow(color: Color.black.opacity(0.16), radius: 5, x: 0, y: 2)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.white.opacity(0.06), lineWidth: 0.5)
            )
            .padding(.horizontal, 16)
    }
}

fileprivate struct SecurityInfoRow: View {
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
                    .foregroundColor(.securityAccentStart)
                    .frame(width: 24, height: 24)
                    .padding(8)
                    .background(
                        RoundedRectangle(cornerRadius: 10)
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [.securityAccentStart.opacity(0.2), .securityAccentEnd.opacity(0.05)]),
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                    )
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.securityTextPrimary)
                
                Text(subtitle)
                    .font(.system(size: 13, weight: .regular))
                    .foregroundColor(.securityTextSecondary)
                    .lineSpacing(4)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}

fileprivate struct SecurityToggleRow: View {
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
            SecurityInfoRow(title: title, subtitle: subtitle, iconName: iconName)
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .toggleStyle(SwitchToggleStyle(tint: .securityAccentStart))
                .scaleEffect(0.85)
                .animation(.spring(response: 0.2), value: isOn)
        }
        .contentShape(Rectangle())
    }
}

// 버튼 애니메이션 스타일
fileprivate struct SecurityScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(.spring(response: 0.2, dampingFraction: 0.6), value: configuration.isPressed)
    }
}


// MARK: - Preview (미리보기)

struct SecurityAuthView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            SecurityAuthView()
        }
    }
}
