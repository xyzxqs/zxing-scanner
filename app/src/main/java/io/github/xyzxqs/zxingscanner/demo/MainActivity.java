package io.github.xyzxqs.zxingscanner.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(android.R.id.content)
                .setOnClickListener(v -> {
                    Intent intent = CaptureActivity.newIntent(MainActivity.this);
                    startActivity(intent);
                });
    }
}
