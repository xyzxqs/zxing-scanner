package io.github.xyzxqs.zxingscanner.decode;

import android.util.Log;

import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;


/**
 * Wrap PlanarYUVLuminanceSource and support rotate.
 * <p>
 * PlanarYUVLuminanceSource 不支持旋转还是final的！为了支持条码的 TRY_HARDER 模式，封装此类。
 *
 * @author xyzxqs
 */
public class RotatableYUVLuminanceSource extends LuminanceSource {
    private static final String TAG = "YUVLuminanceSource";
    private PlanarYUVLuminanceSource realSource;

    private final byte[] yuvData;
    private final int dataWidth;
    private final int dataHeight;
    private final int left;
    private final int top;

    public RotatableYUVLuminanceSource(byte[] yuvData,
                                       int dataWidth,
                                       int dataHeight,
                                       int left,
                                       int top,
                                       int width,
                                       int height,
                                       boolean reverseHorizontal) {
        super(width, height);
        this.yuvData = yuvData;
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.left = left;
        this.top = top;
        realSource = new PlanarYUVLuminanceSource(yuvData, dataWidth, dataHeight, left, top, width, height, reverseHorizontal);
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

    public int[] renderThumbnail() {
        return realSource.renderThumbnail();
    }

    @Override
    public boolean isRotateSupported() {
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public LuminanceSource rotateCounterClockwise() {
        //向右旋转90度

        byte[] rightRotate90Data = new byte[yuvData.length];
        for (int y = 0; y < dataHeight; y++) {
            for (int x = 0; x < dataWidth; x++) {
                rightRotate90Data[x * dataHeight + dataHeight - y - 1] = yuvData[x + y * dataWidth];
            }
        }

        return new RotatableYUVLuminanceSource(rightRotate90Data,
                dataHeight/*dataWidth*/,
                dataWidth/*dataHeight*/,
                dataHeight - (top + getHeight())/*left*/,
                left/*top*/,
                getHeight()/*width*/,
                getWidth()/*height*/,
                false);
    }

    @Override
    public LuminanceSource rotateCounterClockwise45() {
        //现在最新的zxing v3.3.3 没有使用这个方法。
        //防止以后的使用了而不崩，返回this？
        Log.w(TAG,"rotateCounterClockwise45 was called but not impl!");
        return this;
    }

    /**
     * @return width of image from {@link #renderThumbnail()}
     */
    public int getThumbnailWidth() {
        return realSource.getThumbnailWidth();
    }

    /**
     * @return height of image from {@link #renderThumbnail()}
     */
    public int getThumbnailHeight() {
        return realSource.getThumbnailHeight();
    }
}
