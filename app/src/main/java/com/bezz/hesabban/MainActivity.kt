package com.bezz.hesabban

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bezz.hesabban.ui.AppRoot
import com.bezz.hesabban.ui.theme.HesabBanTheme
import com.bezz.hesabban.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result handled via hasPermissions() re-check in Compose */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HesabBanTheme {
                val viewModel: TransactionViewModel = viewModel()
                var permissionsRequested by remember { mutableStateOf(false) }

                if (!permissionsRequested && !hasSmsPermissions()) {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                    )
                    permissionsRequested = true
                }

                AppRoot(viewModel = viewModel, onRequestSmsImport = {
                    if (hasSmsPermissions()) {
                        viewModel.importSmsHistory()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                        )
                    }
                })
            }
        }
    }

    private fun hasSmsPermissions(): Boolean {
        val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        val receive = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        return read == PackageManager.PERMISSION_GRANTED && receive == PackageManager.PERMISSION_GRANTED
    }
}
