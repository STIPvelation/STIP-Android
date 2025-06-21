import SwiftUI

// MARK: - 디자인 시스템 확장

private extension Color {
    static let modeAccentStart = Color(red: 48/255, green: 198/255, blue: 232/255)
    static let modeAccentEnd = Color(red: 81/255, green: 145/255, blue: 240/255)
    static let modeBackground = Color(UIColor.systemGroupedBackground)
    static let modeCard = Color(UIColor.secondarySystemGroupedBackground)
    static let modeDivider = Color(UIColor.separator).opacity(0.5)
    static let modeTextPrimary = Color(UIColor.label)
    static let modeTextSecondary = Color(UIColor.secondaryLabel)
}

// MARK: - Data Models and Enums

// 화면 모드 선택을 위한 열거형
enum ScreenMode: String, CaseIterable {
    case normal = "일반 모드"
    case dark = "다크 모드"
    case design = "디자인 모드"
    case user = "유저 모드"
    
    var description: String {
        switch self {
        case .normal: return "STIP에서 제공하는 일반 화면 모드 입니다."
        case .dark: return "STIP에서 제공하는 다크 모드 입니다."
        case .design: return "분기별로 신규 디자인 화면을 제공합니다."
        case .user: return "유저의 디자인으로 화면을 제공합니다."
        }
    }
}


// MARK: - Main View

struct ModeSettingsView: View {
    // --- UI 상태를 관리하기 위한 변수들 ---
    @State private var isScreenOnModeEnabled = false
    // --- 삭제된 부분 ---
    // @State private var isExecutionEffectEnabled = false
    // @State private var isNewOrderEffectEnabled = false
    @State private var isOrderBookEffectEnabled = false
    @State private var selectedScreenMode: ScreenMode = .normal
    
    // '준비중' 알림창 표시를 위한 상태 변수
    @State private var showPreparingAlert = false

    var body: some View {
        ZStack {
            // 배경 그라디언트
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.modeBackground.opacity(0.7),
                    Color.modeBackground.opacity(0.4)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 0) {
                List {
                    // --- 일반 화면 섹션 ---
                    Section {
                        ModeToggleRow(
                            title: "꺼짐 방지 모드",
                            subtitle: "앱 사용 중 화면 꺼짐 예방.",
                            isOn: Binding(
                                get: { isScreenOnModeEnabled },
                                set: { _ in showPreparingAlert = true }
                            )
                        )
                    } header: {
                        SectionHeaderView(title: "일반 화면", icon: "slider.horizontal.3")
                    }
                    .listRowBackground(Color.modeCard)

                    // --- 거래소 화면 섹션 ---
                    Section {
                        RefreshRow(
                            title: "IP정보 갱신 하기",
                            subtitle: "IP정보를 새롭게 갱신합니다.",
                            buttonAction: { showPreparingAlert = true }
                        )
                    } header: {
                        SectionHeaderView(title: "거래소 화면", icon: "chart.line.uptrend.xyaxis")
                    }
                    .listRowBackground(Color.modeCard)
                    
                    // --- 오더 화면 섹션 ---
                    Section {
                        RadioSelectionRow(
                            title: "호가창 증감 범위",
                            subtitle: "호가창의 수가 증가 혹은 감소에 따라 반응 효과.",
                            isSelected: isOrderBookEffectEnabled,
                            action: { showPreparingAlert = true }
                        )
                    } header: {
                        SectionHeaderView(title: "오더 화면", icon: "list.bullet.rectangle")
                    }
                    .listRowBackground(Color.modeCard)

                    // --- 화면 모드 섹션 ---
                    Section {
                        ForEach(ScreenMode.allCases, id: \.self) { mode in
                            ModeSelectionRow(
                                title: mode.rawValue,
                                subtitle: mode.description,
                                isSelected: selectedScreenMode == mode,
                                action: { showPreparingAlert = true }
                            )
                        }
                    } header: {
                        SectionHeaderView(title: "화면 모드", icon: "display")
                    }
                    .listRowBackground(Color.modeCard)
                }
                .listStyle(.insetGrouped)
            }
        }
        .navigationTitle("모드 설정")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("모드 설정")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.modeTextPrimary)
            }
        }
        .alert("준비중", isPresented: $showPreparingAlert) {
            Button("확인", role: .cancel) { }
        } message: {
            Text("해당 기능은 현재 준비중입니다.")
        }
    }
}


