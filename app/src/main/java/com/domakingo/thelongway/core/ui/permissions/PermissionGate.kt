package com.domakingo.thelongway.core.ui.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * A generic UI gate that ensures a set of permissions is granted before rendering or triggering logic.
 */
@Composable
fun PermissionGate(
    permissions: Array<String>,
    onPermissionsResult: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        onPermissionsResult(results.values.all { it })
    }

    LaunchedEffect(Unit) {
        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            launcher.launch(permissions)
        } else {
            onPermissionsResult(true)
        }
    }

    content()
}
