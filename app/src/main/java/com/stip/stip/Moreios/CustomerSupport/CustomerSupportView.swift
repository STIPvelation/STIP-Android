import SwiftUI

struct CustomerSupportView: View {
    // MARK: - 상태 변수
    @State private var selectedFAQIndex: Int? = nil
    @State private var showContactSheet = false
    
    // 전화번호 상수 - static으로 변경하여 property initializer 오류 해결
    private static let phoneNumber = "02-2238-4345"
    
    // MARK: - 데이터
    private let faqItems = [
        FAQItem(question: "STIP은 어떤 서비스인가요?",
                answer: "STIP(Share The Intellectual Property)은 특허, 상표, 드라마, 영화, 음악 등 다양한 지식재산(IP)을 누구나 쉽게 거래할 수 있는 혁신적인 플랫폼입니다. 창작자와 기업은 IP 소유권을 이전하지 않고 '통상실시권'을 통해 자금을 조달할 수 있으며, 투자자는 유망한 IP에 조각 투자하여 수익을 공유할 수 있습니다."),

        FAQItem(question: "PIN 비밀번호를 잊어버렸어요.",
                answer: "로그인 화면의 'PIN번호 를 잊으셨나요?'를 통해 휴대폰 인증후 PIN번호를 재설정할 수 있습니다."),


        FAQItem(question: "입출금 관련.",
                    answer: "입출금 신청 시 등록된 계좌로 실시간 입출금됩니다. 다만, 이용하시는 은행의 점검 시간(보통 23:30 ~ 00:30)이나 시스템 점검 시에는 점검이 끝난 후 순차적으로 입금 처리됩니다."),

    ]
    
    // 문자열 연결 분리하여 컴파일러 부하 감소
    private var contactMethods: [ContactMethod] {
        // 전화번호를 이제 설명에서 제외하고 다이얼로그에서만 표시
        return [
            ContactMethod(title: "전화 상담", description: "평일 09:00 - 18:00", icon: "phone", action: .call),
            ContactMethod(title: "상담 문의", description: "24시간 접수 가능", icon: "envelope", action: .email),
            ContactMethod(title: "카카오톡 상담", description: "평일 08:00 - 24:00", icon: "message", action: .chat),
            ContactMethod(title: "사칭사기제보", description: "상장을 전제로 대가를 요구받은경우", icon: "exclamationmark.triangle", action: .fraudReport),
            ContactMethod(title: "불공정 거래", description: "불공정 거래 신고", icon: "hand.raised", action: .unfairTrading),
            ContactMethod(title: "거래내역서 발급", description: "DIP거래증명서,DIP거래내역서 발급 신청", icon: "doc.text", action: .transactionHistory)
        ]
    }
    
    // MARK: - 뷰 본문
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // 헤더 섹션
                VStack(spacing: 12) {
                    Image(systemName: "bubble.left.and.bubble.right.fill")
                        .font(.system(size: 40))
                        .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                        .padding(.bottom, 8)
                    
                    Text("무엇을 도와드릴까요?")
                        .font(.system(size: 22, weight: .bold))
                    
                    Text("질문을 확인하거나 고객센터로 문의해주세요.")
                        .font(.system(size: 16))
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                }
                .padding(.vertical, 20)
                
                // 자주 묻는 질문 섹션
                VStack(alignment: .leading, spacing: 16) {
                    Text("자주 묻는 질문")
                        .font(.system(size: 18, weight: .bold))
                        .padding(.horizontal)
                    
                    ForEach(Array(faqItems.enumerated()), id: \.element.id) { index, item in
                        VStack(spacing: 0) {
                            Button(action: {
                                withAnimation {
                                    if selectedFAQIndex == index {
                                        selectedFAQIndex = nil
                                    } else {
                                        selectedFAQIndex = index
                                    }
                                }
                            }) {
                                HStack {
                                    Text(item.question)
                                        .font(.system(size: 16))
                                        .foregroundColor(.primary)
                                        .multilineTextAlignment(.leading)
                                    
                                    Spacer()
                                    
                                    Image(systemName: selectedFAQIndex == index ? "chevron.up" : "chevron.down")
                                        .foregroundColor(.gray)
                                }
                                .padding()
                                .background(Color.white)
                            }
                            
                            if selectedFAQIndex == index {
                                Text(item.answer)
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                                    .padding()
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color(UIColor.systemGray6))
                            }
                        }
                        .background(Color.white)
                        .cornerRadius(8)
                        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
                    }
                }
                .padding(.horizontal)
                
                Divider()
                    .padding(.vertical)
                
                // 문의하기 섹션
                VStack(alignment: .center, spacing: 16) {
                    Text("문의하기")
                        .font(.system(size: 18, weight: .bold))
                        .frame(maxWidth: .infinity, alignment: .center)
                    
                    Button(action: {
                        showContactSheet = true
                    }) {
                        HStack {
                            Text("고객센터에 문의하기")
                                .font(.headline)
                            
                            Spacer()
                            
                            Image(systemName: "arrow.right")
                        }
                        .foregroundColor(.white)
                        .padding()
                        .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                        .cornerRadius(8)
                    }
                    .padding(.horizontal)
                }
                
                // 운영시간 안내
                VStack(spacing: 8) {
                    Text("운영시간")
                        .font(.system(size: 16, weight: .semibold))
                    
                    Text("평일 09:00 - 18:00 (공휴일 제외)")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                    
                  
                }
                .padding(.top, 30)
                .padding(.bottom, 50)
            }
        }
        .navigationTitle("고객센터")
        .navigationBarTitleDisplayMode(.inline)
        .background(Color(UIColor.systemBackground).edgesIgnoringSafeArea(.all))
        .sheet(isPresented: $showContactSheet) {
            ContactSheetView(contactMethods: contactMethods)
        }
    }
}

