import SwiftUI

// MARK: - 디자인 시스템

private extension Color {
    // IP스왑 색상 팔레트
    static let ipSwapPrimary = Color(red: 57/255, green: 169/255, blue: 255/255)
    static let ipSwapSecondary = Color(red: 44/255, green: 93/255, blue: 220/255)
    static let ipSwapBackground = Color(UIColor.systemBackground)
    static let ipSwapCardBg = Color(UIColor.secondarySystemBackground)
    static let ipSwapTextPrimary = Color(UIColor.label)
    static let ipSwapTextSecondary = Color(UIColor.secondaryLabel)
    static let ipSwapBorder = Color(UIColor.separator).opacity(0.5)
}

// MARK: - 데이터 모델 정의
struct IPSwapItem: Identifiable {
    let id = UUID()
    let ipNumber: String
    let description: String
    let remainingPeriod: String
    let swapTarget: String
    let imageName: String
    let swapMethod: String
    
    // 상세 정보에만 필요한 데이터
    let registrationAuthority: String
    let personInCharge: String
    let ownerContact: String
    let detailedDescription: String
}

// MARK: - 메인 뷰 (✅ 이름이 IPSwapDetailView로 변경됨)
// 이 뷰가 앱의 시작점이 됩니다.
struct IPSwapDetailView: View {
    
    // 샘플 데이터 생성
    let sampleItems: [IPSwapItem] = [
        .init(ipNumber: "US-01-14567755", description: "전도체 전기 자동차 충전 방식관련", remainingPeriod: "7년", swapTarget: "자동 충전 방식관련 IP", imageName: "img-ev", swapMethod: "기술 제휴 + 매출에 따른 수익 배분 혹은 지정 개런티", registrationAuthority: "미국 특허청", personInCharge: "변리사 회사 이름", ownerContact: "support@tesla.com", detailedDescription: "전도체 전기 자동차 충전 방식관련에 있어서 현재 시장에서 상용화중인 방식으로 원하는 방식에 맞추어 IP를 활용한 개발 계획서 준비완료 및 제공 기간을 정해서 기술적인 제공도 가능"),
        .init(ipNumber: "CN-15-12345755", description: "휴머노이드 전체 개발 특허", remainingPeriod: "5년", swapTarget: "휴머노이드 AI관리 특허", imageName: "img-robot", swapMethod: "기술 스왑", registrationAuthority: "중국 특허청", personInCharge: "담당자 이름", ownerContact: "contact@boston-dynamics.com", detailedDescription: "휴머노이드 로봇의 전체 개발과 관련된 포괄적인 특허입니다."),
        .init(ipNumber: "JP-18-12345757", description: "모니터 O-LED 독점 특허", remainingPeriod: "3년", swapTarget: "신재생 에너지 관련 특허", imageName: "img-monitor", swapMethod: "기술 스왑", registrationAuthority: "일본 특허청", personInCharge: "담당자 이름", ownerContact: "display@lg.com", detailedDescription: "차세대 모니터 O-LED 패널 관련 독점 특허 기술."),
        .init(ipNumber: "US-2357-4566", description: "스타벅스 아프리카 대륙 독점권", remainingPeriod: "20년", swapTarget: "맥도날드 중동 독점권", imageName: "img-starbucks", swapMethod: "매장 교환 및 매출 쉐어", registrationAuthority: "미국 상표청", personInCharge: "변리사 회사 이름", ownerContact: "legal@starbucks.com", detailedDescription: "아프리카 대륙 전체에 대한 스타벅스 브랜드 독점 운영 권리.")
    ]
    
    var body: some View {
        IPSwapListView(items: sampleItems)
            .accentColor(.black)
    }
}

// MARK: - 1. IP 스왑 목록 화면
struct IPSwapListView: View {
    let items: [IPSwapItem]
    @State private var selectedFilter: String = "ALL IP"
    @State private var searchText: String = ""
    @State private var isSearchFocused: Bool = false
    
