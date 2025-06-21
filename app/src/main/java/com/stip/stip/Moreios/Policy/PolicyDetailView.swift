// PolicyDetailView.swift

import SwiftUI

// MARK: - 정책 상세 페이지 (라우터 역할)
struct PolicyDetailView: View {
    let policyTitle: String
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        Group {
            if PolicyContentStore.shared.isMultiPartPolicy(title: policyTitle) {
                MultiPartPolicyDetailView(policyTitle: policyTitle)
            } else if let content = PolicyContentStore.shared.getSimplePolicyContent(for: policyTitle) {
                ZStack {
                    ScrollView {
                        VStack(spacing: 0) {
                            if content == "준비중" {
                                Text(content)
                                    .font(.title2)
                                    .foregroundColor(.gray)
                                    .padding(.horizontal, 28)
                                    .padding(.vertical, 16)
                                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                                    .padding(.top, 100)  // Push it down from the top for better centering
                            } else {
                                Text(content)
                                    .font(.body)
                                    .lineSpacing(4)
                                    .padding(.horizontal, 28)
                                    .padding(.vertical, 16)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            
                            Spacer(minLength: 60) // 버튼을 위한 공간
                        }
                    }
                    
                    // 확인 버튼
                    VStack {
                        Spacer()
                        Button(action: {
                            presentationMode.wrappedValue.dismiss()
                        }) {
                            Text("확인")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(Color(hex: "30C6E8"))
                                .cornerRadius(12)
                        }
                        .padding(.horizontal, 28)
                        .padding(.bottom, 16)
                    }
                }
            } else {
                Text("죄송합니다. 요청하신 약관 내용을 찾을 수 없습니다.")
                    .padding()
            }
        }
        .navigationTitle(policyTitle)
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - 복합 약관 전용 상세 페이지

struct MultiPartPolicyDetailView: View {
    let policyTitle: String
    private let policy: MultiPartPolicy?
    
    @Environment(\.presentationMode) var presentationMode
    @State private var currentPage = 0
    
    init(policyTitle: String) {
        self.policyTitle = policyTitle
        self.policy = PolicyContentStore.shared.getMultiPartPolicy(for: policyTitle)
    }

    var body: some View {
        if let policy = policy {
            VStack(spacing: 0) {
                // 커스텀 상단 페이지 인디케이터
                if policy.parts.count > 1 {
                    HStack(spacing: 8) {
                        ForEach(0..<policy.parts.count, id: \.self) { index in
                            Circle()
                                .fill(currentPage == index ? Color(hex: "30C6E8") : Color.gray.opacity(0.3))
                                .frame(width: 8, height: 8)
                                .animation(.easeInOut, value: currentPage)
                        }
                    }
                    .padding(.vertical, 12)
                }
                
                TabView(selection: $currentPage) {
                    ForEach(0..<policy.parts.count, id: \.self) { index in
                        let part = policy.parts[index]
                        ZStack {
                            ScrollView {
                                VStack(spacing: 0) {
                                    Text(part.content)
                                        .padding(.horizontal, 28)
                                        .padding(.vertical, 16)
                                        .font(.body)
                                        .lineSpacing(4)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                    
                                    Spacer(minLength: 60) // 버튼을 위한 공간
                                }
                            }
                            
                            // 확인 버튼
                            VStack {
                                Spacer()
                                Button(action: {
                                    presentationMode.wrappedValue.dismiss()
                                }) {
                                    Text("확인")
                                        .font(.headline)
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 16)
                                        .background(Color(hex: "30C6E8"))
                                        .cornerRadius(12)
                                }
                                .padding(.horizontal, 28)
                                .padding(.bottom, 16)
                            }
                        }
                        .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never)) // 기본 닫 인디케이터 숨김
            }
            .navigationTitle(policy.title)
        } else {
            Text("복합 약관 내용을 불러오는 데 실패했습니다.")
                .navigationTitle(policyTitle)
        }
    }
}
