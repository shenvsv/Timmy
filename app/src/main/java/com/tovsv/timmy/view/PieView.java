package com.tovsv.timmy.view;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.utils.Utils;
import com.tovsv.timmy.app.Constants;
import com.tovsv.timmy.listener.PieTouchListener;
import com.tovsv.timmy.model.AppInfo;
import com.tovsv.timmy.util.AppInfoList;
import com.tovsv.timmy.util.DLog;

import java.util.ArrayList;


/**
 * Created by shenvsv on 14/11/6.
 */
public class PieView extends View implements ValueAnimator.AnimatorUpdateListener {
    private boolean isDataSet;
    //paints
    private Paint mInfoPaint;
    private Canvas mDrawCanvas;
    protected Paint mDrawPaint;
    private Paint mHolePaint;

    //
    private float mRotationAngle = 270f;
    private float mSliceSpace = 0f;
    private float mHoleRadiusPercent = 50f;

    private float mOffsetLeft = 0;
    private float mOffsetTop = 0;
    private float mOffsetRight = 0;
    private float mOffsetBottom = 0;

    private float mPhaseY = 1f;
    private float mStartAngle = 0f;

    private int mBackgroundColor = Color.WHITE;

    private ArrayList<Float> mAngles;

    //
//    AppInfoList mList;
    private Bitmap mDrawBitmap;
    private Paint mRenderPaint;
    protected RectF mContentRect = new RectF();
    private RectF mCircleBox = new RectF();
    private PieTouchListener mListener;
    private ObjectAnimator mAnimatorY;



    public PieView(Context context) {
        super(context);
        init();
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Utils.init(getContext().getResources());
        mListener = new PieTouchListener(this);
        isDataSet = false;
        initPaints();
    }

    private void prepare(){
        prepareContent();
    }

    private void prepareContent(){

        mOffsetBottom = (int) Utils.convertDpToPixel(0);
        mOffsetLeft = (int) Utils.convertDpToPixel(0);
        mOffsetRight = (int) Utils.convertDpToPixel(0);
        mOffsetTop = (int) Utils.convertDpToPixel(0);

        mContentRect.set(mOffsetLeft,
                mOffsetTop,
                getWidth() - mOffsetRight,
                getHeight() - mOffsetBottom);

    }

    public float getDiameter() {
        if (mContentRect == null)
            return 0;
        else
            return Math.min(mContentRect.width(), mContentRect.height());
    }

    public PointF getCenter() {
        return new PointF(getWidth() / 2f, getHeight() / 2f);
    }

    private void initPaints(){
        // initialize the utils

        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(Color.rgb(247, 189, 51)); // orange
        mInfoPaint.setTextAlign(Paint.Align.CENTER);
        mInfoPaint.setTextSize(Utils.convertDpToPixel(12f));

        mRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRenderPaint.setStyle(Paint.Style.FILL);

        mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG); //抗锯齿
        mHolePaint.setColor(Color.rgb(254,255,255));

