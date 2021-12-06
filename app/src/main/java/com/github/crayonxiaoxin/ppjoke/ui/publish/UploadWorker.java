package com.github.crayonxiaoxin.ppjoke.ui.publish;


import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.github.crayonxiaoxin.libcommon.utils.FileUploadManager;

public class UploadWorker extends Worker {

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String file = inputData.getString("file");
        boolean isUri = inputData.getBoolean("isUri", false);
        String fileUrl;
        if (isUri) {
            fileUrl = FileUploadManager.upload(Uri.parse(file));
        } else {
            fileUrl = FileUploadManager.upload(file);
        }
        if (TextUtils.isEmpty(fileUrl)) {
            return Result.failure();
        } else {
            Data outputData = new Data.Builder().putString("fileUrl", fileUrl).build();
            return Result.success(outputData);
        }
    }
}
