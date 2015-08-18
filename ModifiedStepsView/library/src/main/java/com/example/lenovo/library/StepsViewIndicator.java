package com.example.lenovo.library;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 10129302 guopf on 15-6-26.
 */
public class StepsViewIndicator extends View
{

    private static final String tag = StepsViewIndicator.class.getSimpleName();

    private static final int THUMB_SIZE = 100;
    private Paint paint;
    private Paint selectedPaint;
    private int mNumOfStep = 2;
    private float mLineHeight;

    private float mThumbRadius;
    private float mCircleRadius;
    private float mPadding;

    /**
     * 已经完成部分的颜色
     */
    private int mThumbColor = Color.GRAY;
    /**
     * 未完成部分的颜色
     */
    private int mBarColor = Color.GREEN;

    private float mCenterY, mLeftX, mLeftY, mRightX, mRightY;
    private float mDelta;

    private List<Float> mThumbContainerXPosition = new ArrayList<>();
    private int mCompletedPosition;

    /**
     * 每一步成功的icon
     */
    private Drawable complete_icon;
    /**
     * 每一步，失败的icon
     */
    private Drawable failed_icon;

    private Rect rect = new Rect();

    private boolean isError = false;
    private OnDrawListener mDrawListener;

    private int xOffset;
    /**
     * 动画 0-1的时间
     */
    private float mInterpolatedTime;
    /**
     * 等待动画
     */
    private WaitingAnimation waitingAnimation;
    /**
     * 等待小圆点画笔
     */
    private Paint waitDotsPaint;
    /**
     * 等待小圆点的半径
     */
    private int waitDotRadius = 2;
    /**
     * 动画完成一次
     */
    private boolean sign = true;

    public interface OnDrawListener
    {
        void onFinish();
    }

    public StepsViewIndicator(Context context)
    {
        this(context, null);
    }

