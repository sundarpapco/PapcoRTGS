package com.papco.sundar.papcortgs.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.papco.sundar.papcortgs.R;

/**
 * Created by msund on 3/18/2018.
 */

public class DividerDecoration extends RecyclerView.ItemDecoration {

    ShapeDrawable divider;
    Context context;

    private int leftmargin,rightmargin,topmargin,bottommargin=0;
    int endSpacing=0;

    public DividerDecoration(Context context){

        this.context=context;
        this.divider=prepareDivider(1,context.getResources().getColor(R.color.colorPrimary));
        endSpacing=getPixelValue(64);

    }

    public DividerDecoration(Context context, int thicknessInDp,int color) {

        this(context);
        divider.setIntrinsicHeight(getPixelValue(thicknessInDp));
        divider.getPaint().setColor(color);
    }

    public DividerDecoration(Context context,int color){

        this(context);
        divider.getPaint().setColor(color);

    }



    public DividerDecoration setMargins(int leftMargin, int rightMargin){

        if(leftMargin<0)
            leftmargin=0;
        else
            leftmargin=getPixelValue(leftMargin);

        if(rightMargin<0)
            rightmargin=0;
        else
            rightmargin=getPixelValue(rightMargin);


        return this;
    }



    private ShapeDrawable prepareDivider(int thickness,int color){
        ShapeDrawable divider=new ShapeDrawable(new RectShape());
        int heightInPixel=getPixelValue(thickness);
        divider.setIntrinsicHeight(heightInPixel);
        divider.getPaint().setColor(color);
        return divider;
    }

    public DividerDecoration setEndSpacing(int endSpacingInDp){

        if(endSpacingInDp<0)
            endSpacing=0;
        else
            endSpacing=getPixelValue(endSpacingInDp);

        return this;

    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {

        //leftmargin=context.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        //rightmargin=(parent.getWidth()-parent.getPaddingEnd())-leftmargin;
        //leftmargin=sixteen_dp;
        //rightmargin=parent.getWidth()-sixteen_dp;
        int rightMargin=parent.getWidth()-rightmargin;

        for(int i=0;i<parent.getChildCount();++i){

            View child=parent.getChildAt(i);
            RecyclerView.LayoutParams params=(RecyclerView.LayoutParams)child.getLayoutParams();

            topmargin=child.getBottom()+params.bottomMargin;
            bottommargin=topmargin+divider.getIntrinsicHeight();

            divider.setBounds(leftmargin,topmargin,rightMargin,bottommargin);
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
            outRect.bottom=endSpacing;
            return;
        }

        outRect.bottom=divider.getIntrinsicHeight();
    }

    private int getPixelValue(int Dp){

        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,Dp,context.getResources().getDisplayMetrics());

    }
}
