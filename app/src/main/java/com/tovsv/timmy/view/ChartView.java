package com.tovsv.timmy.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

import com.github.mikephil.charting.charts.PieRadarChartBase;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

/**
 * Created by shenvsv on 14-10-14.
 */

public class ChartView extends PieRadarChartBase<PieData> {

    /**
     * rect object that represents the bounds of the piechart, needed for
     * drawing the circle
     */
    private RectF mCircleBox = new RectF();

    /**
     * array that holds the width of each pie-slice in degrees
     */
    private float[] mDrawAngles;

    /**
     * array that holds the absolute angle in degrees of each slice
     */
    private float[] mAbsoluteAngles;

    /**
     * if true, the white hole inside the chart will be drawn
     */
    private boolean mDrawHole = true;

    /**
     * variable for the text that is drawn in the center of the pie-chart. If
     * this value is null, the default is "Total Value\n + getYValueSum()"
     */
    private String mCenterText = null;

    /**
     * indicates the size of the hole in the center of the piechart, default:
     * radius / 2
     */
    private float mHoleRadiusPercent = 50f;

    /**
     * the radius of the transparent circle next to the chart-hole in the center
     */
    private float mTransparentCircleRadius = 55f;

    /**
     * if enabled, centertext is drawn
     */
    private boolean mDrawCenterText = true;

    /**
     * set this to true to draw the x-values next to the values in the pie
     * slices
     */
    private boolean mDrawXVals = true;

    /**
     * if set to true, all values show up in percent instead of their real value
     */
    private boolean mUsePercentValues = false;

    /**
     * paint for the hole in the center of the pie chart and the transparent
     * circle
     */
    private Paint mHolePaint;

