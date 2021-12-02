package com.github.crayonxiaoxin.ppjoke.ui.detail;

import static android.content.Context.INPUT_METHOD_SERVICE;

import static com.github.crayonxiaoxin.ppjoke.ui.InteractionPresenter.showToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import com.github.crayonxiaoxin.libcommon.dialog.LoadingDialog;
import com.github.crayonxiaoxin.libcommon.global.AppGlobals;
import com.github.crayonxiaoxin.libcommon.utils.FileUploadManager;
import com.github.crayonxiaoxin.libcommon.utils.FileUtils;
import com.github.crayonxiaoxin.libcommon.utils.PixUtils;
import com.github.crayonxiaoxin.libcommon.utils.StatusBar;
import com.github.crayonxiaoxin.libcommon.view.ViewHelper;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.LayoutCommentDialogBinding;
import com.github.crayonxiaoxin.ppjoke.model.Comment;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;
import com.github.crayonxiaoxin.ppjoke.ui.publish.CaptureActivity;

import java.util.concurrent.atomic.AtomicInteger;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {
    private LayoutCommentDialogBinding mBinding;
    private static final String URL_ADD_COMMENT = "/comment/addComment";
    private static final String KEY_ITEM_ID = "key_item_id";
    private long itemId;
    private CommentAddedListener mListener;
    private Uri fileUri;
    private int width, height;
    private boolean isVideo = false;
    private LoadingDialog loadingDialog;
    private String fileUrl;
    private String coverUrl;

    public static CommentDialog newInstance(long itemId) {
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            CommentDialog.this.onActivityResult(result.getResultCode(), result.getData());
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(R.style.sendCommentDialog);

        ViewGroup viewById = window.findViewById(R.id.content); // 似乎只要 findViewById 不用 setContainer 都可以生效（宽度占满全屏）
        mBinding = LayoutCommentDialogBinding.inflate(inflater, viewById, false);

        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);

        // 设置透明背景
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.dimAmount = 0f;
        attributes.gravity = Gravity.BOTTOM;
        attributes.windowAnimations = R.style.sendCommentDialog;
        attributes.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
        attributes.height = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);
        // SOFT_INPUT_ADJUST_NOTHING 可以取消弹起键盘时输入框动画
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        // 点击空白处隐藏
        window.getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    hideInput();
                    dismiss();
                }
                return false;
            }
        });

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        mBinding.getRoot().setLayoutParams(layoutParams);
        // 防止点击 frameLayout 隐藏
        mBinding.getRoot().setClickable(true);
        mBinding.getRoot().setFocusable(true);

        ViewHelper.setViewOutline(mBinding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);

        if (getArguments() != null) {
            itemId = getArguments().getLong(KEY_ITEM_ID);
        }

        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                showSoftInputMethod();
            }
        });
        dismissWhenPressBack();

        return mBinding.getRoot();
    }

    private void dismissWhenPressBack() {
        mBinding.inputView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.e("TAG", "onKey: back");
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    Log.e("TAG", "onKey: back");
                    mBinding.inputView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideInput();
                            dismiss();
                        }
                    }, 200);
                    return true;
                }
                return false;
            }
        });
    }

    public void hideInput() {
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mBinding.inputView.getWindowToken(), 0);
    }

    private void showSoftInputMethod() {
        mBinding.inputView.setFocusable(true);
        mBinding.inputView.setFocusableInTouchMode(true);
        //请求获得焦点
        mBinding.inputView.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
//        manager.showSoftInput(mBinding.inputView, 0);
        manager.showSoftInput(mBinding.inputView, InputMethodManager.RESULT_SHOWN);
        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.comment_send) {
            publishComment();
        } else if (id == R.id.comment_video) {
            activityResultLauncher.launch(CaptureActivity.intentActivityForResult(getActivity()));
        } else if (id == R.id.comment_delete) {
            resetFile();
        }
    }

    private void resetFile() {
        fileUri = null;
        width = 0;
        height = 0;
        isVideo = false;
        mBinding.commentCover.setImageDrawable(null);
        mBinding.commentExtLayout.setVisibility(View.GONE);
        mBinding.commentVideo.setEnabled(true);
        mBinding.commentVideo.setAlpha(255);
    }

    private void publishComment() {
        Editable editable = mBinding.inputView.getText();
        if (TextUtils.isEmpty(editable)) return;
        if (isVideo && fileUri != null) {
            FileUtils.generateVideoCover(getContext(), fileUri).observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    if (!TextUtils.isEmpty(coverPath)) {
                        Log.e("TAG", "onChanged: 1");
                        uploadFile(coverPath, fileUri);
                    }
                }
            });
        } else if (fileUri != null) {
            Log.e("TAG", "onChanged: 2");
            uploadFile(null, fileUri);
        } else {
            publish();
        }
    }

    @SuppressLint("RestrictedApi")
    private void uploadFile(String coverPath, Uri fileUri) {
        showLoadingDialog();
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    int remain = count.decrementAndGet();
                    Log.e("TAG", "upload cover: " + coverPath);
                    coverUrl = FileUploadManager.upload(coverPath);
                    if (remain <= 0) {
                        if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverUrl)) {
                            publish();
                        } else {
                            hideLoadingDialog();
                            showToast(getString(R.string.file_upload_failed));
                        }
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int remain = count.decrementAndGet();
                fileUrl = FileUploadManager.upload(fileUri);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        hideLoadingDialog();
                        showToast(getString(R.string.file_upload_failed));
                    }
                }
            }
        });
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
//        resetAll();
    }

    @SuppressLint("RestrictedApi")
    private void resetAll() {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mBinding.inputView.setText("");
                resetFile();
            }
        });
    }

    private void publish() {
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post(URL_ADD_COMMENT)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("width", width)
                .addParam("height", height)
                .addParam("video_url", isVideo ? fileUrl : "")
                .addParam("image_url", isVideo ? coverUrl : fileUrl)
                .execute(new JsonCallback<Comment>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        hideLoadingDialog();
                        resetAll();
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败：" + response.message);
                        hideLoadingDialog();
                    }
                });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
            loadingDialog.setLoadingText(getString(R.string.upload_text));
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    @SuppressLint("RestrictedApi")
    private void hideLoadingDialog() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                }
            });
        }

    }

    private void onCommentSuccess(Comment body) {
        if (body != null && mListener != null) {
            mListener.onAddComment(body);
        }
    }

    public void onActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            fileUri = data.getParcelableExtra(CaptureActivity.RESULT_FILE_PATH);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);
            if (fileUri != null) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUri(fileUri);
                if (isVideo) {
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }
            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setAlpha(80);
        }
    }

    public interface CommentAddedListener {
        void onAddComment(Comment comment);
    }

    public void setOnCommentAddedListener(CommentAddedListener listener) {
        mListener = listener;
    }
}
