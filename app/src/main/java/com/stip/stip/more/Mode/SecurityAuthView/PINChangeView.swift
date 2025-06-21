import SwiftUI
import UIKit

// 기존 AuthSelectionView 사용을 위한 참조

// MARK: - ViewModel
@MainActor
class PINChangeViewModel: ObservableObject {
    // PIN 변경 단계를 정의하는 열거형
    enum PINChangeStep {
        case verifyingCurrent, enteringNew, confirmingNew, completed
    }
    
    // --- @Published 프로퍼티: 변경 시 뷰가 자동으로 업데이트됩니다 ---
    @Published var currentStep: PINChangeStep = .verifyingCurrent
    @Published var pin: String = ""
    @Published var errorText: String?
    @Published var shouldLogout = false // 로그아웃 후 화면 전환을 위한 상태 변수
    
    private var newPin: String = ""
    private var verificationAttempts = 0 // 현재 PIN 확인 시도 횟수
    private let pinLength = 6
    // 실제 앱에서는 Keychain 등 안전한 곳에서 현재 PIN을 가져와야 합니다.
    private let validCurrentPin = "123456"

    // --- Computed Properties: 현재 단계에 따라 다른 텍스트를 보여줍니다 ---
    var title: String {
        switch currentStep {
        case .verifyingCurrent: "현재 PIN 확인"
        case .enteringNew: "새 PIN 입력"
        case .confirmingNew: "새 PIN 확인"
        case .completed: "변경 완료"
        }
    }
    
    var subtitle: String {
        switch currentStep {
        case .verifyingCurrent: "계정 보호를 위해 현재 PIN 6자리를 입력해주세요."
        case .enteringNew: "새로 사용할 6자리 PIN을 입력해주세요."
        case .confirmingNew: "확인을 위해 한 번 더 입력해주세요."
        case .completed: "PIN 번호가 성공적으로 변경되었습니다."
        }
    }
    
    // --- Public Methods: 뷰에서 호출하는 함수들 ---
    func numberTapped(_ number: String) {
        // 잠금 상태에서는 입력을 받지 않음
        guard pin.count < pinLength else { return }
        errorText = nil
        pin.append(number)
        
        if pin.count == pinLength {
            processPin()
        }
    }
    
    func deleteLastDigit() {
        guard !pin.isEmpty else { return }
        pin.removeLast()
    }
    
    // --- Private Logic: 핵심 PIN 처리 로직 ---
    private func processPin() {
        switch currentStep {
        case .verifyingCurrent:
            if pin == validCurrentPin {
                // 성공: 다음 단계로
                currentStep = .enteringNew
                pin = ""
            } else {
                // 실패: 에러 카운트 증가 및 메시지 표시
                verificationAttempts += 1
                if verificationAttempts >= 5 {
                    // 5회 실패 시 로그아웃 상태로 변경
                    shouldLogout = true
                } else {
                    errorText = "PIN이 일치하지 않습니다. (\(verificationAttempts)/5)"
                }
                pin = ""
            }
        case .enteringNew:
            // 새 PIN 저장 후 확인 단계로
            newPin = pin
            currentStep = .confirmingNew
            pin = ""
        case .confirmingNew:
            if pin == newPin {
                // 성공: PIN 저장 및 완료
                // 실제 앱에서는 여기에 Keychain에 새 PIN을 저장하는 로직을 추가합니다.
                print("새로운 PIN [\(pin)] 저장 성공!")
                currentStep = .completed
            } else {
                // 실패: 새 PIN 입력 단계로 되돌아가기
                errorText = "PIN이 일치하지 않습니다. 다시 시도해주세요."
                currentStep = .enteringNew
                pin = ""
                newPin = ""
            }
        case .completed:
            break
        }
    }
}

