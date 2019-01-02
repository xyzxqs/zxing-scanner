package io.github.xyzxqs.zxingscanner.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.github.xyzxqs.zxingscanner.encode.ZxingEncoder;

public class EncodeActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, EncodeActivity.class);
        return i;
    }

    ImageView qrcodeView;
    ImageView barcodeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        qrcodeView = findViewById(R.id.qrcode);
        barcodeView = findViewById(R.id.barcode);

        try {
            Bitmap qrcode = ZxingEncoder.encodeAsBitmap("https://github.com/xyzxqs/zxing-scanner", 618);
            qrcodeView.setImageBitmap(qrcode);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }

        try {
            Bitmap barcode = ZxingEncoder.encodeAsBitmap("hello world", BarcodeFormat.CODE_128, 618, 200);
            barcodeView.setImageBitmap(barcode);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }

    }
}
