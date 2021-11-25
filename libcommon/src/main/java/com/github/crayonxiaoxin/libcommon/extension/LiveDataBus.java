package com.github.crayonxiaoxin.libcommon.extension;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.ConcurrentHashMap;

public class LiveDataBus {

    private static class Lazy {
        public static LiveDataBus sLiveDataBus = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return Lazy.sLiveDataBus;
    }

    private ConcurrentHashMap<String, StickyLiveData> mHashMap = new ConcurrentHashMap<>();

    public StickyLiveData with(String eventName) {
        StickyLiveData stickyLiveData = mHashMap.get(eventName);
        if (stickyLiveData == null) {
            stickyLiveData = new StickyLiveData(eventName);
            mHashMap.put(eventName, stickyLiveData);
        }
        return stickyLiveData;
    }

    public class StickyLiveData<T> extends LiveData<T> {
        private final String mEventName;
        private T mStickyData;
        private int mVersion = 0;

        public StickyLiveData(String eventName) {
            mEventName = eventName;
        }

        @Override
        public void setValue(T value) {
            mVersion++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            mVersion++;
            super.postValue(value);
        }

        public void setStickyData(T stickyData) {
            this.mStickyData = stickyData;
            setValue(stickyData);
        }

        public void postStickyData(T stickyData) {
            this.mStickyData = stickyData;
            postValue(stickyData);
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            observerSticky(owner, observer, false);
        }

        private void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean isSticky) {
            super.observe(owner, new WrapperObserver(this, observer, isSticky));
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) { // ON_DESTROY 移除 StickyLiveData
                        mHashMap.remove(mEventName);
                    }
                }
            });
        }

        public class WrapperObserver<T> implements Observer<T> {
            private StickyLiveData mLiveData;
            private Observer<T> mObserver;
            private boolean mIsSticky;
            private int mLastVersion = 0;

            public WrapperObserver(StickyLiveData liveData, Observer<T> observer, boolean isSticky) {
                mLiveData = liveData;
                mObserver = observer;
                mIsSticky = isSticky;
                mLastVersion = liveData.mVersion;
            }

            @Override
            public void onChanged(T t) {
                if (mLastVersion >= mLiveData.mVersion) { // 如果已经发送过了，则不发送
                    if (mIsSticky && mLiveData.mStickyData != null) { // 如果需要接收粘性事件，则继续发送
                        mObserver.onChanged((T) mLiveData.mStickyData);
                    }
                    return;
                }
                mLastVersion = mLiveData.mVersion;
                mObserver.onChanged(t);
            }
        }
    }


}
