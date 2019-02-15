package com.example.techflitter.docscanner.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.techflitter.docscanner.R;
import com.example.techflitter.docscanner.utils.AppPermissions;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.ui.PolygonView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PictureCallback, View.OnClickListener {

    ScanbotCameraView cameraView;
    ContourDetectorFrameHandler contourDetectorFrameHandler;
    ContourDetector detector;
    PolygonView polygonView;
    ImageView snapCapture, flashOnOff;
    boolean flashEnabled = true;

    private static final String[] ALL_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final int ALL_REQUEST_CODE = 0;
    private AppPermissions mRuntimePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRuntimePermission = new AppPermissions(this);

        if (mRuntimePermission.hasPermission(ALL_PERMISSIONS)) {
            Initilization();
        } else {
            mRuntimePermission.requestPermission(ALL_PERMISSIONS, ALL_REQUEST_CODE);
        }
    }

    private void Initilization() {
        getSupportActionBar().hide();
        cameraView = (ScanbotCameraView) findViewById(R.id.camera);
        polygonView = (PolygonView) findViewById(R.id.polygonView);
        snapCapture = (ImageView) findViewById(R.id.snapCapture);
        flashOnOff = (ImageView) findViewById(R.id.flashOnOff);

        snapCapture.setOnClickListener(this);
        flashOnOff.setOnClickListener(this);

        detector = new ContourDetector();

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

        contourDetectorFrameHandler.addResultHandler(polygonView);

        AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);

        cameraView.addPictureCallback(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ALL_REQUEST_CODE:
                List<Integer> permissionResults = new ArrayList<>();
                for (int grantResult : grantResults) {
                    permissionResults.add(grantResult);
                }
                if (permissionResults.contains(PackageManager.PERMISSION_DENIED)) {
                    Toast.makeText(this, "All Permissions not granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "All Permissions granted", Toast.LENGTH_SHORT).show();
                    Initilization();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            cameraView.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //FlashOnOFf(!flashEnabled);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            cameraView.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        final Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        DetectionResult result1 = detector.detect(image);  // use Bitmap or byte[]
        List<PointF> polygon = detector.getPolygonF();

        final Bitmap result = detector.processImageF(bitmap, polygon, 0);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Intent in1 = new Intent(MainActivity.this, Main2Activity.class);
        in1.putExtra("image", byteArray);
        startActivity(in1);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.snapCapture:
                cameraView.takePicture(false);
                break;

            case R.id.flashOnOff:
                FlashOnOFf(!flashEnabled);
                cameraView.useFlash(!flashEnabled);
                flashEnabled = !flashEnabled;

                break;

        }
    }

    private void FlashOnOFf(boolean flashEnabled) {
        flashOnOff.setImageResource(flashEnabled ? R.drawable.flash_on : R.drawable.flash_off);
    }
}
