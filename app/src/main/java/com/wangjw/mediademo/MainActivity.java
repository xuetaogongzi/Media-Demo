package com.wangjw.mediademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final int REQ_CODE_MEDIA = 1;

    private Button mBtnVideo;
    private Button mBtnAudio;

    private boolean mIsPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        mBtnVideo = (Button) findViewById(R.id.Button_Video);
        mBtnAudio = (Button) findViewById(R.id.Button_Audio);

        mBtnVideo.setOnClickListener(this);
        mBtnAudio.setOnClickListener(this);

        checkPermission();
    }

    @Override
    public void onClick(View v) {
        if (!mIsPermission) {
            return;
        }

        if (v.getId() == R.id.Button_Video) {
            Intent intent = new Intent(this, VideoActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.Button_Audio) {
            Intent intent = new Intent(this, AudioActivity.class);
            startActivity(intent);
        }
    }

    private void checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        List<String> permissList = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissList.add(Manifest.permission.CAMERA);
        }

        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            permissList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (permissList.size() > 0) {
            String[] PermissionArr = new String[permissList.size()];
            for (int i = 0; i < PermissionArr.length; i++) {
                PermissionArr[i] = permissList.get(i);
            }
            ActivityCompat.requestPermissions(this, PermissionArr, REQ_CODE_MEDIA);
        } else {
            mIsPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_MEDIA && grantResults.length > 0) {
            boolean grantAll = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    grantAll = false;
                    break;
                }
            }
            if (grantAll) {
                mIsPermission = true;
            }
        }
    }
}
