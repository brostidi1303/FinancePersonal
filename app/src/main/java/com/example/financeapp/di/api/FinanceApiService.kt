package com.example.financeapp.di.api

import com.example.financeapp.data.AnomaliesResponse
import com.example.financeapp.data.NarrativeResponse
import com.example.financeapp.data.RegimeHistoryResponse
import com.example.financeapp.data.RegimeResponse
import retrofit2.Response
import retrofit2.http.*

interface FinanceApiService {
    //
    @GET("regime")
    suspend fun getMarketRegime(): Response<RegimeResponse>

    // ✅ NEW: Endpoint để lấy lịch sử thị trường max là 90 ngày
    @GET("regime/history")
    suspend fun getMarketRegimeHistory(
        @Query("days") days: Int = 30
    ): Response<RegimeHistoryResponse>

    /**
     * Lấy anomalies theo ngày
     * @param targetDate Format: "yyyy-MM-dd" (VD: "2026-02-13")
     * Nếu không truyền targetDate, API sẽ trả về ngày gần nhất
     */
    @GET("anomalies")
    suspend fun getAnomalies(
        @Query("target_date") targetDate: String? = null
    ): Response<AnomaliesResponse>

    @GET("narrative")
    suspend fun getNarrative(
        @Query("target_date") targetDate: String? = null
    ): Response<NarrativeResponse>
}