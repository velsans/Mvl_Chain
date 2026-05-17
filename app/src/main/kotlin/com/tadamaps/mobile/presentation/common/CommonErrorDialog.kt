package com.tadamaps.mobile.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tadamaps.mobile.R

/**
 * App-wide error dialog: **OK** clears the message only; caller must not navigate on dismiss.
 */
@Composable
fun CommonErrorDialog(
    message: String?,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.common_error_title),
    confirmLabel: String = stringResource(R.string.common_error_ok),
) {
    if (message.isNullOrBlank()) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = confirmLabel)
            }
        },
    )
}
