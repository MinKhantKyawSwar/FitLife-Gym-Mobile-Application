package com.example.fitlife.utils;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * LinearLayoutManager that measures all items so RecyclerView with wrap_content
 * inside a ScrollView shows all items instead of clipping.
 */
public class WrapContentLinearLayoutManager extends LinearLayoutManager {
    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {
        View firstView = recycler.getViewForPosition(0);
        if (firstView != null) {
            measureChild(firstView, widthSpec, heightSpec);
            int width = View.MeasureSpec.getSize(widthSpec);
            int height = firstView.getMeasuredHeight() * state.getItemCount();
            setMeasuredDimension(width, height);
            recycler.recycleView(firstView);
        } else {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }
}
