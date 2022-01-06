package com.github.crayonxiaoxin.ppjoke.ui.my;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.TypeReference;
import com.github.crayonxiaoxin.libcommon.utils.StatusBar;
import com.github.crayonxiaoxin.libnavannotation.FragmentDestination;
import com.github.crayonxiaoxin.libnetwork.ApiResponse;
import com.github.crayonxiaoxin.libnetwork.ApiService;
import com.github.crayonxiaoxin.libnetwork.JsonCallback;
import com.github.crayonxiaoxin.libnetwork.PostRequest;
import com.github.crayonxiaoxin.libnetwork.Request;
import com.github.crayonxiaoxin.ppjoke.R;
import com.github.crayonxiaoxin.ppjoke.databinding.FragmentMyBinding;
import com.github.crayonxiaoxin.ppjoke.model.Feed;
import com.github.crayonxiaoxin.ppjoke.model.TestUser;
import com.github.crayonxiaoxin.ppjoke.model.User;
import com.github.crayonxiaoxin.ppjoke.ui.login.UserManager;

import java.util.ArrayList;
import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/my", needLogin = false)
public class MyFragment extends Fragment {

    private FragmentMyBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMyBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        mBinding.setUser(user);
        UserManager.get().refresh().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mBinding.setUser(user);
            }
        });
        mBinding.actionLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.fragment_my_logout))
                        .setPositiveButton(R.string.fragment_my_logout_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                UserManager.get().logout();
                                getActivity().onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.fragment_my_logout_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });
        mBinding.goDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.startActivity(getContext(), ProfileActivity.TAB_TYPE_ALL);
            }
        });
        mBinding.userComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.startActivity(getContext(), ProfileActivity.TAB_TYPE_COMMENT);
            }
        });
        mBinding.userFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.startActivity(getContext(), ProfileActivity.TAB_TYPE_FEED);
            }
        });
        mBinding.avatar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
//                ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
//                    @Override
//                    public void run() {
////                        // 指明 泛型类型
////                        ApiService.<List<Feed>>post("/feeds/queryHotFeedsList")
////                               .execute(new JsonCallback<List<Feed>>() {
////                                   @Override
////                                   public void onSuccess(ApiResponse<List<Feed>> response) {
////                                       super.onSuccess(response);
////                                   }
////                               });
////                        // 不指明 泛型类型
////                        PostRequest<Object> post = ApiService.post("/feeds/queryHotFeedsList");
////                        // addParam 中作了以下操作 强转为 PostRequest
////                        PostRequest post2 = (PostRequest) post;
////                        post2.execute(new JsonCallback<List<Feed>>() {
////
////                        });
////                        // 如果直接使用，因为没有指明泛型，所以 T 默认为 object
////                        ApiService.post("/feeds/queryHotFeedsList")
////                                .execute(new JsonCallback<Object>() {
////
////                                });
//
//                    }
//                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBar.fitSystemBar(getActivity(), false, false, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        StatusBar.fitSystemBar(getActivity(), hidden, hidden, false);
    }
}