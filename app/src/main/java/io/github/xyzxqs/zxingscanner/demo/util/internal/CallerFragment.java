package io.github.xyzxqs.zxingscanner.demo.util.internal;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.util.SparseArray;

import io.github.xyzxqs.zxingscanner.demo.util.Caller;


/**
 * @author xyzxqs
 */

public class CallerFragment extends Fragment {
    private final SparseArray<Caller.OnActivityResult> activityResultArray;
    private final SparseArray<Caller.OnRequestPermissionsResult> requestPermissionsResultArray;

    public CallerFragment() {
        activityResultArray = new SparseArray<>();
        requestPermissionsResultArray = new SparseArray<>();
    }

    public static CallerFragment newInstance() {
        return new CallerFragment();
    }

    public void startActivityForResult(Intent intent, int requestCode, Caller.OnActivityResult onActivityResult) {
        activityResultArray.put(requestCode, onActivityResult);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Caller.OnActivityResult result = activityResultArray.get(requestCode, null);
        if (result != null) {
            result.onCallerActivityResult(requestCode, resultCode, data);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(String[] permissions, int requestCode, Caller.OnRequestPermissionsResult onResult) {
        requestPermissionsResultArray.put(requestCode, onResult);
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Caller.OnRequestPermissionsResult result = requestPermissionsResultArray.get(requestCode, null);
        if (result != null) {
            result.onCallerPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
