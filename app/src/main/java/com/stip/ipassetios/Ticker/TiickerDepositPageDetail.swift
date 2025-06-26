import SwiftUI
import Foundation

// 버튼 터치 효과를 위한 커스텀 버튼 스타일
struct DepositButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.96 : 1)
            .opacity(configuration.isPressed ? 0.9 : 1)
            .animation(.easeInOut(duration: 0.2), value: configuration.isPressed)
    }
}

struct TiickerDepositPageDetail: View {
    let transaction: Transaction
    @Environment(\.presentationMode) var presentationMode
    @State private var showCopiedToast = false
    
    // Mock address for demo - would be replaced with actual wallet address from API
    let mockDepositAddress = "3423jdkfjcsdfisdifdfdfsdg4"
    
    // 통화 표시를 위한 포맷터
    private let numberFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()
    
    // 정보 항목 표시를 위한 헬퍼 함수
    private func infoRow(icon: String, iconColor: Color, text: String) -> some View {
        HStack(alignment: .top, spacing: 14) {
            // 아이콘을 둘러싼 디자인
            ZStack {
                Circle()
                    .fill(iconColor.opacity(0.12))
                    .frame(width: 32, height: 32)
                
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(iconColor)
            }
            
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(Color.hex("666B73"))
                .lineSpacing(4)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    var body: some View {
        ZStack {
            // 배경 그라디언트
            LinearGradient(gradient: Gradient(colors: [Color.white, Color.hex("FAFBFF")]),
                           startPoint: .top, endPoint: .bottom)
            .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 0) {
                    // Header
                    ZStack {
                        Text("\(transaction.type) 입금하기")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(Color.hex("252A31"))
                            .frame(maxWidth: .infinity)
                        
                        HStack {
                            Button(action: {
                                presentationMode.wrappedValue.dismiss()
                            }) {
                                ZStack {
                                    Circle()
                                        .fill(Color.hex("F5F7FA"))
                                        .frame(width: 36, height: 36)
                                        .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
                                    
                                    Image(systemName: "chevron.left")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(Color.hex("252A31"))
                                }
                            }
                            .buttonStyle(DepositButtonStyle())
                            Spacer()
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 16)
                    .padding(.bottom, 16)
                    
                    // QR Code 섹션
                    VStack(spacing: 30) {
                        ZStack {
                            // 프리미엄 QR 코드 디자인
                            RoundedRectangle(cornerRadius: 28)
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [Color.white, Color.hex("FAFCFF")]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 220, height: 220)
                                .shadow(color: Color.black.opacity(0.08), radius: 16, x: 0, y: 6)
                            
                            // 테두리 효과
                            RoundedRectangle(cornerRadius: 28)
                                .stroke(
                                    LinearGradient(
                                        gradient: Gradient(colors: [
                                            Color.white.opacity(0.8),
                                            Color.hex("30C6E8").opacity(0.3)
                                        ]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    ),
                                    lineWidth: 1.5
                                )
                                .frame(width: 220, height: 220)
                            
                            // QR 코드 배경
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color.white)
                                .frame(width: 180, height: 180)
                                .shadow(color: Color.black.opacity(0.03), radius: 4, x: 0, y: 2)
                            
                            // QR 코드
                            Image(systemName: "qrcode")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 170, height: 170)
                                .foregroundColor(Color.hex("252A31"))
                            
                            // 로고 오버레이
                            ZStack {
                                Circle()
                                    .fill(Color.white)
                                    .frame(width: 48, height: 48)
                                    .shadow(color: Color.black.opacity(0.1), radius: 3, x: 0, y: 1)
                                
                                Text(transaction.type)
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.hex("30C6E8"))
                            }
                        }
                        
                        
                        // 입금주소 레이블
                        HStack(spacing: 8) {
                            Image(systemName: "link")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(Color.hex("30C6E8"))
                            
                            Text("입금주소")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(Color.hex("666B73"))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 24)
                        .padding(.top, 24)
                        
                        // 프리미엄 주소 표시 디자인
                        ZStack {
                            // 배경 디자인
                            RoundedRectangle(cornerRadius: 16)
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [Color.hex("F8F9FC"), Color.hex("F3F6FA")]),
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16)
                                        .stroke(
                                            LinearGradient(
                                                gradient: Gradient(colors: [
                                                    Color.white,
                                                    Color.hex("30C6E8").opacity(0.2)
                                                ]),
                                                startPoint: .topLeading,
                                                endPoint: .bottomTrailing
                                            ),
                                            lineWidth: 1
                                        )
                                )
                                .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 3)
                            
