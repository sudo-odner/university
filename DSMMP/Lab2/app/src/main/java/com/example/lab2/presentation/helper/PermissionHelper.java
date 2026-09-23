package com.example.lab2.presentation.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.example.lab2.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PermissionHelper {

    public interface PermissionDeniedCallback {
        void onDenied();
    }

    public static void checkNotificationPermission(Context context,
                                                   ActivityResultLauncher<String> launcher,
                                                   PermissionDeniedCallback onDenied) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.dialog_permission_title)
                        .setMessage(R.string.dialog_permission_message)
                        .setPositiveButton(R.string.dialog_permission_positive, (dialog, which) ->
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton(R.string.dialog_permission_negative, (dialog, which) -> {
                            if (onDenied != null) onDenied.onDenied();
                        })
                        .show();
            }
        }
    }
}
