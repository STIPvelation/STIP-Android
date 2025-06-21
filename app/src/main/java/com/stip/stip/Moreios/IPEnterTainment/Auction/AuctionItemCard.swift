import SwiftUI

// MARK: - 경매 아이템 카드 뷰
struct AuctionItemCard: View {
    let auction: AuctionItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 이미지 섹션
            ZStack(alignment: .topTrailing) {
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
                    Image(auction.imageNames[0])
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 180)
                        .clipped()
                        .overlay(
                            LinearGradient(
                                gradient: Gradient(colors: [Color.clear, Color.black.opacity(0.3)]),
                                startPoint: .center,
                                endPoint: .bottom
                            )
                        )
                }
                
                // IP 유형 태그
                Text(auction.ipType.rawValue)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(tagColor(for: auction.ipType))
                    .cornerRadius(4)
                    .padding(12)
            }
            
            VStack(alignment: .leading, spacing: 8) {
                // 제목
                Text(auction.title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .lineLimit(1)
                
                // 등록번호
                HStack(spacing: 4) {
                    Image(systemName: "number")
                        .font(.footnote)
                        .foregroundColor(.secondary)
                    Text("경매 IP: \(auction.registrationNumber)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                // 현재 가격
                HStack {
                    Text("현재 가격")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Text(auction.formattedCurrentPrice)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.primary)
                }
                
                Divider()
                
                // 남은 시간 & 입찰 수
                HStack {
                    HStack(spacing: 4) {
                        Image(systemName: "clock")
                            .font(.footnote)
                            .foregroundColor(.secondary)
                        Text("\(auction.formattedRemainingTime)")
                            .font(.footnote)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    HStack(spacing: 4) {
                        Image(systemName: "person")
                            .font(.footnote)
                            .foregroundColor(.secondary)
                        Text("\(auction.bidCount)명 참여")
                            .font(.footnote)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(12)
            .background(Color.white)
        }
        .background(Color.white)
        .cornerRadius(8)
        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 2)
    }
    
    // 서로 다른 IP 유형에 다른 색상 표시
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
}
