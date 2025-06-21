import SwiftUI

struct PinVerificationView: View {
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    @State private var pin = ""
    @State private var showError = false
    @State private var attempts = 0
    @State private var keypadNumbers: [String] = (0...9).map { String($0) }.shuffled()
    
    // UserData 싱글톤 모델 사용
    @ObservedObject private var userData = UserData.shared
    private let pinLength = 6
    
    // 부모 뷰에 결과를 전달하기 위한 클로저
    var onComplete: (Bool) -> Void
    
    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 0) {
                    // 상단 네비게이션 바
                    HStack {
                        Button(action: {
                            onComplete(false)
                        }) {
                            Image(systemName: "chevron.left")
                                .foregroundColor(Color.hex("14181B"))
                                .padding()
                        }
                        
                        Spacer()
                        
                        Text("PIN 비밀번호 확인")
                            .font(.system(size: horizontalSizeClass == .compact ? 17 : 20, weight: .semibold))
                        
                        Spacer()
                        
                        // 정렬을 위해 비어있는 Spacer 추가
                        Spacer().frame(width: 40)
                    }
                    .padding(.top, 8)
                    
                    Spacer()
                        .frame(height: horizontalSizeClass == .compact ? 40 : 60)
                    
                    // 안내 문구
                    VStack(spacing: 16) {
                        Text("PIN 비밀번호 확인")
                            .font(.system(size: horizontalSizeClass == .compact ? 22 : 26, weight: .bold))
                        
                        Text("계정 정보 수정을 위해\n비밀번호를 입력해 주세요")
                            .font(.system(size: horizontalSizeClass == .compact ? 16 : 18))
                            .foregroundColor(Color.hex("595F63"))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, horizontalSizeClass == .compact ? 40 : 60)
                    }
                    .padding(.bottom, horizontalSizeClass == .compact ? 40 : 60)
                    
                    // PIN 입력 표시기
                    HStack(spacing: horizontalSizeClass == .compact ? 16 : 24) {
                        ForEach(0..<pinLength, id: \.self) { index in
                            Circle()
                                .fill(index < pin.count ? Color.hex("30C6E8") : Color.hex("E5E5EA"))
                                .frame(width: horizontalSizeClass == .compact ? 16 : 20, 
                                       height: horizontalSizeClass == .compact ? 16 : 20)
                        }
                    }
                    .padding(.bottom, horizontalSizeClass == .compact ? 40 : 60)
                    
                    if showError {
                        Text("PIN 번호가 일치하지 않습니다")
                            .foregroundColor(.red)
                            .font(.system(size: horizontalSizeClass == .compact ? 14 : 16))
                            .padding(.bottom, 20)
                    }
                    
                    Spacer()
                        .frame(height: horizontalSizeClass == .compact ? 20 : 40)
                    
                    // 숫자 키패드
                    VStack(spacing: horizontalSizeClass == .compact ? 20 : 30) {
                        ForEach(0..<3) { row in
                            HStack(spacing: horizontalSizeClass == .compact ? 20 : 30) {
                                ForEach(0..<3) { col in
                                    let index = row * 3 + col
                                    PINKeypadButton(number: keypadNumbers[index]) {
                                        numberTapped(keypadNumbers[index])
                                    }
                                    .frame(width: horizontalSizeClass == .compact ? 80 : 100, 
                                           height: horizontalSizeClass == .compact ? 60 : 70)
                                }
                            }
                        }
                        
                        HStack(spacing: horizontalSizeClass == .compact ? 20 : 30) {
                            PINKeypadButton(number: keypadNumbers[9]) {
                                numberTapped(keypadNumbers[9])
                            }
                            .frame(width: horizontalSizeClass == .compact ? 80 : 100, 
                                   height: horizontalSizeClass == .compact ? 60 : 70)
                            
                            Button(action: {
                                withAnimation { keypadNumbers.shuffle() }
                            }) {
                                Text("재배열")
                                    .font(.system(size: horizontalSizeClass == .compact ? 16 : 18, weight: .regular))
                                    .foregroundColor(Color.hex("14181B"))
                                    .frame(width: horizontalSizeClass == .compact ? 80 : 100, 
                                           height: horizontalSizeClass == .compact ? 60 : 70)
                                    .background(Color.hex("F2F2F7"))
                                    .cornerRadius(30)
                            }
                            
                            Button(action: {
                                if !pin.isEmpty { pin.removeLast() }
                                showError = false
                            }) {
                                Image(systemName: "delete.left")
                                    .font(.system(size: horizontalSizeClass == .compact ? 24 : 28, weight: .light))
                                    .foregroundColor(Color.hex("14181B"))
                                    .frame(width: horizontalSizeClass == .compact ? 80 : 100, 
                                           height: horizontalSizeClass == .compact ? 60 : 70)
                                    .background(Color.hex("F2F2F7"))
                                    .cornerRadius(30)
                            }
                        }
                    }
                    .padding(.bottom, horizontalSizeClass == .compact ? 40 : 60)
                }
                .frame(minHeight: geometry.size.height)
                .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
            }
            .background(Color.white)
        }
        .edgesIgnoringSafeArea(.bottom)
    }
    
    private func numberTapped(_ number: String) {
        if pin.count < pinLength {
            pin.append(number)
            showError = false // 새로운 번호 입력 시 에러 메시지 숙김
            
            // PIN 입력이 완료되면 자동 확인
            if pin.count == pinLength {
                verifyPin()
            }
        }
    }
    
    private func verifyPin() {
        if userData.verifyPin(pin) {
            // 성공 시, 부모에게 true 전달. 화면 전환은 부모의 몫.
            onComplete(true)
        } else {
            attempts += 1
            showError = true
            pin = ""
            // 실패 시, 부모에게 false 전달.
            // onComplete(false) // 5회 실패 시 자동으로 닫히게 하려면 여기서 호출
            
            if attempts >= 5 {
                // 5회 실패 시 자동으로 닫기
                onComplete(false)
            }
        }
    }
}

// PINKeypadButton과 Color.hex()는 이미 프로젝트에 정의되어 있으므로 여기서는 재선언하지 않습니다.

struct PinVerificationView_Previews: PreviewProvider {
    static var previews: some View {
        PinVerificationView(onComplete: { _ in })
    }
}
