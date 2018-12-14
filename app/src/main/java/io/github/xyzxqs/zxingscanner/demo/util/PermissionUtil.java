package io.github.xyzxqs.zxingscanner.demo.util;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;


public class PermissionUtil {

    private PermissionUtil() {
    }

    public static void checkPermission4AccessFile(final View view, final Action onPermissionGrated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Caller.checkSelfPermissions(view.getContext(), 23, new Caller.Callback() {
                @Override
                public void shouldShowRequestPermissionsRationale(int reqCode, final Caller.Delegate delegate) {
                    Snackbar.make(view, "需要文件权限来继续操作", Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(Color.WHITE)
                            .setAction("允许", v -> delegate.canRequestPermission())
                            .show();
                }

                @Override
                public void permissionsGranted(int reqCode) {
                    onPermissionGrated.run();
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        else {
            onPermissionGrated.run();
        }
    }

    public static void checkPermission4AccessCamera(final View view, final Action onPermissionGrated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Caller.checkSelfPermissions(view.getContext(), 23, new Caller.Callback() {
                @Override
                public void shouldShowRequestPermissionsRationale(int reqCode, final Caller.Delegate delegate) {
                    Snackbar.make(view, "需要相机权限来继续操作", Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(Color.WHITE)
                            .setAction("允许", v -> delegate.canRequestPermission())
                            .show();
                }

                @Override
                public void permissionsGranted(int reqCode) {
                    onPermissionGrated.run();
                }
            }, Manifest.permission.CAMERA);
        }
        else {
            onPermissionGrated.run();
        }
    }
}