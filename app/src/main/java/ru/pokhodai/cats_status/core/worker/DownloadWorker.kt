package ru.pokhodai.cats_status.core.worker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.pokhodai.cats_status.R
import ru.pokhodai.cats_status.core.constans.Notification
import ru.pokhodai.cats_status.core.constans.Worker
import ru.pokhodai.cats_status.core.constans.Worker.WORK_DELAY
import ru.pokhodai.cats_status.data.remote.DownloadService
import ru.pokhodai.cats_status.data.repository.DownloadRepository
import ru.pokhodai.cats_status.presentation.activity.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val downloadRepository: DownloadRepository,
): CoroutineWorker(context, workerParameters) {

    private var status_code: Int? = null

    override suspend fun doWork(): Result {
        startForeground()
        delay(WORK_DELAY)
        val status_code =  inputData.getInt(MainViewModel.STATUS_CODE, 404)

        return withContext(Dispatchers.IO) {
            val response = downloadRepository.downloadCat(status_code)

            if (response.isSuccessful) {
                response.body()?.let { body ->

                    val file = File(context.filesDir, "image.jpg")
                    val output = FileOutputStream(file)

                    output.use { stream ->
                        try {
                            stream.write(body.bytes())
                        } catch (e: IOException) {
                            return@withContext Result.failure(workDataOf(Worker.ERROR to e.message))
                        }
                    }

                    return@withContext Result.success(workDataOf(Worker.IMAGE_URI to file.toUri().toString()))
                }
                return@withContext Result.failure()
            } else {
                if (response.code().toString().startsWith("5")) Result.retry()
                return@withContext Result.failure(workDataOf(Worker.ERROR to "Network error"))
            }
        }
    }

    private suspend fun startForeground() {
        setForeground(
            ForegroundInfo(
                Notification.NOTIFICATION_ID,
                NotificationCompat.Builder(context, Notification.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Downloading...")
                    .setContentText("Downloading in progress!")
                    .build()
            )
        )
    }

    fun setStatusCode(status_code: Int) {
        this.status_code = status_code
    }
}