package com.liang.lollipop.lcrop.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.liang.lollipop.lcrop.R;
import com.liang.lollipop.lcrop.drawable.MaskDrawable;
import com.liang.lollipop.lcrop.drawable.SimplMaskDrawable;

/**
 * Created by Lollipop on 2017/08/02.
 * 图片剪裁的ImageView
 * 实际是剪裁成不同比例的矩形
 * 但是提供不同尺寸的选择框以及只有的选择款样式定义。
 * 图片的加载，使用Glide来加载。
 * 其中使用了非常讨巧的方案。
 * 具体细节请参见代码。
 */
public class LCropImageView extends AppCompatImageView {

    public static final int MaskStyle_NONE = -1;
    public static final int MaskStyle_RECTANGULAR = 0;
    public static final int MaskStyle_OVAL = 1;

    private boolean isDebug = true;

    //待处理的目标bitmap
    private Bitmap targetBitmap;
    //绘制用的画笔
    private Paint drawPaint;
    //参数类
    private Option option;
    //图片尺寸变化的矩阵
    private Matrix matrix;
    //位图的着色器
    private BitmapShader bitmapShader;
    //位置的偏移量
    private Point offsetPoint;
    //拉伸的虚拟尺寸
    private Point extendPoint;
    //绘制的尺寸
    private Point drawBitmapSize;
    //窗口的尺寸边界
    private RectF windowBound;
    //手指的上一次间距
    private float lastTouchSpace = -1;
    //手指的数量
    private int touchSize = 0;
    //上次手指位置（拖拽）
    private Point lastTouchPoint;
    //上一次缩放的结果
    private float lastScale = 1;
    //绘制用的范围
    private Rect drawRect;
    //位移Point
    private Point translatePoint;

    public LCropImageView(Context context) {
        this(context,null);
    }

    public LCropImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LCrop_LCropImageView, defStyleAttr, 0);
        initCropImageView();
        if(option == null)
            option = new Option();
        option.maskColor = a.getColor(R.styleable.LCrop_LCropImageView_maskColor,0x80000000);
        switch (a.getInteger(R.styleable.LCrop_LCropImageView_maskStyle,MaskStyle_RECTANGULAR)){
            case MaskStyle_RECTANGULAR:
                option.maskDrawable = new SimplMaskDrawable(SimplMaskDrawable.WindowType.RECTANGULAR);
                break;
            case MaskStyle_OVAL:
                option.maskDrawable = new SimplMaskDrawable(SimplMaskDrawable.WindowType.OVAL);
                break;
            case MaskStyle_NONE:
            default:
                option.maskDrawable = null;
        }
        if(option.maskDrawable!=null)
            option.maskDrawable.setMaskColor(option.maskColor);
        option.windowMaxHeightWeight = a.getFloat(R.styleable.LCrop_LCropImageView_windowHeightRatio,0.8f);
        option.windowMaxWidthWeight = a.getFloat(R.styleable.LCrop_LCropImageView_windowWidthRatio,0.8f);
        option.windowHeightWeight = a.getInteger(R.styleable.LCrop_LCropImageView_windowHeightWeight,1);
        option.windowWidthWeight = a.getInteger(R.styleable.LCrop_LCropImageView_windowWidthWeight,1);
        a.recycle();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
//        super.setImageDrawable(drawable);
        setImageBitmap(drawableToBitmap(drawable));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        initCropImageView();
//        super.setImageBitmap(bm);
        //如果当前缓存的图片对象不为空并且没有被回收，那么就发起回收并且置空，请求垃圾回收
        if(targetBitmap!=null&&!targetBitmap.isRecycled()){
            targetBitmap.recycle();
            targetBitmap = null;
            System.gc();
        }
        targetBitmap = bm;

