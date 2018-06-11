package ru.kotrik.bugtracker.CustomClasses;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class CustomItemDecoration extends RecyclerView.ItemDecoration {

    private int spacing;

    public CustomItemDecoration(Context context, int padding) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, metrics);
    }

    @Override
    public final void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position != state.getItemCount() - 1) {
            outRect.bottom = spacing;
        }
    }

}
