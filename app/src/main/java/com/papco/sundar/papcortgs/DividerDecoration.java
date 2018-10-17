package com.papco.sundar.papcortgs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by msund on 3/18/2018.
 */

public class DividerDecoration extends RecyclerView.ItemDecoration {

    Drawable divider;
    Context context;
    int sixteen_dp;

    public DividerDecoration(Context context, Drawable divider) {
        this.context=context;
        this.divider=divider;
        sixteen_dp=context.getResources().getDimensionPixelSize(R.dimen.sixteen_dp);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int leftmargin,rightmargin,topmargin,bottommargin=0;

        //leftmargin=context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        //rightmargin=(parent.getWidth()-parent.getPaddingEnd())-leftmargin;
        //leftmargin=sixteen_dp;
        //rightmargin=parent.getWidth()-sixteen_dp;
        leftmargin=0;
        rightmargin=parent.getWidth();

        for(int i=0;i<parent.getChildCount();++i){

            View child=parent.getChildAt(i);
            RecyclerView.LayoutParams params=(RecyclerView.LayoutParams)child.getLayoutParams();

            topmargin=child.getBottom()+params.bottomMargin;
            bottommargin=topmargin+divider.getIntrinsicHeight();

            divider.setBounds(leftmargin,topmargin,rightmargin,bottommargin);
            divider.draw(canvas);

        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(parent.getChildAdapterPosition(view)==0) {
            super.getItemOffsets(outRect, view, parent, state);
            return;
        }

        if(parent.getChildAdapterPosition(view)==state.getItemCount()-1) {
            Log.d("decoration:", "view position: "+Integer.toString(parent.getChildAdapterPosition(view)));
            Log.d("decoration:", "child count: "+Integer.toString(parent.getChildCount()));
            outRect.bottom=context.getResources().getDimensionPixelSize(R.dimen.sixteen_dp);
            return;
        }

        outRect.bottom=divider.getIntrinsicHeight();
    }
}
