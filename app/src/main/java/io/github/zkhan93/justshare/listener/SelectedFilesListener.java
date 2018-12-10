package io.github.zkhan93.justshare.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.github.zkhan93.justshare.utils.LogUtil;

public class SelectedFilesListener extends BroadcastReceiver {
    public static final String TAG = SelectedFilesListener.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "onReceive");
    }
}
