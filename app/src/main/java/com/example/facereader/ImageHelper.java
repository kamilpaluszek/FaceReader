package com.example.facereader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

//klasa obsługująca operacje na Bitmapach
public class ImageHelper {

    //metoda odpowiedzialna za narysowanie na bitmapie kwadratu twarzy oraz wyniku predykcji emocji
    public static Bitmap drawRectOnBitmap(Bitmap mBitmap, int faces, String rectx[], String recty[], String rectheigh[], String rectwidth[], String emotions[])
    {
        //skopiowanie oryginalnej mapy do zmiennej
        Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //nowy canvas - bedziemy po nim rysowac
        Canvas canvas = new Canvas(bitmap);

        //ustawienie opcji rysowania
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(8);

        //narysowanie na bitmapie kwadratu dla kazdej twarzy
        for (int i = 0; i<faces; i++) {
            canvas.drawRect(Float.parseFloat(rectx[i]),
                    Float.parseFloat(recty[i]),
                    Float.parseFloat(rectx[i]) + Float.parseFloat(rectheigh[i]),
                    Float.parseFloat(recty[i]) + Float.parseFloat(rectwidth[i]),
                    paint);

            //zmienne pomocnicze dla narysowania napisu emocji
            float cX = Float.parseFloat(rectx[i]) + Float.parseFloat(rectwidth[i]);
            float cY = Float.parseFloat(recty[i]) + Float.parseFloat(rectheigh[i]);

            drawTextOnBitmap(canvas, 50, cX / 2 + cX / 5, cY + 70, Color.WHITE, emotions[i]);
        }
        return bitmap;
    }

    //funkcja odpowiedzialna za narysowanie napisu emocji na canvasie
    private static void drawTextOnBitmap(Canvas canvas, int textSize, float cX, float cY, int color, String emotion)
    {
        //ustawienie opcji rysowania
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSize);

        canvas.drawText(emotion, cX, cY, paint);
    }

    //funkcja służąca do zmniejszenia naszego wybranego obrazu
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {

        //obliczenie stosunku rozdzielczosci naszego zdjecia do maksymalnej dopuszczalnej wielkosci naszego zdjecia
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        //szerokosc i wysokosc obliczona na podstawie wczesniejszego stosunku
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}
