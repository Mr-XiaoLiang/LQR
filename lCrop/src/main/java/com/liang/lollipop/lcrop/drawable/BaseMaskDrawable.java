package com.liang.lollipop.lcrop.drawable;

import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Created by Lollipop on 2017/08/02.
 * 简单的蒙版绘制类
 */
public abstract class BaseMaskDrawable extends MaskDrawable {

    private static final RectF ZERO_MASK_BOUNDS = new RectF();

    private RectF baseMaskBounds = ZERO_MASK_BOUNDS;
    private int baseMaskColor = Color.TRANSPARENT;

    @Override
    public void setMaskColor(int color) {
        if(baseMaskColor!=color){
            baseMaskColor = color;
            onMaskColorChange(baseMaskColor);
            invalidateSelf();
        }
    }


    @Override
    public void setMaskBounds(float left, float top, float right, float bottom) {
        RectF oldBounds = baseMaskBounds;

        if (oldBounds == ZERO_MASK_BOUNDS) {
            oldBounds = baseMaskBounds = new RectF();
        }

        if (oldBounds.left != left || oldBounds.top != top ||
                oldBounds.right != right || oldBounds.bottom != bottom) {
            if (!oldBounds.isEmpty()) {
                // first invalidate the previous bounds
                invalidateSelf();
            }
            baseMaskBounds.set(left, top, right, bottom);
            onMaskBoundsChange(baseMaskBounds);
            invalidateSelf();
        }
    }

    /**
     * 当蒙版的颜色改变时的回调函数
     * 此函数会在蒙版颜色改变时触发
     * 可以不依赖本函数，但是建议将蒙版绘制为半透明的颜色
     * 为用户提供更大的视野，以及让他们看清楚选择框外的图像
     * @param color
     */
    protected void onMaskColorChange(int color){

    }

    @NonNull
    public RectF getMaskBounds() {
        if(baseMaskBounds==ZERO_MASK_BOUNDS)
            return new RectF();
        return baseMaskBounds;
    }

    /**
     * 蒙版的尺寸范围变化的回调函数
     * 此范围代表了真正的选择框尺寸
     * 如果不按照此尺寸绘制指示物
     * 可能会造成用户的误操作以及不可知的问题
     * @param bounds 选择框范围
     */
    protected void onMaskBoundsChange(RectF bounds) {
    }

    public int getMaskColor() {
        return baseMaskColor;
    }
}
