//
//  IPDonationView.swift
//
//  Generated on 2025. 6. 11.
//  A modern and sophisticated UI for an IP Donation service.
//  (Terms & Conditions updated)
//

import SwiftUI

// MARK: - 1. Design System: Colors
// Color(hex:) 초기화는 ColorExtension.swift에 구현되어 있습니다.
extension Color {
    static let deepNavy = Color(hex: "#1C2A4E")
    static let softGray = Color(hex: "#F4F6F8")
    static let mainAccentColor = Color(hex: "#30C6E8")
    static let darkGray = Color(hex: "#333333")
    static let mediumGray = Color(hex: "#888888")
}


// MARK: - 2. Main View: IPDonationView
struct IPDonationView: View {
    @State private var isShowingTermsSheet = false
    
    var body: some View {
        // <<< NavigationView를 여기서 제거했습니다.
        VStack(spacing: 0) {
            
            // 프로세스 인디케이터
            HStack {
                Spacer()
                ProcessIndicatorView()
                Spacer()
            }
            .padding(.vertical, 8)
            .background(Color.white)
            
            ZStack {
                Color.softGray.ignoresSafeArea()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                            
                        Text("잠들어 있는 당신의 IP,\n새로운 가치를 깨울 시간")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.deepNavy)
                            .lineSpacing(6)
                            
                        FileUploadView()
                            
                        InfoCardView(
                            title: "기부자 정보",
                            details: [
                                ("연락처", "010-1234-5678"),
                                ("소유권자", "홍길동")
                            ],
                            footnote: "본인 회원가입시 정보가 자동 표시됩니다."
                        )
                            
                        DocumentLinkView()
                            
                        Spacer(minLength: 30)
                            
                        Button(action: {
                            isShowingTermsSheet = true
                        }) {
                            Text("약관 동의하고 기부 진행하기")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .frame(height: 56)
                                .frame(maxWidth: .infinity)
                                .background(Color.mainAccentColor)
                                .cornerRadius(14)
                        }
                            
                    }
                    .padding(20)
                }
            }
        }
        .navigationTitle("IP 기부") // 이 수식어들은 부모의 NavigationView에 적용됩니다.
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $isShowingTermsSheet) {
            TermsAndConditionsView()
        }
        .accentColor(.deepNavy)
        // <<< NavigationView 닫는 괄호도 여기서 제거했습니다.
    }
}


// MARK: - 3. Modal View: TermsAndConditionsView
// 실제 약관 내용을 4개 조항으로 나누어 업데이트했습니다.
struct TermsAndConditionsView: View {
    @Environment(\.presentationMode) var presentationMode
    
    // 각 약관의 동의 상태 (4개로 수정)
    @State private var agreesToTerm1 = false
    @State private var agreesToTerm2 = false
    @State private var agreesToTerm3 = false
    @State private var agreesToTerm4 = false
    
    // 전체 동의 상태 계산 (4개 조항 모두 확인)
    private var allAgreed: Bool {
        agreesToTerm1 && agreesToTerm2 && agreesToTerm3 && agreesToTerm4
    }

    // `전체 동의` 토글을 위한 바인딩
    private var allAgreedBinding: Binding<Bool> {
        Binding(
            get: { self.allAgreed },
            set: {
                self.agreesToTerm1 = $0
                self.agreesToTerm2 = $0
                self.agreesToTerm3 = $0
                self.agreesToTerm4 = $0
            }
        )
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                Text("서비스 이용을 위해\n아래 필수 약관에 동의해주세요.")
                    .font(.system(size: 22, weight: .bold))
                    .multilineTextAlignment(.center)
                    .lineSpacing(5)
                    .padding(.vertical, 30)

                Toggle(isOn: allAgreedBinding) {
                    Text("전체 약관에 동의합니다.")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.deepNavy)
                }
                .toggleStyle(CheckboxToggleStyle())
                .padding()
                .background(Color.softGray)
                .cornerRadius(12)
                
                Divider().padding(.vertical)