    var body: some View {
        ZStack(alignment: .bottom) {
            // 배경 색상
            Color.ipSwapBackground.edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 0) {
                // 검색창
                HStack(spacing: 12) {
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(isSearchFocused ? .ipSwapPrimary : .ipSwapTextSecondary)
                            .font(.system(size: 16))
                            .padding(.leading, 12)
                        
                        TextField("검색어를 입력하세요", text: $searchText)
                            .font(.system(size: 15))
                            .padding(.vertical, 12)
                            .onTapGesture {
                                isSearchFocused = true
                            }
                        
                        if !searchText.isEmpty {
                            Button(action: { searchText = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.ipSwapTextSecondary)
                                    .padding(.trailing, 8)
                            }
                        }
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color.ipSwapCardBg)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(isSearchFocused ? Color.ipSwapPrimary : Color.clear, lineWidth: 1.5)
                            )
                            .shadow(color: Color.black.opacity(0.06), radius: 8, x: 0, y: 4)
                    )
                    .animation(.easeInOut(duration: 0.2), value: isSearchFocused)
                }
                .padding(.horizontal)
                .padding(.top, 16)
                
                // 카테고리 필터
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(["ALL IP", "특허권", "상표권", "디자인권"], id: \.self) { filter in
                            FilterChip(title: filter, isSelected: selectedFilter == filter) {
                                withAnimation(.spring()) {
                                    selectedFilter = filter
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 16)
                }
                
                ScrollView {
                    LazyVStack(spacing: 20) {
                        ForEach(items) { item in
                            NavigationLink(destination: IPSwapItemDetailView(item: item)) {
                                IPSwapListRow(item: item)
                            }
                            .buttonStyle(IPSwapScaleButtonStyle())
                        }
                    }
                    .padding(.horizontal)
                    .padding(.bottom, 100)
                }
                .refreshable {
                    // 새로고침 액션
                }
            }
            
            // 하단 등록 버튼
            VStack(spacing: 0) {
                Divider()
                    .background(Color.gray.opacity(0.2))
                
                NavigationLink(destination: IPSwapRegistrationView()) {
                    PrimaryButton(title: "IP스왑 등록")
                }
                .padding(.horizontal)
                .padding(.vertical, 12)
                .background(AppColors.background.opacity(0.95))
            }
        }
        .navigationTitle("IP스왑")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// 필터 칩 컴포넌트
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 13, weight: isSelected ? .semibold : .regular))
                .padding(.horizontal, 16)
                .padding(.vertical, 9)
                .foregroundColor(isSelected ? .white : .ipSwapTextPrimary)
                .background(
                    Group {
                        if isSelected {
                            LinearGradient(gradient: Gradient(colors: [.ipSwapPrimary, .ipSwapSecondary]),
                                          startPoint: .topLeading, endPoint: .bottomTrailing)
                        } else {
                            Color.ipSwapCardBg
                        }
                    }
                )
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(isSelected ? Color.clear : Color.ipSwapBorder, lineWidth: 1)
                )
                .shadow(color: isSelected ? Color.ipSwapPrimary.opacity(0.3) : Color.clear, radius: 5, x: 0, y: 2)
                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isSelected)
        }
    }
}

// MARK: - 2. 상세 정보 화면 (✅ 이름이 IPSwapItemDetailView로 변경됨)
struct IPSwapItemDetailView: View {
    let item: IPSwapItem
    @State private var scrollOffset: CGFloat = 0
    
    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                GeometryReader { geometry in
                    Color.clear.preference(
                        key: ScrollOffsetPreferenceKey.self,
                        value: geometry.frame(in: .named("scrollView")).minY
                    )
                }
                .frame(height: 0)
                