// MARK: - Reusable Views (재사용 가능한 뷰)

// 섹션 헤더 뷰 - 아이콘을 포함한 모던한 디자인
struct SectionHeaderView: View {
    let title: String
    let icon: String
    
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundColor(.modeAccentStart)
                .font(.system(size: 16, weight: .medium))
            
            Text(title)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(.modeTextPrimary)
        }
        .padding(.bottom, 6)
    }
}

// 기본 텍스트 행 (제목 + 부제목)
struct InfoRow: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(.modeTextPrimary)
            
            Text(subtitle)
                .font(.system(size: 13))
                .foregroundColor(.modeTextSecondary)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(.vertical, 4)
    }
}

// 토글 스위치가 있는 행
struct ModeToggleRow: View {
    let title: String
    let subtitle: String
    @Binding var isOn: Bool

    var body: some View {
        HStack {
            InfoRow(title: title, subtitle: subtitle)
            Spacer()
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(.accentBlue)
                .scaleEffect(0.9)
                .animation(.spring(response: 0.2, dampingFraction: 0.5), value: isOn)
        }
        .contentShape(Rectangle())
        .padding(.vertical, 6)
    }
}

// '갱신' 버튼이 있는 행
struct RefreshRow: View {
    let title: String
    let subtitle: String
    let buttonAction: () -> Void

    var body: some View {
        HStack {
            InfoRow(title: title, subtitle: subtitle)
            Spacer()
            Button(action: buttonAction) {
                Text("갱신")
                    .font(.system(size: 14, weight: .medium))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        Capsule()
                            .fill(LinearGradient(
                                gradient: Gradient(colors: [.modeAccentStart, .modeAccentEnd]),
                                startPoint: .leading,
                                endPoint: .trailing
                            ))
                    )
                    .foregroundColor(.white)
            }
            .buttonStyle(ModeScaleButtonStyle())
        }
        .padding(.vertical, 6)
    }
}

// 버튼 애니메이션 스타일
struct ModeScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(.spring(response: 0.2, dampingFraction: 0.6), value: configuration.isPressed)
    }
}

// 원형 선택 버튼이 있는 행 (라디오 버튼 스타일)
struct RadioSelectionRow: View {
    let title: String
    let subtitle: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                InfoRow(title: title, subtitle: subtitle)
                Spacer()
                ZStack {
                    Circle()
                        .fill(isSelected ? Color.modeCard : Color.clear)
                        .frame(width: 30, height: 30)
                        .shadow(color: isSelected ? .modeAccentStart.opacity(0.2) : .clear, radius: 3, x: 0, y: 1)
                    
                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                        .font(.system(size: 20))
                        .foregroundColor(isSelected ? .modeAccentStart : Color(.systemGray3))
                }
            }
        }
        .buttonStyle(ScaleButtonStyle())
        .contentShape(Rectangle())
        .padding(.vertical, 4)
    }
}

// 화면 모드 선택을 위한 행
struct ModeSelectionRow: View {
    let title: String
    let subtitle: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                InfoRow(title: title, subtitle: subtitle)
                Spacer()
                ZStack {
                    Circle()
                        .fill(isSelected ? Color.modeCard : Color.clear)
                        .frame(width: 30, height: 30)
                        .shadow(color: isSelected ? .modeAccentStart.opacity(0.2) : .clear, radius: 3, x: 0, y: 1)
                    
                    Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                        .font(.system(size: 20, weight: isSelected ? .medium : .regular))
                        .foregroundColor(isSelected ? .modeAccentStart : Color(.systemGray3))
                }
            }
            .padding(.vertical, 6)
        }
        .buttonStyle(ScaleButtonStyle())
        .contentShape(Rectangle())
    }
}


// MARK: - Preview (미리보기)

struct ModeSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ModeSettingsView()
        }
    }
}

