package io.github.xyzxqs.zxingscanner.demo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.capture).setOnClickListener(v -> onCaptureClick());
        findViewById(R.id.encode).setOnClickListener(v -> onEncodeClick());
    }

    void onCaptureClick() {
        Intent intent = CaptureActivity.newIntent(MainActivity.this);
        startActivity(intent);
    }

    void onEncodeClick() {
        Intent intent = EncodeActivity.newIntent(MainActivity.this);
        startActivity(intent);
    }
}
