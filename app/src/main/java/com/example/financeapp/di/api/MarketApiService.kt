package com.example.financeapp.di.api

import com.example.financeapp.data.market.ChartHistoryResponse
import com.example.financeapp.data.market.SsiBoardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API biểu đồ công khai của VNDirect. Base: https://dchart-api.vndirect.com.vn/
 * Không cần API key. Trả OHLCV theo nghìn đồng.
 */
interface VnDirectChartService {

    /**
     * @param symbol mã cổ phiếu hoặc chỉ số ("FPT", "VNINDEX")
     * @param resolution "D" = ngày, "15" = 15 phút
     * @param from/to epoch seconds
     */
    @GET("dchart/history")
    suspend fun getHistory(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String = "D",
        @Query("from") from: Long,
        @Query("to") to: Long
    ): Response<ChartHistoryResponse>
}

/**
 * API bảng giá công khai của SSI. Base: https://iboard-query.ssi.com.vn/
 * Trả snapshot toàn sàn trong 1 request (giá theo đồng).
 */
interface SsiBoardService {

    /** @param exchange "hose", "hnx", "upcom" */
    @GET("stock/exchange/{exchange}")
    suspend fun getExchangeBoard(
        @Path("exchange") exchange: String
    ): Response<SsiBoardResponse>
}