                // 약관 목록
                ScrollView {
                    VStack(spacing: 16) {
                        // --- 약관 내용 업데이트 ---
                        TermItemView(isAgreed: $agreesToTerm1,
                                     title: "제 1조: IP 관리 및 라이선싱 권한 위임",
                                     summary: "기부하는 IP의 사용 및 라이선싱 권한을 STIP에 위임하여, STIP가 이를 활용해 수익을 창출하고 관리하는 것에 동의합니다. IP의 소유권은 기부자에게 유지됩니다.",
                                     fullText: """
                                     본인은 본 IP에 대한 소유권을 계속 보유합니다.
                                     본인은 “IP 기부하기 신청” 버튼을 클릭함으로써, 본 IP의 법적 보호 잔존 기간 동안 다음 각 호의 권한 전부를 STIP에게 포괄적으로 위임합니다.
                                     가. 본 IP를 STIP의 판단과 전략에 따라 다른 지식재산권과 결합하거나 단독으로 상품 또는 서비스로 개발·가공하고, 이를 STIP이 운영하는 플랫폼이나 기타 적절한 채널을 통해 제공(예: 포트폴리오에 등재, 이하 “상업화”)하는 권한.
                                     나. 제3자(개인, 기업, 기관 등 국내외 모든 사용자)에게 본 IP에 대한 비독점적 사용권(통상실시권)을 STIP이 합리적으로 정하는 조건(사용료율, 사용방식, 사용 기간, 허용 범위, 조건 등 포함)에 따라 허락하고, 이와 관련된 계약을 체결, EULA, 갱신 또는 해지할 수 있는 일체의 권한.
                                     다. 상기 라이선싱 활동 및 IP 포트폴리오 관리에 필요한 모든 관리 업무, 마케팅 및 홍보 활동, 기술 및 시장 동향 분석, 침해자 사용자에게 법상, 권리상 관련 법적 조치(단, 소송 등 중대한 법적 분쟁 발생 시에는 기부자와 협의)를 취할 수 있는 권한.
                                     """)
                        
                        TermItemView(isAgreed: $agreesToTerm2,
                                     title: "제 2조: STIP의 역할 및 준수 사항",
                                     summary: "STIP는 위임받은 IP의 가치를 높이기 위해 최선을 다하며, 관리/마케팅/법률 자문 등 모든 과정을 공정하고 투명하게 처리할 의무를 가집니다.",
                                     fullText: """
                                     STIP은 위임받은 권한에 따라 본 IP를 포함한 포트폴리오의 가치를 제고하고 공익적 확산을 위해 최선을 다해 아래와 같이 라이선싱 활동을 수행합니다.
                                     - 공익적 라이선싱 활동을 통해 발생한 모든 순수익(사용료, 로열티) 기부 등 일체의 경제적 가치가 먼저 STIP의 운영 경비(IP 및 관련 포트폴리오의 관리, 마케팅, 라이선싱 활동, 기부금 관리, 법률 자문 등에 직접적이고 합리적으로 소요되는 비용)에 우선 충당됩니다.
                                     - 위 운영경비를 공제한 후 발생하는 순수익금 전액은 사회 공헌 목적으로 기부됩니다.
                                     """)
                        
                        TermItemView(isAgreed: $agreesToTerm3,
                                     title: "제 3조: 기부금 용도 및 기부처 선정",
                                     summary: "IP 활용으로 발생한 수익금은 기부자님의 의견을 수렴하여 투명한 절차에 따라 사회 공헌을 위해 사용됩니다.",
                                     fullText: """
                                     제 1항 및 2항에 따라 발생한 기부하는 순수익금의 구체적인 기부 용도 및 기부처는, 위임된 라이선싱 권한을 기반으로 기부해주신 핵심 기부자님들의 의견을 종합적으로 수렴하고, STIP 내부의 투명한 심의 절차 및 논의를 거쳐 공정하게 결정할 것입니다.
                                     """)
                        
                        TermItemView(isAgreed: $agreesToTerm4,
                                     title: "제 4조: 권리 상실 또는 제한",
                                     summary: "기부된 IP의 권리가 제3자에게 이전되거나 소멸될 경우, 본 계약은 자동으로 종료되며 STIP에 대한 권리 위임도 해지됩니다.",
                                     fullText: """
                                     본인은 상기 또는 향후 특허 본 IP의 소유권이 이전되지 않으며, 본 IP를 상용화하고 이에 대한 통상실시권을 제3자에게 허여하며 이를 관리하는 모든 실질적인 권한을 STIP에게 위임하고, 그 결과 발생하는 순수익금이 STIP의 운영비를 제외하고 사회에 기부된다는 점을 충분히 숙지하고 명확히 이해하였으며, 자발적인 의사로 본 동의를 진행하고 “IP 기부하기를 신청” 버튼을 클릭합니다.
                                     """)
                        // --- 약관 내용 업데이트 끝 ---
                    }
                }
                
                Spacer()
                
                Button(action: {
                    print("기부 최종 완료!")
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("동의하고 기부 완료하기")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                        .frame(height: 56)
                        .frame(maxWidth: .infinity)
                        .background(allAgreed ? Color.mainAccentColor : Color.mediumGray.opacity(0.5))
                        .cornerRadius(14)
                }
                .disabled(!allAgreed)
                .animation(.easeInOut, value: allAgreed)

            }
            .padding(.horizontal, 20)
            .padding(.bottom)
            .navigationTitle("IP 기부 약관 동의")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .foregroundColor(.darkGray)
                    }
                }
            }
        }
    }
}


// MARK: - 4. Reusable Components & Styles
struct ProcessIndicatorView: View {
    var body: some View {
        HStack {
            ProcessStep(number: "1", text: "정보 입력", isActive: true)
            ProcessArrow()
            ProcessStep(number: "2", text: "약관 동의")
            ProcessArrow()
            ProcessStep(number: "3", text: "기부 완료")
        }
    }
    
