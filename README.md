# PermissionUtil

# Usage

    PermissionUtil.with(this) // Activity or Fragment
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
        
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // don't forget
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
