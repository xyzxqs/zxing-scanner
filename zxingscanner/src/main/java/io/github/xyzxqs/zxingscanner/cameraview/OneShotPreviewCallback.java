package io.github.xyzxqs.zxingscanner.cameraview;

/**
 * @author xyzxqs
 */
public interface OneShotPreviewCallback {
    void onShot(byte[] data, int width, int height);
}
