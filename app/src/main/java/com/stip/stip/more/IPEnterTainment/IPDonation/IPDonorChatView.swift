import SwiftUI

struct IPDonorChatView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var message = ""
    @State private var messages: [ChatMessage] = [
        ChatMessage(sender: "김지식", content: "안녕하세요! 저는 특허 2개를 기부했습니다.", time: "오후 3:15", isCurrentUser: false),
        
     
       
       
    ]
    
    // 기부 가능한 기관 목록 (예시)
    let donationOrganizations = [
        "대한의료봉사협회",
        "세계아동구호재단",
        "환경보호연합",
        "기술나눔센터",
        "지역사회발전재단"
    ]
    
    @State private var showingDonationSheet = false
    @State private var showingIPExchangeSheet = false
    
    var body: some View {
        VStack(spacing: 0) {
            // 헤더
            ZStack {
                // 배경
                Rectangle()
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: 1)
                
                // 헤더 콘텐츠
                HStack {
                    // 뒤로가기 버튼
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.deepNavy)
                            .padding()
                    }
                    
                    Spacer()
                    
                    // 메뉴 버튼
                    Menu {
                        Button(action: {
                            showingDonationSheet = true
                        }) {
                            Label("기부처 선정하기", systemImage: "heart")
                        }
                        
                        Button(action: {
                            showingIPExchangeSheet = true
                        }) {
                            Label("IP 거래소 상장", systemImage: "arrow.triangle.2.circlepath")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .foregroundColor(.deepNavy)
                            .padding()
                    }
                }
                
                // 센터 제목
                Text("IP 기부자 커뮤니티")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.deepNavy)
                    .frame(maxWidth: .infinity, alignment: .center)
            }
            .frame(height: 44)
            .background(Color.white)
            
            // 참여자 정보
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 20) {
                    DonorBadgeView(name: "홍길동", donationCount: 3, isCurrentUser: true)
                    DonorBadgeView(name: "김지식", donationCount: 2)
                    DonorBadgeView(name: "이기부", donationCount: 5)
                    DonorBadgeView(name: "박나눔", donationCount: 1)
                    DonorBadgeView(name: "최창의", donationCount: 4)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .background(Color.softGray.opacity(0.5))
            
            // 채팅 구분선
            HStack {
                Text("기부자 채팅")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.mediumGray)
                
                Spacer()
                
                Text("총 \(messages.count)개의 메시지")
                    .font(.system(size: 12))
                    .foregroundColor(.mediumGray)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(Color.white)
            
            // 채팅 메시지
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 16) {
                    ForEach(messages) { message in
                        ChatBubbleView(message: message)
                    }
                }
                .padding(16)
            }
            
            // 메시지 입력
            HStack(spacing: 12) {
                Button(action: {
                    // 파일 첨부 기능
                }) {
                    Image(systemName: "paperclip")
                        .font(.system(size: 20))
                        .foregroundColor(.mediumGray)
                }
                
                TextField("메시지를 입력하세요", text: $message)
                    .padding(10)
                    .background(Color.softGray)
                    .cornerRadius(20)
                
                Button(action: {
                    sendMessage()
                }) {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 20))
                        .foregroundColor(message.isEmpty ? .mediumGray : .mainAccentColor)
                }
                .disabled(message.isEmpty)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.white)
            .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: -1)
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showingDonationSheet) {
            DonationSelectionView(organizations: donationOrganizations)
        }
        .sheet(isPresented: $showingIPExchangeSheet) {
            IPExchangeView()
        }
    }
    
    private func sendMessage() {
        guard !message.isEmpty else { return }
        
        let newMessage = ChatMessage(
            sender: "홍길동",
            content: message,
            time: formatCurrentTime(),
            isCurrentUser: true
        )
        
        messages.append(newMessage)
        message = ""
    }
    
    private func formatCurrentTime() -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "a h:mm"
        return formatter.string(from: Date())
    }
}

// 기부자 뱃지 컴포넌트
struct DonorBadgeView: View {
    let name: String
    let donationCount: Int
    var isCurrentUser: Bool = false
    
    var body: some View {
        VStack {
            ZStack {
                Circle()
                    .fill(isCurrentUser ? Color.mainAccentColor : Color.deepNavy)
                    .frame(width: 50, height: 50)
                
                Text(String(name.prefix(1)))
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
            }
            
            Text(name)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(.darkGray)
            
            HStack(spacing: 2) {
                Image(systemName: "gift.fill")
                    .font(.system(size: 10))
                Text("\(donationCount)개")
                    .font(.system(size: 10))
            }
            .foregroundColor(.mediumGray)
        }
    }
}

// 채팅 메시지 모델
struct ChatMessage: Identifiable {
    let id = UUID()
    let sender: String
    let content: String
    let time: String
    let isCurrentUser: Bool
}

// 채팅 버블 컴포넌트
struct ChatBubbleView: View {
    let message: ChatMessage
    
    var body: some View {
        HStack {
            if message.isCurrentUser {
                Spacer()
            }
            
            VStack(alignment: message.isCurrentUser ? .trailing : .leading, spacing: 4) {
                if !message.isCurrentUser {
                    Text(message.sender)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.mediumGray)
                }
                
                HStack(alignment: .bottom, spacing: 4) {
                    if message.isCurrentUser {
                        Text(message.time)
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                    }
                    
                    Text(message.content)
                        .font(.system(size: 15))
                        .foregroundColor(message.isCurrentUser ? .white : .darkGray)
                        .padding(.vertical, 10)
                        .padding(.horizontal, 12)
                        .background(message.isCurrentUser ? Color.mainAccentColor : Color.white)
                        .cornerRadius(16)
                        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
                    
                    if !message.isCurrentUser {
                        Text(message.time)
                            .font(.system(size: 10))
                            .foregroundColor(.mediumGray)
                    }
                }
            }
            
            if !message.isCurrentUser {
                Spacer()
            }
        }
    }
}

