import SwiftUI

// MARK: - 데이터 모델

// 국가 정보를 담기 위한 모델
struct Country: Identifiable, Hashable {
    let id: String // 국가 코드 (예: "KR", "US")
    let name: String
    let emoji: String
}

// MARK: - ViewModel

@MainActor
class OverseasLoginViewModel: ObservableObject {
    // 앱에서 지원하는 국가 목록
    @Published var allCountries: [Country] = [
        Country(id: "KR", name: "대한민국", emoji: "🇰🇷"),
        Country(id: "GR", name: "그리스", emoji: "🇬🇷"),
        Country(id: "ZA", name: "남아프리카 공화국", emoji: "🇿🇦"),
        Country(id: "NL", name: "네덜란드", emoji: "🇳🇱"),
        Country(id: "NO", name: "노르웨이", emoji: "🇳🇴"),
        Country(id: "NZ", name: "뉴질랜드", emoji: "🇳🇿"),
        Country(id: "TW", name: "대만", emoji: "🇹🇼"),
        Country(id: "DK", name: "덴마크", emoji: "🇩🇰"),
        Country(id: "DE", name: "독일", emoji: "🇩🇪"),
        Country(id: "RU", name: "러시아", emoji: "🇷🇺"),
        Country(id: "MY", name: "말레이시아", emoji: "🇲🇾"),
        Country(id: "MX", name: "멕시코", emoji: "🇲🇽"),
        Country(id: "US", name: "미국", emoji: "🇺🇸"),
        Country(id: "VN", name: "베트남", emoji: "🇻🇳"),
        Country(id: "BE", name: "벨기에", emoji: "🇧🇪"),
        Country(id: "BR", name: "브라질", emoji: "🇧🇷"),
        Country(id: "SA", name: "사우디아라비아", emoji: "🇸🇦"),
        Country(id: "SE", name: "스웨덴", emoji: "🇸🇪"),
        Country(id: "CH", name: "스위스", emoji: "🇨🇭"),
        Country(id: "ES", name: "스페인", emoji: "🇪🇸"),
        Country(id: "SG", name: "싱가포르", emoji: "🇸🇬"),
        Country(id: "AE", name: "아랍에미리트", emoji: "🇦🇪"),
        Country(id: "AR", name: "아르헨티나", emoji: "🇦🇷"),
        Country(id: "IE", name: "아일랜드", emoji: "🇮🇪"),
        Country(id: "GB", name: "영국", emoji: "🇬🇧"),
        Country(id: "AT", name: "오스트리아", emoji: "🇦🇹"),
        Country(id: "AU", name: "호주", emoji: "🇦🇺"),
        Country(id: "IL", name: "이스라엘", emoji: "🇮🇱"),
        Country(id: "EG", name: "이집트", emoji: "🇪🇬"),
        Country(id: "IT", name: "이탈리아", emoji: "🇮🇹"),
        Country(id: "IN", name: "인도", emoji: "🇮🇳"),
        Country(id: "ID", name: "인도네시아", emoji: "🇮🇩"),
        Country(id: "JP", name: "일본", emoji: "🇯🇵"),
        Country(id: "CN", name: "중국", emoji: "🇨🇳"),
        Country(id: "CL", name: "칠레", emoji: "🇨🇱"),
        Country(id: "CA", name: "캐나다", emoji: "🇨🇦"),
        Country(id: "CO", name: "콜롬비아", emoji: "🇨🇴"),
        Country(id: "TH", name: "태국", emoji: "🇹🇭"),
        Country(id: "TR", name: "터키", emoji: "🇹🇷"),
        Country(id: "PT", name: "포르투갈", emoji: "🇵🇹"),
        Country(id: "PL", name: "폴란드", emoji: "🇵🇱"),
        Country(id: "FR", name: "프랑스", emoji: "🇫🇷"),
        Country(id: "FI", name: "핀란드", emoji: "🇫🇮"),
        Country(id: "PH", name: "필리핀", emoji: "🇵🇭"),
        Country(id: "HK", name: "홍콩", emoji: "🇭🇰"),
    ]

    @Published var searchText = ""

