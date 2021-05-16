package com.chewwwyong.project_messenger.Util;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionTool {

    public static final int PERMISSION_REQUEST_SETTING = 8000;
    public static final int PERMISSION_REQUEST_CALL_PHONE = 8001;
    public static final int PERMISSION_REQUEST_EXTERNAL_STORAGE_WRITE = 8002;
    public static final int PERMISSION_REQUEST_CAMERA = 8003;
    public static final int PERMISSION_REQUEST_LOCATION = 8003;

    private static PermissionTool instance;

    public static PermissionTool getInstance() {
        if (instance == null)
            instance = new PermissionTool();

        return instance;
    }

    public boolean isReadExternalStorageGranted(AppCompatActivity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestReadExternalStoragePermission(AppCompatActivity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_EXTERNAL_STORAGE_WRITE);
    }

    public boolean isWriteExternalStorageGranted(AppCompatActivity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestWriteExternalStoragePermission(AppCompatActivity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_EXTERNAL_STORAGE_WRITE);
    }

    public boolean isCameraGranted(AppCompatActivity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraPermission(AppCompatActivity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CAMERA);
    }
    public void requestMultiPermission(AppCompatActivity activity, String[] permissions,
                                       int requestCode) {
        ActivityCompat.requestPermissions(activity,
                permissions,
                requestCode);
    }
}
