package io.github.xyzxqs.zxingscanner.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import io.github.xyzxqs.zxingscanner.BuildConfig;

/**
 * @author xyzxqs
 */
public class ZxingDecoder {
    private final MultiFormatReader multiFormatReader;

    private ZxingDecoder(Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    @Nullable
    public Result decode(LuminanceSource source) {
        Result rawResult = null;
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            }
            catch (ReaderException | RuntimeException e) {
                // continue
                if (BuildConfig.DEBUG) {
                    if (!(e instanceof ReaderException)) {
                        Log.e("decoder", "decode error", e);
                    }
                }
            } finally {
                multiFormatReader.reset();
            }
        }
        return rawResult;
    }

    /**
     * decode from png or jpeg bytes src
     */
    public Result decodeImage(byte[] imgSrc, int offset, int length, boolean compress) throws IOException {
        Bitmap bitmap;
        if (compress) {
            bitmap = BitmapUtils.decodeByteArrayWithCompress(imgSrc, offset, length);
        }
        else {
            bitmap = BitmapFactory.decodeByteArray(imgSrc, offset, length);
        }
        return decodeBitmap(bitmap);
    }

    public Result decodeImage(byte[] imgSrc, boolean compress) throws IOException {
        return decodeImage(imgSrc, 0, imgSrc.length, compress);
    }

    @Nullable
    public Result decodeBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] data = new int[width * height];
        bitmap.getPixels(data, 0, width, 0, 0, width, height);

        RotatableRGBLuminanceSource luminanceSource = new RotatableRGBLuminanceSource(width, height, data);
        return decode(luminanceSource);
    }


    public static class Builder {
        private final Map<DecodeHintType, Object> hints;
        private Collection<BarcodeFormat> decodeFormats = null;

        static final Set<BarcodeFormat> PRODUCT_FORMATS;
        static final Set<BarcodeFormat> INDUSTRIAL_FORMATS;
        static final Set<BarcodeFormat> QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE);
        static final Set<BarcodeFormat> DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX);
        static final Set<BarcodeFormat> AZTEC_FORMATS = EnumSet.of(BarcodeFormat.AZTEC);
        static final Set<BarcodeFormat> PDF417_FORMATS = EnumSet.of(BarcodeFormat.PDF_417);

        private static final Set<BarcodeFormat> ONE_D_FORMATS;

        static {
            PRODUCT_FORMATS = EnumSet.of(BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.RSS_14,
                    BarcodeFormat.RSS_EXPANDED);
            INDUSTRIAL_FORMATS = EnumSet.of(BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.ITF,
                    BarcodeFormat.CODABAR);
            ONE_D_FORMATS = EnumSet.copyOf(PRODUCT_FORMATS);
            ONE_D_FORMATS.addAll(INDUSTRIAL_FORMATS);
        }

        public Builder() {
            hints = new EnumMap<>(DecodeHintType.class);
        }

        public ZxingDecoder build() {
            if (decodeFormats == null || decodeFormats.isEmpty()) {
                decodeFormats = EnumSet.noneOf(BarcodeFormat.class);

                decodeFormats.addAll(QR_CODE_FORMATS);
                decodeFormats.addAll(PRODUCT_FORMATS);
                decodeFormats.addAll(INDUSTRIAL_FORMATS);
                decodeFormats.addAll(DATA_MATRIX_FORMATS);

                // default exclude
                //decodeFormats.addAll(AZTEC_FORMATS);
                //decodeFormats.addAll(PDF417_FORMATS);
            }
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
            boolean containsOneD = false;
            for (BarcodeFormat fmt : decodeFormats) {
                if (ONE_D_FORMATS.contains(fmt)) {
                    containsOneD = true;
                    break;
                }
            }
            if (containsOneD) {//如果包含条码，添加try harder选项
                hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            }
            return new ZxingDecoder(hints);
        }

        public Builder possibleFormats(Collection<BarcodeFormat> decodeFormats) {
            this.decodeFormats = decodeFormats;
            return this;
        }

        public Builder baseHints(Map<DecodeHintType, ?> baseHints) {
            if (baseHints != null) {
                hints.putAll(baseHints);
            }
            return this;
        }

        public Builder characterSet(String characterSet) {
            if (characterSet != null) {
                hints.put(DecodeHintType.CHARACTER_SET, characterSet);
            }
            return this;
        }

        public Builder resultPointCallback(ResultPointCallback resultPointCallback) {
            hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
            return this;
        }
    }
}
