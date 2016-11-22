package com.wangjw.mediademo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.wangjw.cameralib.CameraFragment;
import com.wangjw.cameralib.CameraFragmentListener;
import com.wangjw.cameralib.CameraTouchListener;
import com.wangjw.mediademo.global.AppConfig;
import com.wangjw.mediademo.utils.DateUtil;
import com.wangjw.mediademo.utils.StorageUtils;
import com.wangjw.mediademo.utils.ToastUtil;
import com.wangjw.mediademo.widget.RecordProgressBar;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangjw on 16/11/22.
 */

public class VideoActivity extends AppCompatActivity implements View.OnClickListener, CameraFragmentListener, CameraTouchListener, RecordProgressBar.OnFinishListener {

    private Button mBtnStart;
    private RecordProgressBar mProgressBar;
    private ImageButton mBtnPlay;
    private Button mBtnOk;
    private Button mBtnRetry;

    private CameraFragment mCameraFragment =  new CameraFragment();;
    private MediaRecorder mRecorder;
    private String mVideoPath;

    private boolean mIsRecording = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        findViews();
        initViews();
    }

    private void findViews() {
        mBtnStart = (Button) findViewById(R.id.Button_Record_Start);
        mProgressBar = (RecordProgressBar) findViewById(R.id.RecordProgressBar_Record_Video);
        mBtnPlay = (ImageButton) findViewById(R.id.ImageButton_Record_Play);
        mBtnOk = (Button) findViewById(R.id.Button_Record_Ok);
        mBtnRetry = (Button) findViewById(R.id.Button_Record_Retry);
    }

    private void initViews() {
        mBtnOk.setEnabled(false);
        mBtnRetry.setEnabled(false);

        findViewById(R.id.ImageView_Record_Close).setOnClickListener(this);
        findViewById(R.id.ImageView_Record_Camera).setOnClickListener(this);
        mBtnRetry.setOnClickListener(this);
        mBtnOk.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);

        initCamera();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ImageView_Record_Close) {                   //关闭
            finish();
        } else if (view.getId() == R.id.ImageView_Record_Camera) {          //切换前后摄像头
            mCameraFragment.switchCamera();
        } else if(view.getId() == R.id.Button_Record_Retry) {               //重试
            mProgressBar.reset();
            mBtnPlay.setVisibility(View.GONE);
            mVideoPath = null;
            mBtnRetry.setEnabled(false);
            mBtnOk.setEnabled(false);
            mBtnStart.setEnabled(true);
            mCameraFragment.restartCamera();
        } else if(view.getId() == R.id.Button_Record_Ok) {                  //确定
            if (TextUtils.isEmpty(mVideoPath)) {
                ToastUtil.showToast(this, R.string.pls_record_video_first);
            }
        } else if(view.getId() == R.id.ImageButton_Record_Play) {
            Intent intent = new Intent(this, FragmentHolderActivity.class);
            intent.putExtra(FragmentHolderActivity.KEY_FRAGMENT_TYPE, PlayLocalVideoFragment.class);
            Bundle bundle = new Bundle();
            bundle.putString(PlayLocalVideoFragment.KEY_VIDEO_PATH, mVideoPath);
            intent.putExtra(FragmentHolderActivity.KEY_FRAGMENT_DATA, bundle);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaRecorder();
    }

    private void initCamera() {
        int n = Camera.getNumberOfCameras();
        if (n <= 1) {
            findViewById(R.id.ImageView_Record_Camera).setVisibility(View.GONE);
        }

        if (n > 1) {
            //默认打开前者摄像头
            mCameraFragment.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            mCameraFragment.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayout_Record_Camera, mCameraFragment).commitAllowingStateLoss();
        mCameraFragment.setTouchable(true);

        mBtnStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBtnStart.setBackgroundResource(R.drawable.btn_record_active);
                    if (!mIsRecording && !mProgressBar.isCountingDown()) {
                        mIsRecording = true;
                        ToastUtil.showToast(VideoActivity.this, R.string.start_record);

                        startRecorder();
                        mProgressBar.startCountDown();
                        mProgressBar.setOnFinishListener(VideoActivity.this);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    mBtnStart.setBackgroundResource(R.drawable.btn_record_normal);
                    onRecordFinish();
                    mIsRecording = false;
                }
                return true;
            }
        });
    }

    @Override
    public void onCameraError() {
        ToastUtil.showToast(this, R.string.camera_init_error);
    }

    @Override
    public void onPictureTaken(Bitmap bitmap) {
    }

    @Override
    public void onLeftSlide() {
    }

    @Override
    public void onRightSlide() {
    }

    @Override
    public void onRecordFinish() {
        if (mIsRecording) {
            long startTime = mProgressBar.getRecordStartTimeMillis();
            mProgressBar.stop();
            releaseMediaRecorder();

            if (System.currentTimeMillis() - startTime < 2000) {
                //录制时间太短了
                mVideoPath = null;
                mProgressBar.reset();
                ToastUtil.showToast(VideoActivity.this, R.string.record_time_too_short);
                return;
            }

            ToastUtil.showToast(VideoActivity.this, R.string.finish_record);
            mBtnPlay.setVisibility(View.VISIBLE);
            mBtnRetry.setEnabled(true);
            mBtnOk.setEnabled(true);
            mBtnStart.setEnabled(false);
            mCameraFragment.closeCamera();
            mIsRecording = false;
        }
    }

    /**
     * 开始录制视频
     */
    private void startRecorder() {
        android.hardware.Camera camera = mCameraFragment.getCamera();
        if (camera == null) {
            onCameraError();
            return;
        }

        mRecorder = new MediaRecorder();
        camera.unlock();
        mRecorder.setCamera(camera);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); //视频源
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);    //音频源
        mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mRecorder.setVideoFrameRate(35);
        mRecorder.setMaxFileSize(0);        //不限制大小
        mRecorder.setMaxDuration(50 * 1000);
        if (mCameraFragment.getCameraId() == 0)
            mRecorder.setOrientationHint(90);
        else {
            //如果是前置摄像头
            mRecorder.setOrientationHint(270);
        }
        mRecorder.setPreviewDisplay(mCameraFragment.getHolder().getSurface());

        String fileName = DateUtil.format(System.currentTimeMillis(), "yyyyMMddHHmmss");
        File cacheDir = StorageUtils.getOwnCacheDirectory(this, AppConfig.VIDEO_CACHE_PATH);
        try {
            mVideoPath = File.createTempFile(fileName, ".mp4", cacheDir).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.setOutputFile(mVideoPath);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            ToastUtil.showToast(this, R.string.record_error);
        }
    }

    /**
     * 资源释放
     */
    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }
    }
}
