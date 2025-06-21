// PolicyListView.swift

import SwiftUI

struct PolicyListView: View {
    @State private var isLoaded = false
    
    // 데이터 구조는 그대로 유지합니다.
    private let policySections: [(title: String, items: [(title: String, icon: String)])] = [
        ("서비스 이용", [
            ("통신사 이용약관", "network"),
            ("거래통화 및 환전 방식 안내", "arrow.left.arrow.right.circle"),
            ("투자책임 및 위험 고지", "exclamationmark.shield"),
            ("거래소 이용약관 및 이용안내", "building.columns")
        ]),
        ("개인정보", [
            ("개인정보 처리방침", "lock.shield"),
            ("인증시 고유식별정보처리동의", "person.badge.key"),
            ("인증시 개인정보 수집동의", "person.text.rectangle"),
            ("개인정보 수집 및 이용동의", "hand.raised")
        ]),
        // ✅ 요청하신 내용을 새로운 섹션으로 추가했습니다.
        ("주요 정책 및 기준", [
            ("스타트업 및 중소기업 지원 정책", "lightbulb.fill"),
            ("자금세탁방지(AML) 및 내부통제 정책", "lock.shield.fill"),
            ("DIP 가치평가 기준 정책", "doc.text.magnifyingglass"),
            ("수수료 투명성 정책", "eye.circle.fill")
        ]),
        ("기타 정책", [
            ("입출금 자동이체 서비스", "arrow.up.arrow.down.circle"),
            ("소비자보호 정책", "figure.child.circle"),
            ("개인정보 수집 동의", "checkmark.shield"),
            ("ESG 정책", "leaf")
        ]),
        // ✅ 새로운 정책 섹션 추가
        ("지원 규정", [
            ("스타트업 및 중소기업 지원 정책", "lightbulb.fill"),
            ("자금세탁방지 및 내부통제 정책", "lock.shield.fill"),
            ("DIP 가치평가 기준 정책", "doc.text.magnifyingglass"),
            ("수수료 투명성 정책", "eye.circle.fill")
        ])
    ]
    
    private let gradients: [LinearGradient] = [
        LinearGradient(gradient: Gradient(colors: [Color.blue.opacity(0.7), Color.purple.opacity(0.7)]), startPoint: .topLeading, endPoint: .bottomTrailing),
        LinearGradient(gradient: Gradient(colors: [Color.green.opacity(0.7), Color.blue.opacity(0.7)]), startPoint: .topLeading, endPoint: .bottomTrailing),
        LinearGradient(gradient: Gradient(colors: [Color.teal.opacity(0.7), Color.cyan.opacity(0.6)]), startPoint: .topLeading, endPoint: .bottomTrailing),
        LinearGradient(gradient: Gradient(colors: [Color.orange.opacity(0.7), Color.red.opacity(0.6)]), startPoint: .topLeading, endPoint: .bottomTrailing),
        // ✅ 새로운 섹션에 사용할 그라데이션을 추가했습니다.
        LinearGradient(gradient: Gradient(colors: [Color.indigo.opacity(0.7), Color.purple.opacity(0.6)]), startPoint: .topLeading, endPoint: .bottomTrailing)
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                ForEach(Array(policySections.enumerated()), id: \.offset) { sectionIndex, section in
                    VStack(alignment: .leading, spacing: 12) {
                        Text(section.title)
                            .font(.headline)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 20)
                            .opacity(isLoaded ? 1 : 0)
                            .offset(y: isLoaded ? 0 : 20)
                            .animation(.easeOut(duration: 0.5).delay(Double(sectionIndex) * 0.1), value: isLoaded)
                        
                        VStack(spacing: 2) {
                            ForEach(Array(section.items.enumerated()), id: \.offset) { itemIndex, item in
                                // PolicyCard가 이제 직접 NavigationLink 역할을 합니다.
                                PolicyCard(
                                    title: item.title,
                                    icon: item.icon,
                                    gradient: gradients[sectionIndex]
                                )
                                .padding(.horizontal)
                                .opacity(isLoaded ? 1 : 0)
                                .offset(y: isLoaded ? 0 : 30)
                                .animation(.easeOut(duration: 0.5).delay(0.2 + Double(sectionIndex * 4 + itemIndex) * 0.05), value: isLoaded)
                            }
                        }
                    }
                }
                VStack(alignment: .center) {
                    Text("STIPvelation v1.0.0")
                        .font(.caption)
                        .foregroundColor(.secondary.opacity(0.7))
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 30)
                .padding(.bottom, 20)
                .opacity(isLoaded ? 1 : 0)
                .animation(.easeOut(duration: 0.5).delay(0.8), value: isLoaded)
            }
            .padding(.top, 16)
            .padding(.bottom, 20)
        }
        .background(Color(UIColor.systemGroupedBackground).edgesIgnoringSafeArea(.all))
        .navigationTitle("정책 및 약관")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if !isLoaded {
                withAnimation { isLoaded = true }
            }
        }
    }
}

// ✅ PolicyCard를 NavigationLink로 직접 구현하여 단순화
struct PolicyCard: View {
    let title: String
    let icon: String
    let gradient: LinearGradient
    
    var body: some View {
        // 카드 자체가 목적지를 가진 내비게이션 링크가 됩니다.
        NavigationLink(destination: PolicyDetailView(policyTitle: title)) {
            HStack(spacing: 16) {
                ZStack {
                    Circle().fill(gradient).frame(width: 40, height: 40).shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 3)
                    Image(systemName: icon).font(.subheadline).foregroundColor(.white)
                }
                VStack(alignment: .leading, spacing: 3) {
                    Text(title).font(.subheadline).fontWeight(.medium).foregroundColor(.primary)
                    Text("해당 정책에 대한 자세한 내용").font(.caption).foregroundColor(.secondary).lineLimit(1)
                }
                Spacer()
                Image(systemName: "chevron.right").font(.footnote).fontWeight(.medium).foregroundColor(.secondary.opacity(0.5))
            }
            .padding(.horizontal, 16).padding(.vertical, 14)
            .background(Color(.systemBackground)) // 배경색을 고정
            .cornerRadius(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.gray.opacity(0.15), lineWidth: 1))
            .shadow(color: .black.opacity(0.04), radius: 4, x: 0, y: 2)
        }
        // 링크 전체가 눌리도록 하고, 파란색으로 변하는 기본 스타일을 제거합니다.
        .buttonStyle(PlainButtonStyle())
    }
}


// ✅ 미리보기 코드에서도 NavigationStack은 필수입니다.
struct PolicyListView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationStack {
            PolicyListView()
        }
    }
}
