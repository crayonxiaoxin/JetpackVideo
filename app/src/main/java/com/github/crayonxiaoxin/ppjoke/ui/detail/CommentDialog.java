package com.github.crayonxiaoxin.ppjoke.ui.detail;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.DialogFragment;

import com.github.crayonxiaoxin.libcommon.global.AppGlobals;
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

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {
    private LayoutCommentDialogBinding mBinding;
    private static final String URL_ADD_COMMENT = "/comment/addComment";
    private static final String KEY_ITEM_ID = "key_item_id";
    private long itemId;
    private CommentAddedListener mListener;

    public static CommentDialog newInstance(long itemId) {
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

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

        } else if (id == R.id.comment_delete) {

        }
    }

    private void publishComment() {
        Editable editable = mBinding.inputView.getText();
        if (TextUtils.isEmpty(editable)) return;
        String commentText = editable.toString();
        ApiService.post(URL_ADD_COMMENT)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("width", 0)
                .addParam("height", 0)
                .addParam("video_url", null)
                .addParam("image_url", null)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AppGlobals.getApplication(), "评论失败：" + response.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

    }

    private void onCommentSuccess(Comment body) {
        if (body != null && mListener != null) {
            mListener.onAddComment(body);
        }
    }

    public interface CommentAddedListener {
        void onAddComment(Comment comment);
    }

    public void setOnCommentAddedListener(CommentAddedListener listener) {
        mListener = listener;
    }
}
