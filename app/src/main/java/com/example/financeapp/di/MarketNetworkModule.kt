package com.example.financeapp.di

import com.example.financeapp.di.api.SsiBoardService
import com.example.financeapp.di.api.VnDirectChartService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class VnDirectRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class SsiRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MarketOkHttp

/**
 * Network cho 2 API thị trường công khai.
 *
 * Dùng OkHttpClient RIÊNG, không gắn AuthInterceptor của backend vnvibe —
 * gửi token nội bộ sang server bên thứ ba là thừa và không an toàn.
 */
@Module
@InstallIn(SingletonComponent::class)
object MarketNetworkModule {

    private const val VNDIRECT_BASE = "https://dchart-api.vndirect.com.vn/"
    private const val SSI_BASE = "https://iboard-query.ssi.com.vn/"

    @Provides
    @Singleton
    @MarketOkHttp
    fun provideMarketOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    @VnDirectRetrofit
    fun provideVnDirectRetrofit(@MarketOkHttp client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(VNDIRECT_BASE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @SsiRetrofit
    fun provideSsiRetrofit(@MarketOkHttp client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(SSI_BASE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideVnDirectChartService(@VnDirectRetrofit retrofit: Retrofit): VnDirectChartService =
        retrofit.create(VnDirectChartService::class.java)

    @Provides
    @Singleton
    fun provideSsiBoardService(@SsiRetrofit retrofit: Retrofit): SsiBoardService =
        retrofit.create(SsiBoardService::class.java)
}