        mDrawPaint = new Paint();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isDataSet){
            canvas.drawText("no data ll", getWidth() / 2, getHeight() / 2, mInfoPaint);
            return;
        }

        if (mDrawBitmap == null || mDrawCanvas == null) {
            // use RGB_565 for best performance
            mDrawBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
            mDrawCanvas = new Canvas(mDrawBitmap);
        }
        mDrawCanvas.drawColor(mBackgroundColor);
        drawData();
        drawHole();

        canvas.drawBitmap(mDrawBitmap, 0, 0, mDrawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // use the pie- and radarchart listener own listener
        if (mListener != null)
            return mListener.onTouch(this, event);
        else
            return super.onTouchEvent(event);
    }



    public float getRadius() {
        return getDiameter()/2f;
    }



    private void drawData() {

        float angle = mRotationAngle;

        for (int i = 0;i<mAngles.size();i++) {
            float a = mAngles.get(i);
            mRenderPaint.setColor(Constants.colors[i]);

            float boxSize = getDiameter() / 2f;
            boxSize *= (float) Math.pow(0.95, i);

            PointF c = getCenter();
            mCircleBox.set(c.x - boxSize, c.y - boxSize, c.x + boxSize, c.y + boxSize);
            if(i == 2){
                mDrawCanvas.drawArc(mCircleBox, mRotationAngle - a + mSliceSpace / 2f, a * mPhaseY
                        - mSliceSpace / 2f, true, mRenderPaint);
            }else {
                mDrawCanvas.drawArc(mCircleBox, angle + mSliceSpace / 2f, a * mPhaseY
                        - mSliceSpace / 2f, true, mRenderPaint);
                angle += a;
            }
        }



        //todo draw

    }

    private void drawHole() {

        float radius = getRadius();

        PointF c = getCenter();

        int color = mHolePaint.getColor();
//        // draw the hole-circle
        mDrawCanvas.drawCircle(c.x, c.y,
                radius / 100 * mHoleRadiusPercent, mHolePaint);
//        // make transparent
        mHolePaint.setColor(Color.rgb(255,65,129));
//        // draw the transparent-circle
        mDrawCanvas.drawCircle(c.x, c.y,
                mPhaseY * radius / 100 * mHoleRadiusPercent /2, mHolePaint);
        mHolePaint.setColor(color);

    }

    public void setData(AppInfoList list){
        isDataSet = true;
        mAngles = new ArrayList<Float>();
        if (list.size() > 3){
            float angle = 0f;
            for (int i = 0;i < 3;i++){
                AppInfo info = list.get(i);
                float a = 360f * info.duration / list.getTotleTime();
                mAngles.add(a);
                angle += a;
            }
            mAngles.add(360f-angle);
        }else {
            for (AppInfo info :list){
                float a = 360f * info.duration / list.getTotleTime();
                mAngles.add(a);
            }
        }
//        mAngles.add(30f);
//        mAngles.add(30f);
//        mAngles.add(30f);
//        mAngles.add(270f);
        prepare();
    }

    public void clean(){
        isDataSet = false;
    }



    public void setStartAngle(float x, float y) {

        mStartAngle = getAngleForPoint(x, y);

        // take the current angle into consideration when starting a new drag
        mStartAngle -= mRotationAngle;
    }

    public void updateRotation(float x, float y) {

        mRotationAngle = getAngleForPoint(x, y);

        // take the offset into consideration
        mRotationAngle -= mStartAngle;

        // keep the angle >= 0 and <= 360
        mRotationAngle = (mRotationAngle + 360f) % 360f;
    }

    public float getAngleForPoint(float x, float y) {

        PointF c = getCenter();

        double tx = x - c.x, ty = y - c.y;
        double length = Math.sqrt(tx * tx + ty * ty);
        double r = Math.acos(ty / length);

        float angle = (float) Math.toDegrees(r);

        if (x > c.x)
            angle = 360f - angle;

        // add 90° because chart starts EAST
        angle = angle + 90f;

        // neutralize overflow
        if (angle > 360f)
            angle = angle - 360f;

        return angle;
    }

    public float distanceToCenter(float x, float y) {

        PointF c = getCenter();

        float dist = 0f;

        float xDist = 0f;
        float yDist = 0f;

        if (x > c.x) {
            xDist = x - c.x;
        } else {
            xDist = c.x - x;
        }

        if (y > c.y) {
            yDist = y - c.y;
        } else {
            yDist = c.y - y;
        }

        dist = (float) Math.sqrt(Math.pow(xDist, 2.0) + Math.pow(yDist, 2.0));

        return dist;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    public void animateY(int durationMillis) {
        mAnimatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f);
        mAnimatorY.setDuration(durationMillis);
        mAnimatorY.addUpdateListener(this);
        mAnimatorY.start();
    }

    public float getPhaseY() {
        return mPhaseY;
    }

    public void setPhaseY(float phase) {
        mPhaseY = phase;
    }
//    public int getIndexForAngle(float angle) {
//        float a = (angle - mRotationAngle + 360) % 360f;
//        return 0;
//    }
}
