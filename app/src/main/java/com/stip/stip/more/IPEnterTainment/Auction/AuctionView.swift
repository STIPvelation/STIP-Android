import SwiftUI

struct AuctionView: View {
    // State variables
    @State private var searchText = ""
    @State private var selectedCategory = "전체"
    @State private var selectedSortOption = "마감임박"
    @State private var showFilters = false
    @State private var selectedAuction: AuctionItem? = nil
    
    // Sample categories
    let categories = ["전체", "특허", "게임", "캐릭터", "프랜차이즈", "음악", "사진", "영화", "웹툰"]
    
    // Sample sort options
    let sortOptions = ["마감임박", "인기순", "최신순", "높은가격순", "낮은가격순"]
    
    // Sample auction data
    let sampleAuctions = [
        AuctionItem(
            id: 1,
            title: "인기 웹툰 '일상모험' IP",
            category: "웹툰",
            ipType: .copyright,
            registrationNumber: "C-2023-056789",
            registrationDate: "2023.03.15",
            expirationDate: "2073.03.15",
            currentPrice: 12500000,
            startingPrice: 10000000,
            remainingTime: 86400,
            bidCount: 25,
            imageNames: ["auction_sample1", "auction_sample_detail1", "auction_sample_detail2"],
            description: "월간 조회수 500만의 인기 웹툰 '일상모험'의 IP 경매입니다. 캐릭터 상품화, 2차 콘텐츠 제작 권리가 포함됩니다.",
            creator: "홍길동",
            creationDate: "2023년",
            rightsIncluded: ["캐릭터 상품화", "2차 저작물 제작", "디지털 콘텐츠 활용"],
            viewCount: 1250,
            territoryRights: ["한국", "중국", "일본", "북미"],
            usageLimitations: ["원작자 크레딧 필수 표기", "성인 콘텐츠 제작 제한"]
        ),
        AuctionItem(
            id: 2,
            title: "게임 '판타지월드' 캐릭터 라이센싱",
            category: "게임",
            ipType: .character,
            registrationNumber: "K-2022-078456",
            registrationDate: "2022.08.10",
            expirationDate: "2032.08.10",
            currentPrice: 8700000,
            startingPrice: 5000000,
            remainingTime: 172800,
            bidCount: 14,
            imageNames: ["auction_sample2"],
            description: "인기 모바일 게임 '판타지월드'의 주요 캐릭터 상품화 라이센싱 경매입니다.",
            creator: "김개발",
            creationDate: "2022년",
            rightsIncluded: ["캐릭터 상품화", "프로모션 활용"],
            viewCount: 876,
            territoryRights: ["한국", "동남아시아"],
            usageLimitations: ["게임 내 캐릭터와 동일한 컨셉 유지"]
        ),
        AuctionItem(
            id: 3,
            title: "힙합 싱글 '도시의 밤' 저작권",
            category: "음악",
            ipType: .music,
            registrationNumber: "M-2024-012345",
            registrationDate: "2024.01.05",
            expirationDate: "2074.01.05",
            currentPrice: 5400000,
            startingPrice: 3000000,
            remainingTime: 259200,
            bidCount: 8,
            imageNames: ["auction_sample3"],
            description: "스트리밍 100만회 돌파한 인디 힙합 음원 저작권 경매입니다.",
            creator: "래퍼J",
            creationDate: "2024년",
            rightsIncluded: ["음원 사용권", "편곡권", "CF 활용권"],
            viewCount: 654,
            territoryRights: ["글로벌"],
            usageLimitations: nil
        ),
        AuctionItem(
            id: 4,
            title: "인기 캐릭터 '몽글이' IP",
            category: "캐릭터",
            ipType: .trademark,
            registrationNumber: "T-2021-089321",
            registrationDate: "2021.11.25",
            expirationDate: "2031.11.25",
            currentPrice: 15800000,
            startingPrice: 12000000,
            remainingTime: 43200,
            bidCount: 32,
            imageNames: ["auction_sample4"],
            description: "SNS 스티커 1000만 다운로드의 인기 캐릭터 '몽글이' IP 판매입니다.",
            creator: "이디자인",
            creationDate: "2021년",
            rightsIncluded: ["캐릭터 상품화", "디지털 콘텐츠 제작", "광고 활용권"],
            viewCount: 1876,
            territoryRights: ["한국", "아시아", "유럽"],
            usageLimitations: ["캐릭터 이미지 변형 제한"]
        ),
        AuctionItem(
            id: 5,
            title: "스마트 결제 시스템 특허 기술",
            category: "특허",
            ipType: .patent,
            registrationNumber: "P-2019-107432",
            registrationDate: "2019.05.18",
            expirationDate: "2039.05.18",
            currentPrice: 47500000,
            startingPrice: 40000000,
            remainingTime: 129600,
            bidCount: 7,
            imageNames: ["auction_sample5"],
            description: "비접촉식 결제와 생체인식을 결합한 혁신적인 보안 결제 시스템 특허입니다. 핀테크/금융 분야에 즉시 활용 가능한 기술로, 이미 시제품 개발 및 테스트가 완료되었습니다.",
            creator: "박기술 / 테크솔루션스(주)",
            creationDate: "2019년",
            rightsIncluded: ["특허 전용실시권", "기술이전", "2차 응용특허 개발권", "시제품 설계도"],
            viewCount: 432,
            territoryRights: ["한국", "미국", "EU", "중국", "일본"],
            usageLimitations: ["제3자 기술이전 제한 (5년)", "원천기술 변경 불가"]
        ),
        AuctionItem(
            id: 6,
            title: "유명 커피 프랜차이즈 브랜드 IP",
            category: "프랜차이즈",
            ipType: .franchise,
            registrationNumber: "F-2020-045231",
            registrationDate: "2020.07.25",
            expirationDate: "2040.07.25",
            currentPrice: 95000,
            startingPrice: 80000,
            remainingTime: 302400,
            bidCount: 12,
            imageNames: ["auction_sample5"],
            description: "국내 50개 매장을 보유한 프리미엄 커피 프랜차이즈 브랜드의 IP 판매. 브랜드명, 레시피, 인테리어 디자인 및 운영 매뉴얼이 포함됩니다.",
            creator: "카페인터내셔널(주)",
            creationDate: "2020년",
            rightsIncluded: ["브랜드 사용권", "매장 운영권", "레시피 및 제조기법", "인테리어 디자인 IP", "직원 교육 시스템"],
            viewCount: 845,
            territoryRights: ["한국", "아시아"],
            usageLimitations: ["브랜드 이미지 보호 의무", "품질 기준 준수"]
        ),
        AuctionItem(
            id: 7,
            title: "인공지능 자율학습 알고리즘 특허 기술",
            category: "특허",
            ipType: .patent,
            registrationNumber: "P-2021-087654",
            registrationDate: "2021.08.12",
            expirationDate: "2041.08.12",
            currentPrice: 85000,
            startingPrice: 70000,
            remainingTime: 215000,
            bidCount: 15,
            imageNames: ["auction_sample5"],
            description: "데이터 업데이트 횟수를 최대 95% 감소시키는 혁신 AI 자율학습 알고리즘 특허입니다. 시스템 자원 소비를 현저히 줄이면서도 높은 학습 정확도를 유지합니다.",
            creator: "김기술 / AI인텔리전스(주)",
            creationDate: "2021년",
            rightsIncluded: ["특허 전용실시권", "기술이전", "프로그램 소스코드", "연구 분석 데이터"],
            viewCount: 890,
            territoryRights: ["한국", "미국", "유럽", "일본"],
            usageLimitations: ["기본 알고리즘 변경 불가"]
        ),
        AuctionItem(
            id: 8,
            title: "고성능 반도체 배치 구조 특허",
            category: "특허",
            ipType: .patent,
            registrationNumber: "P-2022-123456",
            registrationDate: "2022.03.28",
            expirationDate: "2042.03.28",
            currentPrice: 120000,
            startingPrice: 100000,
            remainingTime: 345600,
            bidCount: 9,
            imageNames: ["auction_sample5"],
            description: "연산속도 27% 향상, 염색 방지 구조가 내장된 혁신적인 고성능 반도체 배치 특허입니다. 머신러닝, 데이터 처리에 특화된 구조를 제공합니다.",
            creator: "박엔지니어 / 세미테크(주)",
            creationDate: "2022년",
            rightsIncluded: ["특허 전용실시권", "회로도면 활용권", "관련 부품 생산권", "반도체 설계도"],
            viewCount: 657,
            territoryRights: ["한국", "타이완", "일본", "미국"],
            usageLimitations: ["국가 안보 관련 분야 활용 시 사전 승인 필요"]
        ),
        AuctionItem(
            id: 9,
            title: "고효율 태양광 발전 기술 특허",
            category: "특허",
            ipType: .patent,
            registrationNumber: "P-2023-078901",
            registrationDate: "2023.02.15",
            expirationDate: "2043.02.15",
            currentPrice: 95000000,
            startingPrice: 80000000,
            remainingTime: 432000,
            bidCount: 18,
            imageNames: ["auction_sample5"],
            description: "기존 태양광 팬널 대비 효율 38% 향상, 기후변화에 강한 신소재 적용 태양광 팬널 제조 기술 특허입니다. 지속가능한 에너지 생산에 혁신적 기여가 가능합니다.",
            creator: "이과학 / 에너지소루션(주)",
            creationDate: "2023년",
            rightsIncluded: ["특허 전용실시권", "기술이전", "제조공법 사용권", "개선기술 우선 활용권"],
            viewCount: 745,
            territoryRights: ["한국", "중국", "인도", "미국", "유럽"],
            usageLimitations: ["3년 내 상용화 의무 존재"]
        )
    ]
    
