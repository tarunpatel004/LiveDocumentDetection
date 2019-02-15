package com.example.techflitter.docscanner.UI;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.techflitter.docscanner.utils.MyHeaderAndFooter;
import com.example.techflitter.docscanner.utils.MyPrefs;
import com.example.techflitter.docscanner.R;
import com.example.techflitter.docscanner.uc.CustomProgressDialog;
import com.example.techflitter.docscanner.utils.AppPermissions;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private ImageView resultView;
    private Button btnCreatePDF;
    Bitmap bmpBitmap;
    CustomProgressDialog customProgressDialog;
    File pictureFile = null;
    File pdfPath = null;
    MyPrefs myPrefs;
    protected static final int MEDIA_TYPE_IMAGE = 1427;
    private static final String[] ALL_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int ALL_REQUEST_CODE = 0;
    private AppPermissions mRuntimePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        bmpBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        mRuntimePermission = new AppPermissions(this);

        Initilization();

    }

    private void Initilization() {
        resultView = (ImageView) findViewById(R.id.result);
        btnCreatePDF = (Button) findViewById(R.id.btnCreatePDF);
        myPrefs = new MyPrefs(getApplicationContext());
        resultView.post(new Runnable() {
            @Override
            public void run() {
                resultView.setImageBitmap(bmpBitmap);
            }
        });
        customProgressDialog = new CustomProgressDialog(Main2Activity.this, R.drawable.progress_img);
        btnCreatePDF.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnCreatePDF:
                if (mRuntimePermission.hasPermission(ALL_PERMISSIONS)) {
                    new SavePhotoTask().execute();
                } else {
                    mRuntimePermission.requestPermission(ALL_PERMISSIONS, ALL_REQUEST_CODE);
                }

                break;
        }
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
                    new SavePhotoTask().execute();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private File getOutputMediaFile(int type) throws IOException {

        File mediaFile;
        File dir = new File(Environment.getExternalStorageDirectory(),
                "/DocScanner");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File imgFolder = new File(dir.getAbsolutePath(), "/.Images");
        if (!imgFolder.exists()) {
            imgFolder.mkdirs();
        }
        if (type == 1427) {
            File imgFolder1 = new File(imgFolder.getAbsolutePath(), "/.Main");
            if (!imgFolder1.exists()) {
                imgFolder1.mkdirs();
            }
            mediaFile = new File(imgFolder1.getAbsolutePath() + File.separator
                    + "DocScanner_Img" + myPrefs.getDocCount() + ".jpg");
            myPrefs.setDocCount(myPrefs.getDocCount() + 1);
        } else {
            return null;
        }

        return mediaFile;
    }

    private void saveImageOrientation() {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("orientation   " + String.valueOf(getScreenOrientation()));
        if (exif != null) {
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(getScreenOrientation()));
            try {
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int orientation = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = 0;
                break;
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + orientation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - orientation + 360) % 360;
        }

        switch (result) {
            case 0:
                orientation = ExifInterface.ORIENTATION_NORMAL;
                break;
            case 90:
                orientation = ExifInterface.ORIENTATION_ROTATE_90;
                break;
            case 180:
                orientation = ExifInterface.ORIENTATION_ROTATE_180;
                break;
            case 270:
                orientation = ExifInterface.ORIENTATION_ROTATE_270;
                break;
            default:
                Log.e("PHOTO", "Unknown screen orientation. Defaulting to " +
                        "portrait.");
                orientation = ExifInterface.ORIENTATION_UNDEFINED;
                break;
        }

        return orientation;
    }

    class SavePhotoTask extends AsyncTask<byte[], String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            customProgressDialog.show();
        }

        @Override
        protected String doInBackground(byte[]... data) {


            try {
                pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                System.out.println("picture path " + pictureFile.getAbsolutePath());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (pictureFile == null) {

                return null;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmpBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            byte[] photoData = stream.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(photoData);
                fos.close();
                saveImageOrientation();
                createPdf();
            } catch (FileNotFoundException e) {
                Log.d("DEV", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("DEV", "Error accessing file: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            customProgressDialog.dismiss();
            Toast.makeText(Main2Activity.this, "Saved PDF :" + pdfPath.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void createPdf() {
        Document document = new Document(PageSize.A4, 38, 38, 38, 38);
        FileOutputStream fOut = null;
        try {
            File dir = new File(Environment.getExternalStorageDirectory(),
                    "/DocScanner");
            if (!dir.exists()) {
                dir.mkdirs();
                Toast.makeText(Main2Activity.this, "dir created", Toast.LENGTH_SHORT).show();
            }

            long time = System.currentTimeMillis();
            String fileName = "sample_" + time + ".pdf";
            File file = new File(dir, fileName);
            fOut = new FileOutputStream(file);

            PdfWriter pdfWriter = PdfWriter.getInstance(document, fOut);
            pdfWriter.setPageEvent(new MyHeaderAndFooter());
            //open the document
            document.open();

            pdfPath = file;
            Log.d("PDFCreator", "PDF Path: " + pdfPath.getAbsolutePath());

            Paragraph p1 = new Paragraph("DocScanner PDF");
            Font paraFont = new Font(Font.FontFamily.COURIER);
            p1.setAlignment(Paragraph.ALIGN_CENTER);
            p1.setFont(paraFont);

            //add paragraph to document
            document.add(p1);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmpBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            Image myImg = Image.getInstance(stream.toByteArray());
            myImg.scaleToFit(595, 815);
            myImg.setAbsolutePosition((PageSize.A4.getWidth() - myImg.getScaledWidth()) / 2, 35);
            //myImg.setAlignment(Image.ALIGN_MIDDLE);
            //add image to document
            // document.add(myImg);
            pdfWriter.getDirectContent().addImage(myImg);

            document.newPage();

            try {
                fOut.flush();
            } catch (IOException ioe) {
            }

        } catch (DocumentException de) {
            Log.e("PDFCreator", "DocumentException:" + de);
        } catch (IOException e) {
            Log.e("PDFCreator", "ioException:" + e);
        } finally {
            document.close();
        }

        try {
            fOut.close();
        } catch (IOException ioe) {
        }

    }
}
