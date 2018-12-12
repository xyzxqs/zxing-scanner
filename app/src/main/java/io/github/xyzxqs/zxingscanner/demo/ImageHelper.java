package io.github.xyzxqs.zxingscanner.demo;

import com.google.zxing.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
            try {
                result = zxingDecoder.decodeImageStream(() ->
                        new FileInputStream(new File(imgFile)));
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


}
