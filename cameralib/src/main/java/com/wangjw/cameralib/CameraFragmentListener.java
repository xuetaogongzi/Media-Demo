package com.wangjw.cameralib;

import android.graphics.Bitmap;

/**
 * Created by wangjw on 16/11/17.
 */

public interface CameraFragmentListener {

    /**
     * A non-recoverable camera error has happened.
     */
    void onCameraError();

    /**
     * A picture has been taken.
     *
     * @param bitmap
     */
    void onPictureTaken(Bitmap bitmap);

}
