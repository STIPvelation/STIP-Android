import Foundation

/// 정책 및 약관 타입을 정의하는 열거형
enum PolicyType: String, CaseIterable, Identifiable {
    case terms = "이용약관"
    case privacy = "개인정보처리방침"
    case esg = "ESG 정책"
    case startupSupport = "스타트업 및 중소사업자 지원 정책"
    case antiMoneyLaundering = "자금세탁방지(AML) 및 내부통제 정책"
    case dipValuation = "DIP 가치평가 기준 정책"
    case feeTransparency = "수수료 투명성 정책"
    
    var id: String { self.rawValue }
    
    // 각 정책의 웹 URL (필요시 사용)
    var policyUrl: URL? {
        switch self {
        case .terms:
            return URL(string: "https://stipvelation.com/terms-of-service")
        case .privacy:
            return URL(string: "https://stipvelation.com/privacy-policy")
        case .esg:
            return URL(string: "https://stipvelation.com/esg-policy")
        case .startupSupport:
            return URL(string: "https://stipvelation.com/startup-support")
        case .antiMoneyLaundering:
            return URL(string: "https://stipvelation.com/aml-policy")
        case .dipValuation:
            return URL(string: "https://stipvelation.com/dip-valuation")
        case .feeTransparency:
            return URL(string: "https://stipvelation.com/fee-transparency")
        }
    }
}
