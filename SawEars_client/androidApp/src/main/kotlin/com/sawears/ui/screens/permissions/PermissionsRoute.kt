package com.sawears.ui.screens.permissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.permissions_explanation
import sawears.shared.generated.resources.permissions_grant_permission_button
import sawears.shared.generated.resources.permissions_open_settings_button
import sawears.shared.generated.resources.permissions_permanently_declined


/**
 * Android specific screen handling permissions logic.
 *
 * Handles three possible cases:
 *
 * 1. User accepts, progress to content
 * 2. User rejects, prompt again
 * 3. User rejects twice, direct user to settings to enable permission
 *
 * @param content the content to progress to once permissions are secured
 *
 * This approach to handling permissions is substantially adapted from code
 * by Philip Lackner. Source code can be found here:
 * - https://github.com/philipplackner/CMP-PermissionHandling
 */
@Composable
fun PermissionsRoute(
    content: @Composable () -> Unit
){

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }

    BindEffect(controller)

    val viewModel = viewModel { PermissionsViewModel(controller) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (viewModel.state) {
            PermissionState.Granted -> {
                content()
            }

            PermissionState.DeniedAlways -> {
                Text(
                    text=Res.string.permissions_permanently_declined.toString(),
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = {
                    controller.openAppSettings()
                }) {
                    Text(Res.string.permissions_open_settings_button.toString())
                }
            }

            else -> {
                Column{
                    Text(
                        text=Res.string.permissions_explanation.toString(),
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.provideOrRequestRecordAudioPermission()
                        }
                    ) {
                        Text(Res.string.permissions_grant_permission_button.toString())
                    }
                }
            }
        }
    }
}