//        int drawWidth = getDrawWidth();
//        int drawHeight = getDrawHeight();

        final int oldWidth = drawBitmapSize.x;
        final int oldHeight = drawBitmapSize.y;

        if(targetBitmap==null){
            drawBitmapSize.set(-2,-2);
            initDrawPaint(null);
            L("drawWidth","drawWidth被置空啦");
        }else{
            drawBitmapSize.set(targetBitmap.getWidth(),targetBitmap.getHeight());
            extendPoint.set(drawBitmapSize.x,drawBitmapSize.y);
            initDrawPaint(targetBitmap);
            L("drawWidth","drawWidth被赋值啦-- "+drawBitmapSize.toString());
        }

        offsetPoint.set(0,0);
        onBitmapChange();

        if(oldWidth!=drawBitmapSize.x||oldHeight!=drawBitmapSize.y){
            requestLayout();
        }
        invalidate();
    }

    /**
     * 将Drawable转换为Bitmap
     * 兼容ImageView的部分设置方法
     * @param drawable drawable绘制画板
     * @return 返回Bitmap
     */
    private Bitmap drawableToBitmap(Drawable drawable){
        if(drawable==null)
            return null;
        Bitmap bitmap;
        //如果是BitmapDrawable，那么就直接去拿位图
        if(drawable instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            bitmap = bitmapDrawable.getBitmap();
        }else{
            //如果并不是位图对象，那么就去获取他的尺寸
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            //当位图本身没有尺寸时，就去获取View的尺寸
            if(width<0)
                width = getMeasuredWidth();
            if(height<0)
                height = getMeasuredHeight();
            //如果最后尺寸仍然为空，那么就返回空，放弃本次获取
            if(width<1||height<1)
                return null;
            //根据属性来新建一个位图，并且将Drawable绘制上去
            bitmap= Bitmap.createBitmap(width,height,
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        //保存Canvas的状态，并且偏移画板，然后绘制矩形
        //模拟用户手指拖拽图片之后的效果
        canvas.save();
        Point t = getTranslate();
        canvas.translate(t.x,t.y);
        canvas.drawRect(getDrawRect(t),drawPaint);
        canvas.restore();
        //如果蒙版存在，就让画板绘制
        if(option.maskDrawable!=null)
            option.maskDrawable.draw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
//        canvas.drawColor(0x8000FF00);
        canvas.drawRect(windowBound,paint);
    }

    private Point getTranslate(){
        if(translatePoint==null)
            initCropImageView();
        //反向移动绘制坐标系，模拟图片被拖拽效果
        int x = offsetPoint.x+getPaddingLeft();
        int y = offsetPoint.y+getPaddingTop();
        //如果左侧拖拽到了窗口的边界，那么就拒绝移动
        if(x>windowBound.left)
            x = (int) (windowBound.left);
        //如果右侧拖拽到了窗口边界，那么就拒绝继续拖拽
        if(extendPoint.x-x<windowBound.right)
            x = (int) (extendPoint.x-windowBound.right);
        //如果顶部拖拽超过了窗口，那么就等于窗口
        if(y>windowBound.top)
            y = (int) (windowBound.top);
        //如果底部超过了窗口，那么就等于窗口
        if(extendPoint.y-y<windowBound.bottom)
            y = (int) (extendPoint.y-windowBound.bottom);

        translatePoint.set(x,y);
        return translatePoint;
    }

    private Rect getDrawRect(Point translate){
        //默认是全屏显示
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = getWidth()-getPaddingRight();
        float bottom = getHeight()-getPaddingBottom();

        //如果边界被拖拽到了屏幕以内，那么就绘制边界内容
        if(-translate.x>left)
            left = -translate.x;

        if(-translate.y>top)
            top = -translate.y;

        if(extendPoint.x-translate.x<right)
            right = extendPoint.x-translate.x;

        if(extendPoint.y-translate.y<bottom)
            bottom = extendPoint.y-translate.y;

        if(drawRect==null)
            initCropImageView();
        drawRect.set((int)left,(int)top,(int)right,(int)bottom);
        L("getDrawRect","PaddingLeft:"+getPaddingLeft()+",PaddingTop:"+getPaddingTop()
                +",PaddingRight:"+getPaddingRight()+",PaddingBottom:"+getPaddingBottom()
                +",translate:"+translate.toString()+",drawRect:"+drawRect.toString());
        return drawRect;
    }

    /**
     * 初始化绘制的画笔
     * @param bitmap 初始化绘制的画笔
     */
    private void initDrawPaint(Bitmap bitmap){
        initCropImageView();
        //如果不为空，那么就置空
        if(bitmapShader!=null||bitmap==null){
            bitmapShader = null;
            drawPaint.setShader(null);
        }
        if(bitmap!=null){
            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            drawPaint.setShader(bitmapShader);
        }
    }

    private void onBitmapChange(){
        L("drawWidth","drawWidth是个啥-- "+getDrawWidth());
        if(bitmapShader==null)
            return;
        if(extendPoint.x<=0||extendPoint.y<=0)
            return;
        if(matrix==null)
            initCropImageView();
        if(getDrawWidth()<1||getDrawHeight()<1)
            return;

        //检查是否到了小尺寸
        if(extendPoint.x < windowBound.width()){
            float w = windowBound.width()/extendPoint.x;
            extendPoint.set((int)(extendPoint.x * w),(int)(extendPoint.y * w));
        }
        if(extendPoint.y < windowBound.height()){
            float w = windowBound.height()/extendPoint.y;
            extendPoint.set((int)(extendPoint.x * w),(int)(extendPoint.y * w));
        }
        //检查是否到了边界
        if(offsetPoint.x>windowBound.left)
            offsetPoint.x = (int) windowBound.left;
        if(offsetPoint.y>windowBound.top)
            offsetPoint.y = (int) windowBound.top;
        if(offsetPoint.x+extendPoint.x<windowBound.right)
            offsetPoint.x = (int) windowBound.right-extendPoint.x;
        if(offsetPoint.y+extendPoint.y<windowBound.bottom)
            offsetPoint.y = (int) windowBound.bottom-extendPoint.y;
        L("onBitmapChange","windowBound:"+windowBound.toString()+",extendPoint:"+extendPoint.toString()+",offsetPoint:"+offsetPoint.toString());

        float scaleX = extendPoint.x * 1.0f / getDrawWidth();
        float scaleY = extendPoint.y * 1.0f / getDrawHeight();
//        float scale = scaleX > scaleY ? scaleX : scaleY;
        float scale = Math.max(scaleX , scaleY);
        //这里有个神坑，每次的赋值矩阵效果，都是相对于上一次叠加的，
        // 所以需要记录上一次变换后最终的比例，然后再计算相对于上次比例的相对比例
        float finalScale = (scale-lastScale)/lastScale+1;
        matrix.postScale(finalScale,finalScale,0,0);
//        offsetPoint.set((int)(offsetPoint.x*finalScale),(int)(offsetPoint.y*finalScale));
        bitmapShader.setLocalMatrix(matrix);
        L("onBitmapChange","scale:"+scale+",lastScale:"+lastScale+",finalScale:"+finalScale);
        lastScale = scale;
        invalidate();
    }

    private void initCropImageView(){
        if(drawPaint==null){
            drawPaint = new Paint();
            drawPaint.setAntiAlias(true);
            drawPaint.setDither(true);
        }
        if(matrix==null)
            matrix = new Matrix();
        if(offsetPoint==null)
            offsetPoint = new Point(0,0);
        if(extendPoint==null)
            extendPoint = new Point(0,0);
        if(windowBound==null)
            windowBound = new RectF();
        if(drawBitmapSize==null)
            drawBitmapSize = new Point(-1,-1);
        if(lastTouchPoint==null)
            lastTouchPoint = new Point(-1,-1);
        if(drawRect==null)
            drawRect = new Rect(0,0,0,0);
        if(translatePoint==null)
            translatePoint = new Point();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initCropImageView();
        requestOptionChange();
        onBitmapChange();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(targetBitmap==null)
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(targetBitmap==null)
            return;
        int widthSize = measureWidth(widthMeasureSpec);
        int heightSize = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = getDrawWidth() + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = getDrawHeight() + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                touchSize = 1;
                lastTouchPoint.set((int)event.getX(),(int)event.getY());
                break;
            case MotionEvent.ACTION_UP:
                touchSize = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                touchSize++;
                if(touchSize>=2){
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);
                    lastTouchSpace = getPointSpace(x1,y1,x2,y2);
                }
                if(touchSize==1)
                    lastTouchPoint.set((int)event.getX(),(int)event.getY());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                touchSize--;
                if(touchSize==1)
                    lastTouchPoint.set((int)event.getX(),(int)event.getY());
                if(touchSize>=2){
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);
                    lastTouchSpace = getPointSpace(x1,y1,x2,y2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;
        }
        return true;
    }

    private void onTouchMove(MotionEvent event){
        if(touchSize>1){
            float x1 = event.getX(0);
            float y1 = event.getY(0);
            float x2 = event.getX(1);
            float y2 = event.getY(1);
            int space = getPointSpace(x1,y1,x2,y2);
            if(lastTouchSpace<0)
                lastTouchSpace = space;
            float offset = space-lastTouchSpace;
            int diagonal = getPointSpace(0,0,extendPoint.x,extendPoint.y);
            offset = (diagonal + offset)/diagonal;
            extendPoint.set((int)(extendPoint.x * offset),(int)(extendPoint.y * offset));
            lastTouchSpace = space;
            L("onTouchMove","x1:"+x1+",y1:"+y1+",x2:"+x2+",y2:"+y2+",space:"+space+",offset:"+offset+",extendPoint:"+extendPoint.toString());
        }else if(touchSize>0){
            float touchX = event.getX();
            float touchY = event.getY();
            float x = offsetPoint.x+(touchX-lastTouchPoint.x);
            float y = offsetPoint.y+(touchY-lastTouchPoint.y);
            offsetPoint.set((int)x,(int)y);
            L("onTouchMove","touchX:"+touchX+",touchY:"+touchY+",lastTouchPoint:"+lastTouchPoint.toString()+",offsetPoint:"+offsetPoint.toString());
            lastTouchPoint.set((int)touchX,(int)touchY);
        }
        onBitmapChange();
    }

    private int getPointSpace(float x1,float y1,float x2,float y2){
        return (int) Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }

    public static class Option{

        /**
         * 选择视窗的高度比例，相对于View本身来说的高度比例
         * 并且绝对居中
         * 此值并不是最后的视窗高度
         * 最后视窗高度将是此值范围内的符合剪裁比例的窗口
         */
        @FloatRange(from = 0,to = 1)
        private float windowMaxHeightWeight = 0.8f;
        /**
         * 选择视窗的宽度比例，相对于View本身来说的宽度比例
         * 并且绝对居中
         * 此值并不是最后的视窗宽度
         * 最后视窗宽度度将是此值范围内的符合剪裁比例的窗口
         */
        @FloatRange(from = 0,to = 1)
        private float windowMaxWidthWeight = 0.8f;
        /**
         * 视窗的纵横比中的纵向权重
         * 用于和横向比对比
         */
        private int windowHeightWeight = 1;
        /**
         * 视窗的纵横比中的横向权重
         * 用于和横向比对比
         */
        private int windowWidthWeight = 1;
        /**
         * 蒙版的颜色
         */
        private int maskColor = 0x80000000;
        /**
         * 蒙版的绘制对象
         */
        private MaskDrawable maskDrawable;

        public float getWindowMaxHeightWeight() {
            return windowMaxHeightWeight;
        }

        public Option setWindowMaxHeightWeight(float windowMaxHeightWeight) {
            this.windowMaxHeightWeight = windowMaxHeightWeight;
            return this;
        }

        public float getWindowMaxWidthWeight() {
            return windowMaxWidthWeight;
        }

        public Option setWindowMaxWidthWeight(float windowMaxWidthWeight) {
            this.windowMaxWidthWeight = windowMaxWidthWeight;
            return this;
        }

        public int getWindowHeightWeight() {
            return windowHeightWeight;
        }

        public Option setWindowHeightWeight(int windowHeightWeight) {
            this.windowHeightWeight = windowHeightWeight;
            return this;
        }

        public int getWindowWidthWeight() {
            return windowWidthWeight;
        }

        public Option setWindowWidthWeight(int windowWidthWeight) {
            this.windowWidthWeight = windowWidthWeight;
            return this;
        }

        public int getMaskColor() {
            return maskColor;
        }

        public Option setMaskColor(int maskColor) {
            this.maskColor = maskColor;
            return this;
        }

        public MaskDrawable getMaskDrawable() {
            return maskDrawable;
        }

        public Option setMaskDrawable(MaskDrawable maskDrawable) {
            this.maskDrawable = maskDrawable;
            return this;
        }

        private Option() {}
    }

    public void requestOptionChange(){
        int width = getWidth()-getPaddingLeft()-getPaddingRight();
        int height = getHeight()-getPaddingBottom()-getPaddingTop();
        float windowMaxHeight = height*option.windowMaxHeightWeight;
        float windowMaxWidth = width*option.windowMaxWidthWeight;
        float weight = Math.min(windowMaxHeight/option.windowHeightWeight,windowMaxWidth/option.windowWidthWeight);
        float windowWidth = weight*option.windowWidthWeight;
        float windowHeight = weight*option.windowHeightWeight;
        float windowLeft = (width-windowWidth)*0.5f+getPaddingLeft();
        float windowTop = (height-windowHeight)*0.5f+getPaddingTop();
        float windowRight = windowLeft+windowWidth;
        float windowBottom = windowTop+windowHeight;
        windowBound.set(windowLeft,windowTop,windowRight,windowBottom);
        if(option.maskDrawable!=null){
            option.maskDrawable.setBounds(getPaddingLeft(),getPaddingTop(),width+getPaddingLeft(),height+getPaddingTop());
            option.maskDrawable.setMaskBounds(windowLeft,windowTop,windowRight,windowBottom);
        }
        if(option.maskDrawable!=null)
            option.maskDrawable.setMaskColor(option.maskColor);
    }

    public Option getOption() {
        return option;
    }

    private int getDrawHeight() {
        if(targetBitmap==null)
            return -1;
        if(drawBitmapSize!=null)
            return drawBitmapSize.y;
        return targetBitmap.getHeight();
    }

    private int getDrawWidth() {
        if(targetBitmap==null)
            return -1;
        if(drawBitmapSize!=null)
            return drawBitmapSize.x;
        return targetBitmap.getWidth();
    }

    private void L(String name,String log){
        if(isDebug)
            Log.e(name,log);
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(targetBitmap!=null&&!targetBitmap.isRecycled()){
            targetBitmap.recycle();
            targetBitmap = null;
        }
        if(bitmapShader!=null){
            bitmapShader = null;
        }
        if(option.maskDrawable!=null)
            option.maskDrawable.setVisible(false, false);
        System.gc();
    }
}
