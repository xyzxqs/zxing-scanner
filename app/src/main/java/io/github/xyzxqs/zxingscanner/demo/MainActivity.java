package io.github.xyzxqs.zxingscanner.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.capture)
    void onCaptureClick() {
        Intent intent = CaptureActivity.newIntent(MainActivity.this);
        startActivity(intent);
    }

    @OnClick(R.id.encode)
    void onEncodeClick() {
        Intent intent = EncodeActivity.newIntent(MainActivity.this);
        startActivity(intent);
    }
}
