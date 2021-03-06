package com.github.crayonxiaoxin.ppjoke.ui.publish;

import static com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter.showToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.alibaba.fastjson.JSONObject;
import com.github.crayonxiaoxin.libcommon.dialog.LoadingDialog;
import com.github.crayonxiaoxin.libcommon.utils.FileUtils;
import com.github.crayonxiaoxin.libcommon.utils.StatusBar;
import com.github.crayonxiaoxin.libnavannotation.ActivityDestination;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.ActivityPublishBinding;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.model.TagList;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityPublishBinding mBinding;
    private Uri fileUri;
    private int width, height;
    private boolean isVideo = false;
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                fileUri = data.getParcelableExtra(CaptureActivity.RESULT_FILE_PATH);
                width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
                height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
                isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);
                showFileThumbnail();
            }
        }
    });
    private String mCoverPath;
    private UUID coverUUID;
    private UUID fileUploadUUID;
    private String coverUploadUrl;
    private String fileUploadUrl;
    private TagList mTagList; // ???????????????
    private LoadingDialog mLoadingDialog;

    private void showFileThumbnail() {
        if (fileUri == null) {
            return;
        }
        mBinding.actionAddFile.setVisibility(View.GONE);
        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.cover.setImageUri(fileUri);
        mBinding.playBtn.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        mBinding.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PreviewActivity.intentActivityForResult(PublishActivity.this, fileUri, isVideo, null));
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBar.fitSystemBar(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_publish);
        mBinding.actionClose.setOnClickListener(this);
        mBinding.actionPublish.setOnClickListener(this);
        mBinding.actionAddTag.setOnClickListener(this);
        mBinding.actionAddFile.setOnClickListener(this);
        mBinding.actionDeleteFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.action_publish:
                publish();
                break;
            case R.id.action_add_tag:
                TabBottomSheetFragment fragment = new TabBottomSheetFragment();
                fragment.setOnTagItemSelectedListener(new TabBottomSheetFragment.OnTagItemSelectedListener() {

                    @Override
                    public void onTagItemSelected(TagList item) {
                        mTagList = item;
                        mBinding.actionAddTag.setText(item.title);
                    }
                });
                fragment.show(getSupportFragmentManager(), "tag_dialog");
                break;
            case R.id.action_add_file:
                activityResultLauncher.launch(CaptureActivity.intentActivityForResult(this));
                break;
            case R.id.action_delete_file:
                mBinding.actionAddFile.setVisibility(View.VISIBLE);
                mBinding.fileContainer.setVisibility(View.GONE);
                mBinding.cover.setImageDrawable(null);
                fileUri = null;
                width = 0;
                height = 0;
                isVideo = false;
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    private void publish() {
        showLoading();
        List<OneTimeWorkRequest> workRequestList = new ArrayList<>();
        if (fileUri != null) {
            if (isVideo) {
                // ?????? cover ????????????????????????????????????????????? ???2?????????
                FileUtils.generateVideoCover(this, fileUri).observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String coverPath) {
                        mCoverPath = coverPath;
                        OneTimeWorkRequest request = getOneTimeWorkRequest(coverPath, false);
                        coverUUID = request.getId();
                        workRequestList.add(request);

                        enqueue(workRequestList);
                    }
                });
            }
            OneTimeWorkRequest request = getOneTimeWorkRequest(fileUri.toString(), true);
            fileUploadUUID = request.getId();
            workRequestList.add(request);
            if (!isVideo) { // ????????? cover ??????????????????????????????????????????
                enqueue(workRequestList);
            }
        } else {
            publishFeed();
        }
    }

    private void enqueue(List<OneTimeWorkRequest> workRequestList) {
        WorkContinuation workContinuation = WorkManager.getInstance(PublishActivity.this).beginWith(workRequestList);
        workContinuation.enqueue();
        workContinuation.getWorkInfosLiveData().observe(PublishActivity.this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // block running enqueued failed success finish
                int completedCount = 0;
                for (WorkInfo workInfo : workInfos) {
                    WorkInfo.State state = workInfo.getState();
                    Data outputData = workInfo.getOutputData();
                    UUID uuid = workInfo.getId();
                    if (state == WorkInfo.State.FAILED) {
                        if (uuid.equals(coverUUID)) {
                            showToast(getString(R.string.file_upload_cover_message));
                        } else if (uuid.equals(fileUploadUUID)) {
                            showToast(getString(R.string.file_upload_original_message));
                        }
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String fileUrl = outputData.getString("fileUrl");
                        if (uuid.equals(coverUUID)) {
                            coverUploadUrl = fileUrl;
                        } else if (uuid.equals(fileUploadUUID)) {
                            fileUploadUrl = fileUrl;
                        }
                        completedCount++;
                    }
                }
                if (completedCount >= workInfos.size()) {
                    publishFeed();
                }
            }
        });
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl", isVideo ? coverUploadUrl : null)
                .addParam("fileUrl", fileUploadUrl)
                .addParam("fileWidth", width)
                .addParam("fileHeight", height)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", mTagList == null ? 0 : mTagList.tagId)
                .addParam("tagTitle", mTagList == null ? 0 : mTagList.title)
                .addParam("feedText", mBinding.inputView.getText().toString())
                .addParam("feedType", isVideo ? Feed.TYPE_VIDEO : Feed.TYPE_IMAGE)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        showToast(getString(R.string.feed_publisj_success));
                        PublishActivity.this.finish();
                        hideLoading();
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                        hideLoading();
                    }
                });
    }

    private void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setLoadingText(getString(R.string.feed_publish_ing));
        }
        mLoadingDialog.show();
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            }
        });
    }

    private OneTimeWorkRequest getOneTimeWorkRequest(String filePath, boolean isUri) {
        Data inputData = new Data.Builder()
                .putString("file", filePath)
                .putBoolean("isUri", isUri)
                .build();
//        Constraints constraints = new Constraints();
//        // ???????????????????????????>15%
//        constraints.setRequiresStorageNotLow(true);
//        // ?????????????????????UNMETERED ??????????????????wifi
//        //constraints.setRequiredNetworkType(NetworkType.UNMETERED);
//        // ??????????????????????????????>15%
//        constraints.setRequiresBatteryNotLow(true);
//        // ?????????????????????????????????
//        constraints.setRequiresCharging(false);
//        // ??????????????????????????????????????????????????????cpu???????????????
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            constraints.setRequiresDeviceIdle(true);
//        }
//        // uri ??????????????????????????????
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            constraints.setContentUriTriggers(null);
//        }
//        // ?????? content ????????????????????????????????????
//        constraints.setTriggerContentUpdateDelay(0);
//        // ?????? content ??????????????????????????????????????????
//        constraints.setTriggerMaxContentDelay(0);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setInputData(inputData)
//                .setConstraints(constraints)
//                // ????????????????????????????????????????????????
//                .setInputMerger(null)
//                // ??????????????????????????????  EXPONENTIAL ???????????????
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
//                // ???????????????????????????
//                .setInitialDelay(10, TimeUnit.SECONDS)
//                // ????????????????????????
//                .setInitialRunAttemptCount(2)
//                // ??????????????????????????????
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
//                // ????????????????????????????????????
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
//                // ?????????????????????????????????finish??????????????????????????????????????????????????????????????????????????????????????????
//                .keepResultsForAtLeast(10, TimeUnit.SECONDS)
                .build();
        return request;
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.publish_exit_message))
                .setNegativeButton(getString(R.string.publish_exit_action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.publish_exit_action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .create().show();
    }
}
