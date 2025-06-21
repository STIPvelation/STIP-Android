import SwiftUI

// MARK: - 로그인 이력 모델

// Codable을 채택하여 JSON 파싱이 가능하도록 준비합니다.
struct LoginRecord: Identifiable, Codable, Equatable {
    let id: UUID
    let date: Date
    let ipAddress: String
    let location: String
    let deviceInfo: String
    let isUnusual: Bool
}

// MARK: - ViewModel

@MainActor // UI 업데이트를 메인 스레드에서 하도록 보장합니다.
class LoginHistoryViewModel: ObservableObject {
    @Published var loginHistory: [LoginRecord] = []
    @Published var isLoading = false
    @Published var filterPeriod: String = "전체" {
        didSet {
            // 필터가 변경될 때마다 데이터를 다시 불러옵니다.
        }
    }
    
    // 필터링된 로그인 이력
    var filteredHistory: [LoginRecord] {
        guard filterPeriod != "전체" else {
            return loginHistory
        }
        
        var dateComponent = DateComponents()
        switch filterPeriod {
        case "1주일": dateComponent.day = -7
        case "1개월": dateComponent.month = -1
        case "3개월": dateComponent.month = -3
        case "6개월": dateComponent.month = -6
        case "1년": dateComponent.year = -1
        default: return loginHistory
        }
        
        if let startDate = Calendar.current.date(byAdding: dateComponent, to: Date()) {
            return loginHistory.filter { $0.date >= startDate }
        }
        
        return loginHistory
    }
    
    // 실제 데이터를 가져오는 함수
    func fetchLoginHistory() {
        isLoading = true
        Task {
            do {
                try await Task.sleep(nanoseconds: 1_000_000_000)
                let fetchedData = getSampleData()
                self.loginHistory = fetchedData
                self.isLoading = false
            } catch {
                print("데이터를 가져오는 데 실패했습니다: \(error)")
                self.isLoading = false
            }
        }
    }
    
    // 모든 기기에서 로그아웃하는 함수
    func logoutFromAllDevices() {
        // 실제 앱에서는 서버에 로그아웃 요청을 보내는 API를 호출합니다.
        print("모든 기기에서 로그아웃을 요청했습니다.")
    }
    
    // 샘플 데이터를 생성하는 private 함수
    private func getSampleData() -> [LoginRecord] {
        return [
            LoginRecord(id: UUID(), date: Date().addingTimeInterval(-60*60*2), ipAddress: "211.233.75.12", location: "대한민국 서울", deviceInfo: "iPhone 16 Pro", isUnusual: false),
            LoginRecord(id: UUID(), date: Date().addingTimeInterval(-60*60*24*2), ipAddress: "211.233.75.12", location: "대한민국 서울", deviceInfo: "iPhone 16 Pro", isUnusual: false),
            LoginRecord(id: UUID(), date: Date().addingTimeInterval(-60*60*24*5), ipAddress: "112.156.132.78", location: "대한민국 부산", deviceInfo: "MacBook Pro", isUnusual: true),
            LoginRecord(id: UUID(), date: Date().addingTimeInterval(-60*60*24*8), ipAddress: "211.233.75.12", location: "대한민국 서울", deviceInfo: "iPhone 16 Pro", isUnusual: false),
            LoginRecord(id: UUID(), date: Date().addingTimeInterval(-60*60*24*35), ipAddress: "182.22.45.190", location: "미국 캘리포니아", deviceInfo: "Windows PC", isUnusual: true),
        ]
    }
}


// MARK: - 로그인 이력 화면

struct LoginHistoryView: View {
    @StateObject private var viewModel = LoginHistoryViewModel()
    
    @State private var showFilterSheet = false
    @State private var showLogoutConfirmAlert = false // 로그아웃 확인 알림 상태
    @State private var shouldLogout = false // 로그아웃 후 AuthSelectionView로 이동을 위한 상태 변수
    
