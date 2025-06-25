import SwiftUI

// 상세 행 UI 컴포넌트
struct DetailRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 14, weight: .regular))
                .foregroundColor(Color(hex: "666666"))
            Spacer()
            Text(value)
                .tradingAmountFont() // 거래금액 폰트 적용
                .foregroundColor(.black)
        }
    }
}

// 메인 뷰
struct WithdrawalDetailView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject private var userData = UserData.shared
    var transactionId: Int
    
    // 트랜잭션 정보 가져오기
    private var transaction: USDTransactionInfo? {
        userData.usdTransactions.first(where: { $0.id == transactionId })
    }
    
    // 트랜잭션 타입
    private var transactionType: USDTransactionType {
        transaction?.type ?? .deposit
    }
    
    // 실제로 사용할 표시 타입 (입금/출금)
    var actualType: String {
        transactionType == .deposit ? "USD 입금" : "USD 출금"
    }
    
    // 금액
    var usdAmount: String {
        transaction?.usdValue ?? "0 USD"
    }
    
    // 원화 금액
    var krwAmount: String {
        transaction?.amount ?? "0 KRW"
    }
    
    // 완료 시간
    var completionTime: String {
        guard let transaction = transaction else { return "" }
        return "\(transaction.date) \(transaction.time)"
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // MARK: - 커스텀 내비게이션 바
            HStack {
                Spacer()
                Text("\(transactionType == .deposit ? "입금" : "출금") 내역 상세")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(.black)
                Spacer()
            }
            // ▼▼▼ 이 한 줄만 추가하면 됩니다 ▼▼▼
            .padding(.vertical, 12)
            .background(
                Color.white.ignoresSafeArea(edges: .top)
            )
            
            // MARK: - 상단 색상 표시 및 금액
            VStack(alignment: .leading, spacing: 8) {
                Text(actualType + " 완료")
                    .font(.system(size: 16, weight: .regular))
                    .foregroundColor(transactionType == .deposit ? Color(hex: "FF3B30") : Color(hex: "007AFF"))
                    .padding(.top, 24) // 헤더가 생긴 만큼 콘텐츠 시작 위치를 조정
                
                Text(usdAmount)
                    .currentPriceFont() // 현재가 폰트 적용
                    .foregroundColor(.black)
                
                Text(krwAmount)
                    .previousDayComparisonFont() // 전일대비 폰트 적용
                    .foregroundColor(Color(hex: "666666"))
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 8)

            // MARK: - 구분선
            Divider()
                .padding(.vertical, 20)
                .padding(.horizontal, 16)
            
            // MARK: - 상세 정보 리스트
            VStack(spacing: 24) {
                DetailRow(label: "완료일시", value: completionTime)
                DetailRow(label: "유형", value: actualType)
                
                // 출금이면 출금계좌 정보 표시
                if transactionType == .withdraw {
                    let accountNumberMasked = userData.bankAccountNumber.replacingOccurrences(of: "^([0-9]{2})([0-9]+)([0-9]{4})$", with: "$1******$3", options: .regularExpression)
                    DetailRow(label: "출금계좌", value: "\(accountNumberMasked) \(userData.bankName)")
                }
            }
            .padding(.horizontal, 16)
            
            // 하단 버튼을 맨 아래로 밀어내기 위한 Spacer
            Spacer()

            // MARK: - 확인 버튼
            Button(action: {
                // 확인 버튼 동작 - 뒤로 가기
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
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(Color.white)
    }
}
