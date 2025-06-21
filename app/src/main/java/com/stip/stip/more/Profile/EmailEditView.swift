import SwiftUI

// MARK: - Design System Extensions

extension Color {
    static let accentBlue = Color(red: 56/255, green: 189/255, blue: 248/255)
    static let deepBlue = Color(red: 23/255, green: 114/255, blue: 180/255)
    static let royalPurple = Color(red: 76/255, green: 29/255, blue: 149/255)
    static let cardBackground = Color(UIColor.systemBackground)
    static let cardBackgroundAlt = Color(UIColor.systemBackground).opacity(0.7)
    static let subtleText = Color(UIColor.secondaryLabel)
    static let inputBackground = Color(UIColor.systemGray6).opacity(0.8)
    static let inputBorder = Color(UIColor.systemGray4)
    static let errorRed = Color.red.opacity(0.9)
    static let successGreen = Color(red: 52/255, green: 199/255, blue: 89/255)
    static let highlightTeal = Color(red: 64/255, green: 200/255, blue: 224/255)
}

// Custom Text Field Style
struct ModernTextFieldStyle: TextFieldStyle {
    var isError: Bool
    var isDisabled: Bool
    
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(18)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Material.ultraThinMaterial)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(
                                isError ? Color.errorRed : 
                                    isDisabled ? Color.inputBorder.opacity(0.3) : 
                                    Color.inputBorder,
                                lineWidth: isError ? 1.5 : 1
                            )
                    )
                    .shadow(color: isError ? Color.errorRed.opacity(0.2) : Color.black.opacity(0.05), 
                            radius: 10, x: 0, y: isError ? 2 : 5)
            )
            .opacity(isDisabled ? 0.7 : 1.0)
    }
}

