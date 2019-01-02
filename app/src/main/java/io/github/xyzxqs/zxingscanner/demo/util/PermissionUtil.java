package io.github.xyzxqs.zxingscanner.demo.util;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;


public class PermissionUtil {

    private PermissionUtil() {
    }

    public static void checkPermission4AccessFile(final View view, final BooleanConsumer permissionResult) {
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
                public void permissionsResult(int reqCode, boolean granted) {
                    permissionResult.accept(granted);
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        else {
            permissionResult.accept(true);
        }
    }

    public static void checkPermission4AccessCamera(final View view, final BooleanConsumer permissionResult) {
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
                public void permissionsResult(int reqCode, boolean granted) {
                    permissionResult.accept(granted);
                }
            }, Manifest.permission.CAMERA);
        }
        else {
            permissionResult.accept(true);
        }
    }
}
