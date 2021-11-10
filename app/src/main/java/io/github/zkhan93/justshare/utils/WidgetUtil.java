package io.github.zkhan93.justshare.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import io.github.zkhan93.justshare.widget.ServerWidget;

public class WidgetUtil {
    public static void triggerUpdateWidget(@NonNull Context context){
        Intent intent = new Intent(context.getApplicationContext(), ServerWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), ServerWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
