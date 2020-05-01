package com.example.facereader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class ImageHelper {

    public static Bitmap drawRectOnBitmap(Bitmap mBitmap, int arraySize, String rectx[], String recty[], String rectheigh[], String rectwidth[], String status[])
    {
        Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(8);

        for (int i = 0; i<arraySize; i++) {
            canvas.drawRect(Float.parseFloat(rectx[i]),
                    Float.parseFloat(recty[i]),
                    Float.parseFloat(rectx[i]) + Float.parseFloat(rectheigh[i]),
                    Float.parseFloat(recty[i]) + Float.parseFloat(rectwidth[i]),
                    paint);

            float cX = Float.parseFloat(rectx[i]) + Float.parseFloat(rectwidth[i]);
            float cY = Float.parseFloat(recty[i]) + Float.parseFloat(rectheigh[i]);

            drawTextOnBitmap(canvas, 50, cX / 2 + cX / 5, cY + 70, Color.WHITE, status[i]);
        }
        return bitmap;
    }

    private static void drawTextOnBitmap(Canvas canvas, int textSize, float cX, float cY, int color, String status)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSize);

        canvas.drawText(status, cX, cY, paint);
    }

}
