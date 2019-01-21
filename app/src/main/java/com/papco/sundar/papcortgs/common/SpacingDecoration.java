package com.papco.sundar.papcortgs.common;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

public class SpacingDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;

    Context context;
    int topSpacing, bottomSpacing, itemSpacing;
    int orientation;

    public SpacingDecoration(Context context, int orientation, float topSpacingDp, float itemSpacingDp, float bottomSpacingDp) {
        this.context = context;

        //converting the given dp value to pixels
        topSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topSpacingDp, context.getResources().getDisplayMetrics());
        bottomSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomSpacingDp, context.getResources().getDisplayMetrics());
        itemSpacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemSpacingDp, context.getResources().getDisplayMetrics());
        this.orientation = orientation;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (parent.getChildAdapterPosition(view) == 0) {
            if (orientation == HORIZONTAL)
                outRect.left = (int) topSpacing;
            else
                outRect.top = (int) topSpacing;
        }


        int spacing=parent.getChildAdapterPosition(view)==state.getItemCount()-1?bottomSpacing:itemSpacing;

        if (orientation == HORIZONTAL)
            outRect.right = spacing;
        else
            outRect.bottom = spacing;


    }

}
