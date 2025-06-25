package com.stip.stip.signup.di

import com.stip.stip.signup.api.AuthService
import com.stip.stip.signup.api.KakaoLocationService
import com.stip.stip.signup.api.MemberService
import com.stip.stip.signup.api.repository.auth.AuthRepository
import com.stip.stip.signup.api.repository.auth.AuthRepositoryImpl
import com.stip.stip.signup.api.repository.kakao.KakaoLocationRepository
import com.stip.stip.signup.api.repository.kakao.KakaoLocationRepositoryImpl
import com.stip.stip.signup.api.repository.member.MemberRepository
import com.stip.stip.signup.api.repository.member.MemberRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideAuthRepository(
        authService: AuthService
    ): AuthRepository = AuthRepositoryImpl(authService)

    @Provides
    fun provideMemberRepository(
        memberService: MemberService
    ): MemberRepository = MemberRepositoryImpl(memberService)

    @Provides
    fun provideKakoLocationRepository(
        kakaoLocationService: KakaoLocationService
    ): KakaoLocationRepository = KakaoLocationRepositoryImpl(kakaoLocationService)

}