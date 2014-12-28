
package com.tovsv.timmy.listener;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.github.mikephil.charting.utils.Highlight;
import com.tovsv.timmy.view.PieView;


public class PieTouchListener extends SimpleOnGestureListener implements OnTouchListener {

    private PieView mChart;

    private GestureDetector mGestureDetector;

    public PieTouchListener(PieView ctx) {
        this.mChart = ctx;

        mGestureDetector = new GestureDetector(ctx.getContext(), this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {

        if (mGestureDetector.onTouchEvent(e))
            return true;

        // rotation by touch
            float x = e.getX();
            float y = e.getY();

            switch (e.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    mChart.setStartAngle(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mChart.updateRotation(x, y);
                    mChart.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent me) {
        // todo
    };

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    /** reference to the last highlighted object */
    private Highlight mLastHighlight = null;

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        float distance = mChart.distanceToCenter(e.getX(), e.getY());

        // check if a slice was touched
        if (distance > mChart.getRadius() || distance < mChart.getRadius() / 2f) {

        } else {

//            int index = mChart.getIndexForAngle(mChart.getAngleForPoint(e.getX(), e.getY()));

        }

        return true;
    }
}
