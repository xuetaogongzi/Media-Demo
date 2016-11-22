package com.wangjw.mediademo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hjy on 3/9/16.<br>
 */
public class RecordProgressBar extends View {

    public static interface OnFinishListener {
        public void onRecordFinish();
    }

    private int mMaxSecond = 30;
    private long mRecordStartTimeMillis = 0;     //录制开始时间

    private Paint mPaint = new Paint();
    private CountDownTimer mTimer;

    private int mInternalMaxProgress = 10000;
    private int mInternalProgress= 0;

    private boolean mCountingDown = false;

    private OnFinishListener mListener;

    public RecordProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint.setColor(0xfffe585c);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 设置倒计时最大秒数
     *
     * @param maxSecond
     */
    public void setMaxSecond(int maxSecond) {
        if(mMaxSecond <= 0)
            throw new IllegalArgumentException("maxProgress must be greater than 0");
        mMaxSecond = maxSecond;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        int w = (int)((mInternalProgress / (mInternalMaxProgress + 0f)) * getWidth());
        canvas.drawRect(0, 0, w, getHeight(), mPaint);
    }

    /**
     * 开始倒计时
     */
    public void startCountDown() {
        stop();
        mCountingDown = true;
        mInternalProgress = 0;
        mTimer = new CountDownTimer(mMaxSecond * 1000, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                mInternalProgress = (int)((mMaxSecond * 1000f - millisUntilFinished) / (mMaxSecond * 1000) * mInternalMaxProgress);
                invalidate();
            }

            @Override
            public void onFinish() {
                mInternalProgress = mInternalMaxProgress;
                invalidate();
                mCountingDown = false;
                if(mListener != null)
                    mListener.onRecordFinish();
                mRecordStartTimeMillis = 0;
            }
        };
        mTimer.start();
        mRecordStartTimeMillis = System.currentTimeMillis();
    }

    public void stop() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mRecordStartTimeMillis = 0;
        mCountingDown = false;
    }

    public void reset() {
        stop();
        mInternalProgress = 0;
        invalidate();
    }

    public boolean isCountingDown() {
        return mCountingDown;
    }

    public long getRecordStartTimeMillis() {
        return mRecordStartTimeMillis;
    }

    public void setOnFinishListener(OnFinishListener listener) {
        mListener = listener;
    }

}