    var filteredAuctions: [AuctionItem] {
        var auctions = sampleAuctions
        
        // Apply category filter
        if selectedCategory != "전체" {
            auctions = auctions.filter { $0.category == selectedCategory }
        }
        
        // Apply search filter
        if !searchText.isEmpty {
            auctions = auctions.filter { $0.title.localizedCaseInsensitiveContains(searchText) }
        }
        
        // Apply sorting
        switch selectedSortOption {
        case "마감임박":
            auctions.sort { $0.remainingTime < $1.remainingTime }
        case "인기순":
            auctions.sort { $0.bidCount > $1.bidCount }
        case "최신순":
            // In a real app this would sort by creation date
            auctions.sort { $0.id > $1.id }
        case "높은가격순":
            auctions.sort { $0.currentPrice > $1.currentPrice }
        case "낮은가격순":
            auctions.sort { $0.currentPrice < $1.currentPrice }
        default:
            break
        }
        
        return auctions
    }
    
    var body: some View {
        ZStack {
            if let selectedItem = selectedAuction {
                // Detailed view when an item is selected
                AuctionDetailView(auction: selectedItem, isPresented: Binding<Bool>(get: { selectedAuction != nil }, set: { if !$0 { selectedAuction = nil } }))
            } else {
                // Main list view
                VStack(spacing: 0) {
                    // Search bar
                    searchBar
                    
                    // Category selector
                    categorySelector
                        .padding(.vertical, 8)
                    
                    // Sort and filter options
                    filterAndSort
                    
                    // Auction list
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(filteredAuctions) { auction in
                                AuctionItemCard(auction: auction)
                                    .onTapGesture {
                                        selectedAuction = auction
                                    }
                            }
                        }
                        .padding(16)
                    }
                    .background(Color(red: 244/255, green: 247/255, blue: 252/255))
                }
                .background(Color.white)
                .navigationTitle("IP옥션")
                .navigationBarTitleDisplayMode(.inline)
            }
        }
    }
    
    // MARK: - Components
    
    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            
            TextField("검색어를 입력하세요", text: $searchText)
                .foregroundColor(.primary)
            
            if !searchText.isEmpty {
                Button(action: {
                    searchText = ""
                }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                }
            }
        }
        .padding(10)
        .background(Color(red: 244/255, green: 247/255, blue: 252/255))
        .cornerRadius(8)
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
    
    private var categorySelector: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(categories, id: \.self) { category in
                    Button(action: {
                        selectedCategory = category
                    }) {
                        Text(category)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(selectedCategory == category ? Color(red: 48/255, green: 198/255, blue: 232/255) : Color(red: 244/255, green: 247/255, blue: 252/255))
                            .foregroundColor(selectedCategory == category ? .white : .primary)
                            .cornerRadius(20)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }
    
    private var filterAndSort: some View {
        HStack {
            Button(action: {
                showFilters.toggle()
            }) {
                HStack {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                    Text("필터")
                }
                .foregroundColor(.primary)
                .padding(.vertical, 8)
                .padding(.horizontal, 12)
            }
            
            Spacer()
            
            Menu {
                ForEach(sortOptions, id: \.self) { option in
                    Button(option) {
                        selectedSortOption = option
                    }
                }
            } label: {
                HStack {
                    Text(selectedSortOption)
                    Image(systemName: "chevron.down")
                }
                .foregroundColor(.primary)
                .padding(.vertical, 8)
            }
        }
        .padding(.horizontal, 16)
        .background(Color.white)
        .overlay(Divider(), alignment: .bottom)
    }
}
