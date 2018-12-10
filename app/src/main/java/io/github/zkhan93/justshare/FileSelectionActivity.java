package io.github.zkhan93.justshare;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.zkhan93.justshare.models.FileMeta;
import io.github.zkhan93.justshare.utils.LogUtil;
import io.github.zkhan93.justshare.utils.PreferenceUtil;
import io.github.zkhan93.justshare.utils.WidgetUtil;

public class FileSelectionActivity extends AppCompatActivity {
    public static final String TAG = FileSelectionActivity.class.getSimpleName();
    private static int SELECT_FILES_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent selectFilesIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        selectFilesIntent.setType("*/*");
        selectFilesIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "audio/*",
                "image/*",
                "application/*",
                "video/*",
        });
        selectFilesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        selectFilesIntent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(selectFilesIntent, SELECT_FILES_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_FILES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(this);
            Set<Uri> files = new HashSet<>();
            if (data == null)
                LogUtil.d(TAG, "data is null");
            else if (data.getData() != null) {
                files.add(data.getData());
            } else if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    ClipData.Item item = data.getClipData().getItemAt(i);
                    files.add(item.getUri());
                }
            } else
                LogUtil.d(TAG, "getExtras() and getClipData() both are null");
            List<FileMeta> fileMetas = new ArrayList<>();
            for (Uri uri : files) {
                if (uri.getScheme() == null)
                    continue;
                FileMeta fileMeta = new FileMeta();
                fileMeta.name = uri.getLastPathSegment();
                fileMeta.uri = uri.toString();
                if (uri.getScheme().compareTo("content") == 0) {
                    Cursor cursor = null;
                    try {
                        String[] columns = new String[]{
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                MediaStore.MediaColumns.MIME_TYPE,
                                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                MediaStore.MediaColumns.SIZE};
                        cursor = getContentResolver().query(uri, columns, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            LogUtil.d(TAG, Uri.decode(uri.toString()));
                            for (String col : columns) {

                                LogUtil.d(TAG, "%s: %s", col, cursor.getString(cursor.getColumnIndex
                                        (col)));
                            }

                            int idx = cursor.getColumnIndex(MediaStore.MediaColumns
                                    .DISPLAY_NAME);
                            if (idx != -1)
                                fileMeta.name = cursor.getString(idx);
                            idx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                            if (idx != -1)
                                fileMeta.length = cursor.getLong(idx);
                            idx = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
                            if (idx != -1)
                                fileMeta.mimeType = cursor.getString(idx);
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                }
                LogUtil.d(TAG, "file: %s", fileMeta.toString());
                fileMetas.add(fileMeta);
            }
            PreferenceUtil.saveMetaList(spf, "files", fileMetas);
            WidgetUtil.triggerUpdateWidget(this.getApplicationContext());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        finish();
    }
}
