package io.github.xyzxqs.zxingscanner.demo;

import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.github.xyzxqs.zxingscanner.decode.ZxingDecoder;
import io.github.xyzxqs.zxingscanner.demo.util.ThreadUtils;

public class ImageHelper {


    private ZxingDecoder zxingDecoder = null;

    public void decodeImage(String imgFile, ImageDecodeCallback callback) {
        if (zxingDecoder == null) {
            zxingDecoder = new ZxingDecoder.Builder()
                    .build();
        }
        ThreadUtils.runOnBackgroundThread(() -> {
            Result result = null;
            try (FileInputStream fis = new FileInputStream(new File(imgFile))) {
                result = zxingDecoder.decodeImage(toByteArray(fis), true);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Result finalResult = result;
            ThreadUtils.runOnMainThread(() -> {
                if (callback != null) {
                    if (finalResult == null) {
                        callback.onNotFound();
                    }
                    else {
                        callback.onFound(finalResult);
                    }
                }
            });
        });
    }

    public interface ImageDecodeCallback {
        void onFound(Result rawResult);

        void onNotFound();
    }

    private byte[] toByteArray(InputStream is) {
        if (is == null) {
            return new byte[0];
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int read;
        byte[] data = new byte[4096];

        try {
            while ((read = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, read);
            }
        }
        catch (Exception ignored) {
            return new byte[0];
        } finally {
            try {
                buffer.close();
            }
            catch (IOException ignored) {
            }
        }

        return buffer.toByteArray();
    }
}
