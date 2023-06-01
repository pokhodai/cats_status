package ru.pokhodai.cats_status.presentation.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import coil.compose.rememberAsyncImagePainter
import dagger.hilt.android.AndroidEntryPoint
import ru.pokhodai.cats_status.core.constans.Worker
import ru.pokhodai.cats_status.ui.theme.Cats_statusTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status_code = 500

        val data = Data.Builder().apply {
            putInt(MainViewModel.STATUS_CODE, status_code)
        }.build()

        val downloadWorker = viewModel.downloadWorker.setInputData(data).build()

        setContent {
            Cats_statusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val workInfos by remember(viewModel.workManager) {
                        val worker =
                            viewModel.workManager.getWorkInfosForUniqueWorkLiveData("download")
                        worker
                    }.observeAsState()

                    val downloadInfo = remember(workInfos) {
                        workInfos?.find { it.id == downloadWorker.id }
                    }

                    val imageUri by derivedStateOf {
                        downloadInfo?.outputData?.getString(Worker.IMAGE_URI)?.toUri()
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        imageUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = uri
                                ),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.workManager
                                    .beginUniqueWork(
                                        "download",
                                        ExistingWorkPolicy.KEEP,
                                        downloadWorker
                                    )
                                    .enqueue()
                            },
                            enabled = downloadInfo?.state != WorkInfo.State.RUNNING
                        ) {
                            Text(text = "Start download")
                        }
                    }
                }
            }
        }
    }
}