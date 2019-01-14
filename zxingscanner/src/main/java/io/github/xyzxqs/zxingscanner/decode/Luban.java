package io.github.xyzxqs.zxingscanner.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Responsible for starting compress and managing active and cached resources.
 * <p>
 * xyzxqs note: refactor from https://github.com/Curzibn/Luban
 */
enum Luban {
    SINGLE;

    void compress(byte[] buf, int offset, int length, OutputStream stream, boolean focusAlpha) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        try (ByteArrayInputStream is = new ByteArrayInputStream(buf, offset, length)) {
            BitmapFactory.decodeStream(is, null, options);
        }

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        options.inJustDecodeBounds = false;
        options.inSampleSize = computeSize(srcWidth, srcHeight);

        Bitmap tagBitmap;
        try (ByteArrayInputStream is = new ByteArrayInputStream(buf, offset, length)) {
            tagBitmap = BitmapFactory.decodeStream(is, null, options);
        }

        if (tagBitmap == null) {
            Log.e("luban", "not a image file?");
            return;
        }
        if (Checker.SINGLE.isJPG(buf, offset, length)) {
            tagBitmap = rotatingImage(tagBitmap, Checker.SINGLE.getOrientation(buf, offset, length));
        }

        tagBitmap.compress(focusAlpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 60, stream);
        tagBitmap.recycle();
    }

    private int computeSize(int srcWidth, int srcHeight) {
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            }
            else if (longSide < 4990) {
                return 2;
            }
            else if (longSide > 4990 && longSide < 10240) {
                return 4;
            }
            else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        }
        else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        }
        else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    private Bitmap rotatingImage(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private enum Checker {
        SINGLE;

        private static final String TAG = "Luban";

        private static final byte[] JPEG_SIGNATURE = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

        /**
         * Determine if it is JPG.
         * <p>
         * data is image file input stream byte
         */
        boolean isJPG(byte[] data, int offset, int len) {
            if (data == null || Math.min(data.length, len) < 3) {
                return false;
            }
            byte[] signatureB = new byte[]{data[offset], data[1 + offset], data[2 + offset]};
            return Arrays.equals(JPEG_SIGNATURE, signatureB);
        }

        /**
         * Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
         */
        int getOrientation(byte[] jpeg, int offset, int len) {
            if (jpeg == null) {
                return 0;
            }
            if (offset > jpeg.length) {
                throw new ArrayIndexOutOfBoundsException();
            }

            int length = 0;

            // ISO/IEC 10918-1:1993(E)
            while (offset + 3 < Math.min(offset + len, jpeg.length) && (jpeg[offset++] & 0xFF) == 0xFF) {
                int marker = jpeg[offset] & 0xFF;

                // Check if the marker is a padding.
                if (marker == 0xFF) {
                    continue;
                }
                offset++;

                // Check if the marker is SOI or TEM.
                if (marker == 0xD8 || marker == 0x01) {
                    continue;
                }
                // Check if the marker is EOI or SOS.
                if (marker == 0xD9 || marker == 0xDA) {
                    break;
                }

                // Get the length and check if it is reasonable.
                length = pack(jpeg, offset, 2, false);
                if (length < 2 || offset + length > jpeg.length) {
                    Log.e(TAG, "Invalid length");
                    return 0;
                }

                // Break if the marker is EXIF in APP1.
                if (marker == 0xE1 && length >= 8
                        && pack(jpeg, offset + 2, 4, false) == 0x45786966
                        && pack(jpeg, offset + 6, 2, false) == 0) {
                    offset += 8;
                    length -= 8;
                    break;
                }

                // Skip other markers.
                offset += length;
                length = 0;
            }

            // JEITA CP-3451 Exif Version 2.2
            if (length > 8) {
                // Identify the byte order.
                int tag = pack(jpeg, offset, 4, false);
                if (tag != 0x49492A00 && tag != 0x4D4D002A) {
                    Log.e(TAG, "Invalid byte order");
                    return 0;
                }
                boolean littleEndian = (tag == 0x49492A00);

                // Get the offset and check if it is reasonable.
                int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
                if (count < 10 || count > length) {
                    Log.e(TAG, "Invalid offset");
                    return 0;
                }
                offset += count;
                length -= count;

                // Get the count and go through all the elements.
                count = pack(jpeg, offset - 2, 2, littleEndian);
                while (count-- > 0 && length >= 12) {
                    // Get the tag and check if it is orientation.
                    tag = pack(jpeg, offset, 2, littleEndian);
                    if (tag == 0x0112) {
                        int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                        switch (orientation) {
                            case 1:
                                return 0;
                            case 3:
                                return 180;
                            case 6:
                                return 90;
                            case 8:
                                return 270;
                        }
                        Log.e(TAG, "Unsupported orientation");
                        return 0;
                    }
                    offset += 12;
                    length -= 12;
                }
            }

            Log.e(TAG, "Orientation not found");
            return 0;
        }

        private int pack(byte[] bytes, int offset, int length, boolean littleEndian) {
            int step = 1;
            if (littleEndian) {
                offset += length - 1;
                step = -1;
            }

            int value = 0;
            while (length-- > 0) {
                value = (value << 8) | (bytes[offset] & 0xFF);
                offset += step;
            }
            return value;
        }

    }
}
