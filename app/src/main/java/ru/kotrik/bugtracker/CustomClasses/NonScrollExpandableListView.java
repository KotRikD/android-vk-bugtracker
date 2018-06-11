package ru.kotrik.bugtracker.CustomClasses;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Created by kotoriku on 24.03.2018.
 */

public class NonScrollExpandableListView extends ExpandableListView {

    public NonScrollExpandableListView(Context context) {
        super(context);
    }

    public NonScrollExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}