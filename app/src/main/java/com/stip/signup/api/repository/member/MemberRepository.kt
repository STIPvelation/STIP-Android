package com.stip.stip.signup.api.repository.member

import com.skydoves.sandwich.ApiResponse
import com.stip.stip.signup.model.MemberData
import com.stip.stip.signup.model.RequestPinNumber
import com.stip.stip.signup.model.RequestSignUpMember
import com.stip.stip.signup.model.ResponseExistMember
import com.stip.stip.signup.model.ResponseSignUpMember
import retrofit2.http.Body

interface MemberRepository {

    /**
     * (4) 회원 가입
     */
    suspend fun postSignUpMembers(
        @Body requestSignUpMember: RequestSignUpMember
    ): ApiResponse<ResponseSignUpMember>

    /**
     * 로그인 화면 PIN 번호 수정
     */
    suspend fun patchMemberPin(
        di: String,
        requestPinNumber: RequestPinNumber
    ): ApiResponse<Unit>

    /**
     * PIN 번호 확인
     */
    suspend fun verifyMemberPin(
        di: String,
        requestPinNumber: RequestPinNumber
    ): ApiResponse<Unit>

    /**
     * 회원 조회
     */
    suspend fun getMembers(
    ): ApiResponse<MemberData>


    /**
     * 로그인 화면 회원 존재 여부
     */
    suspend fun getExistenceMember(
        di: String
    ): ApiResponse<ResponseExistMember>


    /**
     * 로그인 화면 회원 삭제
     */
    suspend fun deleteMembers(
        di: String
    ): ApiResponse<Unit>


    /**
     * 회원 탈퇴
     */
    suspend fun deleteMembers(
    ): ApiResponse<Unit>

}