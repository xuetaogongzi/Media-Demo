package com.wangjw.cameralib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wangjw on 16/11/17.
 */

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback, View.OnTouchListener {

    public static final String TAG = "Mustache/CameraFragment";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int cameraId;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private CameraFragmentListener listener;
    private int displayOrientation;
    private int layoutOrientation;
    private Activity mActivity;

    private CameraOrientationListener orientationListener;
    private CameraTouchListener touchListener;

    //SurfaceView缩放事件参数
    private int max_zoom = 0;
    private int curr_zoom = 0;
    private int mode = NONE;
    private float x1, x2;
    private boolean[] tracking = {false, false, false, false, false};
    private boolean canTouch = true;
    private GestureAnalyser ga;

    private static final int NONE = 0;  //初始状态
    private static final int SINGLE = 1;  //单指
    private static final int DOUBLE = 2;  //双指

    private boolean mClosed = false;

    /**
     * On activity getting attached.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalArgumentException("Activity has to implement CameraFragmentListener interface");
        }

        if (!(activity instanceof CameraTouchListener)) {
            throw new IllegalArgumentException("Activity has to implement CameraTouchListener interface");
        }

        listener = (CameraFragmentListener) activity;
        touchListener = (CameraTouchListener) activity;
        orientationListener = new CameraOrientationListener(activity);
    }

    /**
     * On creating view for fragment.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CameraPreview preview = new CameraPreview(mActivity);
        preview.getHolder().addCallback(this);

        ga = new GestureAnalyser();
        preview.setOnTouchListener(this);

        return preview;
    }

    /**
     * On fragment getting resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (mClosed) {
            return;
        }

        //检查权限
        if (mActivity != null) {
            int cameraPermission = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        orientationListener.enable();
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e(TAG, "Can't open camera with id " + cameraId, e);
            listener.onCameraError();
            return;
        }
    }

    /**
     * On fragment getting paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mClosed) {
            return;
        }

        //检查权限
        if (mActivity != null) {
            int cameraPermission = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        orientationListener.disable();

        if (camera != null) {
            stopCameraPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera() {
        if (camera != null) {
            stopCameraPreview();
            camera.release();
            camera = null;
        }
        mClosed = true;
    }

    /**
     * 重新开启摄像头
     */
    public void restartCamera() {
        closeCamera();
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e(TAG, "Can't open camera with id " + cameraId, e);
            listener.onCameraError();
            return;
        }

        startCameraPreview();
        mClosed = false;
    }

    public boolean switchCamera() {
        int n = Camera.getNumberOfCameras();
        if (n == 1) {
            return false;
        }

        if (cameraId == 0) {
            cameraId = 1;
        } else {
            cameraId = 0;
        }

        if (camera != null) {
            stopCameraPreview();
            camera.release();
            camera = null;
        }

        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e(TAG, "Can't open camera with id " + cameraId, e);
            listener.onCameraError();
            return false;
        }

        startCameraPreview();
        return true;
    }

    /**
     * Start the camera preview.
     */
    private void startCameraPreview() {
        if (camera == null) {
            return;
        }
        determineDisplayOrientation();
        setupCamera();

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Can't start camera preview due to Exception", e);
            listener.onCameraError();
        }
    }

    /**
     * Stop the camera preview.
     */
    private void stopCameraPreview() {
        try {
            if (camera != null) {
                camera.stopPreview();
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception during stopping camera preview.");
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly.
     */
    public void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int displayOrientation;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        this.displayOrientation = displayOrientation;
        this.layoutOrientation = degrees;

        camera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters.
     */
    public void setupCamera() {
        Camera.Parameters param = camera.getParameters();

        Camera.Size bestPreviewSize = determineBestPreviewSize(param);
        Camera.Size bestPictureSize = determineBestPictureSize(param);
        param.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        param.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        List<String> focusModes = param.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(param);
    }

    private Camera.Size determineBestPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    private Camera.Size determineBestPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    private Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
        Camera.Size bestSize = null;

        for (Camera.Size currentSize : sizes) {
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

            if (isDesiredRatio && isInBounds && isBetterSize) {
                bestSize = currentSize;
            }
        }

        if (bestSize == null) {
            listener.onCameraError();
            return sizes.get(0);
        }

        return bestSize;
    }

    /**
     * Take a picture and notify the listener once the picture is taken.
     */
    public void takePicture() {
        orientationListener.rememberOrientation();
        camera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {//快门声

            }
        }, null, this);
    }

    /**
     * A picture has been taken.
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        int rotation = (displayOrientation + orientationListener.getRememberedOrientation() + layoutOrientation) % 360;

        if (rotation != 0) {
            Bitmap oldBitmap = bitmap;
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            oldBitmap.recycle();
        }

        listener.onPictureTaken(bitmap);
        camera.startPreview();
    }

    /**
     * On camera preview surface created.
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        startCameraPreview();
    }

    /**
     * On camera preview surface changed.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * On camera preview surface getting destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public Camera getCamera() {
        return camera;
    }

    public SurfaceHolder getHolder() {
        return surfaceHolder;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    /**
     * 缩放相机放大倍数
     *
     * @param zoom
     */
    public void zoomCamera(int zoom) {
        System.out.println("zoom Camera " + zoom);
        if (camera == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        if (parameters == null || !parameters.isZoomSupported()) {
            return;
        }

        parameters.setZoom(zoom);
        camera.setParameters(parameters);
        if (parameters.isSmoothZoomSupported()) {
            camera.startSmoothZoom(zoom);
        }
    }

    /**
     * 打开闪光灯
     */
    public void turnOnLight() {
       if (camera == null) {
           return;
       }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    /**
     * 关闭闪光灯
     */
    public void turnOffLight() {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    /**
     * 设置是否响应OnTouch事件
     *
     * @param touchable
     */
    public void setTouchable(boolean touchable) {
        canTouch = touchable;
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!canTouch) {
            return true;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = SINGLE;
                x1 = event.getX();
                startTracking(0);
                ga.trackGesture(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = DOUBLE;
                startTracking(event.getPointerCount() - 1);
                ga.trackGesture(event);
                break;
            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                if (tracking[1]) {
                    max_zoom = camera.getParameters().getMaxZoom();
                    if (ga.getGesture(event).getGestureFlag() == GestureAnalyser.PINCH_2) {
                        curr_zoom--;
                        if (curr_zoom <= 0 || max_zoom <= 0) {
                            curr_zoom = 0;
                        }
                        zoomCamera(curr_zoom);
                    } else if (ga.getGesture(event).getGestureFlag() == GestureAnalyser.UNPINCH_2) {
                        curr_zoom++;
                        if (curr_zoom > max_zoom) {
                            curr_zoom = max_zoom;
                        }
                        zoomCamera(curr_zoom);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (tracking[0] && !tracking[1] && mode == SINGLE) {
                    if (x2 - x1 > -100) {
                        touchListener.onLeftSlide();
                    } else {
                        touchListener.onRightSlide();
                    }
                }
                mode = NONE;
                stopTracking(0);
                ga.untrackGesture();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (tracking[1]) {
                    //手势处理
                }
                stopTracking(event.getPointerCount() - 1);
                ga.untrackGesture();
                break;
        }

        return true;
    }

    private void startTracking(int nthPointer) {
        for (int i = 0; i <= nthPointer; i++) {
            tracking[i] = true;
        }
    }

    private void stopTracking(int nthPointer) {
        for (int i = 0; i <= nthPointer; i++) {
            tracking[i] = false;
        }
    }
}
