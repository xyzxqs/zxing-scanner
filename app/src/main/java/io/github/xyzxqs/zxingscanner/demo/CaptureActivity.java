package io.github.xyzxqs.zxingscanner.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.xyzxqs.cameraview.CameraView;
import io.github.xyzxqs.cameraview.OneShotPreviewCallback;
import io.github.xyzxqs.zxingscanner.decode.BitmapUtils;
import io.github.xyzxqs.zxingscanner.decode.RotatablePlanarYUVLuminanceSource;
import io.github.xyzxqs.zxingscanner.demo.util.Caller;
import io.github.xyzxqs.zxingscanner.demo.util.PermissionUtil;
import io.github.xyzxqs.zxingscanner.demo.util.RealPathUtils;

public class CaptureActivity extends AppCompatActivity implements CameraViewHelper.OnDecodeResult {

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, CaptureActivity.class);
        return i;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.camera_view)
    CameraView cameraView;

    @BindView(R.id.viewfinder_view)
    ViewfinderView viewfinderView;

    @BindView(R.id.image)
    ImageView imageView;

    private CameraViewHelper cameraViewHelper;
    private ImageHelper imageHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        imageHelper = new ImageHelper();
        cameraViewHelper = new CameraViewHelper(getLifecycle(), cameraView);
        cameraViewHelper.setDecodeResultHandler(this);
        viewfinderView.setCameraViewHelper(cameraViewHelper);
        cameraViewHelper.setResultPointCallback(point ->
                viewfinderView.addPossibleResultPoint(point));

        cameraView.addCallback(new CameraView.Callback() {
            @Override
            public void onCameraOpened(CameraView cameraView) {
                super.onCameraOpened(cameraView);
                cameraViewHelper.startDecode();

            }
        });
    }


    @Override
    public void handleDecodeResult(@Nullable RotatablePlanarYUVLuminanceSource source, Result rawResult) {
        Toast.makeText(this, rawResult.getText(), Toast.LENGTH_LONG)
                .show();
//        finish();
        if (source != null) {

            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                BitmapUtils.buildThumbnail(source, stream);
                Bitmap bitmap = BitmapUtils.decodeStream(stream);
                viewfinderView.drawResultBitmap(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_capture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isFullScreen = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.select_from_gallery) {
            PermissionUtil.checkPermission4AccessFile(cameraView,
                    this::startGalleryIntent);
            return true;
        }
        else if (item.getItemId() == R.id.one_shot_preview) {
            cameraView.requestOneShotPreview(new OneShotPreviewCallback() {
                @Override
                public void onShot(byte[] data, int width, int height) {

                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
                    Rect pr;
                    if (isFullScreen) {
                        pr = new Rect(1, 1, width - 2, height - 2);
                    }
                    else {
                        pr = cameraViewHelper.getFramingRectInPreview();
                    }
                    try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                        yuvImage.compressToJpeg(pr, 50, stream);
                        Bitmap bitmap = BitmapUtils.decodeStream(stream);

                        imageView.setVisibility(View.VISIBLE);
                        imageView.setRotation(cameraView.getCameraRotation());
                        imageView.setImageBitmap(bitmap);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            });
        }
        else if (item.getItemId() == R.id.clear_preview) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
            viewfinderView.drawResultBitmap(null);
            cameraViewHelper.startDecode();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startGalleryIntent() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "没有图册app", Toast.LENGTH_LONG).show();
            return;
        }

        Caller.startActivityForResult(this, galleryIntent, 233,
                (requestCode, resultCode, data) ->
                        onGalleryResultIntent(data));
    }


    private void onGalleryResultIntent(@Nullable Intent data) {
        if (data == null) {
            Toast.makeText(this, "未获取到图片", Toast.LENGTH_LONG).show();
            return;
        }
        Uri temp = data.getData();
        if (temp != null) {
            String realPath = RealPathUtils.getRealPath(this, temp);
            imageHelper.decodeImage(realPath, new ImageHelper.ImageDecodeCallback() {
                @Override
                public void onFound(Result rawResult) {
                    handleDecodeResult(null, rawResult);
                }

                @Override
                public void onNotFound() {
                    Toast.makeText(CaptureActivity.this, "未发现二维码/条码", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }
}
