package io.github.xyzxqs.zxingscanner.demo;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

import com.google.zxing.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.xyzxqs.zxingscanner.cameraview.CameraView;
import io.github.xyzxqs.zxingscanner.decode.RotatableYUVLuminanceSource;
import io.github.xyzxqs.zxingscanner.decode.ZxingDecoder;
import io.github.xyzxqs.zxingscanner.demo.util.ThreadUtils;

import static io.github.xyzxqs.zxingscanner.demo.util.PermissionUtil.checkPermission4AccessCamera;

/**
 * @author xyzxqs
 */
public class CameraViewHelper implements LifecycleObserver {

    private final CameraView cameraView;
    private final Lifecycle lifecycle;
    private ZxingDecoder zxingDecoder = null;

    public CameraViewHelper(Lifecycle lifecycle, CameraView cameraView) {
        this.cameraView = cameraView;
        this.lifecycle = lifecycle;
        lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume() {
        checkPermission4AccessCamera(cameraView, cameraView::start);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void onPause() {
        cameraView.stop();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        lifecycle.removeObserver(this);
    }

    public void startDecode() {
        if (zxingDecoder == null) {
            zxingDecoder = new ZxingDecoder.Builder()
                    .build();
        }
        cameraView.requestOneShotPreview((data, width, height) ->
        {
            ThreadUtils.runOnBackgroundThread(() -> {
                RotatableYUVLuminanceSource source = new RotatableYUVLuminanceSource(data, width, height,
                        0, 0, width, height, false);
                final Result result = zxingDecoder.decode(source);

                ThreadUtils.runOnMainThread(() -> {
                    if (result == null) {
                        startDecode();
                    }
                    else {
                        handleDecodeResult(result);
                    }
                });
            });
        });
    }


    private OnDecodeResult onDecodeResult = null;

    private void handleDecodeResult(Result rawResult) {
        if (onDecodeResult != null) {
            onDecodeResult.handleDecodeResult(rawResult);
        }
    }

    public void setDecodeResultHandler(OnDecodeResult onDecodeResult) {
        this.onDecodeResult = onDecodeResult;
    }

    public interface OnDecodeResult {
        void handleDecodeResult(Result rawResult);
    }
}
