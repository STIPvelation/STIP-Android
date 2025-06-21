//
//  WebtoonView.swift
//  STIP
//
//  Created on 6/10/25.
//

import SwiftUI

struct WebtoonItem: Identifiable {
    let id = UUID()
    let title: String
    let author: String
    let coverImage: String
    let rating: Double
    let categories: [String]
    let isNew: Bool
    let isUpdated: Bool
}

public struct IPWebtoonView: View {
    @State private var searchText = ""
    @State private var selectedCategory: String? = nil
    @State private var activeTab = 0
    
    private let categories = ["전체", "액션", "판타지", "로맨스", "코미디", "드라마", "스릴러"]
    
    // Sample data
    private let webtoons = [
        WebtoonItem(title: "IP툰1", author: "현도", coverImage: "webtoon1", rating: 4.8, categories: ["코미디"], isNew: false, isUpdated: true),
        WebtoonItem(title: "IP툰2", author: "현도", coverImage: "webtoon2", rating: 4.9, categories: ["판타지", "액션"], isNew: false, isUpdated: false),
        WebtoonItem(title: "IP툰3", author: "현도", coverImage: "webtoon3", rating: 4.7, categories: ["로맨스", "코미디"], isNew: false, isUpdated: true),
        WebtoonItem(title: "IP툰4", author: "현도", coverImage: "webtoon4", rating: 4.6, categories: ["액션", "판타지"], isNew: true, isUpdated: false),
        WebtoonItem(title: "IP툰5", author: "현도", coverImage: "webtoon5", rating: 4.8, categories: ["드라마"], isNew: false, isUpdated: true),
        WebtoonItem(title: "IP툰6", author: "현도", coverImage: "webtoon6", rating: 4.5, categories: ["액션"], isNew: false, isUpdated: false),
        WebtoonItem(title: "IP툰7", author: "현도", coverImage: "webtoon7", rating: 4.7, categories: ["로맨스", "드라마"], isNew: false, isUpdated: true),
        WebtoonItem(title: "IP툰8", author: "현도", coverImage: "webtoon8", rating: 4.9, categories: ["드라마"], isNew: true, isUpdated: true)
    ]
    
    var filteredWebtoons: [WebtoonItem] {
        var filtered = webtoons
        
        if let category = selectedCategory, category != "전체" {
            filtered = filtered.filter { $0.categories.contains(category) }
        }
        
        if !searchText.isEmpty {
            filtered = filtered.filter { $0.title.contains(searchText) || $0.author.contains(searchText) }
        }
        
        return filtered
    }
    
    // --- body 부분을 아래와 같이 수정 ---
    public var body: some View {
        VStack(spacing: 0) {
            // 커스텀 헤더(ZStack)를 제거했습니다. 네이티브 네비게이션 바로 대체됩니다.
            
            // Search bar
            SearchBar(text: $searchText)
                .padding(.horizontal)
                .padding(.bottom)
            
            // Categories
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(categories, id: \.self) { category in
                        CategoryButton(
                            title: category,
                            isSelected: selectedCategory == category,
                            action: {
                                selectedCategory = category == selectedCategory ? nil : category
                            }
                        )
                    }
                }
                .padding(.horizontal)
            }
            .padding(.bottom)
            
            // Tab selector
            HStack(spacing: 0) {
                TabButton(title: "인기", isSelected: activeTab == 0) {
                    activeTab = 0
                }
                TabButton(title: "신작", isSelected: activeTab == 1) {
                    activeTab = 1
                }
                TabButton(title: "완결", isSelected: activeTab == 2) {
                    activeTab = 2
                }
            }
            .padding(.bottom)
            
            // Content
            ScrollView {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                    ForEach(filteredWebtoons) { webtoon in
                        WebtoonCard(webtoon: webtoon)
                    }
                }
                .padding()
            }
        }
        .background(Color(.systemBackground))
        // --- 아래 수정자들이 네이티브 네비게이션 바를 설정합니다 ---
        .navigationTitle("IP웹툰")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                Button(action: {}) {
                    Image(systemName: "magnifyingglass")
                }
                Button(action: {}) {
                    Image(systemName: "bell")
                }
            }
        }
    }
}

// --- 아래 헬퍼 뷰들은 수정할 필요가 없습니다 ---

struct SearchBar: View {
    @Binding var text: String
    
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            
            TextField("검색어를 입력하세요", text: $text)
                .foregroundColor(.primary)
            
            if !text.isEmpty {
                Button(action: {
                    text = ""
                }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                }
            }
        }
        .padding(8)
        .background(Color(.systemGray6))
        .cornerRadius(10)
    }
}

struct CategoryButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    // 1. 여기에 활성 색상을 정의합니다.
    private let activeColor = Color(red: 48/255, green: 198/255, blue: 232/255) // #30C6E8

    var body: some View {
        Button(action: action) {
            Text(title)
                .fontWeight(.medium)
                .padding(.vertical, 8)
                .padding(.horizontal, 16)
                // 2. 여기서 .blue를 activeColor로 변경합니다.
                .background(isSelected ? activeColor : Color(.systemGray6))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}

struct TabButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    // Define the custom blue color with hex #30C6E8
    private let activeColor = Color(red: 48/255, green: 198/255, blue: 232/255) // #30C6E8
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Text(title)
                    .font(.system(size: 16, weight: isSelected ? .bold : .regular))
                    .foregroundColor(isSelected ? activeColor : .gray)
                
                Rectangle()
                    .fill(isSelected ? activeColor : Color.clear)
                    .frame(height: 2)
            }
        }
        .frame(maxWidth: .infinity)
    }
}

struct WebtoonCard: View {
    let webtoon: WebtoonItem
    
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            ZStack(alignment: .bottomTrailing) {
                // We're using a rectangle as placeholder since we don't have actual images
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .aspectRatio(2/3, contentMode: .fit)
                    .cornerRadius(8)
                    .overlay(
                        Text(webtoon.coverImage)
                            .foregroundColor(.gray)
                    )
                
                // Status badges (NEW or UP)
                HStack(spacing: 4) {
                    if webtoon.isNew {
                        Text("NEW")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.vertical, 2)
                            .padding(.horizontal, 6)
                            .background(Color.orange)
                            .cornerRadius(4)
                    }
                    
                    if webtoon.isUpdated {
                        Text("UP")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.vertical, 2)
                            .padding(.horizontal, 6)
                            .background(Color.red)
                            .cornerRadius(4)
                    }
                }
                .padding(6)
            }
            
            Text(webtoon.title)
                .font(.system(size: 14, weight: .medium))
                .lineLimit(1)
            
            HStack {
                Text(webtoon.author)
                    .font(.system(size: 12))
                    .foregroundColor(.gray)
                
                Spacer()
                
                HStack(spacing: 2) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 10))
                        .foregroundColor(.yellow)
                    
                    Text(String(format: "%.1f", webtoon.rating))
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                }
            }
        }
        .padding(8)
        .background(Color(.systemBackground))
        .cornerRadius(10)
        .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
    }
}

#Preview {
    // Preview를 제대로 보려면 NavigationView 안에서 봐야 합니다.
    NavigationView {
        IPWebtoonView()
    }
}