                VStack(alignment: .leading, spacing: 0) {
                    // 헤더 이미지
                    ZStack(alignment: .bottom) {
                        Image(uiImage: UIImage(named: item.imageName) ?? UIImage())
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(height: 280 + max(0, scrollOffset))
                            .offset(y: min(0, scrollOffset / 2))
                            .clipped()
                        
                        // 그래디언트 오버레이
                        LinearGradient(
                            gradient: Gradient(colors: [Color.black.opacity(0.0), Color.black.opacity(0.6)]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .frame(height: 120)
                        
                        // 이미지 위에 표시되는 타이틀
                        VStack(alignment: .leading) {
                            Text(item.ipNumber)
                                .font(.title3.bold())
                                .foregroundColor(.white)
                            
                            Text(item.description)
                                .font(.subheadline)
                                .foregroundColor(.white.opacity(0.9))
                        }
                        .padding(20)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    
                    // 콘텐츠 카드
                    VStack(alignment: .leading, spacing: 20) {
                        // 주요 정보 섹션
                        detailSection(title: "기본 정보", icon: "info.circle.fill") {
                            VStack(spacing: 14) {
                                detailRow(icon: "number.circle.fill", label: "IP번호", value: item.ipNumber)
                                detailRow(icon: "building.columns.fill", label: "IP등록기관", value: item.registrationAuthority)
                                detailRow(icon: "calendar", label: "잔존기간", value: item.remainingPeriod)
                                detailRow(icon: "person.fill", label: "담당", value: item.personInCharge)
                                detailRow(icon: "envelope.fill", label: "연락처", value: item.ownerContact)
                            }
                        }
                        
                        // 스왑 정보 섹션
                        detailSection(title: "스왑 정보", icon: "arrow.left.arrow.right") {
                            VStack(spacing: 16) {
                                InfoCard(title: "보유IP설명", content: item.detailedDescription)
                                InfoCard(title: "스왑 대상", content: item.swapTarget)
                                InfoCard(title: "스왑 방법", content: item.swapMethod)
                            }
                        }
                        
                        // 관심 표시 버튼
                        Button(action: {}) {
                            HStack(spacing: 10) {
                                Image(systemName: "star")
                                Text("관심 IP로 등록")
                                Spacer()
                            }
                            .foregroundColor(AppColors.accent)
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .strokeBorder(AppColors.accent, lineWidth: 1.5)
                            )
                        }
                    }
                    .padding(.vertical, 22)
                    .padding(.horizontal, 20)
                    .background(AppColors.cardBackground)
                    .cornerRadius(24, corners: [.topLeft, .topRight])
                    .offset(y: -20)
                }
                .padding(.bottom, 100)
            }
            .coordinateSpace(name: "scrollView")
            .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
                scrollOffset = value
            }
            .edgesIgnoringSafeArea(.top)
            
            // 하단 스왑 신청 버튼
            VStack(spacing: 0) {
                Divider()
                    .background(Color.gray.opacity(0.2))
                
                NavigationLink(destination: IPSwapApplicationView()) {
                    PrimaryButton(title: "스왑 신청")
                }
                .padding(.horizontal)
                .padding(.vertical, 12)
                .background(AppColors.cardBackground.opacity(0.98))
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {}) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(scrollOffset < -50 ? AppColors.textPrimary : .white)
                }
            }
        }
    }
    
    private func detailSection<Content: View>(title: String, icon: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            // 섹션 헤더
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(AppColors.primary)
                
                Text(title)
                    .font(.title3.bold())
                    .foregroundColor(AppColors.textPrimary)
            }
            
            content()
        }
    }
    
    private func detailRow(icon: String, label: String, value: String) -> some View {
        HStack(alignment: .center, spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 15))
                .foregroundColor(AppColors.primary)
                .frame(width: 24, height: 24)
            
            Text(label)
                .font(.subheadline)
                .foregroundColor(AppColors.textSecondary)
                .frame(width: 80, alignment: .leading)
            
            Text(value)
                .font(.subheadline.bold())
                .foregroundColor(AppColors.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.vertical, 4)
    }
}

// 세부 정보 카드 컴포넌트
struct InfoCard: View {
    let title: String
    let content: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .foregroundColor(AppColors.textPrimary)
            
            Text(content)
                .font(.subheadline)
                .foregroundColor(AppColors.textSecondary)
                .lineSpacing(4)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.gray.opacity(0.05))
        .cornerRadius(12)
    }
}

// 스크롤 위치 추적을 위한 PreferenceKey
struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

// 참고: cornerRadius(_:corners:) 확장 메서드와 RoundedCorner 구조체는
// 이미 VerificationCodeEntryView.swift 파일에 정의되어 있으므로 여기서는 중복 정의하지 않음


// MARK: - 3. IP 스왑 등록 화면
struct IPSwapRegistrationView: View {
    // 섹터 선택을 위한 상태 변수
    @State private var selectedSector: String = ""
    @State private var showingSectorPicker = false
    
    // 이미지 업로드를 위한 상태 변수
    @State private var selectedImage: UIImage?
    @State private var showingImagePicker = false
    
    // 입력 필드를 위한 상태 변수들
    @State private var ipNumber = ""
    @State private var contactInfo = ""
    
    // 변리사 정보 (필수 항목)
    @State private var patentAttorneyName = ""
    @State private var patentAttorneyCompany = ""
    @State private var patentAttorneyContact = ""
    
    // 기타 입력 필드 상태 변수
    @State private var description = ""
    @State private var swapTarget = ""
    @State private var swapMethod = ""
    
