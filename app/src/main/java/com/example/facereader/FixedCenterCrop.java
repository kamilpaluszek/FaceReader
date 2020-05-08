package com.example.facereader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class FixedCenterCrop extends androidx.appcompat.widget.AppCompatImageView {

    final int MAX_SCALE_FACTOR = 2;
    public FixedCenterCrop(final Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Drawable d = this.getDrawable();

        if (d != null) {
            // ceil not round - avoid thin vertical gaps along the left/right edges
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            if (width > (d.getIntrinsicWidth()*MAX_SCALE_FACTOR)) width = d.getIntrinsicWidth()*MAX_SCALE_FACTOR;
            final int height = (int) Math.ceil(width * (float) d.getIntrinsicHeight() / d.getIntrinsicWidth());
            this.setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}