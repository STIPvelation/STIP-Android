import SwiftUI

// MARK: - 경매 상세 뷰
struct AuctionDetailView: View {
    let auction: AuctionItem
    @Binding var isPresented: Bool
    @State private var showBidSheet = false
    @State private var bidAmount: String = ""
    @State private var showBidSuccessAlert = false
    @State private var showMinBidWarning = false
    
    // 최대 입찰 가능 금액 (무제한)
    private var maxBidAmount: Int {
        return Int.max
    }
    
    // 최소 입찰 가능 금액 ($ 기반)
    private var minBidAmount: Int {
        return auction.currentPrice + 1000
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // 이미지 슬라이더
                imageCarousel
                
                // 기본 정보 섹션
                infoSection
                
                // 구분선
                Rectangle()
                    .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                    .frame(height: 8)
                
                // 상세 정보 섹션
                detailsSection
                
                // 구분선
                Rectangle()
                    .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                    .frame(height: 8)
                
                // 권리 포함 정보
                rightsSection
                
                // 경매 정보 섹션 (조회수 등)
                auctionStatsSection
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: {
                    isPresented = false
                }) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(.primary)
                }
            }
            ToolbarItem(placement: .principal) {
                Text("경매 상세")
                    .font(.headline)
            }
        }
        .safeAreaInset(edge: .bottom) {
            // 하단 고정 입찰 버튼
            VStack(spacing: 12) {
                HStack {
                    VStack(alignment: .leading) {
                        Text("현재 가격")
                            .font(.callout)
                            .foregroundColor(.secondary)
                        Text(auction.formattedCurrentPrice)
                            .font(.title3)
                            .fontWeight(.bold)
                    }
                    
                    Spacer()
                    
                    Button(action: {
                        self.showBidSheet = true
                    }) {
                        Text("입찰하기")
                            .fontWeight(.semibold)
                            .padding(.horizontal, 30)
                            .padding(.vertical, 14)
                            .background(Color(red: 48/255, green: 198/255, blue: 232/255))
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                }
                
                HStack {
                    Image(systemName: "clock")
                        .foregroundColor(.orange)
                    Text("남은 시간: \(auction.formattedRemainingTime)")
                        .foregroundColor(.orange)
                        .fontWeight(.medium)
                    Spacer()
                    Text("\(auction.bidCount)명 참여")
                        .foregroundColor(.secondary)
                }
                .font(.footnote)
            }
            .padding(16)
            .background(Color.white)
            .shadow(color: Color.black.opacity(0.05), radius: 5, y: -2)
        }
        .sheet(isPresented: $showBidSheet) {
            bidSheetView
        }
        .alert("입찰에 성공했습니다", isPresented: $showBidSuccessAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("입찰이 성공적으로 접수되었습니다.")
        }
        .alert("최소 입찰금액 미달", isPresented: $showMinBidWarning) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("최소 입찰금액 \(formatPrice(minBidAmount)) 이상을 입찰해야 합니다.")
        }
    }
    
    // MARK: - Components
    
    // 이미지 슬라이더
    private var imageCarousel: some View {
        TabView {
            if auction.imageNames.isEmpty {
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .aspectRatio(16/9, contentMode: .fit)
                    .overlay {
                        Image(systemName: "photo")
                            .font(.largeTitle)
                            .foregroundColor(.gray)
                    }
            } else {
                ForEach(auction.imageNames, id: \.self) { imageName in
                    Image(imageName)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 300)
                        .clipped()
                }
            }
        }
        .frame(height: 300)
        .tabViewStyle(PageTabViewStyle())
    }
    
    // 기본 정보 섹션
    private var infoSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            // IP 유형 및 카테고리
            HStack(spacing: 8) {
                Text(auction.ipType.rawValue)
                    .font(.footnote)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(tagColor(for: auction.ipType))
                    .foregroundColor(.white)
                    .cornerRadius(4)
                
                Text(auction.category)
                    .font(.footnote)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(Color(red: 244/255, green: 247/255, blue: 252/255))
                    .cornerRadius(4)
                    .foregroundColor(.secondary)
            }
            
            // 제목
            Text(auction.title)
                .font(.title2)
                .fontWeight(.bold)
                .multilineTextAlignment(.leading)
                
            // IP 등록번호
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text("IP 등록번호:")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                Text(auction.registrationNumber)
                    .font(.subheadline)
                    .fontWeight(.medium)
            }
            
            // 등록일 및 만료일
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("등록일")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text(auction.registrationDate)
                        .font(.subheadline)
                }
                
                Spacer()
                
                if let expirationDate = auction.expirationDate {
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("만료일")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text(expirationDate)
                            .font(.subheadline)
                    }
                }
            }
            .padding(.vertical, 4)
            
            Divider()
            
            // 시작 가격
            HStack {
                Text("시작 가격")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                Text(auction.formattedStartingPrice)
                    .font(.subheadline)
                    .strikethrough()
                    .foregroundColor(.secondary)
            }
            
            // 현재 가격
            HStack {
                Text("현재 가격")
                    .font(.headline)
                    .foregroundColor(.primary)
                Text(auction.formattedCurrentPrice)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.primary)
            }
        }
        .padding(16)
    }
    
    // 상세 정보 섹션
    private var detailsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("상세 정보")
                .font(.headline)
                .fontWeight(.bold)
            
            Divider()
            
            // IP 상세 설명
            Text(auction.description)
                .font(.body)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.vertical, 4)
            
            // 권리 소유 지역 표시
            VStack(alignment: .leading, spacing: 8) {
                Text("권리 활용 가능 지역")
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .padding(.top, 4)
                
                HStack(spacing: 6) {
                    ForEach(auction.territoryRights, id: \.self) { territory in
                        Text(territory)
                            .font(.caption)
                            .padding(.vertical, 4)
                            .padding(.horizontal, 8)
                            .background(Color(red: 240/255, green: 240/255, blue: 240/255))
                            .cornerRadius(4)
                    }
                }
            }
            
            Divider()
                .padding(.vertical, 8)
            
            // 제작 정보
            VStack(alignment: .leading, spacing: 8) {
                HStack(alignment: .top) {
                    Text("소유자/제작자")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .frame(width: 100, alignment: .leading)
                    
                    Text(auction.creator)
                        .font(.subheadline)
                }
                
                HStack(alignment: .top) {
                    Text("제작/등록연도")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .frame(width: 100, alignment: .leading)
                    
                    Text(auction.creationDate)
                        .font(.subheadline)
                }
                
                // 사용 제한이 있는 경우
                if let limitations = auction.usageLimitations, !limitations.isEmpty {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("사용 제한 사항")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        ForEach(limitations, id: \.self) { limitation in
                            HStack(alignment: .top, spacing: 8) {
                                Image(systemName: "exclamationmark.triangle")
                                    .font(.caption)
                                    .foregroundColor(.orange)
                                
                                Text(limitation)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            .padding(.vertical, 2)
                        }
                    }
                    .padding(.top, 8)
                }
            }
            .padding(.vertical, 8)
        }
        .padding(16)
    }
    
    // 권리 포함 정보 - IP 전용 권리
    private var rightsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("포함된 IP 권리")
                .font(.headline)
                .fontWeight(.bold)
            
            Divider()
            
            VStack(alignment: .leading, spacing: 10) {
                ForEach(auction.rightsIncluded, id: \.self) { right in
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: "checkmark.shield.fill")
                            .foregroundColor(rightsColor(for: auction.ipType))
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text(right)
                                .font(.subheadline)
                            
                            // 더 상세한 권리 설명 추가 (규모 예시)
                            if right.contains("상품화") {
                                Text("제한 없는 상품화 권리 내역에 따라 포함")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            } else if right.contains("저작") || right.contains("제작") {
                                Text("본 저작권과 관련된 2차 저작물 제작 및 활용 권리")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            } else if right.contains("활용") {
                                Text("지정된 활용 범위 내에서 사용 가능")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .padding(.vertical, 8)
        }
        .padding(16)
    }
    
    // 경매 정보 섹션
    private var auctionStatsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("IP 경매 정보")
                .font(.headline)
                .fontWeight(.bold)
            
            Divider()
            
            // IP 만료일 및 경매 종료일 동시 표시
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("경매 시작일")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("2023.09.25")
                            .font(.subheadline)
                    }
                    
                    Spacer()
                    
                    VStack(alignment: .trailing, spacing: 4) {
                        Text("경매 종료일")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text(Date(timeIntervalSinceNow: auction.remainingTime).formatted(date: .abbreviated, time: .shortened))
                            .font(.subheadline)
                    }
                }
                .padding(.bottom, 4)
                
                Divider()
                    .padding(.vertical, 4)
                
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "eye")
                            .foregroundColor(.secondary)
                        Text("\(auction.viewCount) 명 조회")
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    HStack(spacing: 8) {
                        Image(systemName: "heart")
                            .foregroundColor(.secondary)
                        Text("관심 추가")
                            .foregroundColor(.secondary)
                    }
                }
                
                // IP 관련 추가 정보
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.seal")
                            .foregroundColor(.secondary)
                        Text("IP 권리 검증 완료")
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    HStack(spacing: 8) {
                        Image(systemName: "clock.arrow.circlepath")
                            .foregroundColor(.secondary)
                        Text("IP 여부 확인: \(Date().addingTimeInterval(-86400 * 3).formatted(date: .abbreviated, time: .omitted))")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .font(.subheadline)
        }
        .padding(16)
    }
    
    // 입찰 시트 - IP 옵션 포함
    private var bidSheetView: some View {
        ScrollView {
            VStack(spacing: 20) {
                // 상단 여백 추가
                Spacer().frame(height: 20)
            HStack {
                Text("IP 경매 입찰")
                    .font(.title2)
                    .fontWeight(.bold)
                
                Spacer()
                
                Button(action: {
                    showBidSheet = false
                }) {
                    Image(systemName: "xmark.circle.fill")
                        .font(.title3)
                        .foregroundColor(.gray)
                }
            }
            
            // IP 유형 및 등록번호 표시
            HStack {
                Text(auction.ipType.rawValue)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(tagColor(for: auction.ipType))
                    .foregroundColor(.white)
                    .cornerRadius(4)
                
                Text("번호: \(auction.registrationNumber)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            VStack(alignment: .leading, spacing: 8) {
                Text("현재 입찰가")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                Text(auction.formattedCurrentPrice)
                    .font(.title3)
                    .fontWeight(.semibold)
            }
            
            VStack(alignment: .leading, spacing: 8) {
                Text("입찰 금액")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                HStack {
                    Text("$")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    TextField("입찰 금액을 입력하세요", text: $bidAmount)
                        .keyboardType(.numberPad)
                }
                .padding()
                .background(Color(red: 244/255, green: 247/255, blue: 252/255))
                .cornerRadius(8)
                .onChange(of: bidAmount) { oldValue, newValue in
                    // 천 단위 구분자 포맷팅 적용
                    let cleanValue = newValue.replacingOccurrences(of: ",", with: "")
                    
                    if let number = Int(cleanValue) {
                        let formatter = NumberFormatter()
                        formatter.numberStyle = .decimal
                        bidAmount = formatter.string(from: NSNumber(value: number)) ?? cleanValue
                    } else if newValue.isEmpty {
                        // 빈 문자열은 허용
                        bidAmount = ""
                    } else {
                        // 숫자가 아니면 이전 값으로 돌림
                        if bidAmount != newValue {
                            bidAmount = oldValue
                        }
                    }
                }
                
                Text("최소 \(formatPrice(minBidAmount)) 이상 입찰해야 합니다.")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            // IP 관련 주의 사항 추가
            VStack(alignment: .leading, spacing: 6) {
                Text("IP 권리 취득 관련 주의사항:")
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundColor(.secondary)
                
                Text("입찰 시 취소할 수 없으며, 낙찰될 경우 IP 권리 이전 및 기술이전 의무가 발생합니다.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text("낙찰 후 \(auction.ipType.rawValue) 권리 이전과 관련하여 별도의 계약서 작성 및 등록이 필요하며, 관련 비용은 낙찰자 부담입니다.")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding()
            .background(Color(red: 255/255, green: 247/255, blue: 237/255))
            .cornerRadius(8)
            
            Spacer()
            
            Button(action: {
                // 입찰 로직 구현하기
                // 입력값의 콤마 제거
                let cleanBidAmount = bidAmount.replacingOccurrences(of: ",", with: "")
                
                // 최소 입찰금액 검증
                // 입력값은 달러 단위로 수정해야 함 (원이 아닌 달러로 비교)
                if let bidValue = Int(cleanBidAmount) {
                    // $ 수치로 비교 - minBidAmount는 /1000 되지 않은 원래 가격
                    // 분명한 단위로 닌 테스트
                    let adjustedMinBid = minBidAmount / 1000
                    
                    if bidValue >= adjustedMinBid {
                        showBidSheet = false
                        showBidSuccessAlert = true
                    } else {
                        showMinBidWarning = true
                    }
                } else {
                    showMinBidWarning = true
                }
            }) {
                Text("IP 경매 입찰하기")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(tagColor(for: auction.ipType))
                    .cornerRadius(12)
            }
            }
            .padding(.horizontal)
            .padding(.bottom, 30)
        }
        .presentationDetents([.medium, .large])
    }
    
    // 금액 포맷팅 함수 (달러 표시, 3자리 0 제외)
    private func formatPrice(_ price: Int) -> String {
        // 3자리 0 제외한 값 계산
        let adjustedPrice = price / 1000
        
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.positivePrefix = "$"
        return formatter.string(from: NSNumber(value: adjustedPrice)) ?? "$\(adjustedPrice)"
    }
    
    // IP 유형별 태그 색상
    private func tagColor(for ipType: IPType) -> Color {
        switch ipType {
        case .patent:
            return Color(red: 37/255, green: 116/255, blue: 169/255) // 파란색
        case .trademark:
            return Color(red: 40/255, green: 167/255, blue: 69/255) // 초록색
        case .copyright, .music, .movie:
            return Color(red: 121/255, green: 82/255, blue: 179/255) // 보라색
        case .design:
            return Color(red: 225/255, green: 83/255, blue: 97/255) // 분홍색
        case .character:
            return Color(red: 240/255, green: 173/255, blue: 78/255) // 주황색
        case .franchise:
            return Color(red: 91/255, green: 192/255, blue: 190/255) // 청록색
        default:
            return Color(red: 108/255, green: 117/255, blue: 125/255) // 회색
        }
    }
    
    // 권리 섹션에 사용할 색상
    private func rightsColor(for ipType: IPType) -> Color {
        switch ipType {
        case .patent:
            return Color(red: 37/255, green: 116/255, blue: 169/255).opacity(0.9) // 파란색
        case .trademark:
            return Color(red: 40/255, green: 167/255, blue: 69/255).opacity(0.9) // 초록색
        case .copyright, .music, .movie:
            return Color(red: 121/255, green: 82/255, blue: 179/255).opacity(0.9) // 보라색
        case .character, .franchise:
            return Color(red: 240/255, green: 173/255, blue: 78/255).opacity(0.9) // 주황색
        default:
            return Color(red: 48/255, green: 198/255, blue: 232/255) // 기본 앱 색상
        }
    }
}
