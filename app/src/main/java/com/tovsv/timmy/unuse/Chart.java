
package com.tovsv.timmy.unuse;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.MarkerView;
import com.github.mikephil.charting.utils.SelInfo;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public abstract class Chart<T extends ChartData<? extends DataSet<? extends Entry>>> extends View
        implements AnimatorUpdateListener {

    public static final String LOG_TAG = "MPChart";

    protected int mColorDarkBlue = Color.rgb(41, 128, 186);
    protected int mColorDarkRed = Color.rgb(232, 76, 59);

    /**
     * string that is drawn next to the values in the chart, indicating their
     * unit
     */
    protected String mUnit = "";

    /**
     * flag that holds the background color of the view and the color the canvas
     * is cleared with
     */
    protected int mBackgroundColor = Color.WHITE;

    /** custom formatter that is used instead of the auto-formatter if set */
    protected ValueFormatter mValueFormatter = null;

    /**
     * flag that indicates if the default formatter should be used or if a
     * custom one is set
     */
    private boolean mUseDefaultFormatter = true;

    /** chart offset to the left */
    protected float mOffsetLeft = 12;

    /** chart toffset to the top */
    protected float mOffsetTop = 12;

    /** chart offset to the right */
    protected float mOffsetRight = 12;

    /** chart offset to the bottom */
    protected float mOffsetBottom = 12;

    /**
     * object that holds all data relevant for the chart (x-vals, y-vals, ...)
     * that are currently displayed
     */
    protected T mCurrentData = null;

    /**
     * object that holds all data that was originally set for the chart, before
     * it was modified or any filtering algorithms had been applied
     */
    protected T mOriginalData = null;

    /** final bitmap that contains all information and is drawn to the screen */
    protected Bitmap mDrawBitmap;

    /** the canvas that is used for drawing on the bitmap */
    protected Canvas mDrawCanvas;

    /** the lowest value the chart can display */
    protected float mYChartMin = 0.0f;

    /** the highest value the chart can display */
    protected float mYChartMax = 0.0f;

    /** paint for the x-label values */
    protected Paint mXLabelPaint;

    /** paint for the y-label values */
    protected Paint mYLabelPaint;

    /**
     * paint object used for darwing the bitmap to the screen
     */
    protected Paint mDrawPaint;

    /** paint used for highlighting values */
    protected Paint mHighlightPaint;

    /**
     * paint object used for drawing the description text in the bottom right
     * corner of the chart
     */
    protected Paint mDescPaint;

    /**
     * paint object for drawing the information text when there are no values in
     * the chart
     */
    protected Paint mInfoPaint;

    /**
     * paint object for drawing values (text representing values of chart
     * entries)
     */
    protected Paint mValuePaint;

    /** this is the paint object used for drawing the data onto the chart */
    protected Paint mRenderPaint;

    /** paint for the legend labels */
    protected Paint mLegendLabelPaint;

    /** paint used for the legend forms */
    protected Paint mLegendFormPaint;

    /** paint used for the limit lines */
    protected Paint mLimitLinePaint;

    /** description text that appears in the bottom right corner of the chart */
    protected String mDescription = "Description.";

    /** flag that indicates if the chart has been fed with data yet */
    protected boolean mDataNotSet = true;

    /** if true, units are drawn next to the values in the chart */
    protected boolean mDrawUnitInChart = false;

    /** the range of y-values the chart displays */
    protected float mDeltaY = 1f;

    /** the number of x-values the chart displays */
    protected float mDeltaX = 1f;

    /** matrix to map the values to the screen pixels */
    protected Matrix mMatrixValueToPx = new Matrix();

    /** matrix for handling the different offsets of the chart */
    protected Matrix mMatrixOffset = new Matrix();

    /** matrix used for touch events */
    protected final Matrix mMatrixTouch = new Matrix();

    /** if true, touch gestures are enabled on the chart */
    protected boolean mTouchEnabled = true;

    /** if true, y-values are drawn on the chart */
    protected boolean mDrawYValues = true;

    /** if true, value highlightning is enabled */
    protected boolean mHighlightEnabled = true;

    /** flag indicating if the legend is drawn of not */
    protected boolean mDrawLegend = true;

    /** this rectangle defines the area in which graph values can be drawn */
    protected RectF mContentRect = new RectF();

    /** the legend object containing all data associated with the legend */
    protected Legend mLegend;

    /** listener that is called when a value on the chart is selected */
    protected OnChartValueSelectedListener mSelectionListener;

    /** text that is displayed when the chart is empty */
    private String mNoDataText = "No chart data available.";

    /**
     * text that is displayed when the chart is empty that describes why the
     * chart is empty
     */
    private String mNoDataTextDescription;

    /** default constructor for initialization in code */
    public Chart(Context context) {
        super(context);
        init();
    }

    /** constructor for initialization in xml */
    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /** even more awesome constructor */
    public Chart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize all paints and stuff
     */
    protected void init() {

        // initialize the utils
        Utils.init(getContext().getResources());

        // do screen density conversions
        mOffsetBottom = (int) Utils.convertDpToPixel(mOffsetBottom);
        mOffsetLeft = (int) Utils.convertDpToPixel(mOffsetLeft);
        mOffsetRight = (int) Utils.convertDpToPixel(mOffsetRight);
        mOffsetTop = (int) Utils.convertDpToPixel(mOffsetTop);

        mRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRenderPaint.setStyle(Style.FILL);

        mDrawPaint = new Paint();

        mDescPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDescPaint.setColor(Color.BLACK);
        mDescPaint.setTextAlign(Align.RIGHT);
        mDescPaint.setTextSize(Utils.convertDpToPixel(9f));

        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(Color.rgb(247, 189, 51)); // orange
        mInfoPaint.setTextAlign(Align.CENTER);
        mInfoPaint.setTextSize(Utils.convertDpToPixel(12f));

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setColor(Color.rgb(63, 63, 63));
        mValuePaint.setTextAlign(Align.CENTER);
        mValuePaint.setTextSize(Utils.convertDpToPixel(9f));

        mLegendFormPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendFormPaint.setStyle(Style.FILL);
        mLegendFormPaint.setStrokeWidth(3f);

        mLegendLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendLabelPaint.setTextSize(Utils.convertDpToPixel(9f));

        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setStyle(Style.STROKE);
        mHighlightPaint.setStrokeWidth(2f);
        mHighlightPaint.setColor(Color.rgb(255, 187, 115));

        mXLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLabelPaint.setColor(Color.BLACK);
        mXLabelPaint.setTextAlign(Align.CENTER);
        mXLabelPaint.setTextSize(Utils.convertDpToPixel(10f));

        mYLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mYLabelPaint.setColor(Color.BLACK);
        mYLabelPaint.setTextSize(Utils.convertDpToPixel(10f));

        mLimitLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLimitLinePaint.setStyle(Style.STROKE);
    }

    protected boolean mOffsetsCalculated = false;

    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     *
     * @param data
     */
    public void setData(T data) {

        if (data == null) {
            Log.e(LOG_TAG,
                    "Cannot set data for chart. Provided data object is null.");
            return;
        }

//        Log.i(LOG_TAG, "xvalcount: " + data.getXValCount());
//        Log.i(LOG_TAG, "entrycount: " + data.getYValCount());

        // LET THE CHART KNOW THERE IS DATA
        mDataNotSet = false;
        mOffsetsCalculated = false;
        mCurrentData = data;
        mOriginalData = data;

        prepare();

        // calculate how many digits are needed
        calcFormats();

        Log.i(LOG_TAG, "Data is set.");
    }

    /**
     * Clears the chart from all data and refreshes it (by calling
     * invalidate()).
     */
    public void clear() {
        mCurrentData = null;
        mOriginalData = null;
        mDataNotSet = true;
        invalidate();
    }

    /**
     * Returns true if the chart is empty (meaning it's data object is either
     * null or contains no entries).
     *
     * @return
     */
    public boolean isEmpty() {

        if (mOriginalData == null)
            return true;
        else {

            if (mOriginalData.getYValCount() <= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * does needed preparations for drawing
     */
    public abstract void prepare();

    /**
     * Lets the chart know its underlying data has changed and performs all
     * necessary recalculations.
     */
    public abstract void notifyDataSetChanged();

    /**
     * calculates the offsets of the chart to the border depending on the
     * position of an eventual legend or depending on the length of the y-axis
     * and x-axis labels and their position
     */
    protected abstract void calculateOffsets();

    /**
     * calcualtes the y-min and y-max value and the y-delta and x-delta value
     */
    protected void calcMinMax(boolean fixedValues) {
        // only calculate values if not fixed values
        if (!fixedValues) {
            mYChartMin = mCurrentData.getYMin();
            mYChartMax = mCurrentData.getYMax();
        }

        // calc delta
        mDeltaY = Math.abs(mYChartMax - mYChartMin);
        mDeltaX = mCurrentData.getXVals().size() - 1;
    }

    /**
     * calculates the required number of digits for the values that might be
     * drawn in the chart (if enabled)
     */
    protected void calcFormats() {

        // check if a custom formatter is set or not
        if (mUseDefaultFormatter) {

            float reference = 0f;

            if (mOriginalData == null || mOriginalData.getXValCount() < 2) {

                reference = Math.max(Math.abs(mYChartMin), Math.abs(mYChartMax));
            } else {
                reference = mDeltaY;
            }

            int digits = Utils.getDecimals(reference);

            StringBuffer b = new StringBuffer();
            for (int i = 0; i < digits; i++) {
                if (i == 0)
                    b.append(".");
                b.append("0");
            }

            DecimalFormat formatter = new DecimalFormat("###,###,###,##0" + b.toString());
            mValueFormatter = new DefaultValueFormatter(formatter);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDataNotSet) { // check if there is data
            // if no data, inform the user

            return;
        }

        if (!mOffsetsCalculated) {

            calculateOffsets();
            mOffsetsCalculated = true;
        }

        if (mDrawBitmap == null || mDrawCanvas == null) {

            // use RGB_565 for best performance
            mDrawBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
            mDrawCanvas = new Canvas(mDrawBitmap);
        }

        mDrawCanvas.drawColor(mBackgroundColor); // clear all
    }



    /**
     * Sets up all the matrices that will be used for scaling the coordinates to
     * the display. Offset and Value-px.
     */
    protected void prepareMatrix() {

        prepareMatrixValuePx();

        prepareMatrixOffset();

        Log.i(LOG_TAG, "Matrices prepared.");
    }

    /**
     * Prepares the matrix that transforms values to pixels.
     */
    protected void prepareMatrixValuePx() {

        float scaleX = (float) ((getWidth() - mOffsetRight - mOffsetLeft) / mDeltaX);
        float scaleY = (float) ((getHeight() - mOffsetTop - mOffsetBottom) / mDeltaY);

        // setup all matrices
        mMatrixValueToPx.reset();
        mMatrixValueToPx.postTranslate(0, -mYChartMin);
        mMatrixValueToPx.postScale(scaleX, -scaleY);
    }

    /**
     * Prepares the matrix that contains all offsets.
     */
    protected void prepareMatrixOffset() {

        mMatrixOffset.reset();

        mMatrixOffset.postTranslate(mOffsetLeft, getHeight() - mOffsetBottom);

        // mMatrixOffset.setTranslate(mOffsetLeft, 0);
        // mMatrixOffset.postScale(1.0f, -1.0f);
    }

    /**
     * sets up the content rect that restricts the chart surface
     */
    protected void prepareContentRect() {

        mContentRect.set(mOffsetLeft,
                mOffsetTop,
                getWidth() - mOffsetRight,
                getHeight() - mOffsetBottom);
    }

    /**
     * Generates an automatically prepared legend depending on the DataSets in
     * the chart and their colors.
     */
    public void prepareLegend() {

        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        // loop for building up the colors and labels used in the legend
        for (int i = 0; i < mOriginalData.getDataSetCount(); i++) {

            DataSet<? extends Entry> dataSet = mOriginalData.getDataSetByIndex(i);

            ArrayList<Integer> clrs = dataSet.getColors();
            int entryCount = dataSet.getEntryCount();

            // if we have a barchart with stacked bars
            if (dataSet instanceof BarDataSet && ((BarDataSet) dataSet).getStackSize() > 1) {

                BarDataSet bds = (BarDataSet) dataSet;
                String[] sLabels = bds.getStackLabels();

                for (int j = 0; j < clrs.size() && j < entryCount && j < bds.getStackSize(); j++) {

                    labels.add(sLabels[j % sLabels.length]);
                    colors.add(clrs.get(j));
                }

                // add the legend description label
                colors.add(-2);
                labels.add(bds.getLabel());

            } else if (dataSet instanceof PieDataSet) {

                ArrayList<String> xVals = mOriginalData.getXVals();
                PieDataSet pds = (PieDataSet) dataSet;

                for (int j = 0; j < clrs.size() && j < entryCount && j < xVals.size(); j++) {

                    labels.add(xVals.get(j));
                    colors.add(clrs.get(j));
                }

                // add the legend description label
                colors.add(-2);
                labels.add(pds.getLabel());

            } else { // all others

                for (int j = 0; j < clrs.size() && j < entryCount; j++) {

                    // if multiple colors are set for a DataSet, group them
                    if (j < clrs.size() - 1 && j < entryCount - 1) {

                        labels.add(null);
                    } else { // add label to the last entry

                        String label = mOriginalData.getDataSetByIndex(i).getLabel();
                        labels.add(label);
                    }

                    colors.add(clrs.get(j));
                }
            }
        }

        Legend l = new Legend(colors, labels);

        if (mLegend != null) {
            // apply the old legend settings to a potential new legend
            l.apply(mLegend);
        }

        mLegend = l;
    }

    /**
     * Transforms an arraylist of Entry into a float array containing the x and
     * y values transformed with all matrices for the LINECHART or SCATTERCHART.
     *
     * @param entries
     * @return
     */
    protected float[] generateTransformedValuesLineScatter(ArrayList<? extends Entry> entries) {

        float[] valuePoints = new float[entries.size() * 2];

        for (int j = 0; j < valuePoints.length; j += 2) {

            Entry e = entries.get(j / 2);

            valuePoints[j] = e.getXIndex();
            valuePoints[j + 1] = e.getVal() * mPhaseY;
        }

        transformPointArray(valuePoints);

        return valuePoints;
    }

    /**
     * Transforms an arraylist of Entry into a float array containing the x and
     * y values transformed with all matrices for the BARCHART.
     *
     * @param entries
     * @param dataSet the dataset index
     * @return
     */
    protected float[] generateTransformedValuesBarChart(ArrayList<? extends Entry> entries,
            int dataSet) {

        float[] valuePoints = new float[entries.size() * 2];

        int setCount = mOriginalData.getDataSetCount();
        BarData bd = (BarData) mOriginalData;
        float space = bd.getGroupSpace();

        for (int j = 0; j < valuePoints.length; j += 2) {

            Entry e = entries.get(j / 2);

            // calculate the x-position, depending on datasetcount
            float x = e.getXIndex() + (j / 2 * (setCount - 1)) + dataSet + 0.5f + space * (j / 2)
                    + space / 2f;
            float y = e.getVal();

            valuePoints[j] = x;
            valuePoints[j + 1] = y * mPhaseY;
        }

        transformPointArray(valuePoints);

        return valuePoints;
    }

    /**
     * transform a path with all the given matrices VERY IMPORTANT: keep order
     * to value-touch-offset
     *
     * @param path
     */
    protected void transformPath(Path path) {

        path.transform(mMatrixValueToPx);
        path.transform(mMatrixTouch);
        path.transform(mMatrixOffset);
    }

    /**
     * Transforms multiple paths will all matrices.
     *
     * @param paths
     */
    protected void transformPaths(ArrayList<Path> paths) {

        for (int i = 0; i < paths.size(); i++) {
            transformPath(paths.get(i));
        }
    }

    /**
     * Transform an array of points with all matrices. VERY IMPORTANT: Keep
     * matrix order "value-touch-offset" when transforming.
     *
     * @param pts
     */
    protected void transformPointArray(float[] pts) {

        mMatrixValueToPx.mapPoints(pts);
        mMatrixTouch.mapPoints(pts);
        mMatrixOffset.mapPoints(pts);
    }

    /**
     * Transform a rectangle with all matrices.
     *
     * @param r
     */
    protected void transformRect(RectF r) {

        mMatrixValueToPx.mapRect(r);
        mMatrixTouch.mapRect(r);
        mMatrixOffset.mapRect(r);
    }
    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS CODE FOR HIGHLIGHTING */

    /**
     * array of Highlight objects that reference the highlighted slices in the
     * chart
     */
    protected Highlight[] mIndicesToHightlight = new Highlight[0];

    /**
     * checks if the given index in the given DataSet is set for highlighting or
     * not
     *
     * @param xIndex
     * @param dataSetIndex
     * @return
     */
    public boolean needsHighlight(int xIndex, int dataSetIndex) {

        // no highlight
        if (!valuesToHighlight())
            return false;

        for (int i = 0; i < mIndicesToHightlight.length; i++)

            // check if the xvalue for the given dataset needs highlight
            if (mIndicesToHightlight[i].getXIndex() == xIndex
                    && mIndicesToHightlight[i].getDataSetIndex() == dataSetIndex
                    && xIndex <= mDeltaX)
                return true;

        return false;
    }

    /**
     * Returns true if there are values to highlight, false if there are no
     * values to highlight. Checks if the highlight array is null, has a length
     * of zero or if the first object is null.
     *
     * @return
     */
    public boolean valuesToHighlight() {
        return mIndicesToHightlight == null || mIndicesToHightlight.length <= 0
                || mIndicesToHightlight[0] == null ? false
                : true;
    }

    /**
     * Highlights the values at the given indices in the given DataSets. Provide
     * null or an empty array to undo all highlighting. This should be used to
     * programmatically highlight values. This DOES NOT generate a callback to
     * the OnChartValueSelectedListener.
     *
     * @param highs
     */
    public void highlightValues(Highlight[] highs) {

        // set the indices to highlight
        mIndicesToHightlight = highs;

        // redraw the chart
        invalidate();
    }

    /**
     * Highlights the value selected by touch gesture. Unlike
     * highlightValues(...), this generates a callback to the
     * OnChartValueSelectedListener.
     *
     * @param highs
     */
    public void highlightTouch(Highlight high) {

        if (high == null)
            mIndicesToHightlight = null;
        else {

            // set the indices to highlight
            mIndicesToHightlight = new Highlight[] {
                    high
            };
        }

        // redraw the chart
        invalidate();

        if (mSelectionListener != null) {

            if (!valuesToHighlight())
                mSelectionListener.onNothingSelected();
            else {

                Entry e = getEntryByDataSetIndex(high.getXIndex(),
                        high.getDataSetIndex());

                // notify the listener
                mSelectionListener.onValueSelected(e, high.getDataSetIndex());
            }
        }
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW CODE IS FOR THE MARKER VIEW */

    /** if set to true, the marker view is drawn when a value is clicked */
    protected boolean mDrawMarkerViews = true;

    /** the view that represents the marker */
    protected MarkerView mMarkerView;

    /**
     * draws all MarkerViews on the highlighted positions
     */
    protected void drawMarkers() {

        // if there is no marker view or drawing marker is disabled
        if (mMarkerView == null || !mDrawMarkerViews || !valuesToHighlight())
            return;

        for (int i = 0; i < mIndicesToHightlight.length; i++) {

            int xIndex = mIndicesToHightlight[i].getXIndex();
            int dataSetIndex = mIndicesToHightlight[i].getDataSetIndex();

            if (xIndex <= mDeltaX && xIndex <= mDeltaX * mPhaseX) {

                Entry e = getEntryByDataSetIndex(xIndex, dataSetIndex);

                // make sure entry not null
                if (e == null)
                    continue;

                float[] pos = getMarkerPosition(e, dataSetIndex);

                // check bounds
                if (pos[0] < mOffsetLeft || pos[0] > getWidth() - mOffsetRight
                        || pos[1] < mOffsetTop || pos[1] > getHeight() - mOffsetBottom)
                    continue;

                // callbacks to update the content
                mMarkerView.refreshContent(e, dataSetIndex);

                mMarkerView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                mMarkerView.layout(0, 0, mMarkerView.getMeasuredWidth(),
                        mMarkerView.getMeasuredHeight());
                mMarkerView.draw(mDrawCanvas, pos[0], pos[1]);
            }
        }
    }

    /**
     * Returns the actual position in pixels of the MarkerView for the given
     * Entry in the given DataSet.
     *
     * @param xIndex
     * @param dataSetIndex
     * @return
     */
    private float[] getMarkerPosition(Entry e, int dataSetIndex) {

        float xPos = (float) e.getXIndex();

        // make sure the marker is in the center of the bars in BarChart and
        // CandleStickChart


        // position of the marker depends on selected value index and value
        float[] pts = new float[] {
                xPos, e.getVal() * mPhaseY
        };

        transformPointArray(pts);

        return pts;
    }

    /**
     * ################ ################ ################ ################
     * Animation support below Honeycomb thanks to Jake Wharton's awesome
     * nineoldandroids library: https://github.com/JakeWharton/NineOldAndroids
     */
    /** CODE BELOW THIS RELATED TO ANIMATION */

    /** the phase that is animated and influences the drawn values on the y-axis */
    protected float mPhaseY = 1f;

    /** the phase that is animated and influences the drawn values on the x-axis */
    protected float mPhaseX = 1f;

    /** objectanimator used for animating values on y-axis */
    private ObjectAnimator mAnimatorY;

    /** objectanimator used for animating values on x-axis */
    private ObjectAnimator mAnimatorX;

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart.
     *
     * @param durationMillisX
     * @param durationMillisY
     */
    public void animateXY(int durationMillisX, int durationMillisY) {

        mAnimatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f);
        mAnimatorY.setDuration(
                durationMillisY);
        mAnimatorX = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f);
        mAnimatorX.setDuration(
                durationMillisX);

        // make sure only one animator produces update-callbacks (which then
        // call invalidate())
        if (durationMillisX > durationMillisY) {
            mAnimatorX.addUpdateListener(this);
        } else {
            mAnimatorY.addUpdateListener(this);
        }

        mAnimatorX.start();
        mAnimatorY.start();
    }

    /**
     * Animates the rendering of the chart on the x-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart.
     *
     * @param durationMillis
     */
    public void animateX(int durationMillis) {

        mAnimatorX = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f);
        mAnimatorX.setDuration(durationMillis);
        mAnimatorX.addUpdateListener(this);
        mAnimatorX.start();
    }

    /**
     * Animates the rendering of the chart on the y-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart.
     *
     * @param durationMillis
     */
    public void animateY(int durationMillis) {

        mAnimatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f);
        mAnimatorY.setDuration(durationMillis);
        mAnimatorY.addUpdateListener(this);
        mAnimatorY.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator va) {

        // redraw everything after animation value change
        invalidate();

        // Log.i(LOG_TAG, "UPDATING, x: " + mPhaseX + ", y: " + mPhaseY);
    }

    /**
     * This gets the y-phase that is used to animate the values.
     *
     * @return
     */
    public float getPhaseY() {
        return mPhaseY;
    }

    /**
     * This modifys the y-phase that is used to animate the values.
     *
     * @param phase
     */
    public void setPhaseY(float phase) {
        mPhaseY = phase;
    }

    /**
     * This gets the x-phase that is used to animate the values.
     *
     * @return
     */
    public float getPhaseX() {
        return mPhaseX;
    }

    /**
     * This modifys the x-phase that is used to animate the values.
     *
     * @param phase
     */
    public void setPhaseX(float phase) {
        mPhaseX = phase;
    }


    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS ONLY GETTERS AND SETTERS */

    /**
     * Returns the canvas object the chart uses for drawing.
     *
     * @return
     */
    public Canvas getCanvas() {
        return mDrawCanvas;
    }

    /**
     * set a selection listener for the chart
     *
     * @param l
     */
    public void setOnChartValueSelectedListener(OnChartValueSelectedListener l) {
        this.mSelectionListener = l;
    }

    /**
     * If set to true, value highlighting is enabled which means that values can
     * be highlighted programmatically or by touch gesture.
     *
     * @param enabled
     */
    public void setHighlightEnabled(boolean enabled) {
        mHighlightEnabled = enabled;
    }

    /**
     * returns true if highlighting of values is enabled, false if not
     *
     * @return
     */
    public boolean isHighlightEnabled() {
        return mHighlightEnabled;
    }

    /**
     * returns the total value (sum) of all y-values across all DataSets
     *
     * @return
     */
    public float getYValueSum() {
        return mCurrentData.getYValueSum();
    }

    /**
     * returns the current y-max value across all DataSets
     *
     * @return
     */
    public float getYMax() {
        return mCurrentData.getYMax();
    }

    /**
     * returns the lowest value the chart can display
     *
     * @return
     */
    public float getYChartMin() {
        return mYChartMin;
    }

    /**
     * returns the highest value the chart can display
     *
     * @return
     */
    public float getYChartMax() {
        return mYChartMax;
    }

    /**
     * returns the current y-min value across all DataSets
     *
     * @return
     */
    public float getYMin() {
        return mCurrentData.getYMin();
    }

    /**
     * Get the total number of X-values.
     *
     * @return
     */
    public float getDeltaX() {
        return mDeltaX;
    }

    /**
     * returns the average value of all values the chart holds
     *
     * @return
     */
    public float getAverage() {
        return getYValueSum() / mCurrentData.getYValCount();
    }

    /**
     * returns the average value for a specific DataSet (with a specific label)
     * in the chart
     *
     * @param dataSetLabel
     * @return
     */
    public float getAverage(String dataSetLabel) {

        DataSet<? extends Entry> ds = mCurrentData.getDataSetByLabel(dataSetLabel, true);

        return ds.getYValueSum()
                / ds.getEntryCount();
    }

    /**
     * returns the total number of values the chart holds (across all DataSets)
     *
     * @return
     */
    public int getValueCount() {
        return mCurrentData.getYValCount();
    }

    /**
     * Returns the center point of the chart (the whole View) in pixels.
     *
     * @return
     */
    public PointF getCenter() {
        return new PointF(getWidth() / 2f, getHeight() / 2f);
    }

    /**
     * Returns the center of the chart taking offsets under consideration.
     * (returns the center of the content rectangle)
     *
     * @return
     */
    public PointF getCenterOffsets() {
        return new PointF(mContentRect.centerX(), mContentRect.centerY());
    }

    /**
     * sets the size of the description text in pixels, min 7f, max 14f
     *
     * @param size
     */
    public void setDescriptionTextSize(float size) {

        if (size > 14f)
            size = 14f;
        if (size < 7f)
            size = 7f;

        mInfoPaint.setTextSize(Utils.convertDpToPixel(size));
    }

    /**
     * set a description text that appears in the bottom right corner of the
     * chart, size = Y-legend text size
     *
     * @param desc
     */
    public void setDescription(String desc) {
        this.mDescription = desc;
    }

    /**
     * Sets the text that informs the user that there is no data available with
     * which to draw the chart.
     *
     * @param text
     */
    public void setNoDataText(String text) {
        mNoDataText = text;
    }

    /**
     * Sets descriptive text to explain to the user why there is no chart
     * available Defaults to empty if not set
     *
     * @param text
     */
    public void setNoDataTextDescription(String text) {
        mNoDataTextDescription = text;
    }

    /**
     * Sets the offsets from the border of the view to the actual chart in every
     * direction manually. Provide density pixels -> they are then rendered to
     * pixels inside the chart
     *
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setOffsets(float left, float top, float right, float bottom) {

        mOffsetBottom = Utils.convertDpToPixel(bottom);
        mOffsetLeft = Utils.convertDpToPixel(left);
        mOffsetRight = Utils.convertDpToPixel(right);
        mOffsetTop = Utils.convertDpToPixel(top);
    }

    public float getOffsetLeft() {
        return mOffsetLeft;
    }

    public float getOffsetBottom() {
        return mOffsetBottom;
    }

    public float getOffsetRight() {
        return mOffsetRight;
    }

    public float getOffsetTop() {
        return mOffsetTop;
    }

    /**
     * Set this to false to disable all gestures and touches on the chart,
     * default: true
     *
     * @param enabled
     */
    public void setTouchEnabled(boolean enabled) {
        this.mTouchEnabled = enabled;
    }

    /**
     * set this to true to draw y-values on the chart NOTE (for bar and
     * linechart): if "maxvisiblecount" is reached, no values will be drawn even
     * if this is enabled
     *
     * @param enabled
     */
    public void setDrawYValues(boolean enabled) {
        this.mDrawYValues = enabled;
    }

    /**
     * sets the view that is displayed when a value is clicked on the chart
     *
     * @param v
     */
    public void setMarkerView(MarkerView v) {
        mMarkerView = v;
    }

    /**
     * returns the view that is set as a marker view for the chart
     *
     * @return
     */
    public MarkerView getMarkerView() {
        return mMarkerView;
    }

    /**
     * if set to true, units are drawn next to values in the chart, default:
     * false
     *
     * @param enabled
     */
    public void setDrawUnitsInChart(boolean enabled) {
        mDrawUnitInChart = enabled;
    }

    /**
     * sets the unit that is drawn next to the values in the chart, e.g. %
     *
     * @param unit
     */
    public void setUnit(String unit) {
        mUnit = unit;
    }

    /**
     * Returns the unit that is used for the values in the chart
     *
     * @return
     */
    public String getUnit() {
        return mUnit;
    }

    /**
     * set this to true to draw the legend, false if not
     *
     * @param enabled
     */
    public void setDrawLegend(boolean enabled) {
        mDrawLegend = enabled;
    }

    /**
     * returns true if drawing the legend is enabled, false if not
     *
     * @return
     */
    public boolean isDrawLegendEnabled() {
        return mDrawLegend;
    }

    /**
     * Returns the legend object of the chart. This method can be used to
     * customize the automatically generated legend. IMPORTANT: this will return
     * null if no data has been set for the chart when calling this method
     *
     * @return
     */
    public Legend getLegend() {
        return mLegend;
    }

    /**
     * Returns the rectangle that defines the borders of the chart-value surface
     * (into which the actual values are drawn).
     *
     * @return
     */
    public RectF getContentRect() {
        return mContentRect;
    }

    /** paint for the grid lines (only line and barchart) */
    public static final int PAINT_GRID = 3;

    /** paint for the grid background (only line and barchart) */
    public static final int PAINT_GRID_BACKGROUND = 4;

    /** paint for the y-legend values (only line and barchart) */
    public static final int PAINT_YLABEL = 5;

    /** paint for the x-legend values (only line and barchart) */
    public static final int PAINT_XLABEL = 6;

    /**
     * paint for the info text that is displayed when there are no values in the
     * chart
     */
    public static final int PAINT_INFO = 7;

    /** paint for the value text */
    public static final int PAINT_VALUES = 8;

    /** paint for the inner circle (linechart) */
    public static final int PAINT_CIRCLES_INNER = 10;

    /** paint for the description text in the bottom right corner */
    public static final int PAINT_DESCRIPTION = 11;

    /** paint for the line surrounding the chart (only line and barchart) */
    public static final int PAINT_BORDER = 12;

    /** paint for the hole in the middle of the pie chart */
    public static final int PAINT_HOLE = 13;

    /** paint for the text in the middle of the pie chart */
    public static final int PAINT_CENTER_TEXT = 14;

    /** paint for highlightning the values of a linechart */
    public static final int PAINT_HIGHLIGHT = 15;

    /** paint object used for the limit lines */
    public static final int PAINT_RADAR_WEB = 16;

    /** paint used for all rendering processes */
    public static final int PAINT_RENDER = 17;

    /** paint used for the legend */
    public static final int PAINT_LEGEND_LABEL = 18;

    /** paint object used for the limit lines */
    public static final int PAINT_LIMIT_LINE = 19;

    /**
     * set a new paint object for the specified parameter in the chart e.g.
     * Chart.PAINT_VALUES
     *
     * @param p the new paint object
     * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES,
     *            ...
     */
    public void setPaint(Paint p, int which) {

        switch (which) {
            case PAINT_INFO:
                mInfoPaint = p;
                break;
            case PAINT_DESCRIPTION:
                mDescPaint = p;
                break;
            case PAINT_VALUES:
                mValuePaint = p;
                break;
            case PAINT_RENDER:
                mRenderPaint = p;
                break;
            case PAINT_LEGEND_LABEL:
                mLegendLabelPaint = p;
                break;
            case PAINT_XLABEL:
                mXLabelPaint = p;
                break;
            case PAINT_YLABEL:
                mYLabelPaint = p;
                break;
            case PAINT_HIGHLIGHT:
                mHighlightPaint = p;
                break;
            case PAINT_LIMIT_LINE:
                mLimitLinePaint = p;
                break;
        }
    }

    /**
     * Returns the paint object associated with the provided constant.
     *
     * @param which e.g. Chart.PAINT_LEGEND_LABEL
     * @return
     */
    public Paint getPaint(int which) {
        switch (which) {
            case PAINT_INFO:
                return mInfoPaint;
            case PAINT_DESCRIPTION:
                return mDescPaint;
            case PAINT_VALUES:
                return mValuePaint;
            case PAINT_RENDER:
                return mRenderPaint;
            case PAINT_LEGEND_LABEL:
                return mLegendLabelPaint;
            case PAINT_XLABEL:
                return mXLabelPaint;
            case PAINT_YLABEL:
                return mYLabelPaint;
            case PAINT_HIGHLIGHT:
                return mHighlightPaint;
            case PAINT_LIMIT_LINE:
                return mLimitLinePaint;
        }

        return null;
    }

    /**
     * returns true if drawing the marker-view is enabled when tapping on values
     * (use the setMarkerView(View v) method to specify a marker view)
     *
     * @return
     */
    public boolean isDrawMarkerViewEnabled() {
        return mDrawMarkerViews;
    }

    /**
     * Set this to true to draw a user specified marker-view when tapping on
     * chart values (use the setMarkerView(MarkerView mv) method to specify a
     * marker view). Default: true
     *
     * @param enabled
     */
    public void setDrawMarkerViews(boolean enabled) {
        mDrawMarkerViews = enabled;
    }

    /**
     * Sets the formatter to be used for drawing the values inside the chart. If
     * no formatter is set, the chart will automatically determine a reasonable
     * formatting (concerning decimals) for all the values that are drawn inside
     * the chart. Set this to NULL to re-enable auto formatting.
     *
     * @param f
     */
    public void setValueFormatter(ValueFormatter f) {
        mValueFormatter = f;

        if (f == null)
            mUseDefaultFormatter = true;
        else
            mUseDefaultFormatter = false;
    }

    /**
     * Returns the formatter used for drawing the values inside the chart.
     *
     * @return
     */
    public ValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    /**
     * sets the draw color for the value paint object
     *
     * @param color
     */
    public void setValueTextColor(int color) {
        mValuePaint.setColor(color);
    }

    /**
     * Sets the font size of the values that are drawn inside the chart.
     *
     * @param size
     */
    public void setValueTextSize(float size) {
        mValuePaint.setTextSize(Utils.convertDpToPixel(size));
    }

    /**
     * returns true if y-value drawing is enabled, false if not
     *
     * @return
     */
    public boolean isDrawYValuesEnabled() {
        return mDrawYValues;
    }

    /**
     * returns the x-value at the given index
     *
     * @param index
     * @return
     */
    public String getXValue(int index) {
        if (mCurrentData == null || mCurrentData.getXValCount() <= index)
            return null;
        else
            return mCurrentData.getXVals().get(index);
    }

    /**
     * returns the y-value for the given index from the DataSet with the given
     * label
     *
     * @param index
     * @param dataSetLabel
     * @return
     */
    public float getYValue(int index, String dataSetLabel) {
        DataSet<? extends Entry> set = mCurrentData.getDataSetByLabel(dataSetLabel, true);
        return set.getYVals().get(index).getVal();
    }

    /**
     * returns the y-value for the given x-index and DataSet index
     *
     * @param index
     * @param dataSet
     * @return
     */
    public float getYValue(int xIndex, int dataSetIndex) {
        DataSet<? extends Entry> set = mCurrentData.getDataSetByIndex(dataSetIndex);
        return set.getYValForXIndex(xIndex);
    }

    /**
     * returns the DataSet with the given index in the DataSet array held by the
     * ChartData object.
     *
     * @param index
     * @return
     */
    public DataSet<? extends Entry> getDataSetByIndex(int index) {
        return mCurrentData.getDataSetByIndex(index);
    }

    /**
     * returns the DataSet with the given label that is stored in the ChartData
     * object.
     *
     * @param type
     * @return
     */
    public DataSet<? extends Entry> getDataSetByLabel(String dataSetLabel) {
        return mCurrentData.getDataSetByLabel(dataSetLabel, true);
    }

    /**
     * returns the Entry object from the first DataSet stored in the ChartData
     * object. If multiple DataSets are used, use getEntry(index, type) or
     * getEntryByDataSetIndex(xIndex, dataSetIndex);
     *
     * @param index
     * @return
     */
    public Entry getEntry(int index) {
        return mCurrentData.getDataSetByIndex(0).getYVals().get(index);
    }

    /**
     * returns the Entry object at the given index from the DataSet with the
     * given label.
     *
     * @param index
     * @param dataSetLabel
     * @return
     */
    public Entry getEntry(int index, String dataSetLabel) {
        return mCurrentData.getDataSetByLabel(dataSetLabel, true).getYVals().get(index);
    }

    /**
     * Returns the corresponding Entry object at the given xIndex from the given
     * DataSet. INFORMATION: This method does calculations at runtime. Do not
     * over-use in performance critical situations.
     *
     * @param xIndex
     * @param dataSetIndex
     * @return
     */
    public Entry getEntryByDataSetIndex(int xIndex, int dataSetIndex) {
        return mCurrentData.getDataSetByIndex(dataSetIndex).getEntryForXIndex(xIndex);
    }

    /**
     * Returns an array of SelInfo objects for the given x-index. The SelInfo
     * objects give information about the value at the selected index and the
     * DataSet it belongs to. INFORMATION: This method does calculations at
     * runtime. Do not over-use in performance critical situations.
     *
     * @param xIndex
     * @return
     */
    public ArrayList<SelInfo> getYValsAtIndex(int xIndex) {

        ArrayList<SelInfo> vals = new ArrayList<SelInfo>();

        for (int i = 0; i < mCurrentData.getDataSetCount(); i++) {

            // extract all y-values from all DataSets at the given x-index
            float yVal = mCurrentData.getDataSetByIndex(i).getYValForXIndex(xIndex);

            if (!Float.isNaN(yVal)) {
                vals.add(new SelInfo(yVal, i));
            }
        }

        return vals;
    }

    /**
     * Get all Entry objects at the given index across all DataSets.
     * INFORMATION: This method does calculations at runtime. Do not over-use in
     * performance critical situations.
     *
     * @param xIndex
     * @return
     */
    public ArrayList<Entry> getEntriesAtIndex(int xIndex) {

        ArrayList<Entry> vals = new ArrayList<Entry>();

        for (int i = 0; i < mCurrentData.getDataSetCount(); i++) {

            DataSet<? extends Entry> set = mCurrentData.getDataSetByIndex(i);

            Entry e = set.getEntryForXIndex(xIndex);

            if (e != null) {
                vals.add(e);
            }
        }

        return vals;
    }

    /**
     * Returns the ChartData object the chart CURRENTLY represents (not
     * dependant on zoom level). It contains all values and information the
     * chart displays. If filtering algorithms have been applied, this returns
     * the filtered state of data.
     *
     * @return
     */
    public T getDataCurrent() {
        return mCurrentData;
    }

    /**
     * Returns the ChartData object that ORIGINALLY has been set for the chart.
     * It contains all data in an unaltered state, before any filtering
     * algorithms have been applied.
     *
     * @return
     */
    public T getDataOriginal() {
        return mOriginalData;
    }

    /**
     * returns the percentage the given value has of the total y-value sum
     *
     * @param val
     * @return
     */
    public float getPercentOfTotal(float val) {
        return val / mCurrentData.getYValueSum() * 100f;
    }

    /**
     * sets a typeface for the value-paint
     *
     * @param t
     */
    public void setValueTypeface(Typeface t) {
        mValuePaint.setTypeface(t);
    }

    /**
     * sets the typeface for the description paint
     *
     * @param t
     */
    public void setDescriptionTypeface(Typeface t) {
        mDescPaint.setTypeface(t);
    }

    /**
     * sets the background color for the chart --> this also sets the color the
     * canvas is cleared with
     */
    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);

        mBackgroundColor = color;
    }

    /**
     * Saves the chart with the given name to the given path on the sdcard
     * leaving the path empty "" will put the saved file directly on the SD card
     * chart is saved as a PNG image, example: saveToPath("myfilename",
     * "foldername1/foldername2");
     *
     * @param title
     * @param pathOnSD e.g. "folder1/folder2/folder3"
     * @return returns true on success, false on error
     */
    public boolean saveToPath(String title, String pathOnSD) {

        OutputStream stream = null;
        try {
            stream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
                    + pathOnSD + "/" + title
                    + ".png");

            /*
             * Write bitmap to file using JPEG or PNG and 40% quality hint for
             * JPEG.
             */
            mDrawBitmap.compress(CompressFormat.PNG, 40, stream);

            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Saves the current state of the chart to the gallery as a JPEG image. The
     * filename and compression can be set. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName e.g. "my_image"
     * @param quality e.g. 50, min = 0, max = 100
     * @return returns true if saving was successfull, false if not
     */
    public boolean saveToGallery(String fileName, int quality) {

        // restrain quality
        if (quality < 0 || quality > 100)
            quality = 50;

        long currentTime = System.currentTimeMillis();

        File extBaseDir = Environment.getExternalStorageDirectory();
        File file = new File(extBaseDir.getAbsolutePath() + "/DCIM");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false;
            }
        }

        String filePath = file.getAbsolutePath() + "/" + fileName;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);

            mDrawBitmap.compress(CompressFormat.JPEG, quality, out); // control
            // the jpeg
            // quality

            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        long size = new File(filePath).length();

        ContentValues values = new ContentValues(8);

        values.put(Images.Media.TITLE, fileName);
        values.put(Images.Media.DISPLAY_NAME, fileName);
        values.put(Images.Media.DATE_ADDED, currentTime);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DESCRIPTION, "MPAndroidChart-Library Save");
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, filePath);
        values.put(Images.Media.SIZE, size);

        return getContext().getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values) == null
                ? false : true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        prepareContentRect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private class DefaultValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public DefaultValueFormatter(DecimalFormat f) {
            mFormat = f;
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value);
        }
    }

}
