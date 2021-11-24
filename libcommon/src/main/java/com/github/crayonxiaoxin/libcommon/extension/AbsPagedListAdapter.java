package com.github.crayonxiaoxin.libcommon.extension;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbsPagedListAdapter<T, VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T, VH> {
    private SparseArray<View> mHeaders = new SparseArray<>();
    private SparseArray<View> mFooters = new SparseArray<>();
    private int BASE_ITEM_TYPE_HEADER = 100_000;
    private int BASE_ITEM_TYPE_FOOTER = 200_000;

    protected AbsPagedListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void addHeaderView(View header) {
        if (mHeaders.indexOfValue(header) < 0) {
            mHeaders.put(BASE_ITEM_TYPE_HEADER++, header);
            notifyDataSetChanged();
        }
    }

    public void removeHeaderView(View header) {
        int index = mHeaders.indexOfValue(header);
        if (index >= 0) {
            mHeaders.removeAt(index);
            notifyDataSetChanged();
        }
    }

    public void addFooterView(View footer) {
        if (mFooters.indexOfValue(footer) < 0) {
            mFooters.put(BASE_ITEM_TYPE_FOOTER++, footer);
            notifyDataSetChanged();
        }
    }

    public void removeFooterView(View footer) {
        int index = mFooters.indexOfValue(footer);
        if (index >= 0) {
            mFooters.removeAt(index);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return itemCount + mHeaders.size() + mFooters.size();
    }

    public int getOriginalItemCount() {
        return getItemCount() - mHeaders.size() - mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaders.keyAt(position);
        }
        if (isFooterPosition(position)) {
            position = position - getOriginalItemCount() - mHeaders.size();
            return mFooters.keyAt(position);
        }
        position = position - mHeaders.size();
        return super.getItemViewType(position);
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaders.size();
    }

    private boolean isFooterPosition(int position) {
        return position >= getItemCount() - mFooters.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaders.indexOfKey(viewType) >= 0) {
            View header = mHeaders.get(viewType);
            return (VH) new RecyclerView.ViewHolder(header) {
            };
        }
        if (mFooters.indexOfKey(viewType) >= 0) {
            View footer = mFooters.get(viewType);
            return (VH) new RecyclerView.ViewHolder(footer) {
            };
        }
        return onCreateViewHolder2(parent, viewType);
    }

    protected abstract VH onCreateViewHolder2(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            return;
        }
        position = position - mHeaders.size();
        onBindViewHolder2(holder, position);
    }

    protected abstract void onBindViewHolder2(VH holder, int position);

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(new AdapterDataObserverProxy(observer));
    }

    // paging 的坑：如果在网络加载之前添加 header，paging 会找不到正确的位置
    class AdapterDataObserverProxy extends RecyclerView.AdapterDataObserver {
        private RecyclerView.AdapterDataObserver mObserver;

        public AdapterDataObserverProxy(RecyclerView.AdapterDataObserver observer) {
            mObserver = observer;
        }

        @Override
        public void onChanged() {
            mObserver.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mObserver.onItemRangeChanged(positionStart + mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mObserver.onItemRangeChanged(positionStart + mHeaders.size(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mObserver.onItemRangeInserted(positionStart + mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mObserver.onItemRangeRemoved(positionStart + mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mObserver.onItemRangeMoved(fromPosition + mHeaders.size(), toPosition + mHeaders.size(), itemCount);
        }

        @Override
        public void onStateRestorationPolicyChanged() {
            mObserver.onStateRestorationPolicyChanged();
        }
    }
}
