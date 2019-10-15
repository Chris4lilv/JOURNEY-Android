package com.example.android.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Arrays;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class SingleImageActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView guideIndicator;
    private static final int REQUEST_CODE = 1;

    private SharedPreferences sharedPreferences;
    private boolean secondTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_single_image);

        imageView = findViewById(R.id.imageView2);
        guideIndicator = findViewById(R.id.guide_indicator);
        Intent intent = getIntent();
        String url = intent.getStringExtra("IMAGE_URL");
        Glide.with(this).load(url).asBitmap().into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Make activities transition animated and kill the activity
                ActivityCompat.finishAfterTransition(SingleImageActivity.this);
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (ContextCompat.checkSelfPermission(SingleImageActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //Request permission
                    ActivityCompat.requestPermissions(SingleImageActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1);
                }else{
                    imageLongClick();
                }
                return true;
            }
        });
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        secondTime = sharedPreferences.getBoolean("SecondTime", false);
        if(secondTime){
            new GuideView.Builder(this)
                    .setTitle("Hold to save image, tap to return")
                    .setGravity(Gravity.auto) //optional
                    .setDismissType(DismissType.anywhere) //optional - default DismissType.targetView
                    .setContentTextSize(12)//optional
                    .setTitleTextSize(14)//optional
                    .setTargetView(guideIndicator)
                    .build()
                    .show();
            sharedPreferences.edit().remove("SecondTime").apply();
        }

    }

    private void imageLongClick(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SingleImageActivity.this);
        builder.setMessage("Save image?");
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = imageView.getDrawingCache();
                String path = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"image_file", "description");
                Uri uri = Uri.fromFile(new File(path));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                sendBroadcast(mediaScanIntent);
                Snackbar.make(imageView, "Saved", Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageLongClick();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
