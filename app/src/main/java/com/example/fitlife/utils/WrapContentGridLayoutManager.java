package com.example.fitlife.utils;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * GridLayoutManager that measures all items so RecyclerView with wrap_content
 * inside a ScrollView shows all items instead of clipping.
 */
public class WrapContentGridLayoutManager extends GridLayoutManager {
    public WrapContentGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        int spanCount = getSpanCount();
        int itemCount = state.getItemCount();
        if (itemCount == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }
        View firstView = recycler.getViewForPosition(0);
        if (firstView != null) {
            measureChild(firstView, widthSpec, heightSpec);
            int width = View.MeasureSpec.getSize(widthSpec);
            int itemHeight = firstView.getMeasuredHeight();
            int rows = (itemCount + spanCount - 1) / spanCount;
            int height = itemHeight * rows;
            setMeasuredDimension(width, height);
            recycler.recycleView(firstView);
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }
}
