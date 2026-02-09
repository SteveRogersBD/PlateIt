package com.example.plateit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {
    private static final int MAX_RADIUS = 25;
    private static final int DEFAULT_DOWN_SAMPLING = 1;

    private final Context context;
    private final int radius;
    private final int sampling;

    public BlurTransformation(Context context) {
        this(context, MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context context, int radius) {
        this(context, radius, DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context context, int radius, int sampling) {
        this.context = context.getApplicationContext();
        this.radius = radius;
        this.sampling = sampling;
    }

    @Override
    public Bitmap transform(Bitmap source) {

        int scaledWidth = source.getWidth() / sampling;
        int scaledHeight = source.getHeight() / sampling;

        Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) sampling, 1 / (float) sampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);

        source.recycle();
        rs.destroy(); // Properly destroy RenderScript to avoid memory leaks

        return bitmap;
    }

    @Override
    public String key() {
        return "BlurTransformation(radius=" + radius + ", sampling=" + sampling + ")";
    }
}
