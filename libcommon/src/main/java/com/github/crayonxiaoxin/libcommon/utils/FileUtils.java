package com.github.crayonxiaoxin.libcommon.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    @SuppressLint("RestrictedApi")
    public static LiveData<String> generateVideoCover(String filePath) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath);
                Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    bitmap = retriever.getFrameAtIndex(-1);
                } else {
                    bitmap = retriever.getFrameAtTime(1);
                }
                if (bitmap != null) {
                    byte[] bytes = compressBitmap(bitmap, 200);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(bytes);
                        fos.flush();
                        fos.close();
                        fos = null;
                        liveData.postValue(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    liveData.postValue(null);
                }

            }
        });
        return liveData;
    }

    @SuppressLint("RestrictedApi")
    public static LiveData<String> generateVideoCover(Context context, Uri filePath) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(context, filePath);
                Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    bitmap = retriever.getFrameAtIndex(1);
                } else {
                    bitmap = retriever.getFrameAtTime(1);
                }
                if (bitmap != null) {
                    byte[] bytes = compressBitmap(bitmap, 200);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(bytes);
                        fos.flush();
                        fos.close();
                        fos = null;
                        liveData.postValue(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    liveData.postValue(null);
                }

            }
        });
        return liveData;
    }

    private static byte[] compressBitmap(Bitmap bitmap, int limit) {
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            while (baos.toByteArray().length > limit * 1024) {
                baos.reset();
                options -= 5;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            }
            byte[] bytes = baos.toByteArray();
            if (baos != null) {
                try {
                    baos.close();
                    baos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bytes;
        }
        return null;
    }


}
