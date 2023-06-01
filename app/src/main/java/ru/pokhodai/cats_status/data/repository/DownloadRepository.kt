package ru.pokhodai.cats_status.data.repository

import okhttp3.ResponseBody
import retrofit2.Response
import ru.pokhodai.cats_status.data.remote.DownloadService
import javax.inject.Inject

class DownloadRepository @Inject constructor(
    private val downloadService: DownloadService
) {

    suspend fun downloadCat(status_code: Int): Response<ResponseBody> {
        return downloadService.download(status_code)
    }
}