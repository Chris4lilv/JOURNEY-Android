package com.example.android.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SingleImageActivity extends AppCompatActivity {
    ImageView imageView;

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
        Intent intent = getIntent();
        String url = intent.getStringExtra("IMAGE_URL");
        Glide.with(this).load(url).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Make activities transition animated and kill the activity
                ActivityCompat.finishAfterTransition(SingleImageActivity.this);
            }
        });
    }
}
