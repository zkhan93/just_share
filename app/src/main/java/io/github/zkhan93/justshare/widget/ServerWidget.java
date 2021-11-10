package io.github.zkhan93.justshare.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

import io.github.zkhan93.justshare.FileSelectionActivity;
import io.github.zkhan93.justshare.R;
import io.github.zkhan93.justshare.models.FileMeta;
import io.github.zkhan93.justshare.service.HttpService;
import io.github.zkhan93.justshare.utils.IntentUtil;
import io.github.zkhan93.justshare.utils.LogUtil;
import io.github.zkhan93.justshare.utils.PreferenceUtil;

/**
 * Implementation of App Widget functionality.
 */
public class ServerWidget extends AppWidgetProvider {
    public static final String TAG = ServerWidget.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        LogUtil.d(TAG, "updateAppWidget");
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(context);
        int isRunning = spf.getInt("server_state", 0);
        LogUtil.d(TAG, "isRunning %s", String.valueOf(isRunning));
        List<FileMeta> files = PreferenceUtil.getMetaList(spf, "files");

        PendingIntent pendingStopIntent;
        PendingIntent pendingStartIntent;

        pendingStopIntent = IntentUtil.getPendingIntent(context, HttpService.ACTION_STOP_FOREGROUND_SERVICE);
        pendingStartIntent = IntentUtil.getPendingIntent(context, HttpService.ACTION_START_FOREGROUND_SERVICE);

        Intent selectFilesIntent = new Intent(context, FileSelectionActivity.class);
        PendingIntent pendingSelectFileIntent = PendingIntent.getActivity(context, 0,
                selectFilesIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.server_widget);

        views.setOnClickPendingIntent(R.id.start, pendingStartIntent);
        views.setOnClickPendingIntent(R.id.stop, pendingStopIntent);

        views.setOnClickPendingIntent(R.id.select_files, pendingSelectFileIntent);

        views.setTextViewText(R.id.share_desc, String.format("%s files selected", files.size()));
        if (isRunning == 0) {
            views.setViewVisibility(R.id.start, View.VISIBLE);
            views.setViewVisibility(R.id.stop, View.GONE);
            views.setTextViewText(R.id.server_desc, "Server Not Running");
        } else {
            views.setViewVisibility(R.id.start, View.GONE);
            views.setViewVisibility(R.id.stop, View.VISIBLE);
            views.setTextViewText(R.id.server_desc, String.format("http://%s:%s", getIPAddress
                    (context), 8000));
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static String getIPAddress(@NonNull Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService
                (Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            LogUtil.e(TAG, "Unable to get host address.");
            ipAddressString = "0.0.0.0";
        }

        return ipAddressString;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


}