                            HStack(spacing: 12) {
                                // 주소
                                Text(mockDepositAddress)
                                    .font(.system(size: 15, weight: .medium))
                                    .foregroundColor(Color.hex("252A31"))
                                    .lineLimit(1)
                                    .truncationMode(.middle)
                                
                                Spacer()
                                
                                // 내장 복사 버튼
                                Button(action: {
                                    UIPasteboard.general.string = mockDepositAddress
                                    
                                    // 택틸 피드백 추가
                                    let generator = UIImpactFeedbackGenerator(style: .medium)
                                    generator.impactOccurred()
                                    
                                    withAnimation(.spring()) {
                                        showCopiedToast = true
                                        
                                        // 2초 후 토스트 메시지 자동 숨김
                                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                            withAnimation(.easeOut(duration: 0.5)) {
                                                showCopiedToast = false
                                            }
                                        }
                                    }
                                }) {
                                    Image(systemName: "doc.on.doc")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(Color.hex("30C6E8"))
                                        .frame(width: 36, height: 36)
                                        .background(Circle().fill(Color.white))
                                }
                            }
                            .padding(.vertical, 18)
                            .padding(.horizontal, 20)
                        }
                        .frame(height: 72)
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 24)
                        .padding(.top, 12)
                        
                        // 색상 그라디언트가 적용된 프리미엄 복사 버튼
                        Button(action: {
                            UIPasteboard.general.string = mockDepositAddress
                            
                            // 택틸 피드백 추가
                            let generator = UIImpactFeedbackGenerator(style: .medium)
                            generator.impactOccurred()
                            
                            withAnimation(.spring()) {
                                showCopiedToast = true
                                
                                // 2초 후 토스트 메시지 자동 숨김
                                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                    withAnimation(.easeOut(duration: 0.5)) {
                                        showCopiedToast = false
                                    }
                                }
                            }
                        }) {
                            HStack(spacing: 10) {
                                Image(systemName: "doc.on.doc")
                                    .font(.system(size: 16))
                                Text("복사")
                                    .font(.system(size: 16, weight: .semibold))
                            }
                            .foregroundColor(.white)
                            .frame(width: 140)
                            .frame(height: 50)
                            .background(
                                LinearGradient(
                                    gradient: Gradient(colors: [Color.hex("30C6E8"), Color.hex("30C6E8").opacity(0.8)]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 25))
                            .shadow(color: Color.hex("30C6E8").opacity(0.3), radius: 8, x: 0, y: 4)
                        }
                        .buttonStyle(DepositButtonStyle())
                        .padding(.top, 20)
                        
                        
                        // 정보 섹션 - 프리미엄 카드 디자인
                        VStack(spacing: 0) {
                            // 제목 섹션
                            HStack(spacing: 10) {
                                ZStack {
                                    Circle()
                                        .fill(Color.hex("30C6E8").opacity(0.12))
                                        .frame(width: 32, height: 32)
                                    
                                    Image(systemName: "exclamationmark.circle")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(Color.hex("30C6E8"))
                                }
                                
                                Text("입금 전 꼭 알아두세요!")
                                    .font(.system(size: 17, weight: .bold))
                                    .foregroundColor(Color.hex("252A31"))
                                
                                Spacer()
                            }
                            .padding(.horizontal, 24)
                            .padding(.top, 24)
                            .padding(.bottom, 20)
                            
                            // 구분선
                            Rectangle()
                                .fill(
                                    LinearGradient(
                                        gradient: Gradient(colors: [Color.hex("30C6E8").opacity(0.3), Color.hex("30C6E8").opacity(0.05)]),
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                                )
                                .frame(height: 1)
                                .padding(.horizontal, 24)
                            
                            // 정보 내용
                            VStack(spacing: 18) {
                                // 첫 번째 정보 항목
                                infoRow(icon: "checkmark.circle", iconColor: Color.hex("30C6E8"),
                                        text: "해당 주소는 \(transaction.type) 전용 입금 주소입니다. 다른 디지털 IP 입금 시 오류 또는 손실이 발생할 수 있으며 복구가 불가능합니다.")
                                
                                // 두 번째 정보 항목
                                infoRow(icon: "shield.checkerboard", iconColor: Color.hex("30C6E8"),
                                        text: "당사의 이상거래 검토 절차에 따라 자금 출처 확인을 위한 서류 제출이 요구될 수 있으며, 이에 따라 입금이 지연될 수 있습니다.")
                                
                                // 세 번째 정보 항목
                                infoRow(icon: "info.circle", iconColor: Color.hex("30C6E8"),
                                        text: "서류 검토 결과에 따라 입금이 승인되지 않을 수도 있으며, 반송 시 출금 수수료가 부과될 수 있습니다.")
                                
                                // 중요 항목 강조
                                HStack(spacing: 10) {
                                    Image(systemName: "exclamationmark.triangle")
                                        .font(.system(size: 15, weight: .medium))
                                        .foregroundColor(Color.hex("FF9500"))
                                    
                                    Text("본 주소는 입금 전용입니다.")
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(Color.hex("FF9500"))
                                    
                                    Spacer()
                                }
                                .padding(.top, 4)
                            }
                            .padding(.horizontal, 24)
                            .padding(.top, 16)
                            .padding(.bottom, 24)
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 20)
                                .fill(Color.white)
                                .shadow(color: Color.black.opacity(0.08), radius: 12, x: 0, y: 4)
                        )
                        .padding(.horizontal, 24)
                        .padding(.top, 30)
                        .padding(.bottom, 24)
                    }
                    .padding(.bottom, 16)
                }
                .edgesIgnoringSafeArea(.bottom)
            }
            .overlay(
                // 토스트 알림
                showCopiedToast ?
                VStack {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(Color.hex("30C6E8"))
                        
                        Text("주소가 복사되었습니다")
                            .font(.system(size: 15, weight: .semibold))
                    }
                    .foregroundColor(.white)
                    .padding(.vertical, 12)
                    .padding(.horizontal, 20)
                    .background(
                        Capsule()
                            .fill(Color.hex("252A31").opacity(0.9))
                            .shadow(color: Color.black.opacity(0.15), radius: 10, x: 0, y: 5)
                    )
                }
                .transition(.move(edge: .top).combined(with: .opacity))
                .animation(.spring(response: 0.3, dampingFraction: 0.7), value: showCopiedToast)
                .padding(.top, 60)
                : nil
            )
            .navigationBarHidden(true)
        }
    }
    
    // Transaction struct is now defined in TransactionModels.swift
    
    // Preview
    struct TiickerDepositPageDetail_Previews: PreviewProvider {
        static var previews: some View {
            TiickerDepositPageDetail(transaction: Transaction(
                type: "KATV",
                amount: 100,
                priceUSD: 0.5,
                tokenPrice: 0.1,
                totalUSD: 10.0,
                contractAddress: "0x123...",
                category: TransactionCategory.usd))
        }
    }
}
