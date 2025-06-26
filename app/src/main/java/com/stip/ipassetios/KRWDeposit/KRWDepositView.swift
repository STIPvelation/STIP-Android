import SwiftUI

// MARK: - KRW Deposit View
struct KRWDepositView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var copiedAccountInfo: String? = nil
    
    var body: some View {
        VStack(spacing: 0) {
            // Bank Account Info Sections
            ScrollView {
                VStack(spacing: 0) {
                    // Bank Account 1: 국민은행
                    bankAccountSection(
                        bank: "국민은행",
                        accountNumber: "102701-04-435574"
                    )
                    
                    // Bank Account 2: 신한은행
                    bankAccountSection(
                        bank: "신한은행", 
                        accountNumber: "140-015-070902"
                    )
                    
                    // Bank Account 3: 우리은행
                    bankAccountSection(
                        bank: "우리은행",
                        accountNumber: "1005-804-753434"
                    )
                    
                    // Notice Section
                    VStack(alignment: .center, spacing: 8) {
                        Text("[입금 안내]")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color(hex: "30C6E8"))
                            .padding(.top, 30)
                        
                        Text("위에 보이는 지정된 계좌로 입금해 주세요.\n입금이 확인된 후, 본인 계좌에 반영됩니다.")
                            .font(.system(size: 15))
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                            .padding(24)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color(hex: "30C6E8").opacity(0.08))
                            )
                            .padding(.horizontal, 16)
                            .padding(.top, 8)
                            .padding(.bottom, 40)
                    }
                }
            }
        }
        .navigationBarTitle("KRW 입금하기", displayMode: .inline)
        .navigationBarBackButtonHidden(true)
        .navigationBarItems(leading: Button(action: {
            presentationMode.wrappedValue.dismiss()
        }) {
            Image(systemName: "chevron.left")
                .foregroundColor(.black)
        })
    }
    
    private func bankAccountSection(bank: String, accountNumber: String) -> some View {
        ZStack {
            VStack(spacing: 0) {
                HStack(spacing: 16) {
                    // Bank Icon
                    ZStack {
                        Circle()
                            .fill(Color(hex: "30C6E8").opacity(0.15))
                            .frame(width: 48, height: 48)
                        
                        // Use bank-specific icons based on bank name
                        if bank.contains("국민") || bank.contains("KB") {
                            Image("KBBank")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 48, height: 48)
                        } else if bank.contains("신한") || bank.contains("SH") {
                            Image("SHBank")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 48, height: 48)
                        } else if bank.contains("우리") || bank.contains("WR") {
                            Image("WRBank")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 48, height: 48)
                        } else {
                            Image(systemName: "building.columns.fill")
                                .font(.system(size: 20))
                                .foregroundColor(Color(hex: "30C6E8"))
                        }
                    }
                    .padding(.leading, 20)
                    
                    // Account Info
                    VStack(alignment: .leading, spacing: 6) {
                        // Bank Name
                        Text(bank)
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(Color(hex: "30C6E8"))
                        
                        // Account Details
                        HStack(spacing: 12) {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("예금주")
                                    .font(.system(size: 13))
                                    .foregroundColor(.gray)
                                
                                Text("계좌번호")
                                    .font(.system(size: 13))
                                    .foregroundColor(.gray)
                            }
                            
                            VStack(alignment: .leading, spacing: 8) {
                                Text("주식회사 아이피디어그룹")
                                    .font(.system(size: 14, weight: .medium))
                                
                                Text(accountNumber)
                                    .font(.system(size: 14, weight: .medium))
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Copy Button
                    Button(action: {
                        // Copy account number to clipboard
                        UIPasteboard.general.string = accountNumber
                        
                        // Show copy success indicator for this specific bank
                        withAnimation {
                            copiedAccountInfo = bank
                        }
                        
                        // Hide after 2 seconds
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            withAnimation {
                                if copiedAccountInfo == bank {
                                    copiedAccountInfo = nil
                                }
                            }
                        }
                    }) {
                        Image(systemName: "doc.on.doc")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.white)
                            .padding(8)
                            .background(Color(hex: "30C6E8"))
                            .cornerRadius(8)
                    }
                    .padding(.trailing, 20)
                }
                .padding(.vertical, 20)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color.white)
                        .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: 2)
                )
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                
                // Copy success toast notification specific to this bank
                if copiedAccountInfo == bank {
                    Text("계좌번호가 복사되었습니다")
                        .font(.system(size: 14, weight: .medium))
                        .padding(.vertical, 10)
                        .padding(.horizontal, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.black.opacity(0.7))
                        )
                        .foregroundColor(.white)
                        .transition(.opacity)
                        .padding(.vertical, 10)
                }
            }
        }
    }
}

// Using the existing Color extension from ColorExtension.swift

// Preview
struct KRWDepositView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            KRWDepositView()
        }
    }
}
