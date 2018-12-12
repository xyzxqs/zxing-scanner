package io.github.xyzxqs.zxingscanner.decode;

import android.util.Log;

import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;

/**
 * Wrap RGBLuminanceSource and support rotate.
 * <p>
 * RGBLuminanceSource 不支持旋转还是final的！为了支持条码的 TRY_HARDER 模式，封装此类。
 *
 * @author xyzxqs
 */
public class RotatableRGBLuminanceSource extends LuminanceSource {
    private static final String TAG = "RotatableRGBLuminanceSo";
    private final RGBLuminanceSource realSource;

    private final int[] pixels;

    public RotatableRGBLuminanceSource(int width, int height, int[] pixels) {
        super(width, height);
        this.pixels = pixels;
        realSource = new RGBLuminanceSource(width, height, pixels);
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        return realSource.getRow(y, row);
    }

    @Override
    public byte[] getMatrix() {
        return realSource.getMatrix();
    }

    @Override
    public boolean isCropSupported() {
        return realSource.isCropSupported();
    }

    @Override
    public LuminanceSource crop(int left, int top, int width, int height) {
        return realSource.crop(left, top, width, height);
    }

    @Override
    public boolean isRotateSupported() {
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public LuminanceSource rotateCounterClockwise() {
        //向右旋转90度

        int dataHeight = getHeight();
        int dataWidth = getWidth();
        int[] rightRotate90Data = new int[pixels.length];
        for (int y = 0; y < dataHeight; y++) {
            for (int x = 0; x < dataWidth; x++) {
                rightRotate90Data[x * dataHeight + dataHeight - y - 1] = pixels[x + y * dataWidth];
            }
        }

        return new RotatableRGBLuminanceSource(dataHeight, dataWidth, rightRotate90Data);
    }

    @Override
    public LuminanceSource rotateCounterClockwise45() {
        //现在最新的zxing v3.3.3 没有使用这个方法。
        //防止以后的使用了而不崩，返回this？
        Log.w(TAG, "rotateCounterClockwise45 was called but not impl!");
        return this;
    }
}