    private struct ProcessStep: View {
        let number: String
        let text: String
        var isActive: Bool = false
        
        var body: some View {
            HStack(spacing: 4) {
                Text(number)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(isActive ? .white : .mainAccentColor)
                    .frame(width: 20, height: 20)
                    .background(isActive ? Color.mainAccentColor : Color.white)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.mainAccentColor, lineWidth: 1.5))
                
                Text(text)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(isActive ? .deepNavy : .mediumGray)
            }
        }
    }
    
    private struct ProcessArrow: View {
        var body: some View {
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.mediumGray.opacity(0.5))
                .padding(.horizontal, 4)
        }
    }
}

struct FileUploadView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "icloud.and.arrow.up")
                .font(.system(size: 40))
                .foregroundColor(.mainAccentColor)
            Text("IP 등록증을 업로드 해주세요")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(.darkGray)
            Text("파일을 드래그하거나 여기를 탭하세요\n(PDF, JPG, PNG / 최대 10MB)")
                .font(.system(size: 13))
                .foregroundColor(.mediumGray)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .background(Color.white)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(style: StrokeStyle(lineWidth: 2, dash: [8]))
                .foregroundColor(Color.mainAccentColor.opacity(0.7))
        )
    }
}

struct InfoCardView: View {
    let title: String
    let details: [(String, String)]
    let footnote: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.deepNavy)
            
            VStack(spacing: 12) {
                ForEach(details, id: \.0) { label, value in
                    HStack {
                        Text(label)
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(.mediumGray)
                        Spacer()
                        Text(value)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.darkGray)
                    }
                }
            }
            
            Divider().padding(.vertical, 8)
            
            Text(footnote)
                .font(.system(size: 12))
                .foregroundColor(.mediumGray)
        }
        .padding(20)
        .background(Color.white)
        .cornerRadius(16)
    }
}

// IPDonorChatView는 별도의 파일로 이동하였습니다.

struct DocumentLinkView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("필수 확인 서류")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.deepNavy)
            
            NavigationLink(destination: IPDonorChatView()) {
                DocumentRow(icon: "doc.text.magnifyingglass", title: "IP기부 활용 계획", subtitle: "기부된 IP의 가치 창출 및 기여 계획")
            }
            .buttonStyle(PlainButtonStyle())
            
            NavigationLink(destination: IPDonationAgreementView()) {
                DocumentRow(icon: "hand.raised.square.on.square", title: "IP 기부 동의서", subtitle: "IP 기부 및 권리 위임에 관한 동의")
            }
            .buttonStyle(PlainButtonStyle())

        }
    }
    
    private struct DocumentRow: View {
        let icon: String
        let title: String
        let subtitle: String
        
        var body: some View {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(.mainAccentColor)
                    .frame(width: 30)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.darkGray)
                    Text(subtitle)
                        .font(.system(size: 13))
                        .foregroundColor(.mediumGray)
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .foregroundColor(.mediumGray)
            }
            .padding(16)
            .background(Color.white)
            .cornerRadius(12)
        }
    }
}

struct TermItemView: View {
    @Binding var isAgreed: Bool
    let title: String
    let summary: String
    let fullText: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Toggle(isOn: $isAgreed) {
                Text(title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.darkGray)
            }
            .toggleStyle(CheckboxToggleStyle())
            
            DisclosureGroup {
                Text(fullText)
                    .font(.system(size: 14))
                    .foregroundColor(.mediumGray)
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .cornerRadius(8)
                    .padding(.top, 8)
            } label: {
                Text(summary)
                    .font(.system(size: 14, weight: .regular))
                    .foregroundColor(.darkGray)
                    .lineSpacing(4)
                    .padding(.leading, 34)
            }
            .accentColor(.mainAccentColor)
        }
        .padding()
        .background(Color.softGray)
        .cornerRadius(12)
    }
}

struct CheckboxToggleStyle: ToggleStyle {
    func makeBody(configuration: Configuration) -> some View {
        Button(action: {
            configuration.isOn.toggle()
        }) {
            HStack(spacing: 12) {
                Image(systemName: configuration.isOn ? "checkmark.square.fill" : "square")
                    .font(.title2)
                    .foregroundColor(configuration.isOn ? .mainAccentColor : .mediumGray)
                configuration.label
                Spacer()
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
}


// MARK: - 5. Preview
struct IPDonationView_Previews: PreviewProvider {
    static var previews: some View {
        IPDonationView()
    }
}


// MARK: - 6. Color Hex Initializer
// Color(hex:) 초기화는 파일 상단의 Color 확장에 이미 구현되어 있습니다.
// 구현이 없다는 가정하에 추가합니다.
/*
extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        _ = scanner.scanString("#")
        
        var rgb: UInt64 = 0
        scanner.scanHexInt64(&rgb)
        
        let r = Double((rgb >> 16) & 0xFF) / 255.0
        let g = Double((rgb >>  8) & 0xFF) / 255.0
        let b = Double((rgb >>  0) & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}
*/
