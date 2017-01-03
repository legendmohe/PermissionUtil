package com.legendmohe.demo;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.legendmohe.permissionutil.PermissionUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtil.with(this)
                .addPermission(Manifest.permission.CAMERA)
                .setCallback(new PermissionUtil.Callback() {
                    @Override
                    public void onPermissionGranted(String[] permissions) {
                        Log.d(TAG, "onPermissionGranted() called with: permissions = [" + dumpPermissions(permissions) + "]");
                    }

                    @Override
                    public void onPermissionDenied(String[] permissions) {
                        Log.d(TAG, "onPermissionDenied() called with: permissions = [" + dumpPermissions(permissions) + "]");
                    }
                })
                .request();
    }

    private String dumpPermissions(String[] permissions) {
        StringBuffer sb = new StringBuffer();
        for (String permission :
                permissions) {
            sb.append(permission).append("\n");
        }
        if (sb.length() != 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
