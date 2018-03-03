package com.jumproper.fractalfeedback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by jumpr_000 on 8/23/2017.
 */

public class DrawingView extends android.support.v7.widget.AppCompatImageView {
    Paint p;

    public DrawingView(Context context) {
        super(context);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, 20, 20, p);

    }
}
