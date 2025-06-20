package com.stip.stip.signup.api.repository.member

import com.skydoves.sandwich.ApiResponse
import com.stip.stip.signup.api.MemberService
import com.stip.stip.signup.model.MemberData
import com.stip.stip.signup.model.RequestSignUpMember
import com.stip.stip.signup.model.RequestPinNumber
import com.stip.stip.signup.model.ResponseExistMember
import com.stip.stip.signup.model.ResponseSignUpMember
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val memberService: MemberService
): MemberRepository {
    override suspend fun postSignUpMembers(requestSignUpMember: RequestSignUpMember): ApiResponse<ResponseSignUpMember> {
        return memberService.postSignUpMembers(requestSignUpMember)
    }

    override suspend fun patchMemberPin(di: String, requestPinNumber: RequestPinNumber): ApiResponse<Unit> {
        return memberService.patchMemberPin(di, requestPinNumber)
    }

    override suspend fun verifyMemberPin(di: String, requestPinNumber: RequestPinNumber): ApiResponse<Unit> {
        return memberService.verifyMemberPin(di, requestPinNumber)
    }

    override suspend fun getMembers(): ApiResponse<MemberData> {
        return memberService.getMembers()
    }

    override suspend fun getExistenceMember(di: String): ApiResponse<ResponseExistMember> {
        return memberService.getExistenceMember(di)
    }

    override suspend fun deleteMembers(di: String): ApiResponse<Unit> {
        return memberService.deleteMembers(di)
    }

    override suspend fun deleteMembers(): ApiResponse<Unit> {
        return memberService.deleteMembers()
    }
}