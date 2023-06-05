package com.hippo.ehviewer.ui.settings

import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.hippo.ehviewer.R
import com.hippo.ehviewer.download.downloadLocation
import com.hippo.ehviewer.ui.keepNoMediaFileStatus
import com.hippo.ehviewer.ui.login.LocalNavController
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.util.lang.launchNonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DownloadScreen() {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_download)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        val cannotGetDownloadLocation = stringResource(id = R.string.settings_download_cant_get_download_location)
        val selectDownloadDirLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
            treeUri?.run {
                coroutineScope.launch {
                    context.runCatching {
                        contentResolver.takePersistableUriPermission(treeUri, FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)
                        UniFile.fromTreeUri(context, treeUri)!!.run {
                            downloadLocation = this
                            coroutineScope.launchNonCancellable {
                                keepNoMediaFileStatus()
                            }
                        }
                    }.onFailure {
                        snackbarHostState.showSnackbar(cannotGetDownloadLocation)
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(it).nestedScroll(scrollBehavior.nestedScrollConnection)) {
            Preference(
                title = stringResource(id = R.string.settings_download_download_location),
                summary = downloadLocation.uri.toString(),
            ) { selectDownloadDirLauncher.launch(downloadLocation.uri) }
        }
    }
}
