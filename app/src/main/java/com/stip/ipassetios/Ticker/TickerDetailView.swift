import SwiftUI
import Foundation

struct TickerDetailView: View {
    @Environment(\.presentationMode) var presentationMode
    let transaction: Transaction
    let isPolygon: Bool = true // 폴리곤 타입만 사용하도록 고정
    var isDeposit: Bool = true // 기본값은 입금으로 설정, 생성자에서 지정 가능
    
    // 거래 ID 하드코딩 (실제 앱에서는 API나 데이터에서 가져와야 함)
    private var transactionId: String {
        let ids = [
            "POL": "0x522a52fed17eeee939890",
        ]
        
        return ids[transaction.type] ?? "0x\(transaction.contractAddress.prefix(20))"
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            customNavigationBar
            mainInfoSection
            dividerSection
            detailInfoSection
            Spacer()
            confirmationButton
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(Color.white)
        .navigationBarHidden(true)
    }
    
    // 커스텀 내비게이션 바
    private var customNavigationBar: some View {
        HStack {
            Button(action: {
                presentationMode.wrappedValue.dismiss()
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 17))
                    .foregroundColor(.black)
            }
            
            Spacer()
            
            // 입금/출금 타입에 따라 다른 타이틀 표시
            Text(isDeposit ? "입금 내역 상세" : "출금 내역 상세")
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(.black)
            
            Spacer()
            
            Button(action: {
                // 공유 버튼 기능
            }) {
                Image(systemName: "square.and.arrow.up")
                    .font(.system(size: 17))
                    .foregroundColor(.blue)
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 16)
        .background(Color.white.ignoresSafeArea(edges: .top))
    }
    
    // 메인 정보 섹션
    private var mainInfoSection: some View {
        VStack(spacing: 8) {
            // Center aligned completion status
            Text(isDeposit ? "입금 신청완료" : "출금 신청완료")
                .font(.system(size: 16, weight: .regular))
                .foregroundColor(isDeposit ? Color(hex: "FF3B30") : Color(hex: "007AFF"))
                .padding(.top, 24)
                .frame(maxWidth: .infinity, alignment: .center) // Center alignment
            
            // Transaction amount with ticker type (현재가)
            Text("\(transaction.amount, specifier: "%.8f") \(transaction.type)")
                .currentPriceFont()
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .center) // Center alignment
            
            // USD equivalent instead of KRW (전일대비)
            Text("≈ \(String(format: "%.2f", transaction.totalUSD)) USD")
                .previousDayComparisonFont()
                .foregroundColor(Color(hex: "666666"))
                .frame(maxWidth: .infinity, alignment: .center) // Center alignment
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 4) // 간격 축소 (8 -> 4)
    }
    
    // KRW 환산 가격 계산 (오류 수정됨)
    private var krwEquivalent: String {
        // Int 타입을 Double로 변환하여 계산
        let krwValueDouble = Double(transaction.amount) * transaction.priceUSD * 1380.0
        let krwValue = Int(krwValueDouble)
        
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        return numberFormatter.string(from: NSNumber(value: krwValue)) ?? String(krwValue)
    }
    
    // 구분선 섹션
    private var dividerSection: some View {
        Divider()
            .padding(.vertical, 12) // 간격 축소 (20 -> 12)
            .padding(.horizontal, 16)
    }
    
    // 상세 정보 섹션
    private var detailInfoSection: some View {
        VStack(spacing: 20) { // 상세 항목간 간격 축소 (24 -> 20)
            DetailRow(label: "완료일시", value: "2025.06.12 19:24 (UTC +09:00)")
            DetailRow(label: "유형", value: isDeposit ? "입금" : "출금")
            DetailRow(label: "네트워크", value: "Polygon")
            
            // "To" field only shown for withdrawal details
            if !isDeposit {
                DetailRow(label: "To", value: "0x7a250d563739dF2C5dAcb4c659F2488D")
            }
            
            // Transaction ID row with copy button
            HStack {
                DetailRow(label: "거래ID", value: transactionId)
                
                Button(action: {
                    // Copy transaction ID to clipboard
                    UIPasteboard.general.string = transactionId
                }) {
                    Image(systemName: "doc.on.doc")
                        .font(.system(size: 18))
                        .foregroundColor(Color(hex: "30C6E8"))
                        .padding(.trailing, 4)
                }
            }
        }
        .padding(.horizontal, 16)
    }
    
    // 확인 버튼
    private var confirmationButton: some View {
        Button(action: {
            presentationMode.wrappedValue.dismiss()
        }) {
            Text("확인")
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 18)
                .background(Color(hex: "30C6E8"))
                .cornerRadius(8)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
    }
}

// DetailRow는 다른 파일에 정의되어 있다고 가정합니다.
// struct DetailRow: View { ... }

// 앱의 ColorExtension.swift 파일에서 Color(hex:) 확장을 사용합니다.
// extension Color { ... }

// 프로젝트의 실제 Transaction 모델이 아래와 같다고 가정합니다.
// 이 구조체는 별도의 모델 파일에 정의되어야 합니다.
/*
struct Transaction: Identifiable {
    let id = UUID()
    let type: String
    let amount: Int // 또는 Double
    let priceUSD: Double
    let tokenPrice: Double
    let totalUSD: Double
    let contractAddress: String
    let category: Category
    
    enum Category {
        case patent, businessModel, usd
    }
}
*/

struct TickerDetailView_Previews: PreviewProvider {
    static var previews: some View {
        // 미리보기 내부의 Transaction 정의를 삭제하고,
        // 프로젝트의 실제 Transaction 모델을 사용합니다.
        let sampleTransaction = Transaction(
            type: "POL",
            amount: 20, // 실제 Transaction 모델의 amount 타입에 맞춰주세요.
            priceUSD: 1.05,
            tokenPrice: 1.05,
            totalUSD: 21.0,
            contractAddress: "0x522a52fed17eeee81ad79150a1ac7760a5249747f0b3a57eb",
            category: TransactionCategory.patent
        )
        
        return TickerDetailView(transaction: sampleTransaction)
    }
}