// 기부처 선정 뷰
struct DonationSelectionView: View {
    @Environment(\.presentationMode) var presentationMode
    let organizations: [String]
    @State private var selectedOrganization: String?
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Text("IP 기부금 전달처를 선정해주세요")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.deepNavy)
                    .padding(.top, 20)
                
                Text("IP 기부자들의 의견을 종합하여 기부처를 결정합니다.\n여러분의 선택이 중요합니다.")
                    .font(.system(size: 14))
                    .foregroundColor(.mediumGray)
                    .multilineTextAlignment(.center)
                
                List {
                    ForEach(organizations, id: \.self) { org in
                        OrganizationRow(
                            name: org,
                            isSelected: selectedOrganization == org
                        )
                        .contentShape(Rectangle())
                        .onTapGesture {
                            selectedOrganization = org
                        }
                    }
                }
                .listStyle(PlainListStyle())
                
                Spacer()
                
                Button(action: {
                    // 기부처 선정 저장
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("선택 완료")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(height: 50)
                        .frame(maxWidth: .infinity)
                        .background(selectedOrganization != nil ? Color.mainAccentColor : Color.mediumGray)
                        .cornerRadius(12)
                        .padding(.horizontal, 16)
                }
                .disabled(selectedOrganization == nil)
                .padding(.bottom, 16)
            }
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

// 기부처 행 컴포넌트
struct OrganizationRow: View {
    let name: String
    let isSelected: Bool
    
    var body: some View {
        HStack {
            Image(systemName: "building.2")
                .foregroundColor(.mainAccentColor)
                .padding(.trailing, 8)
            
            Text(name)
                .font(.system(size: 16))
                .foregroundColor(.darkGray)
            
            Spacer()
            
            if isSelected {
                Image(systemName: "checkmark")
                    .foregroundColor(.mainAccentColor)
            }
        }
        .padding(.vertical, 8)
    }
}

// IP 거래소 상장 뷰
struct IPExchangeView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var selectedIPs: [String: Bool] = [
        "의료기기 특허 #23541": false,
        "모바일 앱 UI 특허 #78932": false,
        "헬스케어 알고리즘 특허 #45632": false,
        "웨어러블 디자인 특허 #12890": false,
        "데이터 처리 특허 #56723": false
    ]
    
    var selectedCount: Int {
        selectedIPs.filter { $0.value }.count
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                // 헤더 정보
                VStack(spacing: 8) {
                    Text("IP 거래소 상장")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.deepNavy)
                    
                    Text("기부한 IP를 묶어 STIP 거래소에 상장하여 거래할 수 있습니다.")
                        .font(.system(size: 14))
                        .foregroundColor(.mediumGray)
                        .multilineTextAlignment(.center)
                }
                .padding(.top, 20)
                
                // 상태 정보
                HStack(spacing: 20) {
                    InfoCardView(title: "선택된 IP", value: "\(selectedCount)개", icon: "checkmark.circle.fill")
                    InfoCardView(title: "필요한 IP", value: "최소 50개", icon: "arrow.up.right.circle.fill")
                }
                .padding(.horizontal, 16)
                
                // IP 선택 리스트
                List {
                    Section(header: Text("참여 가능한 나의 IP 목록").font(.system(size: 14, weight: .medium))) {
                        ForEach(Array(selectedIPs.keys.sorted()), id: \.self) { ipName in
                            IPSelectionRow(
                                name: ipName,
                                isSelected: selectedIPs[ipName] ?? false
                            ) {
                                selectedIPs[ipName]?.toggle()
                            }
                        }
                    }
                }
                .listStyle(PlainListStyle())
                
                // 하단 버튼
                VStack(spacing: 12) {
                    Button(action: {
                        // IP 상장 프로세스
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("IP 상장 신청하기")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(height: 50)
                            .frame(maxWidth: .infinity)
                            .background(selectedCount > 0 ? Color.mainAccentColor : Color.mediumGray)
                            .cornerRadius(12)
                    }
                    .disabled(selectedCount == 0)
                    
                    Text("※ 최종 상장은 STIP 심사 후 결정됩니다.")
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 16)
            }
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
    
    // 정보 카드 컴포넌트
    struct InfoCardView: View {
        let title: String
        let value: String
        let icon: String
        
        var body: some View {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(.mainAccentColor)
                    .font(.title3)
                
                VStack(alignment: .leading) {
                    Text(title)
                        .font(.system(size: 12))
                        .foregroundColor(.mediumGray)
                    
                    Text(value)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.darkGray)
                }
            }
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color.white)
            .cornerRadius(12)
            .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
        }
    }
    
    // IP 선택 행 컴포넌트
    struct IPSelectionRow: View {
        let name: String
        let isSelected: Bool
        let action: () -> Void
        
        var body: some View {
            Button(action: action) {
                HStack {
                    Image(systemName: "doc.text")
                        .foregroundColor(.mainAccentColor)
                    
                    Text(name)
                        .font(.system(size: 15))
                        .foregroundColor(.darkGray)
                    
                    Spacer()
                    
                    Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                        .foregroundColor(isSelected ? .mainAccentColor : .mediumGray)
                }
                .contentShape(Rectangle())
                .padding(.vertical, 4)
            }
            .buttonStyle(PlainButtonStyle())
        }
    }
}

struct IPDonorChatView_Previews: PreviewProvider {
    static var previews: some View {
        IPDonorChatView()
    }
}
