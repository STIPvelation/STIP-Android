package com.stip.ipasset.ticker.repository

import android.content.Context
import com.stip.ipasset.model.IpAsset

class IpAssetRepository private constructor(private val context: Context) {

    companion object {
        private var instance: IpAssetRepository? = null
        private var testData: List<IpAsset> = emptyList()
        
        fun getInstance(context: Context): IpAssetRepository {
            if (instance == null) {
                instance = IpAssetRepository(context)
            }
            return instance!!
        }
        
        fun setTestData(data: List<IpAsset>) {
            testData = data
        }
    }
    
    fun getIpAssets(): List<IpAsset> {
        return testData
    }
    
    fun getAsset(id: String): IpAsset? {
        return testData.find { it.id == id }
    }
    
    fun updateAsset(id: String, newBalance: Double): IpAsset? {
        val existingIndex = testData.indexOfFirst { it.id == id }
        if (existingIndex != -1) {
            val existing = testData[existingIndex]
            val updated = existing.copy(balance = newBalance, amount = newBalance)
            val mutableList = testData.toMutableList()
            mutableList[existingIndex] = updated
            testData = mutableList
            return updated
        }
        return null
    }
}
