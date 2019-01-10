package io.github.xyzxqs.zxingscanner.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author xyzxqs
 */
public class BitmapUtils {

    private BitmapUtils() {
        //no instance
    }

    public static Bitmap decodeByteArrayWithCompress(byte[] src, int offset, int length) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            compressImage(src, offset, length, stream, false);
            byte[] bytes = stream.toByteArray();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    public static void compressImage(byte[] src, int offset, int length, OutputStream os, boolean focusAlpha) throws IOException {
        Luban.SINGLE.compress(src, offset, length, os, focusAlpha);
    }


    public static void buildThumbnail(PlanarYUVLuminanceSource source, OutputStream out) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    }

    public static void buildThumbnail(RotatablePlanarYUVLuminanceSource source, OutputStream out) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    }
}
