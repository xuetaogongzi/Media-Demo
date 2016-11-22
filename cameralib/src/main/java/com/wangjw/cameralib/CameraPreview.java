package com.wangjw.cameralib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by wangjw on 16/11/17.
 */

public class CameraPreview extends SurfaceView {

    private static final double ASPECT_RATIO = 3.0 / 4.0;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (width < height * ASPECT_RATIO) {
            width = (int) (height * ASPECT_RATIO + .5);
        } else {
            height = (int) (width / ASPECT_RATIO + .5);
        }

        setMeasuredDimension(width, height);

    }
}
