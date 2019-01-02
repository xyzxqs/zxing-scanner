package io.github.xyzxqs.zxingscanner.demo.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import io.github.xyzxqs.zxingscanner.demo.util.internal.CallerFragment;
import io.github.xyzxqs.zxingscanner.demo.util.internal.CallerSupportFragment;


/**
 * @author xyzxqs
 */

public class Caller {
    private static final String CALLER_TAG = "__caller_fragment_tag";

    private Caller() {

    }

    public interface OnActivityResult {
        void onCallerActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface OnRequestPermissionsResult {
        void onCallerPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }

    public interface Delegate {
        void canRequestPermission();
    }

    public interface Callback {
        void shouldShowRequestPermissionsRationale(int reqCode, Delegate delegate);

        void permissionsResult(int reqCode, boolean granted);
    }

    public static void startActivityForResult(Context context, Intent intent, int requestCode, OnActivityResult onActivityResult) {
        if (context instanceof FragmentActivity) {
            FragmentActivity compatActivity = ((FragmentActivity) context);
            FragmentManager supportFragmentManager = compatActivity.getSupportFragmentManager();
            CallerSupportFragment fragment = getCallerSupportFragment(supportFragmentManager);
            fragment.startActivityForResult(intent, requestCode, onActivityResult);
        }
        else if (context instanceof Activity) {
            Activity activity = ((Activity) context);
            android.app.FragmentManager fragmentManager = activity.getFragmentManager();
            CallerFragment fragment = getCallerFragment(fragmentManager);
            fragment.startActivityForResult(intent, requestCode, onActivityResult);
        }
        else {
            throw new IllegalStateException("bad context");
        }
    }

    private static CallerFragment getCallerFragment(android.app.FragmentManager fragmentManager) {
        CallerFragment fragment = (CallerFragment) fragmentManager.findFragmentByTag(CALLER_TAG);
        if (fragment == null) {
            fragment = CallerFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(fragment, CALLER_TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    private static CallerSupportFragment getCallerSupportFragment(FragmentManager supportFragmentManager) {
        CallerSupportFragment fragment = (CallerSupportFragment) supportFragmentManager.findFragmentByTag(CALLER_TAG);
        if (fragment == null) {
            fragment = CallerSupportFragment.newInstance();
            supportFragmentManager.beginTransaction()
                    .add(fragment, CALLER_TAG)
                    .commitAllowingStateLoss();
            supportFragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)//通常不会在外部使用这个方法
    public static void requestPermissions(Context context, @NonNull String[] permissions, int requestCode, OnRequestPermissionsResult onResult) {
        if (context instanceof FragmentActivity) {
            FragmentActivity compatActivity = ((FragmentActivity) context);
            FragmentManager supportFragmentManager = compatActivity.getSupportFragmentManager();
            CallerSupportFragment fragment = getCallerSupportFragment(supportFragmentManager);
            fragment.requestPermissions(permissions, requestCode, onResult);
        }
        else if (context instanceof Activity) {
            Activity activity = ((Activity) context);
            android.app.FragmentManager fragmentManager = activity.getFragmentManager();
            CallerFragment fragment = getCallerFragment(fragmentManager);
            fragment.requestPermissions(permissions, requestCode, onResult);
        }
        else {
            throw new IllegalStateException("bad context");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkSelfPermissions(final Context context, final int reqCode, final Callback callback, final String... permission) {
        if (checkSelfPermissions(context, permission)) {
            callback.permissionsResult(reqCode, true);
        }
        else {
            final OnRequestPermissionsResult result = (requestCode, permissions, grantResults) -> {
                if (callback != null) {
                    callback.permissionsResult(reqCode, verifyPermissions(grantResults));
                }
            };
            if (shouldShowRequestPermissionRationale(context, permission)) {
                callback.shouldShowRequestPermissionsRationale(reqCode,
                        () -> requestPermissions(context, permission, reqCode, result));
            }
            else {
                requestPermissions(context, permission, reqCode, result);
            }
        }
    }

    private interface PermissionChecker {
        boolean should(String permission);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean shouldShowRequestPermissionRationale(Context context, String... permissions) {
        if (permissions.length < 1) {
            return false;
        }
        PermissionChecker checker;
        if (context instanceof FragmentActivity) {
            final FragmentActivity compatActivity = ((FragmentActivity) context);
            checker = compatActivity::shouldShowRequestPermissionRationale;
        }
        else if (context instanceof Activity) {
            final Activity activity = ((Activity) context);
            checker = activity::shouldShowRequestPermissionRationale;
        }
        else {
            throw new IllegalStateException("bad context");
        }

        for (String p : permissions) {
            if (checker.should(p)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkSelfPermissions(Context context, @NonNull String... permission) {
        if (permission.length < 1) {
            return false;
        }

        for (String p : permission) {
            if (ActivityCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static boolean verifyPermissions(int[] grantResults) {
        // At least one onCallerActivityResult must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
