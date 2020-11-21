package com.applex.zephyr_task_1.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.applex.zephyr_task_1.R;
import com.applex.zephyr_task_1.Utilities.JSONHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private String[] storagePermission;
    private final int STORAGE_REQUEST_CODE = 200;
    private File file1, file2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splash_image = findViewById(R.id.splash_image);
        progressBar = findViewById(R.id.progressbar);
        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), R.drawable.assignment, options);

        int width = options.outWidth;
        if (width > displayWidth) {
            options.inSampleSize = Math.round((float) width / (float) displayWidth);
        }
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.assignment, options);
        splash_image.setImageBitmap(scaledBitmap);

        file1 = new File(Environment.getExternalStorageDirectory(), "Zephyr");
        file2 =  new File(Environment.getExternalStorageDirectory() + "/Zephyr","Forms.json");

        if(checkStoragePermission()) {
            if(file1.exists() && file2.exists()) {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }, 1000);
            } else {
                new Background_Task().execute();
            }
        } else {
            requestStoragePermission();
        }
    }

    //////////////////PERMISSION REQUESTS/////////////////
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(SplashActivity.this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result= ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccepted) {
                    if(file1.exists() && file2.exists()) {
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }, 1000);
                    }
                    else {
                        new Background_Task().execute();
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class Background_Task extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(!file1.exists()) {
                if(!file1.mkdirs()) {
                    return false;
                }
            }
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Forms", jsonArray);
                JSONHelper.writeJsonFile(file2, jsonObject.toString());
                return true;
            } catch (JSONException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressBar.setVisibility(View.GONE);
            if(aBoolean) {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }, 1000);
            }
            else {
                Toast.makeText(SplashActivity.this, "Something went wrong...",  Toast.LENGTH_SHORT).show();
            }
        }
    }
}