package io.github.xyzxqs.zxingscanner.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.xyzxqs.zxingscanner.encode.ZxingEncoder;

public class EncodeActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, EncodeActivity.class);
        return i;
    }

    @BindView(R.id.qrcode)
    ImageView qrcodeView;
    @BindView(R.id.barcode)
    ImageView barcodeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);
        ButterKnife.bind(this);


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
