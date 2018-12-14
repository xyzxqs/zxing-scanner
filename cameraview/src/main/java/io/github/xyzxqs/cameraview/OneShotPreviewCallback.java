package io.github.xyzxqs.cameraview;

/**
 * @author xyzxqs
 */
public interface OneShotPreviewCallback {
    void onShot(byte[] data, int width, int height);
}