// Custom Button Style
struct GradientButtonStyle: ButtonStyle {
    var isEnabled: Bool
    var isPrimary: Bool = true
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 17, weight: .bold))
            .foregroundColor(.white)
            .padding(.vertical, 18)
            .frame(maxWidth: .infinity)
            .background(
                Group {
                    if isPrimary && isEnabled {
                        LinearGradient(
                            gradient: Gradient(colors: [Color.accentBlue, Color.highlightTeal, Color.deepBlue]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    } else {
                        LinearGradient(
                            gradient: Gradient(colors: [Color(UIColor.systemGray4), Color(UIColor.systemGray4).opacity(0.8)]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    }
                }
            )
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                    .blendMode(.overlay)
            )
            .shadow(color: isPrimary && isEnabled ? Color.accentBlue.opacity(0.4) : Color.black.opacity(0.05), 
                    radius: 15, x: 0, y: 5)
            .scaleEffect(configuration.isPressed ? 0.98 : 1)
            .animation(.spring(), value: configuration.isPressed)
    }
}

struct EmailEditView: View {
    // MARK: - Properties
    
    @Binding var email: String
    var onSave: (String) -> Void
    
    // 이메일 수정을 위한 상태
    @State private var editingEmail: String
    
    // 인증 절차를 위한 상태
    @State private var isVerificationFlowActive = false
    @State private var verificationCode = ""
    @State private var attempts = 0
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var remainingSeconds = 300 // 5분
    @State private var timer: Timer? = nil
    @State private var showLimitAlert = false
    @State private var showSuccessAlert = false
    @State private var showResendAlert = false
    
    @Environment(\.presentationMode) var presentationMode
    
    // MARK: - Initializer
    
    init(email: Binding<String>, onSave: @escaping (String) -> Void) {
        self._email = email
        self.onSave = onSave
        _editingEmail = State(initialValue: email.wrappedValue)
    }
    
    // MARK: - Body
    
    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    Spacer().frame(height: 35)

                    // 제목
                    VStack(spacing: 8) {
                        Text("이메일 주소를 입력해 주세요")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(Color(UIColor.label))
                        
                        Text("새 이메일 주소로 변경 후 인증이 필요합니다")
                            .font(.system(size: 15))
                            .foregroundColor(.subtleText)
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.bottom, 25)

                    emailDisplaySection
                        
                    newEmailInputSection

                    if isVerificationFlowActive {
                        verificationSection
                    }
                    
                    Spacer().frame(minHeight: 25)

                    guidanceText
                    
                    bottomActionButton
                        .padding(.top, 24)
                        .padding(.bottom, 40)
                }
                .padding(.horizontal, 24)
                .frame(minHeight: geometry.size.height)
            }
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [Color.cardBackground, Color.cardBackground, Color.cardBackground.opacity(0.97)]),
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
        }
        .edgesIgnoringSafeArea(.bottom)
        .navigationBarBackButtonHidden(true)
        .navigationTitle("이메일 변경")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { 
                    withAnimation(.easeInOut(duration: 0.2)) {
                        presentationMode.wrappedValue.dismiss() 
                    }
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "chevron.left")
                            .imageScale(.medium)
                            .fontWeight(.semibold)
                        
                        Text("뒤로")
                            .font(.system(size: 17, weight: .medium))
                    }
                    .foregroundColor(.black)
                    .contentShape(Rectangle())
                }
            }
        }
        .onDisappear {
            timer?.invalidate()
        }
        .alert(isPresented: $showLimitAlert) {
            Alert(
                title: Text("안내"),
                message: Text("인증번호 입력오류 5회로 인증이 불가합니다. 다시 인증코드를 발송하여 인증해 주세요."),
                dismissButton: .default(Text("확인")) {
                    resetVerification()
                }
            )
        }
        .alert("안내", isPresented: $showSuccessAlert) {
            Button("확인") {
                onSave(editingEmail)
                presentationMode.wrappedValue.dismiss()
            }
        } message: {
            Text("이메일 인증이 완료되었습니다.")
        }
        .alert("인증번호 재전송", isPresented: $showResendAlert) {
            Button("취소", role: .cancel) {}
            Button("확인") {
                resendVerificationCode()
            }
        } message: {
            Text("입력하신 이메일로 인증번호를 다시 보내시겠습니까?")
        }
    }
    
    // MARK: - View Components
    
    private var emailDisplaySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 6) {
                Image(systemName: "envelope")
                    .foregroundColor(.accentBlue)
                    .font(.system(size: 12, weight: .semibold))
                
                Text("현재 이메일")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.subtleText)
            }
            
            Text(email)
                .font(.system(size: 16, weight: .medium))
                .padding(18)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Material.regularMaterial)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color.inputBorder.opacity(0.3), lineWidth: 1)
                )
                .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: 4)
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.cardBackground)
                .shadow(color: Color.black.opacity(0.08), radius: 15, x: 0, y: 5)
        )
    }
    
    private var newEmailInputSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 6) {
                Image(systemName: "envelope.badge")
                    .foregroundColor(.accentBlue)
                    .font(.system(size: 12, weight: .semibold))
                    
                Text("새 이메일")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.subtleText)
            }
            .padding(.top, 5)

            TextField("새 이메일 주소를 입력하세요", text: $editingEmail)
                .font(.system(size: 16, weight: .medium))
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .disabled(isVerificationFlowActive)
                .textFieldStyle(
                    ModernTextFieldStyle(
                        isError: false,
                        isDisabled: isVerificationFlowActive
                    )
                )
                .transition(.opacity)
                .animation(.easeInOut(duration: 0.25), value: isVerificationFlowActive)
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.cardBackground)
                .shadow(color: Color.black.opacity(0.08), radius: 15, x: 0, y: 5)
        )
        .padding(.vertical, 8)
    }
    
    private var verificationSection: some View {
        VStack(alignment: .leading, spacing: 20) {
            HStack(spacing: 6) {
                Image(systemName: "shield.fill")
                    .foregroundColor(.accentBlue)
                    .font(.system(size: 12))
                
                Text("이메일 인증")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.subtleText)
            }
            
            VStack(spacing: 15) {
                HStack(spacing: 12) {
                    Image(systemName: "checkmark.shield")
                        .foregroundColor(.subtleText.opacity(0.7))
                        .font(.system(size: 14))
                    
                    TextField("인증번호 입력", text: $verificationCode)
                        .font(.system(size: 16, weight: .medium))
                        .keyboardType(.numberPad)
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 18)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Material.ultraThinMaterial)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(
                                    showError ? Color.errorRed : Color.inputBorder,
                                    lineWidth: showError ? 1.5 : 1
                                )
                        )
                        .shadow(color: showError ? Color.errorRed.opacity(0.2) : Color.black.opacity(0.05), 
                                radius: 10, x: 0, y: showError ? 2 : 5)
                )
                    .frame(maxWidth: .infinity)
                    .transition(.opacity)
                    .animation(.easeOut(duration: 0.2), value: showError)
                
                Button {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                        verifyCode()
                    }
                } label: {
                    Text("인증하기")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 24)
                        .contentShape(Rectangle())
                }
                .buttonStyle(GradientButtonStyle(isEnabled: true, isPrimary: true))
            }
            
            if showError {
                HStack(spacing: 5) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundColor(.errorRed)
                        .font(.system(size: 12))
                        
                    Text(errorMessage)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.errorRed)
                }
                .padding(.horizontal, 5)
                .transition(.move(edge: .top).combined(with: .opacity))
            }
            
            HStack(spacing: 6) {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 12))
                    .foregroundColor(.accentBlue)
                
                Button("인증번호를 받지 못하셨나요?") {
                    showResendAlert = true
                }
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.accentBlue)
            }
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.top, 5)
        }
        .padding(20)
        .background(
            ZStack {
                RoundedRectangle(cornerRadius: 24)
                    .fill(Material.thin)
                    
                RoundedRectangle(cornerRadius: 24)
                    .stroke(Color.accentBlue.opacity(0.15), lineWidth: 1)
            }
        )
        .shadow(color: Color.black.opacity(0.08), radius: 15, x: 0, y: 8)
        .padding(.vertical, 10)
    }
    
    private var guidanceText: some View {
        HStack(alignment: .top, spacing: 16) {
            Image(systemName: "info.circle.fill")
                .foregroundColor(Color.accentBlue)
                .font(.system(size: 22))
                .padding(.top, 3)
            
            Text("""
            입력한 이메일로 인증번호가 발송되며, 발송 후 5분 이내에 인증해야 합니다. 이메일인증이 완료되지 않으면, 해당 이메일은 회원 탈퇴 시 자동 삭제됩니다.
            """)
            .font(.system(size: 14, weight: .regular))
            .foregroundColor(.subtleText)
            .lineSpacing(5)
            .multilineTextAlignment(.leading)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background(
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(Material.ultraThinMaterial)
                
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.accentBlue.opacity(0.1), lineWidth: 1)
            }
        )
        .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 5)
    }
    
    @ViewBuilder
    private var bottomActionButton: some View {
        if isVerificationFlowActive {
            HStack {
                Image(systemName: "timer")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)
                
                Text(timeString)
                    .font(.system(size: 17, weight: .bold))
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 18)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color(UIColor.systemGray4))
                    .shadow(color: Color.black.opacity(0.12), radius: 12, x: 0, y: 6)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .strokeBorder(Color.white.opacity(0.2), lineWidth: 1)
                    .blendMode(.overlay)
            )
        } else {
            Button(action: {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    startVerificationFlow()
                }
            }) {
                HStack(spacing: 8) {
                    Text("이메일 변경하기")
                        .font(.system(size: 17, weight: .bold))
                        
                    if isValidEmail(editingEmail) {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .bold))
                            .transition(.opacity)
                    }
                }
                .foregroundColor(.white)
            }
            .buttonStyle(GradientButtonStyle(isEnabled: isValidEmail(editingEmail)))
            .disabled(!isValidEmail(editingEmail))
            .padding(.top, 5)
        }
    }
    
    // MARK: - Helper Methods
    
    private func getSafeAreaBottom() -> CGFloat {
        let keyWindow = UIApplication.shared.connectedScenes
            .filter { $0.activationState == .foregroundActive }
            .map { $0 as? UIWindowScene }
            .compactMap { $0 }
            .first?.windows
            .filter { $0.isKeyWindow }.first
        
        return keyWindow?.safeAreaInsets.bottom ?? 0
    }
    
    private var timeString: String {
        let minutes = remainingSeconds / 60
        let seconds = remainingSeconds % 60
        return String(format: "(%02d:%02d)", minutes, seconds)
    }
    
    private func isValidEmail(_ email: String) -> Bool {
        let emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailPredicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return emailPredicate.evaluate(with: email) && !email.isEmpty && email != self.email
    }
    
    private func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if remainingSeconds > 0 {
                remainingSeconds -= 1
            } else {
                timer?.invalidate()
                timer = nil
                if !showSuccessAlert {
                    errorMessage = "인증 시간이 만료되었습니다."
                    showError = true
                }
            }
        }
    }
    
    private func startVerificationFlow() {
        isVerificationFlowActive = true
        showError = false
        verificationCode = ""
        attempts = 0
        remainingSeconds = 300
        startTimer()
    }
    
    private func verifyCode() {
        if verificationCode == "1234" {
            timer?.invalidate()
            showSuccessAlert = true
        } else {
            attempts += 1
            if attempts >= 5 {
                showLimitAlert = true
            } else {
                errorMessage = "인증번호를 다시 확인해 주세요. (\(attempts)/5)"
                showError = true
                verificationCode = ""
            }
        }
    }
    
    private func resendVerificationCode() {
        startVerificationFlow()
    }
    
    private func resetVerification() {
        isVerificationFlowActive = false
        attempts = 0
        showError = false
        verificationCode = ""
        timer?.invalidate()
    }
}

// MARK: - Preview

struct EmailEditView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            EmailEditView(email: .constant("example@email.com"), onSave: { newEmail in
                print("Email saved: \(newEmail)")
            })
        }
    }
}