// MARK: - Main View
struct PINChangeView: View {
    @StateObject private var viewModel = PINChangeViewModel()
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        VStack(spacing: 20) {
            // --- 상단 안내 문구 ---
            Text(viewModel.title)
                .font(.system(size: 22, weight: .bold))
                .padding(.top, 40)

            Text(viewModel.subtitle)
                .font(.system(size: 16))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
                .frame(height: 60)

            // PIN 입력 상태 표시기 (완료 상태 아닐 때만 표시)
            if viewModel.currentStep != .completed {
                PINIndicator(pinCount: viewModel.pin.count)
                    .padding(.vertical, 10)
            }

            if let errorText = viewModel.errorText {
                Text(errorText)
                    .foregroundColor(.red)
                    .font(.caption)
                    .padding(.bottom, 10)
            }

            Spacer()

            // --- UI 분기 처리 ---
            switch viewModel.currentStep {
            case .completed:
                // PIN 변경 완료 시
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                    .padding()
                
                Button("완료") {
                    presentationMode.wrappedValue.dismiss()
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(height: 50)
                .frame(maxWidth: .infinity)
                .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                .cornerRadius(10)
                .padding(.horizontal, 40)
                
                Spacer()
                
            default:
                 // 숫자 키패드
                PINKeypad(onNumberTapped: viewModel.numberTapped, onDeleteTapped: viewModel.deleteLastDigit)
            }
        }
        .navigationTitle("PIN 변경")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button {
                    presentationMode.wrappedValue.dismiss()
                } label: {
                    Image(systemName: "chevron.left")
                }
                .tint(.primary)
            }
        }
        // 로그아웃 상태가 되면 AuthSelectionView를 전체 화면으로 띄웁니다.
        .fullScreenCover(isPresented: $viewModel.shouldLogout) {
            AuthSelectionView()
        }
    }
}

// 참고: AuthSelectionView는 이미 /Users/stip/Desktop/STIP/STIP/STIP/Views/Auth/Login/AuthSelectionView.swift에 구현되어 있습니다.
// 따라서 여기서는 중복 선언하지 않고 해당 뷰를 사용합니다.


// MARK: - Reusable Subviews (재사용 가능한 뷰)

struct PINIndicator: View {
    let pinCount: Int
    let maxDigits: Int = 6

    var body: some View {
        HStack(spacing: 16) {
            ForEach(0..<maxDigits, id: \.self) { index in
                Circle()
                    .fill(index < pinCount ? Color(red: 48/255, green: 198/255, blue: 232/255) : Color.gray.opacity(0.3))
                    .frame(width: 16, height: 16)
            }
        }
    }
}

struct PINKeypad: View {
    var onNumberTapped: (String) -> Void
    var onDeleteTapped: () -> Void
    
    private let keypadLayout: [[String]] = [
        ["1", "2", "3"],
        ["4", "5", "6"],
        ["7", "8", "9"],
        ["", "0", "delete"]
    ]
    
    var body: some View {
        VStack(spacing: 16) {
            ForEach(keypadLayout, id: \.self) { row in
                HStack(spacing: 24) {
                    ForEach(row, id: \.self) { key in
                        if key.isEmpty {
                            Spacer().frame(width: 80, height: 80)
                        } else if key == "delete" {
                            Button(action: onDeleteTapped) {
                                Image(systemName: "delete.left")
                                    .font(.title2)
                                    .frame(width: 80, height: 80)
                                    .foregroundColor(.primary)
                            }
                        } else {
                            Button(action: { onNumberTapped(key) }) {
                                Text(key)
                                    .font(.title)
                                    .fontWeight(.medium)
                                    .frame(width: 80, height: 80)
                                    .background(Color.gray.opacity(0.1))
                                    .clipShape(Circle())
                                    .foregroundColor(.primary)
                            }
                        }
                    }
                }
            }
        }
        .padding(.bottom, 20)
    }
}

// MARK: - Preview (미리보기)

struct PINChangeView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            PINChangeView()
        }
    }
}

