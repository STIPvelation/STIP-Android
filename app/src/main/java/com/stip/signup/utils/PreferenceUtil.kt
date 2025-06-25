package com.stip.stip.signup.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.lang.reflect.Type
import com.google.gson.reflect.TypeToken

// TODO: 전역 변수로 유저 정보 관리
// PreferenceUtil 객체에서 SharedPreferences에 저장 및 조회
object PreferenceUtil {

    // 전역 상수 선언
    private const val KEY_MEMBER_INFO = "member_info_json"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_MEMBER_DI = "member_di"

    /**
     * 사용자 정보 업데이트 되면 localStrorage 저장하듯
     * SharedPreferences에 저장
     */
    // TODO: 유저 정보 관리
    fun saveMemberInfo(info: com.stip.stip.model.MemberInfo) {
        val json = Gson().toJson(info)
        preferences.edit().putString(KEY_MEMBER_INFO, json).apply()
    }

    // TODO: 유저 정보 관리
    fun getMemberInfo(): com.stip.stip.model.MemberInfo? {
        val json = preferences.getString(KEY_MEMBER_INFO, null)
        return if (json.isNullOrEmpty()) null else Gson().fromJson(json, com.stip.stip.model.MemberInfo::class.java)
    }

    // TODO: 토큰 관리
    fun saveToken(token: String) {
        preferences.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    // TODO: 토큰 관리
    fun getToken(): String? {
        return preferences.getString(KEY_JWT_TOKEN, null)
    }

    // DI 값 저장
    fun saveMemberDi(di: String) {
        preferences.edit().putString(KEY_MEMBER_DI, di).apply()
    }

    // DI 값 조회
    fun getMemberDi(): String? {
        return preferences.getString(KEY_MEMBER_DI, null)
    }


    private const val PREF_KEY_NAME = "STIP"
    private const val FIRST_PREF_KEY_NAME = "FirstRun"

    private lateinit var preferences: SharedPreferences
    private lateinit var firstPreferences: SharedPreferences

    // 초기화 메서드
    fun init(context: Context){
        preferences = context.getSharedPreferences(PREF_KEY_NAME, Context.MODE_PRIVATE)
        firstPreferences = context.getSharedPreferences(FIRST_PREF_KEY_NAME, Context.MODE_PRIVATE)
    }

    // 여기부터 전역 데이터 접근하는 메서드들
    /** 최초 설치시 나와야 하는 값들만 추가 */
    fun putFirstPutBoolean(key: String, value: Boolean) {
        val editor = firstPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getFirstBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return firstPreferences.getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    fun putString(key: String, value: String){
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return preferences.getString(key, defaultValue) ?: ""
    }

    fun putInt(key: String, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return preferences.getInt(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return preferences.getLong(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return preferences.getFloat(key, defaultValue)
    }

    /* Data Class 형태
    fun putType(key: String, value: ) {
        val gson = Gson()
        val typeToJson = gson.toJson(value)
        val editor = preferences.edit()
        editor.putString(key, typeToJson)
        editor.apply()
    }

    fun getType(key: String): ? {
        val gson = Gson()
        val json: String? = preferences.getString(key, "")
        return gson.fromJson(json, ::class.java)
    }
     */

    /** 기존 내부 저장소 */
    fun clear() {
        val editor: SharedPreferences.Editor = this.preferences.edit()
        editor.clear()
        editor.apply()
    }

    fun remove(key: String) {
        val editor = preferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun <T> putList(key:String, list: List<T>?) {
        val editor = preferences.edit()
        try {
            val gson = Gson()
            val json = gson.toJson(list)
            editor.putString(key, json)
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> getList(key: String, defaultValue: String? = null): List<T>? {
        val gson = Gson()
        val json: String? = preferences.getString(key, defaultValue)
        val type: Type = object : TypeToken<List<T>?>() {}.type
        return gson.fromJson(json, type)
    }
}