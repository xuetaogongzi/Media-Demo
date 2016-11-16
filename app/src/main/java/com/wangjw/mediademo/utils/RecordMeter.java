package com.wangjw.mediademo.utils;

import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Created by wangjw on 16/11/16.
 */

public class RecordMeter {

    private MediaRecorder mRecorder = null;

    public RecordMeter() {

    }

    public void start(String file) {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(file);

            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    public void pause() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
    }

    public void stop() {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mRecorder != null) {
                    mRecorder.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecorder = null;
        }
    }

    public boolean isRecording() {
        return mRecorder != null;
    }

    public int getAmplitude() {
        if (mRecorder != null) {
            return mRecorder.getMaxAmplitude();
        }
        return 0;
    }

}