    // 유효성 검사 오류 표시를 위한 상태 변수
    @State private var showingValidationAlert = false
    @State private var validationMessage = ""
    
    // 기술 중심의 섹터 카테고리 목록
    let sectors = [
        "인공지능",
        "반도체",
        "신재생에너지",
        "전기자동차",
        "제조업",
        "바이오테크놀로지",
        "로보틱스",
        "핀테크",
        "블록체인",
        "사물인터넷(IoT)",
        "5G/6G 통신",
        "클라우드 컴퓨팅",
        "가상현실/증강현실",
        "사이버보안",
        "첨단소재",
        "스마트시티",
        "우주항공",
        "스마트팩토리",
        "의료기기",
        "친환경기술"
    ]
    
    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: 20) {
                    Button(action: {
                        showingImagePicker = true
                    }) {
                        if let image = selectedImage {
                            Image(uiImage: image)
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(maxWidth: .infinity, minHeight: 200)
                                .cornerRadius(12)
                                .overlay(
                                    VStack {
                                        Spacer()
                                        Text("이미지 변경")
                                            .font(.subheadline)
                                            .padding(8)
                                            .background(Color.black.opacity(0.6))
                                            .foregroundColor(.white)
                                            .cornerRadius(8)
                                    }
                                    .padding(12)
                                    , alignment: .bottom
                                )
                        } else {
                            VStack {
                                Image(systemName: "photo.on.rectangle.angled")
                                    .font(.largeTitle)
                                Text("이미지 업로드")
                                    .font(.headline)
                            }
                            .foregroundColor(.gray)
                            .frame(maxWidth: .infinity, minHeight: 200)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .strokeBorder(.gray, style: StrokeStyle(lineWidth: 2, dash: [8]))
                            )
                        }
                    }
                    .sheet(isPresented: $showingImagePicker) {
                        ImagePicker(selectedImage: $selectedImage)
                    }
                    
                    // 섹터 선택 버튼
                    Button(action: {
                        showingSectorPicker = true
                    }) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("섹터")
                                    .font(.system(size: 13, weight: .regular))
                                    .foregroundColor(AppColors.textSecondary)
                                
                                Text(selectedSector.isEmpty ? "기술 섹터를 선택하세요" : selectedSector)
                                    .font(.system(size: 15))
                                    .foregroundColor(selectedSector.isEmpty ? .gray : AppColors.textPrimary)
                            }
                            
                            Spacer()
                            
                            Image(systemName: "chevron.down")
                                .font(.system(size: 14))
                                .foregroundColor(.gray)
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                    }
                    .sheet(isPresented: $showingSectorPicker) {
                        SectorPickerView(selectedSector: $selectedSector, sectors: sectors)
                    }
                    
                    // IP번호
                    FormTextField(label: "IP번호", placeholder: "IP번호를 입력하세요", text: $ipNumber)
                    
                    // 연락처
                    FormTextField(label: "연락처", placeholder: "연락처를 입력하세요", text: $contactInfo)
                    
                    // 담당 변리사 정보 (필수 항목)
                    Group {
                        FormTextField(label: "담당 변리사 이름*", placeholder: "변리사 이름을 입력하세요", text: $patentAttorneyName)
                            .overlay(
                                patentAttorneyName.isEmpty && showingValidationAlert ?
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.red, lineWidth: 1)
                                    .padding(-4) : nil
                            )
                        
                        FormTextField(label: "담당 변리사 회사*", placeholder: "소속 회사명을 입력하세요", text: $patentAttorneyCompany)
                            .overlay(
                                patentAttorneyCompany.isEmpty && showingValidationAlert ?
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.red, lineWidth: 1)
                                    .padding(-4) : nil
                            )
                        
                        FormTextField(label: "담당 변리사 연락처*", placeholder: "변리사 연락처를 입력하세요", text: $patentAttorneyContact)
                            .overlay(
                                patentAttorneyContact.isEmpty && showingValidationAlert ?
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.red, lineWidth: 1)
                                    .padding(-4) : nil
                            )
                    }
                    
                    // 추가 필드들
                    FormTextField(label: "IP 설명", placeholder: "보유하신 IP에 대해 설명해주세요", text: $description, multiline: true)
                    FormTextField(label: "스왑 대상", placeholder: "어떤 IP와 스왑하고 싶으신가요?", text: $swapTarget)
                    FormTextField(label: "스왑 방법", placeholder: "원하시는 스왑 방법을 입력하세요", text: $swapMethod)
                }
                .padding()
                .padding(.bottom, 100)
            }
            
            Button(action: {
                // 필수 입력 항목 유효성 검사
                if patentAttorneyName.isEmpty || patentAttorneyCompany.isEmpty || patentAttorneyContact.isEmpty {
                    showingValidationAlert = true
                    validationMessage = "변리사 이름, 회사, 연락처는 필수 입력 항목입니다."
                } else {
                    // 모든 필수 항목이 입력되었으면 등록 진행
                    // 여기에 등록 로직 구현
                    print("IP스왑 등록 처리 진행")
                }
            }) {
                PrimaryButton(title: "등록하기")
            }
            .padding(.horizontal)
            .padding(.bottom, 8)
            .alert(isPresented: $showingValidationAlert) {
                Alert(
                    title: Text("필수 정보 누락"),
                    message: Text(validationMessage),
                    dismissButton: .default(Text("확인"))
                )
            }
        }
        .navigationTitle("IP스왑 등록")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// ImagePicker와 관련된 코드는 MoreView.swift에 정의된 것을 사용

