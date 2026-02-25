package com.example.financeapp.di.api

import com.example.financeapp.data.AnomaliesResponse
import com.example.financeapp.data.NarrativeResponse
import com.example.financeapp.data.NewsAllResponse
import com.example.financeapp.data.NewsBySectorResponse
import com.example.financeapp.data.NewsBySymbolResponse
import com.example.financeapp.data.NewsResponse
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

    /**
     * Lấy tin tức theo ngày
     * @param targetDate Format: "yyyy-MM-dd" (VD: "2026-02-18")
     * Nếu không truyền targetDate, API sẽ trả về ngày gần nhất
     */
    @GET("news")
    suspend fun getNews(
        @Query("target_date") targetDate: String? = null
    ): Response<NewsResponse>

    /**
     * ✅ NEW: Lấy tất cả tin tức với pagination
     * @param limit Số lượng tin tức trả về (max 100)
     * @param offset Vị trí bắt đầu
     */
    @GET("news/all")
    suspend fun getAllNews(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<NewsAllResponse>

    /**
     * ✅ NEW: Lấy tin tức theo sector
     * @param sector Tên sector (VD: "Ngân hàng", "Công nghệ")
     * Cần encode UTF-8: "Ngân hàng" -> "Ng%C3%A2n%20h%C3%A0ng"
     */
    @GET("news/by-sector/{sector}")
    suspend fun getNewsBySector(
        @Path("sector") sector: String // Bỏ encoded = true
    ): Response<NewsBySectorResponse>

    /**
     * Lấy tin tức theo mã cổ phiếu
     * @param symbol Mã cổ phiếu (VD: "BSR", "FPT")
     */
    @GET("news/by-stock/{symbol}")
    suspend fun getNewsBySymbol(
        @Path("symbol") symbol: String
    ): Response<NewsBySymbolResponse>
}