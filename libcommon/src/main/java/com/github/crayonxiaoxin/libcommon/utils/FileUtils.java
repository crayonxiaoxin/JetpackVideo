package com.github.crayonxiaoxin.libcommon.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.crayonxiaoxin.libcommon.global.AppGlobals;

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
                bitmap = retriever.getFrameAtTime();
                if (bitmap != null) {
                    byte[] bytes = compressBitmap(bitmap, 200);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                    FileOutputStream fos = null;
                    try {
                        file.createNewFile();
                        fos = new FileOutputStream(file);
                        fos.write(bytes);

                        MediaScannerConnection.scanFile(AppGlobals.getApplication(), new String[]{file.getParent()}, new String[]{"image/jpeg"}, null);

                        liveData.postValue(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.flush();
                                fos.close();
                                fos = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
                bitmap = retriever.getFrameAtTime();
                if (bitmap != null) {
                    // 压缩至 200kb 以下
                    byte[] bytes = compressBitmap(bitmap, 200);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                    FileOutputStream fos = null;
                    try {
                        file.createNewFile();
                        fos = new FileOutputStream(file);
//                        Log.e("TAG", "run: bytes " + bytes.length);
                        fos.write(bytes);

                        MediaScannerConnection.scanFile(context, new String[]{file.getParent()}, new String[]{"image/jpeg"}, null);

                        liveData.postValue(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.flush();
                                fos.close();
                                fos = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    liveData.postValue(null);
                }

            }
        });
        return liveData;
    }

    private static byte[] compressBitmap(Bitmap bitmap, int limit) {
        if (bitmap != null && limit > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            while (baos.toByteArray().length > limit * 1024) {
                baos.reset();
                options -= 5;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            }
            byte[] bytes = baos.toByteArray();
            try {
                baos.close();
                baos = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bytes;
        }
        return null;
    }


}