// 섹터 선택을 위한 팝업 뷰
struct SectorPickerView: View {
    @Binding var selectedSector: String
    let sectors: [String]
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            List {
                ForEach(sectors, id: \.self) { sector in
                    Button(action: {
                        selectedSector = sector
                        dismiss()
                    }) {
                        HStack {
                            Text(sector)
                                .foregroundColor(AppColors.textPrimary)
                            
                            Spacer()
                            
                            if selectedSector == sector {
                                Image(systemName: "checkmark")
                                    .foregroundColor(AppColors.primary)
                            }
                        }
                    }
                }
            }
            .navigationBarTitle("섹터 선택", displayMode: .inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("취소") {
                        dismiss()
                    }
                }
            }
        }
    }
}

// MARK: - 4. 스왑 신청 화면
struct IPSwapApplicationView: View {
    @State private var contactInfo = ""
    @State private var ipNumber = ""
    @State private var description = ""
    @State private var terms = ""
    
    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 20) {
                Button(action: {}) {
                    Text("스왑할 IP등록증을 업로드 해주세요")
                        .foregroundColor(.gray)
                        .frame(maxWidth: .infinity, minHeight: 80)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .strokeBorder(.gray, style: StrokeStyle(lineWidth: 1))
                        )
                }
                
                FormTextField(label: "연락처", placeholder: "연락처를 입력하세요", text: $contactInfo)
                FormTextField(label: "IP번호", placeholder: "IP번호를 입력하세요", text: $ipNumber)
                FormTextField(label: "스왑 제안 설명", placeholder: "스왑을 원하시는 이유를 설명해주세요", text: $description, multiline: true)
                FormTextField(label: "동의 조항", placeholder: "스왑 진행 시 회사 정책을 준수하겠습니다(필수)", text: $terms)
                
                Spacer()
            }
            .padding()
            
            Button(action: {}) {
                PrimaryButton(title: "업로드")
            }
            .padding(.horizontal)
            .padding(.bottom, 8)
        }
        .navigationTitle("스왑 신청")
        .navigationBarTitleDisplayMode(.inline)
    }
}


// MARK: - 재사용 가능한 헬퍼 뷰들

struct IPSwapListRow: View {
    let item: IPSwapItem
    @State private var isHovered = false
    
