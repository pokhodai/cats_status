package ru.pokhodai.cats_status.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DownloadService {

    @GET("/{status_code}")
    suspend fun download(
        @Path("status_code") status_code: Int
    ): Response<ResponseBody>
}