    var body: some View {
        VStack(spacing: 0) {
            // 필터 및 정보 헤더
            HeaderView(
                recordCount: viewModel.filteredHistory.count,
                filterPeriod: viewModel.filterPeriod,
                onFilterTapped: { showFilterSheet = true }
            )
            
            // 구분선
            Rectangle()
                .fill(Color(red: 244/255, green: 247/255, blue: 252/255))
                .frame(height: 4)
            
            // --- 데이터 로딩 상태에 따른 분기 처리 ---
            ZStack {
                if viewModel.isLoading {
                    ProgressView()
                } else if viewModel.filteredHistory.isEmpty {
                    Text("해당 기간의 로그인 이력이 없습니다.")
                        .foregroundColor(.gray)
                } else {
                    // 로그인 이력 목록
                    List {
                        ForEach(viewModel.filteredHistory) { record in
                            LoginRecordRow(record: record)
                        }
                        .listRowSeparator(.hidden)
                        .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                    }
                    .listStyle(.plain)
                }
            }
            .frame(maxHeight: .infinity) // ZStack이 남은 공간을 모두 채우도록

            // --- 추가된 부분 ---
            // 모든 기기에서 로그아웃 버튼 (하단)
            VStack {
                Button(action: { showLogoutConfirmAlert = true }) {
                    Text("모든 기기에서 로그아웃")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.red)
                        .padding(.vertical, 8)
                        .frame(maxWidth: .infinity)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.red, lineWidth: 1)
                        )
                }
                .padding()
            }
            .background(Color.white)
        }
        .navigationTitle("로그인 이력")
        .navigationBarTitleDisplayMode(.inline)
        .background(Color(red: 244/255, green: 247/255, blue: 252/255))
        .sheet(isPresented: $showFilterSheet) {
            FilterView(selectedFilter: $viewModel.filterPeriod)
        }
        .onAppear {
            if viewModel.loginHistory.isEmpty {
                viewModel.fetchLoginHistory()
            }
        }
        .alert("모든 기기에서 로그아웃", isPresented: $showLogoutConfirmAlert) {
            Button("취소", role: .cancel) {}
            Button("로그아웃", role: .destructive) {
                viewModel.logoutFromAllDevices()
                shouldLogout = true // 로그아웃 후 AuthSelectionView로 전환
            }
        } message: {
            Text("현재 기기를 제외한 모든 기기에서 로그아웃됩니다. 계속하시겠습니까?")
        }
        // 로그아웃 상태가 되면 AuthSelectionView를 전체 화면으로 띄운다
        .fullScreenCover(isPresented: $shouldLogout) {
            AuthSelectionView()
        }
    }
}

// MARK: - 재사용 뷰

// 헤더 뷰
struct HeaderView: View {
    let recordCount: Int
    let filterPeriod: String
    let onFilterTapped: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Text("총 \(recordCount)건의 로그인 이력")
                    .font(.system(size: 16, weight: .medium))
                
                Spacer()
                
                Button(action: onFilterTapped) {
                    HStack {
                        Text(filterPeriod)
                            .font(.system(size: 14))
                        Image(systemName: "chevron.down")
                            .font(.caption)
                    }
                    .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(
                    RoundedRectangle(cornerRadius: 15)
                        .stroke(Color(red: 48/255, green: 198/255, blue: 232/255), lineWidth: 1)
                )
            }
            
            Text("계정 보호를 위해 주기적으로 로그인 이력을 확인해주세요.")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .background(Color.white)
    }
}


// 로그인 이력 행 컴포넌트
struct LoginRecordRow: View {
    let record: LoginRecord
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(formatDate(record.date))
                    .font(.system(size: 16, weight: .medium))
                
                Spacer()
                
                if record.isUnusual {
                    Text("비정상 접속")
                        .font(.system(size: 12))
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.red)
                        .cornerRadius(10)
                }
            }
            
            VStack(alignment: .leading, spacing: 6) {
                InfoItem(title: "IP 주소", value: record.ipAddress)
                InfoItem(title: "위치", value: record.location)
                InfoItem(title: "기기", value: record.deviceInfo)
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(record.isUnusual ? 0.1 : 0.05), radius: 4)
        )
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy.MM.dd HH:mm:ss"
        return formatter.string(from: date)
    }
}

// 정보 항목 컴포넌트
struct InfoItem: View {
    let title: String
    let value: String
    
    var body: some View {
        HStack(spacing: 12) {
            Text(title)
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .frame(width: 60, alignment: .leading)
            
            Text(value)
                .font(.system(size: 14))
        }
    }
}

// 필터 시트 컴포넌트
struct FilterView: View {
    @Binding var selectedFilter: String
    @Environment(\.dismiss) private var dismiss
    
    let filters = ["전체", "1주일", "1개월", "3개월", "6개월", "1년"]
    
    var body: some View {
        NavigationView {
            List {
                ForEach(filters, id: \.self) { filter in
                    Button(action: {
                        selectedFilter = filter
                        dismiss()
                    }) {
                        HStack {
                            Text(filter)
                                .font(.system(size: 16))
                            
                            Spacer()
                            
                            if selectedFilter == filter {
                                Image(systemName: "checkmark")
                                    .foregroundColor(Color(red: 48/255, green: 198/255, blue: 232/255))
                            }
                        }
                    }
                    .foregroundColor(.primary)
                    .padding(.vertical, 4)
                }
            }
            .listStyle(.plain)
            .navigationBarTitle("기간 선택", displayMode: .inline)
            .navigationBarItems(trailing: Button("취소") {
                dismiss()
            })
        }
    }
}

// MARK: - 미리보기

struct LoginHistoryView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            LoginHistoryView()
        }
    }
}