    var body: some View {
        VStack(spacing: 0) {
            // 이미지 부분
            Image(uiImage: UIImage(named: item.imageName) ?? UIImage())
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(height: 150)
                .frame(maxWidth: .infinity)
                .overlay(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color.black.opacity(0.0),
                            Color.black.opacity(0.3)
                        ]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .overlay(
                    HStack {
                        Text(item.remainingPeriod)
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                Capsule()
                                    .fill(Color.ipSwapPrimary)
                                    .shadow(color: Color.black.opacity(0.2), radius: 3, x: 0, y: 2)
                            )
                        
                        Spacer()
                        
                        HStack(spacing: 4) {
                            Image(systemName: "arrow.left.arrow.right")
                                .font(.system(size: 9))
                            Text("스왑 가능")
                                .font(.system(size: 11, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.ipSwapSecondary.opacity(0.8))
                        .clipShape(Capsule())
                    }
                    .padding([.horizontal, .bottom], 12),
                    alignment: .bottom
                )
                .clipShape(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                )
            
            // 정보 부분
            VStack(alignment: .leading, spacing: 14) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(item.description)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.ipSwapTextPrimary)
                        .lineLimit(1)
                    
                    Text("IP번호: \(item.ipNumber)")
                        .font(.system(size: 13))
                        .foregroundColor(.ipSwapTextSecondary)
                }
                
                HStack(alignment: .center, spacing: 8) {
                    Image(systemName: "arrow.triangle.swap")
                        .font(.system(size: 12))
                        .foregroundColor(.ipSwapPrimary)
                        .frame(width: 20, height: 20)
                        .background(
                            Circle()
                                .fill(Color.ipSwapPrimary.opacity(0.1))
                        )
                    
                    Text("스왑 대상: \(item.swapTarget)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.ipSwapTextSecondary)
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.ipSwapTextSecondary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(Color.ipSwapCardBg)
            .clipShape(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
            )
        }
        .background(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(Color.ipSwapCardBg)
                .shadow(color: Color.black.opacity(isHovered ? 0.12 : 0.07), radius: isHovered ? 12 : 8, x: 0, y: isHovered ? 6 : 4)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .contentShape(RoundedRectangle(cornerRadius: 16))
        .scaleEffect(isHovered ? 1.01 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isHovered)
        .onHover { hovering in
            isHovered = hovering
        }
    }
}

// 버튼 클릭 애니메이션 스타일
struct IPSwapScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .opacity(configuration.isPressed ? 0.9 : 1.0)
            .animation(.spring(response: 0.2, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

struct IPSwapInfoRow: View {
    let label: String
    let value: String
    var isLast: Bool = false
    var valueAlignment: HorizontalAlignment = .trailing
    
    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(label)
                .font(.system(size: 13, weight: .regular))
                .foregroundColor(AppColors.textSecondary)
                .frame(width: 80, alignment: .leading)
            
            if valueAlignment == .trailing {
                Spacer()
                Text(value)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(AppColors.textPrimary)
                    .lineLimit(1)
            } else {
                Text(value)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(AppColors.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .lineLimit(nil)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
        .padding(.bottom, isLast ? 0 : 8)
    }
}

struct FormTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var multiline: Bool = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.system(size: 13, weight: .regular))
                .foregroundColor(AppColors.textSecondary)
                .frame(alignment: .leading)
            
            if multiline {
                ZStack(alignment: .topLeading) {
                    if text.isEmpty {
                        Text(placeholder)
                            .foregroundColor(.gray.opacity(0.8))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 12)
                    }
                    
                    TextEditor(text: $text)
                        .frame(minHeight: 100)
                        .padding(8)
                        .opacity(text.isEmpty ? 0.6 : 1)
                }
                .background(Color.gray.opacity(0.1))
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                )
            } else {
                TextField(placeholder, text: $text)
                    .padding(12)
                    .background(Color.gray.opacity(0.05))
                    .cornerRadius(8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                    )
            }
        }
    }
}

// MARK: - App Color Theme
struct AppColors {
    static let primary = Color(red: 48/255, green: 198/255, blue: 232/255)
    static let secondary = Color(red: 103/255, green: 65/255, blue: 217/255)
    static let accent = Color(red: 255/255, green: 149/255, blue: 0/255)
    static let background = Color(red: 245/255, green: 247/255, blue: 250/255)
    static let cardBackground = Color.white
    static let textPrimary = Color(red: 51/255, green: 51/255, blue: 51/255)
    static let textSecondary = Color(red: 119/255, green: 119/255, blue: 119/255)
    
    static let gradientBackground = LinearGradient(
        gradient: Gradient(colors: [primary, primary.opacity(0.8)]),
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

struct PrimaryButton: View {
    let title: String
    
    var body: some View {
        Text(title)
            .font(.headline.weight(.bold))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(
                AppColors.gradientBackground
            )
            .cornerRadius(12)
            .shadow(color: AppColors.primary.opacity(0.3), radius: 5, x: 0, y: 3)
    }
}

// MARK: - SwiftUI 프리뷰
struct IPSwapDetailView_Previews: PreviewProvider {
    static var previews: some View {
        // ✅ 프리뷰 대상 뷰 이름 변경: IPSwapDetailView
        IPSwapDetailView()
    }
}
