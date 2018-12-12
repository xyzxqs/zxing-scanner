package io.github.xyzxqs.zxingscanner.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author xyzxqs
 */
public class BitmapUtils {

    private BitmapUtils() {
        //no instance
    }

    public static Bitmap decodeStream(InputStreamProvider provider) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            compressImage(provider, stream, false);
            byte[] bytes = stream.toByteArray();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    public static void compressImage(InputStreamProvider srcImg, OutputStream os, boolean focusAlpha) throws IOException {
        Luban.Instance.compress(srcImg, os, focusAlpha);
    }


    public static void buildThumbnail(PlanarYUVLuminanceSource source, OutputStream out) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    }

    public static void buildThumbnail(RotatableYUVLuminanceSource source, OutputStream out) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
    }
}
