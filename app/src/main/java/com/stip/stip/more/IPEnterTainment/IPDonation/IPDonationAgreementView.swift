import SwiftUI

struct IPDonationAgreementView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var isAgreed = false
    @State private var showingConfirmation = false
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                // 상단 안내 텍스트
                Text("IP 기부 동의서")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.deepNavy)
                    .padding(.bottom, 4)
                
                Text("아래 내용을 확인하시고 동의하시면 IP 기부를 진행할 수 있습니다.")
                    .font(.system(size: 16))
                    .foregroundColor(.darkGray)
                    .padding(.bottom, 16)
                
                // 동의서 콘텐츠
                AgreementContentView()
                
                // 동의 체크박스
                Button(action: {
                    isAgreed.toggle()
                }) {
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: isAgreed ? "checkmark.square.fill" : "square")
                            .foregroundColor(isAgreed ? .mainAccentColor : .mediumGray)
                            .font(.system(size: 22))
                        
                        Text("위 내용에 동의하며, IP 기부를 진행하겠습니다.")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.deepNavy)
                            .multilineTextAlignment(.leading)
                    }
                }
                .padding(.vertical, 8)
                
                Spacer(minLength: 40)
                
                // 확인 버튼
                Button(action: {
                    if isAgreed {
                        showingConfirmation = true
                    }
                }) {
                    Text("동의하고 기부 신청하기")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(isAgreed ? Color.mainAccentColor : Color.mediumGray)
                        .cornerRadius(14)
                }
                .disabled(!isAgreed)
            }
            .padding(20)
        }
        .navigationTitle("IP 기부 동의서")
        .navigationBarTitleDisplayMode(.inline)
        .alert(isPresented: $showingConfirmation) {
            Alert(
                title: Text("IP 기부 신청 완료"),
                message: Text("IP 기부 신청이 완료되었습니다.\n기부 진행 상황은 마이페이지에서 확인하실 수 있습니다."),
                dismissButton: .default(Text("확인")) {
                    // 확인 버튼을 누르면 이전 화면으로 돌아갑니다.
                    presentationMode.wrappedValue.dismiss()
                }
            )
        }
    }
}

struct AgreementContentView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            Text("IP 기부 동의 내용")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.deepNavy)
            
            // 동의서 내용 섹션
            AgreementSection(
                title: "1. IP 기부의 의미",
                content: "IP(지식재산권) 기부는 소유하고 계신 특허, 상표, 디자인, 저작권 등의 지식재산권을 STIP에 기부하여 사회적 가치 창출에 기여하는 것을 의미합니다. 기부하신 IP는 STIP의 디지털 IP 거래 플랫폼에서 가치를 창출하고, 이를 통해 발생하는 수익은 사회공헌 활동에 사용됩니다."
            )
            
            AgreementSection(
                title: "2. 권리 위임 범위",
                content: "IP 기부시 소유권은 기부자에게 그대로 유지되며, STIP에는 해당 IP의 사용, 복제, 배포, 라이선싱 등의 권한이 위임됩니다. STIP는 이 권한을 바탕으로 기부된 IP의 가치를 극대화하기 위한 다양한 활동을 진행합니다."
            )
            
            AgreementSection(
                title: "3. 기부자 권리",
                content: "기부자는 IP 기부자 커뮤니티의 일원으로서 기부 IP의 활용 방향에 대한 의견을 제시할 수 있으며, 기부를 통해 발생한 사회적 가치 창출 활동에 참여할 수 있는 권리를 가집니다. 또한 기부 철회가 필요한 경우 관련 절차에 따라 IP 기부를 철회할 수 있습니다."
            )
            
            AgreementSection(
                title: "4. 기부 효력",
                content: "본 동의서에 동의하고 '동의하고 기부 신청하기' 버튼을 클릭하면 IP 기부 신청이 완료되며, 검토 과정을 거쳐 최종 기부가 확정됩니다. 기부 확정 후에는 STIP에서 제공하는 기부 확인서가 발급되며, 이를 통해 기부 사실을 증명할 수 있습니다."
            )
        }
        .padding(16)
        .background(Color.softGray)
        .cornerRadius(12)
    }
}

struct AgreementSection: View {
    let title: String
    let content: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.deepNavy)
            
            Text(content)
                .font(.system(size: 15))
                .foregroundColor(.darkGray)
                .lineSpacing(4)
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}

struct IPDonationAgreementView_Previews: PreviewProvider {
    static var previews: some View {
        IPDonationAgreementView()
    }
}