    // 검색 텍스트를 기반으로 국가 목록을 필터링
    var filteredCountries: [Country] {
        if searchText.isEmpty {
            return allCountries
        } else {
            // 대소문자 구분 없이 검색
            return allCountries.filter { $0.name.localizedCaseInsensitiveContains(searchText) }
        }
    }
}

// MARK: - 메인 뷰

struct OverseasLoginBlockView: View {
    @StateObject private var viewModel = OverseasLoginViewModel()
    
    // '해외 로그인 차단' 기능의 전체 활성화 상태를 영구적으로 저장
    @AppStorage("isOverseasLoginBlockEnabled") private var isOverseasLoginBlockEnabled = false

    var body: some View {
        VStack(spacing: 0) {
            List {
                // 메인 토글 섹션
                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        Toggle(isOn: $isOverseasLoginBlockEnabled.animation()) {
                            Text("해외 로그인 차단")
                                .font(.system(size: 17))
                        }
                        .tint(Color(red: 48 / 255, green: 198 / 255, blue: 232 / 255))
                        
                        Text("허용된 국가를 제외한 모든 해외 IP에서의 로그인을 차단합니다.")
                            .font(.system(size: 14))
                            .foregroundColor(.gray)
                    }
                }
                .listRowInsets(EdgeInsets(top: 15, leading: 16, bottom: 15, trailing: 16))

                // 허용 국가 설정 섹션
                Section(header: Text("허용 국가 설정").bold()) {
                    // 메인 토글이 켜져 있을 때만 국가 목록을 보여줌
                    if isOverseasLoginBlockEnabled {
                        // 검색 바
                        HStack {
                            Image(systemName: "magnifyingglass")
                                .foregroundColor(.gray)
                            TextField("국가 검색", text: $viewModel.searchText)
                                .submitLabel(.done) // 키보드 'return' 버튼을 '완료'로 변경
                        }
                        .padding(EdgeInsets(top: 8, leading: 12, bottom: 8, trailing: 12))
                        .background(Color(.systemGray6))
                        .cornerRadius(10)
                        
                        // 국가 목록
                        ForEach(viewModel.filteredCountries) { country in
                            CountryToggleRow(country: country)
                        }
                    } else {
                        // 기능이 꺼져 있을 때 안내 문구 표시
                        Text("해외 로그인 차단 기능을 켜면 국가별로 로그인 허용 여부를 설정할 수 있습니다.")
                            .font(.system(size: 14))
                            .foregroundColor(.gray)
                            .padding(.vertical, 10)
                    }
                }
            }
            .listStyle(.insetGrouped) // iOS 기본 설정 스타일
        }
        .navigationTitle("해외 로그인 차단")
        .navigationBarTitleDisplayMode(.inline)
        .background(Color.white.edgesIgnoringSafeArea(.all))
        // 화면을 탭하면 키보드를 내리는 제스처 추가
        .onTapGesture {
            hideKeyboard()
        }
    }
}

// MARK: - 재사용 가능한 뷰 (국가별 토글 행)

struct CountryToggleRow: View {
    let country: Country
    
    // 각 국가의 허용 상태를 "login_allow_국가코드" 키를 이용해 별도로 영구 저장
    @AppStorage var isAllowed: Bool
    
    init(country: Country) {
        self.country = country
        // 대한민국은 기본적으로 허용하도록 설정
        self._isAllowed = AppStorage(wrappedValue: country.id == "KR", "login_allow_\(country.id)")
    }
    
    var body: some View {
        HStack {
            Text("\(country.emoji) \(country.name)")
                .font(.system(size: 16))
            Spacer()
            // 대한민국은 항상 허용 상태로, 비활성화하여 변경할 수 없게 만듦
            Toggle(isOn: $isAllowed) {}
                .labelsHidden()
                .tint(Color(red: 48 / 255, green: 198 / 255, blue: 232 / 255))
                .disabled(country.id == "KR")
        }
    }
}

// MARK: - 헬퍼

// 키보드를 내리기 위한 확장
#if canImport(UIKit)
extension View {
    func hideKeyboard() {
        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}
#endif

// MARK: - 미리보기

struct OverseasLoginBlockView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            OverseasLoginBlockView()
        }
    }
}

