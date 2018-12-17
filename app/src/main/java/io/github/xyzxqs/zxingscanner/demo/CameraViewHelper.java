package io.github.xyzxqs.zxingscanner.demo;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;

import io.github.xyzxqs.cameraview.CameraView;
import io.github.xyzxqs.cameraview.Size;
import io.github.xyzxqs.zxingscanner.decode.RotatablePlanarYUVLuminanceSource;
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

    private boolean fullscreenScan = false;

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

                RotatablePlanarYUVLuminanceSource source = null;
                Rect rect;
                if (fullscreenScan) {
                    rect = getCameraViewInPreview();
                }
                else {
                    rect = getFramingRectInPreview();
                }

                if (rect != null) {
                    source = new RotatablePlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                            rect.width(), rect.height(), false);
                }
                final Result result = zxingDecoder.decode(source);
                final RotatablePlanarYUVLuminanceSource finalSource = source;

                ThreadUtils.runOnMainThread(() -> {
                    if (result == null) {
                        startDecode();
                    }
                    else {
                        handleDecodeResult(finalSource, result);
                    }
                });
            });
        });
    }

    private volatile Rect framingRect;
    private volatile Rect framingRectInPreview;
    private volatile Rect cameraViewInPreview;

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        if (framingRect == null) {
            int cameraViewWidth = cameraView.getWidth();
            int cameraViewHeight = cameraView.getHeight();

            int width = findDesiredDimensionInRange(cameraViewWidth, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(cameraViewHeight, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (cameraViewWidth - width) / 2;
            int topOffset = (cameraViewHeight - height) / 2;
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

    @SuppressWarnings("SuspiciousNameCombination")
    @Nullable
    public Rect getCameraViewInPreview() {
        if (cameraViewInPreview == null) {
            Size previewSize = cameraView.getPreviewSize();
            if (previewSize == null) {
                return null;
            }

            int cameraViewWidth = cameraView.getWidth();
            int cameraViewHeight = cameraView.getHeight();

            int realPreviewWidth = previewSize.getWidth();
            int realPreviewHeight = previewSize.getHeight();
            {   //处理

                boolean isRotated = false;
                if (cameraView.getCameraRotation() % 180 == 90) {
                    int tmp = realPreviewHeight;
                    realPreviewHeight = realPreviewWidth;
                    realPreviewWidth = tmp;
                    isRotated = true;
                }

                //调整preview的宽高比 与cameraView的一致

                //宽高比越大的越扁
                if (((cameraViewWidth * 1.0f) / cameraViewHeight) > ((realPreviewWidth * 1.0f) / realPreviewHeight)) {
                    realPreviewHeight = realPreviewWidth * cameraViewHeight / cameraViewWidth;
                }
                else {
                    realPreviewWidth = realPreviewHeight * cameraViewWidth / cameraViewHeight;
                }

                //调整后，如果preview比cameraView大，应该使用cameraView的宽高？(因为不会被压缩？
                if (realPreviewHeight > cameraViewHeight) {
                    Log.d(TAG, "getFramingRectInPreview: set same as cameraView");
                    realPreviewWidth = cameraViewWidth;
                    realPreviewHeight = cameraViewHeight;
                }

                if (isRotated) {
                    int tmp = realPreviewHeight;
                    realPreviewHeight = realPreviewWidth;
                    realPreviewWidth = tmp;
                }
            }


            int realLeft = (previewSize.getWidth() - realPreviewWidth) / 2;
            int realTop = (previewSize.getHeight() - realPreviewHeight) / 2;
            cameraViewInPreview = new Rect(realLeft, realTop, realLeft + realPreviewWidth, realTop + realPreviewHeight);
        }

        return cameraViewInPreview;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Nullable
    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect cameraViewInPreview = getCameraViewInPreview();
            if (cameraViewInPreview == null) {
                return null;
            }

            Rect rect = new Rect(getFramingRect());

            int cameraViewWidth = cameraView.getWidth();
            int cameraViewHeight = cameraView.getHeight();

            int realPreviewWidth = cameraViewInPreview.width();
            int realPreviewHeight = cameraViewInPreview.height();

            boolean isRotated = false;
            if (cameraView.getCameraRotation() % 180 == 90) {
                int tmp = realPreviewHeight;
                realPreviewHeight = realPreviewWidth;
                realPreviewWidth = tmp;
                isRotated = true;
            }

            rect.left = rect.left * realPreviewWidth / cameraViewWidth;
            rect.top = rect.top * realPreviewHeight / cameraViewHeight;

            rect.right = rect.right * realPreviewWidth / cameraViewWidth;
            rect.bottom = rect.bottom * realPreviewHeight / cameraViewHeight;

            int framingWidthInPreview = rect.width();
            int framingHeightInPreview = rect.height();

            if (isRotated) {
                int tmp = framingHeightInPreview;
                framingHeightInPreview = framingWidthInPreview;
                framingWidthInPreview = tmp;
            }


            int realLeft = cameraViewInPreview.left + (cameraViewInPreview.width() - framingWidthInPreview) / 2;
            int realTop = cameraViewInPreview.top + (cameraViewInPreview.height() - framingHeightInPreview) / 2;
            framingRectInPreview = new Rect(realLeft, realTop,
                    realLeft + framingWidthInPreview, realTop + framingHeightInPreview);
        }
        return framingRectInPreview;
    }

    private OnDecodeResult onDecodeResult = null;

    private void handleDecodeResult(RotatablePlanarYUVLuminanceSource source, Result rawResult) {
        if (onDecodeResult != null) {
            onDecodeResult.handleDecodeResult(source, rawResult);
        }
    }

    public void setDecodeResultHandler(OnDecodeResult onDecodeResult) {
        this.onDecodeResult = onDecodeResult;
    }

    public void setResultPointCallback(ResultPointCallback resultPointCallback) {
        this.resultPointCallback = resultPointCallback;
    }

    public interface OnDecodeResult {
        void handleDecodeResult(RotatablePlanarYUVLuminanceSource source, Result rawResult);
    }
}
