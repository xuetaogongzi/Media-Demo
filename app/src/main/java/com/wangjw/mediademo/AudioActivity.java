package com.wangjw.mediademo;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wangjw.mediademo.global.AppConfig;
import com.wangjw.mediademo.utils.DateUtil;
import com.wangjw.mediademo.utils.Logger;
import com.wangjw.mediademo.utils.RecordMeter;
import com.wangjw.mediademo.utils.StorageUtils;
import com.wangjw.mediademo.utils.ToastUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangjw on 16/11/16.
 */

public class AudioActivity extends FragmentActivity implements View.OnClickListener, View.OnTouchListener {

    private static final int MSG_WHAT = 1;
    
    private ImageView mImgBack;
    private LinearLayout mLlAudio;
    private ImageView mImgPlayLabel;
    private TextView mTvDuration;
    private ImageView mImgDelete;

    private TextView mTvVoiceLabel;
    private ImageView mImgVoicePress;
    private TextView mTvAddDuration;

    private Handler mHandler;
    private Runnable mRecordTimerTask;  //用于控制录音时长，到阀值时自动进行手指抬起动作
    private RecordMeter mRecordMeter;  //录音工具类
    private MediaPlayer mMediaPlayer;  //音频播放类
    private File mTempAudioFile;  //音频临时文件
    
    private long mRecordStartTime; //录音开始时间，用于记录录音的时长
    private int mTimeStartIndex; //录音自增时间
    
    private boolean mIsPlaying; //是否在播放
    private boolean mCanRecord; //是否在录音

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        findViews();
        initViews();
    }

    private void findViews() {
        mImgBack = (ImageView) findViewById(R.id.ImageView_Back);
        mLlAudio = (LinearLayout) findViewById(R.id.LinearLayout_Audio);
        mImgPlayLabel = (ImageView) findViewById(R.id.ImageView_Play_Label);
        mTvDuration = (TextView) findViewById(R.id.TextView_Duration);
        mImgDelete = (ImageView) findViewById(R.id.ImageView_Delete);

        mTvVoiceLabel = (TextView) findViewById(R.id.TextView_Voice_Label);
        mImgVoicePress = (ImageView) findViewById(R.id.ImageView_Voice_Press);
        mTvAddDuration = (TextView) findViewById(R.id.TextView_Add_Duration);
    }

    private void initViews() {
        mImgBack.setOnClickListener(this);
        mImgPlayLabel.setOnClickListener(this);
        mImgDelete.setOnClickListener(this);
        mImgVoicePress.setOnTouchListener(this);

        mRecordMeter = new RecordMeter();
        mMediaPlayer = new MediaPlayer();
        
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mTvAddDuration.setText(DateUtil.formatAudioRecordTime(++mTimeStartIndex));
                sendEmptyMessageDelayed(MSG_WHAT, 1000);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelRecord();
        stopPlayAudio();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ImageView_Back) {
            finish();
        } else if (v.getId() == R.id.ImageView_Play_Label) {
            if (mIsPlaying) {
                stopPlayAudio();
                mImgPlayLabel.setImageResource(R.drawable.ic_voice_start);
                mIsPlaying = false;
            } else {
                try {
                    startPlayAudio();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mImgPlayLabel.setImageResource(R.drawable.ic_voice_stop);
                mIsPlaying = true;
            }
        } else if (v.getId() == R.id.ImageView_Delete) {
            mLlAudio.setVisibility(View.INVISIBLE);
            deleteTempFile();
            mTimeStartIndex = 0;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mLlAudio.getVisibility() == View.VISIBLE) {
            ToastUtil.showToast(this, "请先删除录音");
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mImgVoicePress.setImageResource(R.drawable.ic_speak_active);
                mTvVoiceLabel.setText("正在说话");

                if (!mCanRecord) {
                    mRecordStartTime = System.currentTimeMillis();
                    File cacheDir = StorageUtils.getOwnCacheDirectory(this, AppConfig.AUDIO_CACHE_PATH);
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    mTempAudioFile = new File(cacheDir, mRecordStartTime + "");
                    startRecord(mTempAudioFile.getAbsolutePath(), event);
                    Logger.d("Voice", "start record...");
                    mCanRecord = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mCanRecord) {
                    long duration = System.currentTimeMillis() - mRecordStartTime;
                    if (duration < 5000) {
                        tooShort();
                    } else {
                        finishRecord();
                    }
                    mCanRecord = false;
                }
                Logger.d("Voice", "stop record.");

                mRecordMeter.stop();
                mImgVoicePress.setImageResource(R.drawable.ic_speak_normal);
                mTvVoiceLabel.setText("按住说话");
                mTvAddDuration.setText("00:00");
                break;
            }
        }
        return true;
    }

    private void startRecord(String fileName, final MotionEvent event) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);

        mRecordMeter.start(fileName);

        //300秒后强制结束
        mRecordTimerTask = new Runnable() {
            @Override
            public void run() {
                event.setAction(MotionEvent.ACTION_UP);
                mRecordTimerTask = null;
            }
        };

        mHandler.postDelayed(mRecordTimerTask, 299 * 1000);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT, 1000);
    }

    private void finishRecord() {
        removeMessage();
        mLlAudio.setVisibility(View.VISIBLE);
        
        int audioDuration = (int) ((System.currentTimeMillis() - mRecordStartTime) / 1000);
        mTvDuration.setText(DateUtil.formatDurationAudioTime(audioDuration));
    }

    private void cancelRecord() {
        removeMessage();
        mRecordMeter.stop();
        deleteTempFile();
        mTimeStartIndex = 0;
    }

    private void tooShort() {
        cancelRecord();
        ToastUtil.showToast(this, "录音时间不能少于5秒");
    }

    private void removeMessage() {
        mHandler.removeMessages(MSG_WHAT);
        if (mRecordTimerTask != null) {
            mHandler.removeCallbacks(mRecordTimerTask);
            mRecordTimerTask = null;
        }
    }

    private void deleteTempFile() {
        if (mTempAudioFile != null && mTempAudioFile.exists()) {
            mTempAudioFile.delete();
            mTempAudioFile = null;
        }
    }

    private void startPlayAudio() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        stopPlayAudio();

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(mTempAudioFile.getAbsolutePath());
        mMediaPlayer.prepare();
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mImgPlayLabel.setImageResource(R.drawable.ic_voice_start);
                mIsPlaying = false;
            }
        });
    }

    private void stopPlayAudio() {
        //停止其他播放的
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }
}