    public StepsViewIndicator(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public StepsViewIndicator(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public List<Float> getThumbContainerXPosition()
    {
        return mThumbContainerXPosition;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterY = 0.5f * getHeight();
        mLeftX = mPadding;
        mLeftY = mCenterY - (mLineHeight / 2);
        mRightX = getWidth() - mPadding;
        mRightY = 0.5f * (getHeight() + mLineHeight);
        mDelta = (mRightX - mLeftX) / (mNumOfStep - 1);

        mThumbContainerXPosition.add(mLeftX);
        for (int i = 1; i < mNumOfStep - 1; i++)
        {
            mThumbContainerXPosition.add(mLeftX + (i * mDelta));
        }
        mThumbContainerXPosition.add(mRightX);
        mDrawListener.onFinish();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        for (int i = 0; i < mThumbContainerXPosition.size(); i++)
        {
            canvas.drawCircle(mThumbContainerXPosition.get(i), mCenterY, mCircleRadius,
                (i <= mCompletedPosition) ? selectedPaint : paint);
        }

        for (int i = 0; i < mThumbContainerXPosition.size() - 1; i++)
        {
            final float pos = mThumbContainerXPosition.get(i);
            final float pos2 = mThumbContainerXPosition.get(i + 1);
            canvas.drawRect(pos, mLeftY, pos2, mRightY, (i < mCompletedPosition)
                ? selectedPaint
                : paint);
        }

        for (int i = 0; i < mThumbContainerXPosition.size(); i++)
        {
            paint.setStyle(Paint.Style.FILL);
            final float pos = mThumbContainerXPosition.get(i);
            canvas.drawCircle(pos, mCenterY, mCircleRadius, (i <= mCompletedPosition)
                ? selectedPaint
                : paint);

            rect = new Rect((int) (pos - mCircleRadius),
                (int) (mCenterY - mCircleRadius), (int) (pos + mCircleRadius),
                (int) (mCenterY + mCircleRadius));

            //计算偏差
            xOffset = (int) ((pos - mCircleRadius) + (mCircleRadius * 2 - waitDotRadius * 10) / 2);

            if (i < mCompletedPosition)
            {
                complete_icon.setBounds(rect);
                complete_icon.draw(canvas);
            }
            else if (i == mCompletedPosition)
            {
                if (isError)
                {
                    failed_icon.setBounds(rect);
                    failed_icon.draw(canvas);
                }
                else
                { //画进度
                    if (sign)
                    {
                        canvas.save();
                        canvas.rotate(180 * mInterpolatedTime, xOffset + waitDotRadius
                            * 3, getHeight() / 2);
                        canvas.drawCircle(xOffset + waitDotRadius, getHeight() / 2,
                            waitDotRadius, waitDotsPaint);
                        canvas.drawCircle(xOffset + waitDotRadius * 5, getHeight() / 2,
                            waitDotRadius, waitDotsPaint);
                        canvas.restore();
                        canvas.drawCircle(xOffset + waitDotRadius * 9, getHeight() / 2,
                            waitDotRadius, waitDotsPaint);
                    }
                    else
                    {
                        canvas.save();
                        canvas.drawCircle(xOffset + waitDotRadius, getHeight() / 2,
                            waitDotRadius, waitDotsPaint);
                        canvas.rotate(180 * mInterpolatedTime, xOffset + waitDotRadius
                            * 7, getHeight() / 2);
                        canvas.drawCircle(xOffset + waitDotRadius * 5, getHeight() / 2,
                            waitDotRadius, waitDotsPaint);
                        canvas.drawCircle(xOffset + waitDotRadius * 9, getHeight() / 2,
                            waitDotRadius, waitDotsPaint);
                        canvas.restore();
                    }
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        Log.d(tag, " onDetached from window");
        waitingAnimation.cancel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec))
        {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = THUMB_SIZE + 20;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec))
        {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }

        setMeasuredDimension(width, height);
    }

    public void setDrawListener(OnDrawListener listener)
    {
        this.mDrawListener = listener;
    }

    public void setSize(int size)
    {
        mNumOfStep = size;
        invalidate();
    }

    /**
     * 设置进度结束的位置，这是成功的位置
     * 
     * @param position, 从零开始
     */
    public void setCompletedPosition(int position)
    {
        mCompletedPosition = position;
        isError = false;
        invalidate();
    }

    /**
     * 设置进度失败的位置
     * 
     * @param position，从零开始
     */
    public void setErrorPosition(int position)
    {
        mCompletedPosition = position;
        isError = true;
        invalidate();
    }

    public void reset()
    {
        setCompletedPosition(0);
    }

    public void setThumbColor(int thumbColor)
    {
        mThumbColor = thumbColor;
    }

    public void setBarColor(int barColor)
    {
        mBarColor = barColor;
    }

    private void init(Context context, AttributeSet attrs)
    {
        if (attrs != null)
        {
            TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.StepsViewIndicator);
            mNumOfStep = ta.getInt(R.styleable.StepsViewIndicator_numOfSteps, 0);
            ta.recycle();
        }

        init();
    }

    private void init()
    {
        mLineHeight = 0.2f * THUMB_SIZE;
        mThumbRadius = 0.4f * THUMB_SIZE;
        mCircleRadius = 0.7f * mThumbRadius;
        mPadding = 0.5f * THUMB_SIZE;

        complete_icon = getResources().getDrawable(R.drawable.load_toast_check);
        failed_icon = getResources().getDrawable(R.drawable.load_toast_error);

        waitingAnimation = new WaitingAnimation();
        waitingAnimation.setDuration(500);
        waitingAnimation.setInterpolator(new DecelerateInterpolator());
        waitingAnimation.setRepeatCount(Animation.INFINITE);
        waitingAnimation.setRepeatMode(Animation.RESTART);
        startAnimation(waitingAnimation);

        waitDotRadius = (int) mCircleRadius / 5;

        waitDotsPaint = new Paint();
        waitDotsPaint.setAntiAlias(true); // 设置画笔为抗锯齿
        waitDotsPaint.setColor(Color.WHITE); // 设置画笔颜色
        waitDotsPaint.setStyle(Paint.Style.FILL);

        paint = new Paint();
        selectedPaint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mBarColor);
        paint.setStyle(Paint.Style.FILL);

        selectedPaint.setAntiAlias(true);
        selectedPaint.setColor(mThumbColor);
        paint.setStyle(Paint.Style.FILL);
    }

    private class WaitingAnimation extends Animation
    {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t)
        {
            super.applyTransformation(interpolatedTime, t);
            mInterpolatedTime = interpolatedTime;
            invalidate();
            if (mInterpolatedTime == 1)
                sign = !sign;
        }
    }

}
