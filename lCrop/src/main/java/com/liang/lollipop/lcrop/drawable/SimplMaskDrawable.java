package com.liang.lollipop.lcrop.drawable;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.NonNull;

/**
 * Created by Lollipop on 2017/08/02.
 * 简单的蒙版实现类
 * 仅仅实现了圆形以及矩形的蒙版效果
 * 并没有做过多的细节描绘
 */
public class SimplMaskDrawable extends BaseMaskDrawable {

    private WindowType windowType;

    private Path circularPath;

    public SimplMaskDrawable(WindowType windowType) {
        this.windowType = windowType;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        switch (windowType){
            case RECTANGULAR:
                drawRectangular(canvas);
                break;
            case OVAL:
                drawOval(canvas);
                break;
        }
    }

    /**
     * 绘制圆形的阴影框
     * @param canvas View的canvas
     */
    private void drawOval(Canvas canvas){
        canvas.save();
        canvas.clipPath(getCircularPath(), Region.Op.DIFFERENCE);
        canvas.drawColor(getMaskColor());
        canvas.restore();
    }

    /**
     * 绘制矩形的阴影框
     * @param canvas View的canvas
     */
    private void drawRectangular(Canvas canvas){
        canvas.save();
        canvas.clipRect(getMaskBounds(), Region.Op.DIFFERENCE);
        canvas.drawColor(getMaskColor());
        canvas.restore();
    }

    private Path getCircularPath(){
        if(circularPath==null)
            circularPath = new Path();
        circularPath.reset();
        circularPath.addOval(getMaskBounds(), Path.Direction.CW);
        return circularPath;
    }

    public enum WindowType{
        /**
         * 绘制矩形的一个阴影框
         */
        RECTANGULAR,
        /**
         * 绘制一个圆形的阴影框
         */
        OVAL
    }

}
