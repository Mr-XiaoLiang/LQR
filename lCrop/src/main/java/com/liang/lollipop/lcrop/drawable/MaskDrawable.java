package com.liang.lollipop.lcrop.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Lollipop on 2017/08/02.
 * 蒙版的绘制Drawable 基类
 */
public abstract class MaskDrawable extends Drawable {

    public abstract void setMaskBounds(float left, float top, float right, float bottom);

    public abstract void setMaskColor(int color);

    /**
     * 绘制的回调方法
     * 此方法中的Canvas为View的Canvas
     * 因此大小等于View的大小，如果绘制了不透明的图像
     * 可能影响View的整体外观
     * @param canvas
     */
    @Override
    public abstract void draw(@NonNull Canvas canvas);

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
