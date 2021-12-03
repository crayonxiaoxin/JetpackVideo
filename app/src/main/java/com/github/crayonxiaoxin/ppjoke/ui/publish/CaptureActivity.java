package com.github.crayonxiaoxin.ppjoke.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.github.crayonxiaoxin.libnavannotation.ActivityDestination;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityCaptureBinding;
import com.github.crayonxiaoxin.ppjoke.ui.view.RecordView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CaptureActivity extends AppCompatActivity {

    private ActivityCaptureBinding mBinding;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private ArrayList<String> deniedPermissions = new ArrayList<>();
    private ActivityResultLauncher<String[]> requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            deniedPermissions.clear();
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    deniedPermissions.add(entry.getKey());
                }
            }
            if (deniedPermissions.isEmpty()) {
                bindCameraX();
            } else {
                new AlertDialog.Builder(CaptureActivity.this)
                        .setMessage(getString(R.string.capture_permission_message))
                        .setNegativeButton(getString(R.string.capture_permission_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        })
                        .setPositiveButton(getString(R.string.capture_permission_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                String[] denied = new String[deniedPermissions.size()];
                                deniedPermissions.toArray();
                                requestPermissions.launch(deniedPermissions.toArray(denied));
                            }
                        })
                        .create().show();
            }
        }
    });
    private static int rotation = Surface.ROTATION_0;
    private static Size resolution = new Size(1280, 720);
    private static CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private static int aspectRatio = AspectRatio.RATIO_16_9;
    private Preview preview;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private boolean takingPicture;
    private Uri outputFileUri;
    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_FILE_PATH, outputFileUri);
                // 当设备处于竖屏时，宽高需要互换
                intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight());
                intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth());
                intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    });

    public static Intent intentActivityForResult(Activity activity) {
        return new Intent(activity, CaptureActivity.class);
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = cameraProviderFuture.get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (cameraProvider == null) return;
                preview = new Preview.Builder()
                        .setTargetRotation(rotation)
                        .setTargetResolution(resolution)
//                        .setTargetAspectRatio(aspectRatio)
                        .setCameraSelector(cameraSelector)
                        .build();
                preview.setSurfaceProvider(mBinding.textureView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(rotation)
                        .setTargetResolution(resolution)
//                        .setTargetAspectRatio(aspectRatio)
                        .setCameraSelector(cameraSelector)
                        .build();
                videoCapture = new VideoCapture.Builder()
                        .setTargetRotation(rotation)
                        .setTargetResolution(resolution)
//                        .setTargetAspectRatio(aspectRatio)
                        .setCameraSelector(cameraSelector)
                        .setAudioBitRate(3 * 1024 * 1024)
                        .setVideoFrameRate(25)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(CaptureActivity.this, cameraSelector, preview, imageCapture, videoCapture);
            }
        }, ContextCompat.getMainExecutor(this));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_capture);
        requestPermissions.launch(PERMISSIONS);
        mBinding.recordView.setonRecordListener(new RecordView.onRecordListener() {
            @Override
            public void onClick() {
                if (imageCapture == null) return;
                takingPicture = true;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(CaptureActivity.this), new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        onFileSave(outputFileResults.getSavedUri());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showErrorToast(exception.getMessage());
                    }
                });
            }

            @SuppressLint({"MissingPermission", "RestrictedApi"})
            @Override
            public void onLongClick() {
                if (videoCapture == null) return;
                takingPicture = false;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".mp4");
                VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(file).build();
                videoCapture.startRecording(outputFileOptions, ContextCompat.getMainExecutor(CaptureActivity.this), new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        onFileSave(outputFileResults.getSavedUri());
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                if (videoCapture != null) {
                    videoCapture.stopRecording();
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void showErrorToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFileSave(Uri uri) {
        outputFileUri = uri;
        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
        MediaScannerConnection.scanFile(this, new String[]{uri.getPath()}, new String[]{mimeType}, null);

        activityResultLauncher.launch(PreviewActivity.intentActivityForResult(this, outputFileUri, !takingPicture, "完成"));
    }

}
