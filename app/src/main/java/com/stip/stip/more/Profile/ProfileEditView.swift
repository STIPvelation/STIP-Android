import SwiftUI
import PhotosUI

struct ProfileEditView: View {
    // 싱글톤 UserData 모델 사용
    @ObservedObject private var userData = UserData.shared
    
    // 편집을 위한 임시 데이터 저장
    @State private var editedName: String
    @State private var editedPhone: String
    @State private var editedEmail: String
    
    // 화면 전환 및 UI 상태를 위한 변수
    @State private var showEditPhone = false
    @State private var showEditEmail = false
    @State private var showEditProfile = false
    
    // 프로필 이미지 상태를 위한 변수
    @State private var selectedItem: PhotosPickerItem? = nil
    @State private var selectedImageData: Data? = nil
    @State private var profileImage: Image? = nil
    
    @Environment(\.presentationMode) var presentationMode
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    
    // 생성자에서 현재 데이터로 초기화
    init() {
        _editedName = State(initialValue: UserData.shared.userName)
        _editedPhone = State(initialValue: UserData.shared.phoneNumber)
        _editedEmail = State(initialValue: UserData.shared.email)
        
        // 기존에 저장된 프로필 이미지가 있다면 불러오기
        if let existingProfileImage = UserData.shared.profileImage {
            _profileImage = State(initialValue: Image(uiImage: existingProfileImage))
        }
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // 프로필 헤더 섹션
                profileHeaderSection
                    .padding(.top, 20)
                    .padding(.bottom, 20)
                
                // --- 계정 정보 카드 ---
                VStack(spacing: 0) {
                    Text("계정 정보")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color(hex: "232D3F"))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.bottom, 12)
                        .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                    
                    // --- 휴대폰 박스 ---
                    modernSectionItem(icon: "phone.fill", iconColor: "30C6E8", title: "휴대폰 번호", value: userData.phoneNumber) {
                        showEditPhone = true
                    }
                    .padding(.bottom, 8)
                }
                .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                
                // --- 이메일 박스 ---
                modernSectionItem(icon: "envelope.fill", iconColor: "5271FF", title: "이메일", value: userData.email) {
                    showEditEmail = true
                }
                .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                
                // 섹션 간 간격
                Spacer().frame(height: 24)
                
                // --- 필수 정보 카드 ---
                VStack(alignment: .leading, spacing: 0) {
                    HStack(spacing: 12) {
                        // 아이콘
                        ZStack {
                            Circle()
                                .fill(Color(hex: "7466F2").opacity(0.15))
                                .frame(width: 36, height: 36)
                            
                            Image(systemName: "doc.text.fill")
                                .font(.system(size: 16))
                                .foregroundColor(Color(hex: "7466F2"))
                        }
                        
                        Text("필수정보")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color(hex: "232D3F"))
                        
                        Spacer()
                        
                        Button(action: {
                            showEditProfile = true
                        }) {
                            HStack(spacing: 6) {
                                Text("변경")
                                    .font(.system(size: 15, weight: .medium))
                                Image(systemName: "pencil")
                                    .font(.system(size: 13))
                            }
                            .foregroundColor(Color.white)
                            .padding(.vertical, 8)
                            .padding(.horizontal, 14)
                            .background(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        Color(hex: "7466F2"),
                                        Color(hex: "5D56E8")
                                    ]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(Capsule())
                        }
                    }
                    .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                    .padding(.top, horizontalSizeClass == .compact ? 18 : 22)
                    .padding(.bottom, horizontalSizeClass == .compact ? 16 : 20)
                    
                    Divider().padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                    
                    // 필수 정보 항목들
                    modernInfoItem(icon: "person.text.rectangle.fill", iconColor: "7466F2", title: "여권 영문 이름", value: userData.passportName)
                    modernInfoItem(icon: "mappin.and.ellipse", iconColor: "7466F2", title: "주소", value: userData.address)
                    modernInfoItem(icon: "briefcase.fill", iconColor: "7466F2", title: "직업", value: userData.occupation)
                    modernInfoItem(icon: "building.2.fill", iconColor: "7466F2", title: "직장명", value: userData.companyName)
                    modernInfoItem(icon: "location.fill", iconColor: "7466F2", title: "직장주소", value: userData.companyAddress)
                }
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .shadow(color: Color.black.opacity(0.05), radius: 10, x: 0, y: 2)
                .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                
                // 하단 안내 문구
                VStack(alignment: .leading, spacing: 4) {
                    Text("필수 정보는 본인인증을 통해 변경이 가능합니다.")
                    Text("이용 중인 서비스의 실제 반영은 시간이 소요됩니다.")
                }
                .font(.system(size: horizontalSizeClass == .compact ? 13 : 15, weight: .medium))
                .foregroundColor(Color(hex: "6E7491"))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, horizontalSizeClass == .compact ? 20 : 28)
                .padding(.top, 16)
                
                // 저장 버튼
                Button {
                    // 저장 로직 구현
                    userData.userName = editedName
                    userData.email = editedEmail
                    userData.phoneNumber = editedPhone
                    
                    // 프로필 이미지 저장
                    if let imageData = selectedImageData, let uiImage = UIImage(data: imageData) {
                        userData.profileImage = uiImage
                    }
                    
                    // 변경 완료 후 돌아가기
                    presentationMode.wrappedValue.dismiss()
                } label: {
                    Text("변경사항 저장")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color(hex: "30C6E8"),
                                    Color(hex: "2D9CFF")
                                ]),
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(12)
                        .shadow(color: Color(hex: "30C6E8").opacity(0.3), radius: 8, x: 0, y: 4)
                }
                .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
                .padding(.top, 24)
                .padding(.bottom, 32)
            }
        }
        .background(Color(hex: "F8FAFC")) // 전체 배경색을 연한 그레이로 설정
        .navigationTitle("회원정보수정")
        .navigationBarTitleDisplayMode(.inline)
        .edgesIgnoringSafeArea(.bottom)
        // 이메일 수정 시트
        .sheet(isPresented: $showEditEmail) {
            NavigationView {
                // 기존의 EmailEditView 사용하면서 모던 UI 적용
                EmailEditView(email: $editedEmail, onSave: { newEmail in
                    // UserData 업데이트 및 시트 닫기
                    userData.email = newEmail
                    showEditEmail = false
                })
                .background(Color(hex: "F8FAFC")) // 전체 배경색
            }
        }
        // 휴대폰 수정 시트
        .sheet(isPresented: $showEditPhone) {
            NavigationView {
                // 휴대폰 수정 뷰를 이곳에 구현하거나 별도 파일로 구현하세요
                Text("휴대폰 번호 수정")
                    .navigationTitle("휴대폰 수정")
                    .navigationBarTitleDisplayMode(.inline)
            }
        }
    }
    
    // MARK: - Profile Header Section
    
    private var profileHeaderSection: some View {
        VStack(spacing: 0) {
            // 그라디언트 배경
            ZStack {
                // 배경 그라디언트
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color(hex: "30C6E8"),
                        Color(hex: "32A1F1")
                    ]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .frame(height: 100)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                
                // 원 패턴 디코레이션
                Circle()
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 120, height: 120)
                    .offset(x: -140, y: -40)
                
                Circle()
                    .fill(Color.white.opacity(0.1))
                    .frame(width: 80, height: 80)
                    .offset(x: 140, y: 40)
                
                HStack {
                    Text("프로필 편집")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                }
                .padding(.horizontal, 20)
            }
            .padding(.horizontal, horizontalSizeClass == .compact ? 16 : 24)
            
            // 프로필 이미지 섹션
            HStack(alignment: .center, spacing: 0) {
                Spacer()
                
                VStack(spacing: 10) {
                    ZStack {
                        // 프로필 이미지 표시
                        if let profileImage = profileImage {
                            profileImage
                                .resizable()
                                .scaledToFill()
                                .frame(width: 100, height: 100)
                                .clipShape(Circle())
                                .overlay(
                                    Circle()
                                        .strokeBorder(Color.white, lineWidth: 3)
                                )
                                .shadow(color: Color.black.opacity(0.1), radius: 8)
                        } else {
                            // 기본 프로필 이미지
                            ZStack {
                                Circle()
                                    .fill(Color(hex: "F4F7FC"))
                                    .frame(width: 100, height: 100)
                                    .overlay(
                                        Circle()
                                            .strokeBorder(Color.white, lineWidth: 3)
                                    )
                                    .shadow(color: Color.black.opacity(0.1), radius: 8)
                                
                                Image(systemName: "person.fill")
                                    .font(.system(size: 40))
                                    .foregroundColor(Color(hex: "D9DDE3"))
                            }
                        }
                        
                        // 추가 버튼
                        PhotosPicker(
                            selection: $selectedItem,
                            matching: .images
                        ) {
                            ZStack {
                                Circle()
                                    .fill(Color(hex: "30C6E8"))
                                    .frame(width: 32, height: 32)
                                
                                Image(systemName: "camera.fill")
                                    .font(.system(size: 14))
                                    .foregroundColor(.white)
                            }
                            .shadow(color: Color(hex: "30C6E8").opacity(0.3), radius: 4)
                        }
                        .offset(x: 34, y: 34)
                    }
                    
                    Text(userData.userName)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(hex: "232D3F"))
                }
                .offset(y: -50)
                
                Spacer()
            }
        }
        .onChange(of: selectedItem) { oldValue, newValue in
            Task {
                if let data = try? await newValue?.loadTransferable(type: Data.self) {
                    selectedImageData = data
                    if let uiImage = UIImage(data: data) {
                        profileImage = Image(uiImage: uiImage)
                    }
                }
            }
        }
    }
    
    // MARK: - Helper Views

    /// 모던한 섹션 아이템 (아이콘과 함께)
    private func modernSectionItem(icon: String, iconColor: String, title: String, value: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 16) {
                // 아이콘
                ZStack {
                    Circle()
                        .fill(Color(hex: iconColor).opacity(0.15))
                        .frame(width: 40, height: 40)
                    
                    Image(systemName: icon)
                        .font(.system(size: 16))
                        .foregroundColor(Color(hex: iconColor))
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color(hex: "6E7491"))
                    
                    Text(value)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(hex: "232D3F"))
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "B0B7C3"))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.white)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(Color(hex: "E1E5EE"), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
        }
    }

    /// 모던한 아이콘이 있는 정보 항목 (필수정보)
    private func modernInfoItem(icon: String, iconColor: String, title: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .center, spacing: 14) {
                // 아이콘
                ZStack {
                    Circle()
                        .fill(Color(hex: iconColor).opacity(0.1))
                        .frame(width: 36, height: 36)
                    
                    Image(systemName: icon)
                        .font(.system(size: 14))
                        .foregroundColor(Color(hex: iconColor))
                }
                
                VStack(alignment: .leading, spacing: 5) {
                    Text(title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(Color(hex: "6E7491"))

                    Text(value.isEmpty ? "-" : value)
                        .font(.system(size: 16))
                        .foregroundColor(Color(hex: "232D3F"))
                        .fixedSize(horizontal: false, vertical: true)
                }
                
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            
            Divider()
                .padding(.leading, 66)
                .padding(.trailing, 16)
        }
        .background(Color.white.opacity(0.01)) // 터치 감지를 위한 공간 추가
    }
}

// MARK: - Preview

struct ProfileEditView_Previews: PreviewProvider {
    static var previews: some View {
        // Preview를 위해 임시 UserData를 생성하거나, 실제 UserData를 사용
        // UserData 싱글톤이 잘 동작한다면 아래 코드로 충분합니다.
        NavigationView {
            ProfileEditView()
        }
    }
}

// Color(hex:) 확장은 ColorExtensions.swift에 이미 정의되어 있습니다.
