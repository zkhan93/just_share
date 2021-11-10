package io.github.zkhan93.justshare.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;
import io.github.zkhan93.justshare.HttpServer;
import io.github.zkhan93.justshare.R;
import io.github.zkhan93.justshare.utils.IntentUtil;
import io.github.zkhan93.justshare.utils.LogUtil;
import io.github.zkhan93.justshare.utils.PreferenceUtil;
import io.github.zkhan93.justshare.utils.WidgetUtil;

public class HttpService extends Service {
    public static final String TAG = HttpService.class.getSimpleName();

    public static final String ACTION_START_FOREGROUND_SERVICE = "start_service";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "stop_service";

    private SharedPreferences sharedPreferences;

    private HttpServer httpServer;
    private SharedPreferences.OnSharedPreferenceChangeListener filesListListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String
                        s) {
                    if(s.equals("files")) {
                        LogUtil.d(TAG, "file list changes");
                        if (httpServer != null) {
                            httpServer.setFiles(PreferenceUtil.getMetaList(sharedPreferences, "files"));
                        }
                    }
                }
            };

    private HttpServer.InputStreamProvider streamProvider = new HttpServer.InputStreamProvider() {
        @Override
        public InputStream getInputStream(String uri) throws IOException{
            return getContentResolver().openInputStream(Uri.parse(uri));
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(filesListListener);
        LogUtil.d(TAG, "Service created");
    }


    private Notification getNotification() {
        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "just_share_server_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Just Share service channel",
                    NotificationManager.IMPORTANCE_HIGH);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            notificationBuilder = buildNotification(new NotificationCompat.Builder(this));
        }
        return notificationBuilder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action == null) {
                LogUtil.d(TAG, "intent.getAction() is null cannot start or stop service");
            } else {
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForeground(1, getNotification());
                        startHttpServer();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopHttpServer();
                        stopForeground(true);
                        stopSelf();
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private NotificationCompat.Builder buildNotification(@NonNull NotificationCompat.Builder
                                                                 builder) {
        Intent stopIntent = new Intent(this.getApplicationContext(), HttpService.class);
        stopIntent.setAction(HttpService.ACTION_STOP_FOREGROUND_SERVICE);

        builder.setContentTitle("Just Share is serving")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Just Share is serving"))
                .setContentText("Just Share is serving 2 files")
                .addAction(new NotificationCompat.Action(R.drawable.stop, "Stop",
                        IntentUtil.getPendingIntent(getApplicationContext(), HttpService.ACTION_STOP_FOREGROUND_SERVICE)));
        return builder;

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void stopHttpServer() {
        if (httpServer == null) {
            LogUtil.d(TAG, "httpServer is null");
            sharedPreferences.edit().putInt("server_state", 0).apply();
            WidgetUtil.triggerUpdateWidget(this);
            return;
        }
        httpServer.stop();
        sharedPreferences.edit().putInt("server_state", 0).apply();
        WidgetUtil.triggerUpdateWidget(this);
    }

    public void startHttpServer() {
        if (httpServer == null) {
            LogUtil.d(TAG, "httpServer is null");
            try {
                httpServer = new HttpServer(streamProvider);
            } catch (IOException ex) {
                LogUtil.e(TAG, "Exception while creating http server: %s", ex.getLocalizedMessage());
                return;
            }
        }
        if (sharedPreferences != null)
            httpServer.setFiles(PreferenceUtil.getMetaList(sharedPreferences,"files"));
        try {
            httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            sharedPreferences.edit().putInt("server_state", 1).apply();
            WidgetUtil.triggerUpdateWidget(this);
            LogUtil.d(TAG, "httpd server started at http://0.0.0.0:8000");
        } catch (IOException ex) {
            LogUtil.e(TAG, "Exception while starting server: %s", ex.getLocalizedMessage());
        }
    }
}
