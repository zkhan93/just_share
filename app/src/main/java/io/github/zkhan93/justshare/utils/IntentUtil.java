package io.github.zkhan93.justshare.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;

import io.github.zkhan93.justshare.service.HttpService;

public class IntentUtil {
    public static PendingIntent getPendingIntent(@NonNull Context context, @NonNull String action) {
        android.content.Intent intent = new android.content.Intent(context, HttpService.class);
        intent.setAction(action);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= 26) {
            pendingIntent = PendingIntent.getForegroundService(context, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

        } else {
            pendingIntent = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return pendingIntent;
    }
}
