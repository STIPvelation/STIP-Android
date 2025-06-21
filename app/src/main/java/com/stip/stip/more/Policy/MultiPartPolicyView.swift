import SwiftUI

/// 멀티파트 정책 내용을 표시하는 뷰
/// 예: 통신사 이용약관 - LG U+, SKT, KT 등 여러 통신사의 약관을 탭으로 표시
struct MultiPartPolicyView: View {
    let policyTitle: String
    @State private var selectedPartIndex = 0
    @Environment(\.presentationMode) var presentationMode
    
    private var multiPartPolicy: MultiPartPolicy? {
        return PolicyContentStore.shared.getMultiPartPolicy(for: policyTitle)
    }
    
    var body: some View {
        VStack(spacing: 0) {
            if let policy = multiPartPolicy, !policy.parts.isEmpty {
                // 커스텀 통신사 탭 선택기
                VStack(spacing: 0) {
                    HStack(spacing: 2) {
                        ForEach(0..<policy.parts.count, id: \.self) { index in
                            Button(action: {
                                withAnimation(.easeInOut(duration: 0.2)) {
                                    selectedPartIndex = index
                                }
                            }) {
                                Text(policy.parts[index].partTitle)
                                    .font(.subheadline.weight(selectedPartIndex == index ? .semibold : .regular))
                                    .frame(maxWidth: .infinity, minHeight: 44)
                                    .foregroundColor(selectedPartIndex == index ? .white : .gray)
                                    .background(selectedPartIndex == index ? Color(hex: "30C6E8") : Color.gray.opacity(0.1))
                                    .cornerRadius(8)
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 12)
                .padding(.bottom, 8)
                
                Divider()
                    .padding(.top, 12)
                
                // 선택된 파트의 내용
                ScrollView {
                    VStack(spacing: 0) {
                        Text(policy.parts[selectedPartIndex].content)
                            .font(.body)
                            .lineSpacing(4)
                            .padding(.horizontal, 28)  // 8씩 추가된 패딩
                            .padding(.vertical, 16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        
                        Spacer(minLength: 60) // 버튼을 위한 공간
                    }
                }
                
            } else {
                // 내용이 없을 경우 표시할 뷰
                VStack(spacing: 16) {
                    Image(systemName: "doc.text")
                        .font(.largeTitle)
                        .imageScale(.large)
                        .foregroundColor(.gray)
                    
                    Text("내용 준비중")
                        .font(.headline)
                    
                    Text("현재 해당 정책 내용을 준비중입니다.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                
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
                    .padding(.horizontal, 28) // 8씩 추가된 패딩
                    .padding(.bottom, 16)
                }
            }
        }
        .navigationTitle(policyTitle)
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        MultiPartPolicyView(policyTitle: "통신사 이용약관")
    }
}
