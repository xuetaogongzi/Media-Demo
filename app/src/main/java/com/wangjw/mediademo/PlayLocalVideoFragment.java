package com.wangjw.mediademo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by hjy on 3/11/16.<br>
 */

public class PlayLocalVideoFragment extends Fragment {

    public static final String KEY_VIDEO_PATH = "path";

    private VideoView mVideoView;

    private MediaController mController;
    private String mVideoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mVideoPath = args.getString(KEY_VIDEO_PATH);
        }
        if (mVideoPath == null && savedInstanceState != null) {
            mVideoPath = savedInstanceState.getString(KEY_VIDEO_PATH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_local_video, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVideoView = (VideoView) view.findViewById(R.id.VideoView_Play);

        if (!TextUtils.isEmpty(mVideoPath)) {
            mController = new MediaController(getActivity());
            mVideoView.setMediaController(mController);
            mVideoView.setVideoURI(Uri.fromFile(new File(mVideoPath)));
            mVideoView.requestFocus();
            mVideoView.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_VIDEO_PATH, mVideoPath);
    }
}
