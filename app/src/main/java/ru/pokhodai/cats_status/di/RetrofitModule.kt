package ru.pokhodai.cats_status.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.pokhodai.cats_status.data.remote.DownloadService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideBaseUrl() = "https://http.cat"

    @Provides
    @Singleton
    fun provideDownloadService(
        baseUrl: String
    ): DownloadService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .build()
        .create(DownloadService::class.java)
}