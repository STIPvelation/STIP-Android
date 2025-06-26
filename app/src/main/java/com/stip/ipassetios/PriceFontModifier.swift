import SwiftUI

// 가격 관련 텍스트 일관된 폰트 스타일 제공
struct PriceFontModifier {
    // 현재가 폰트 (큰 사이즈, 굵게)
    struct CurrentPrice: ViewModifier {
        func body(content: Content) -> some View {
            content
                .font(.system(size: 28, weight: .bold, design: .default))
        }
    }
    
    // 전일대비 폰트 (중간 사이즈, 중간 굵기)
    struct PreviousDayComparison: ViewModifier {
        func body(content: Content) -> some View {
            content
                .font(.system(size: 16, weight: .medium, design: .default))
        }
    }
    
    // 거래금액 폰트 (표준 사이즈, 일반)
    struct TradingAmount: ViewModifier {
        func body(content: Content) -> some View {
            content
                .font(.system(size: 14, weight: .regular, design: .default))
        }
    }
}

// 사용하기 쉽게 View Extension으로 제공
extension View {
    func currentPriceFont() -> some View {
        self.modifier(PriceFontModifier.CurrentPrice())
    }
    
    func previousDayComparisonFont() -> some View {
        self.modifier(PriceFontModifier.PreviousDayComparison())
    }
    
    func tradingAmountFont() -> some View {
        self.modifier(PriceFontModifier.TradingAmount())
    }
}