    /**
     * paint object for the text that can be displayed in the center of the
     * chart
     */
    private Paint mCenterTextPaint;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG); //抗锯齿
        mHolePaint.setColor(Color.WHITE);

        mCenterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterTextPaint.setColor(mColorDarkBlue);
        mCenterTextPaint.setTextSize(Utils.convertDpToPixel(12f));
        mCenterTextPaint.setTextAlign(Paint.Align.CENTER);

        mValuePaint.setTextSize(Utils.convertDpToPixel(13f));
        mValuePaint.setColor(Color.WHITE);
        mValuePaint.setTextAlign(Paint.Align.CENTER);

        // for the piechart, drawing values is enabled
        mDrawYValues = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDataNotSet)
            return;

        drawHighlights();

        drawData();

        drawAdditional();

        drawValues();

        drawLegend();

        drawDescription();

        drawCenterText();

        canvas.drawBitmap(mDrawBitmap, 0, 0, mDrawPaint);
    }

    /**
     * does all necessary preparations, needed when data is changed or flags
     * that effect the data are changed
     */
    @Override
    public void prepare() {
        super.prepare();

        if (mCenterText == null)
            mCenterText = "Total Value\n" + (int) getYValueSum();
    }

    @Override
    protected void prepareContentRect() {
        super.prepareContentRect();

        // prevent nullpointer when no data set
        if (mDataNotSet)
            return;

        this.setOffsets(3, 3, 3, 3);

        float diameter = getDiameter();
        float boxSize = diameter / 2f;

//        PointF c = getCenterOffsets();
        PointF c = new PointF(getWidth()/2f, getHeight()/2f);

        mCircleBox.set(c.x - boxSize, c.y - boxSize, c.x + boxSize, c.y + boxSize);

    }

    @Override
    protected void calcMinMax(boolean fixedValues) {
        super.calcMinMax(fixedValues);

        calcAngles();
    }

    @Override
    protected void calculateOffsets() {

        // setup offsets for legend
        if (mDrawLegend) {

            float legendRight = 0f, legendBottom = 0f;

            if (mLegend == null)
                return;

            if (mLegend.getPosition() == Legend.LegendPosition.RIGHT_OF_CHART) {

                // this is the space between the legend and the chart
                float spacing = Utils.convertDpToPixel(7f);

                legendRight = mLegend.getMaximumEntryLength(mLegendLabelPaint)
                        + mLegend.getFormSize() + mLegend.getFormToTextSpace() + spacing;

                mLegendLabelPaint.setTextAlign(Paint.Align.LEFT);

            } else if (mLegend.getPosition() == Legend.LegendPosition.BELOW_CHART_LEFT
                    || mLegend.getPosition() == Legend.LegendPosition.BELOW_CHART_RIGHT
                    || mLegend.getPosition() == Legend.LegendPosition.BELOW_CHART_CENTER) {

                legendBottom = mLegendLabelPaint.getTextSize() * 4f;
            }

            mLegend.setOffsetBottom(legendBottom);
            mLegend.setOffsetRight(legendRight);

            float min = Utils.convertDpToPixel(11f);

            mLegend.setOffsetTop(min);
            mLegend.setOffsetLeft(min);

            mOffsetTop = Math.max(mLegend.getFullHeight(mLegendLabelPaint), min);

            applyCalculatedOffsets();
        }
    }

    /**
     * calculates the needed angles for the chart slices
     */
    private void calcAngles() {

        mDrawAngles = new float[mCurrentData.getYValCount()];
        mAbsoluteAngles = new float[mCurrentData.getYValCount()];

        ArrayList<PieDataSet> dataSets = mCurrentData.getDataSets();

        int cnt = 0;

        for (int i = 0; i < mCurrentData.getDataSetCount(); i++) {

            PieDataSet set = dataSets.get(i);
            ArrayList<Entry> entries = set.getYVals();

            for (int j = 0; j < entries.size(); j++) {

                mDrawAngles[cnt] = calcAngle(Math.abs(entries.get(j).getVal()));

                if (cnt == 0) {
                    mAbsoluteAngles[cnt] = mDrawAngles[cnt];
                } else {
                    mAbsoluteAngles[cnt] = mAbsoluteAngles[cnt - 1] + mDrawAngles[cnt];
                }

                cnt++;
            }
        }

    }

    @Override
    protected void drawHighlights() {

        // if there are values to highlight and highlighnting is enabled, do it
        if (mHighlightEnabled && valuesToHighlight()) {

            float angle = 0f;

            for (int i = 0; i < mIndicesToHightlight.length; i++) {

                // get the index to highlight
                int xIndex = mIndicesToHightlight[i].getXIndex();
                if (xIndex >= mDrawAngles.length || xIndex > mDeltaX * mPhaseX)
                    continue;

                if (xIndex == 0)
                    angle = mRotationAngle;
                else
                    angle = mRotationAngle + mAbsoluteAngles[xIndex - 1];

                angle *= mPhaseY;

                float sliceDegrees = mDrawAngles[xIndex];

                float shiftangle = (float) Math.toRadians(angle + sliceDegrees / 2f);

                PieDataSet set = mCurrentData
                        .getDataSetByIndex(mIndicesToHightlight[i]
                                .getDataSetIndex());

                float shift = set.getSelectionShift();
                float xShift = shift * (float) Math.cos(shiftangle);
                float yShift = shift * (float) Math.sin(shiftangle);

                RectF highlighted = new RectF(mCircleBox.left + xShift, mCircleBox.top + yShift,
                        mCircleBox.right
                                + xShift, mCircleBox.bottom + yShift);

                mRenderPaint.setColor(set.getColor(xIndex));

                // redefine the rect that contains the arc so that the
                // highlighted pie is not cut off
                mDrawCanvas.drawArc(highlighted, angle + set.getSliceSpace() / 2f, sliceDegrees
                        - set.getSliceSpace() / 2f, true, mRenderPaint);
            }
        }
    }

    @Override
    protected void drawData() {

        float angle = mRotationAngle;

        ArrayList<PieDataSet> dataSets = mCurrentData.getDataSets();

        int cnt = 0;

        for (int i = 0; i < mCurrentData.getDataSetCount(); i++) {

            PieDataSet dataSet = dataSets.get(i);
            ArrayList<Entry> entries = dataSet.getYVals();

            for (int j = 0; j < entries.size(); j++) {

                float newangle = mDrawAngles[cnt];
                float sliceSpace = dataSet.getSliceSpace();

                Entry e = entries.get(j);

                // draw only if the value is greater than zero
                if ((Math.abs(e.getVal()) > 0.000001)) {

                    if (!needsHighlight(e.getXIndex(), i)) {

                        mRenderPaint.setColor(dataSet.getColor(j));
                        mDrawCanvas.drawArc(mCircleBox, angle + sliceSpace / 2f, newangle * mPhaseY
                                - sliceSpace / 2f, true, mRenderPaint);

                    }

//                    if(sliceSpace > 0f) {
//
//                        PointF outer = getPosition(c, radius, angle);
//                        PointF inner = getPosition(c, radius * mHoleRadiusPercent / 100f, angle);
//                    }
                }

                angle += newangle * mPhaseX;
                cnt++;
            }
        }
    }

    /**
     * draws the hole in the center of the chart and the transparent circle /
     * hole
     */
    private void drawHole() {

        if (mDrawHole) {

            float radius = getRadius();

            PointF c = getCenterCircleBox();

            int color = mHolePaint.getColor();

            // draw the hole-circle
            mDrawCanvas.drawCircle(c.x, c.y,
                    radius / 100 * mHoleRadiusPercent, mHolePaint);

            // make transparent
            mHolePaint.setColor(color & 0x60FFFFFF);

            // draw the transparent-circle
            mDrawCanvas.drawCircle(c.x, c.y,
                    radius / 100 * mTransparentCircleRadius, mHolePaint);

            mHolePaint.setColor(color);
        }
    }

    /**
     * draws the description text in the center of the pie chart makes most
     * sense when center-hole is enabled
     */
    private void drawCenterText() {

        if (mDrawCenterText) {

            PointF c = getCenterCircleBox();

            // get all lines from the text
            String[] lines = mCenterText.split("\n");

            // calculate the height for each line
            float lineHeight = Utils.calcTextHeight(mCenterTextPaint, lines[0]);
            float linespacing = lineHeight * 0.2f;

            float totalheight = lineHeight * lines.length - linespacing * (lines.length - 1);

            int cnt = lines.length;

            float y = c.y;

            for (int i = 0; i < lines.length; i++) {

                String line = lines[lines.length - i - 1];

                mDrawCanvas.drawText(line, c.x, y
                                + lineHeight * cnt - totalheight / 2f,
                        mCenterTextPaint);
                cnt--;
                y -= linespacing;
            }
        }
    }

    @Override
    protected void drawValues() {

        // if neither xvals nor yvals are drawn, return
        if (!mDrawXVals && !mDrawYValues)
            return;

        PointF center = getCenterCircleBox();

        // get whole the radius
        float r = getRadius();

        float off = r / 2f;

        if (mDrawHole) {
            off = (r - (r / 100f * mHoleRadiusPercent)) / 2f;
        }

        r -= off; // offset to keep things inside the chart

        ArrayList<PieDataSet> dataSets = mCurrentData.getDataSets();

        int cnt = 0;

        for (int i = 0; i < mCurrentData.getDataSetCount(); i++) {

            PieDataSet dataSet = dataSets.get(i);
            ArrayList<Entry> entries = dataSet.getYVals();

            for (int j = 0; j < entries.size() * mPhaseX; j++) {

                // offset needed to center the drawn text in the slice
                float offset = mDrawAngles[cnt] / 2;

                // calculate the text position
                float x = (float) (r
                        * Math.cos(Math.toRadians((mRotationAngle + mAbsoluteAngles[cnt] - offset)
                        * mPhaseY)) + center.x);
                float y = (float) (r
                        * Math.sin(Math.toRadians((mRotationAngle + mAbsoluteAngles[cnt] - offset)
                        * mPhaseY)) + center.y);

                String val = "";
                float value = entries.get(j).getVal();

                if (mUsePercentValues)
                    val = mValueFormatter.getFormattedValue(Math.abs(getPercentOfTotal(value))) + " %";
                else
                    val = mValueFormatter.getFormattedValue(value);

                if (mDrawUnitInChart)
                    val = val + mUnit;

                // draw everything, depending on settings
                if (mDrawXVals && mDrawYValues) {

                    // use ascent and descent to calculate the new line
                    // position,
                    // 1.6f is the line spacing
                    float lineHeight = (mValuePaint.ascent() + mValuePaint.descent()) * 1.6f;
                    y -= lineHeight / 2;

                    mDrawCanvas.drawText(val, x, y, mValuePaint);
                    mDrawCanvas.drawText(mCurrentData.getXVals().get(j), x, y + lineHeight,
                            mValuePaint);

                } else if (mDrawXVals && !mDrawYValues) {
                    mDrawCanvas.drawText(mCurrentData.getXVals().get(j), x, y, mValuePaint);
                } else if (!mDrawXVals && mDrawYValues) {

                    mDrawCanvas.drawText(val, x, y, mValuePaint);
                }

                cnt++;
            }
        }
    }

    @Override
    protected void drawAdditional() {
        drawHole();
    }

    /**
     * calculates the needed angle for a given value
     *
     * @param value
     * @return
     */
    private float calcAngle(float value) {
        return value / mCurrentData.getYValueSum() * 360f;
    }

    @Override
    public int getIndexForAngle(float angle) {

        // take the current angle of the chart into consideration
        float a = (angle - mRotationAngle + 360) % 360f;

        for (int i = 0; i < mAbsoluteAngles.length; i++) {
            if (mAbsoluteAngles[i] > a)
                return i;
        }

        return -1; // return -1 if no index found
    }


    @Override
    public float getRadius() {
        if (mCircleBox == null)
            return 0;
        else
            return Math.min(mCircleBox.width() / 2f, mCircleBox.height() / 2f);
    }


    /**
     * returns the center of the circlebox
     *
     * @return
     */
    public PointF getCenterCircleBox() {
        return new PointF(mCircleBox.centerX(), mCircleBox.centerY());
    }


    @Override
    public void setPaint(Paint p, int which) {
        super.setPaint(p, which);

        switch (which) {
            case PAINT_HOLE:
                mHolePaint = p;
                break;
            case PAINT_CENTER_TEXT:
                mCenterTextPaint = p;
                break;
        }
    }

    @Override
    public Paint getPaint(int which) {
        Paint p = super.getPaint(which);
        if (p != null)
            return p;

        switch (which) {
            case PAINT_HOLE:
                return mHolePaint;
            case PAINT_CENTER_TEXT:
                return mCenterTextPaint;
        }

        return null;
    }
}
