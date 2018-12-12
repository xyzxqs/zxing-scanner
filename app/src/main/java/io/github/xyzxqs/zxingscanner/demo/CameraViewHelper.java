package io.github.xyzxqs.zxingscanner.demo;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;

import io.github.xyzxqs.zxingscanner.cameraview.CameraView;
import io.github.xyzxqs.zxingscanner.decode.RotatableYUVLuminanceSource;
import io.github.xyzxqs.zxingscanner.decode.ZxingDecoder;
import io.github.xyzxqs.zxingscanner.demo.util.ThreadUtils;

import static io.github.xyzxqs.zxingscanner.demo.util.PermissionUtil.checkPermission4AccessCamera;

/**
 * @author xyzxqs
 */
public class CameraViewHelper implements LifecycleObserver {
    private static final String TAG = "CameraViewHelper";
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    private final CameraView cameraView;
    private final Lifecycle lifecycle;
    private ZxingDecoder zxingDecoder = null;

    private ResultPointCallback resultPointCallback;

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
                    .resultPointCallback(point -> {
                        if (resultPointCallback != null) {
                            resultPointCallback.foundPossibleResultPoint(point);
                        }
                    })
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

    private Rect framingRect;
    private Rect framingRectInPreview;

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        if (framingRect == null) {
            Point screenResolution = new Point(cameraView.getWidth(), cameraView.getHeight());
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            //现在是一样的。。
            framingRectInPreview = getFramingRect();
        }
        return framingRectInPreview;
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

    public void setResultPointCallback(ResultPointCallback resultPointCallback) {
        this.resultPointCallback = resultPointCallback;
    }

    public interface OnDecodeResult {
        void handleDecodeResult(Result rawResult);
    }
}