// MARK: - 보조 뷰
struct ContactSheetView: View {
    // 전화번호 상수 - CustomerSupportView와 공유
    private static let phoneNumber = "02-2238-4345"
    
    // 알림 창 상태 관리
    @State private var showCallConfirmation = false
    @State private var showPreparingAlert = false
    
    let contactMethods: [ContactMethod]
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            List {
                ForEach(contactMethods) { method in
                    // 첫 번째 항목(전화 상담)의 상단 구분선 제거
                    let isFirstItem = method.id == contactMethods.first?.id
                    Button(action: {
                        handleContactMethod(method)
                        // 일부 연락 방법은 시트를 닫지 않을 수 있음
                        if method.action != .call {
                            self.presentationMode.wrappedValue.dismiss()
                        }
                    }) {
                        HStack(spacing: 16) {
                            Image(systemName: method.icon)
                                .font(.system(size: 24))
                                .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                                .frame(width: 40, height: 40)
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(method.title)
                                    .font(.system(size: 16, weight: .medium))
                                
                                Text(method.description)
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                            }
                            
                            Spacer()
                            
                            Image(systemName: "chevron.right")
                                .foregroundColor(.gray)
                        }
                        .padding(.vertical, 8)
                    }
                    .listRowSeparator(.hidden, edges: isFirstItem ? .top : [])
                }
            }
            .listStyle(PlainListStyle())
            .navigationBarTitleDisplayMode(.inline)
            .listSectionSeparator(.hidden)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text("종합 문의")
                        .font(.system(size: 16, weight: .medium))
                }
            }
            .navigationBarItems(trailing: Button("닫기") {
                self.presentationMode.wrappedValue.dismiss()
            })
            // 전화 확인 다이얼로그
            .alert("고객센터에 전화하기", isPresented: $showCallConfirmation) {
                Button("취소", role: .cancel) {}
                Button("전화하기") {
                    confirmCall()
                }
            } message: {
                Text("02-2238-4345 번호로 연결합니다.")
            }
            // 준비중 알림 다이얼로그
            .alert("준비중", isPresented: $showPreparingAlert) {
                Button("확인", role: .cancel) {
                    self.presentationMode.wrappedValue.dismiss()
                }
            } message: {
                Text("준비중")
            }
        }
    }
    
    // 연락 방법 처리
    private func handleContactMethod(_ method: ContactMethod) {
        switch method.action {
        case .call:
            // 전화 골기 전에 확인 다이얼로그 표시
            showCallConfirmation = true
            // 실제 전화 걸기는 confirmCall() 함수에서 진행
        case .email:
            // 홈페이지 상담 페이지 연결
            if let url = URL(string: "https://stipvelation.com/contact-contact.html") {
                UIApplication.shared.open(url)
            }
        case .chat:
            // 카카오톡 채팅 상담 페이지로 연결
            if let url = URL(string: "http://pf.kakao.com/_ahaGn") {
                UIApplication.shared.open(url)
            }
        case .fraudReport:
            // 사칭사기제보 페이지로 연결 (현재는 기본 상담 페이지로 연결)
            if let url = URL(string: "https://stipvelation.com/contact-contact.html") {
                UIApplication.shared.open(url)
            }
        case .unfairTrading:
            // 불공정 거래 신고 페이지로 연결 (현재는 기본 상담 페이지로 연결)
            if let url = URL(string: "https://stipvelation.com/contact-contact.html") {
                UIApplication.shared.open(url)
            }
        case .transactionHistory:
            // 거래내역서 발급 - 준비중 알림 표시
            showPreparingAlert = true
        }
    }
    
    // 확인 다이얼로그에서 '전화하기' 버튼을 누르면 실행되는 함수
    private func confirmCall() {
        let phoneNumber = "02-2238-4345"
        let cleanPhoneNumber = phoneNumber.replacingOccurrences(of: "-", with: "")
        if let phoneURL = URL(string: "tel://" + cleanPhoneNumber) {
            UIApplication.shared.open(phoneURL)
        }
    }
}

// MARK: - 데이터 모델
struct FAQItem: Identifiable {
    let id = UUID()
    let question: String
    let answer: String
}

struct ContactMethod: Identifiable {
    enum ActionType {
        case call
        case email
        case chat
        case fraudReport
        case unfairTrading
        case transactionHistory
    }
    
    let id = UUID()
    let title: String
    let description: String
    let icon: String
    let action: ActionType
}

// MARK: - 미리보기
struct CustomerSupportView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            CustomerSupportView()
        }
    }
}
