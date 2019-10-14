package com.example.android.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class Gallery extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DemoAdapter adapter;
    GestureDetector gestureDetector;
    FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    ArrayList<String> urlsList;
    private TextView galleryTitle;
    private String key;
    private String title;
    private int color;
    private int comColor;

    private AppBarLayout mAppBarLayout;

    private String mCurrentUser;
    private String mDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mDirectory = mCurrentUser.substring(0, FirebaseAuth.getInstance().getCurrentUser().getEmail().indexOf("@"));

        /**
         * Get URLs of events from DisplayFragment
         */
        Intent intent = getIntent();
        urlsList = intent.getExtras().getStringArrayList("URLs");
        key = intent.getExtras().getString("Key");
        title = intent.getExtras().getString("Title");
        color = intent.getExtras().getInt("Color");
        comColor = intent.getExtras().getInt("ComColor");

        //Set the event caption to be the gallery title
        galleryTitle = findViewById(R.id.gallery_title);
        galleryTitle.setText(title);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();


        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mAppBarLayout.setBackgroundColor(color);
        galleryTitle.setTextColor(comColor);

        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter = new DemoAdapter());

        adapter.replaceAll(urlsList);

        /**
         * Detect gesture on single item in recyclerview
         */
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null) {
                    int position = recyclerView.getChildLayoutPosition(childView);
                    Intent intent = new Intent(new Intent(childView.getContext(), SingleImageActivity.class));
                    intent.putExtra("IMAGE_URL", urlsList.get(position));
                    childView.getContext().startActivity(intent,
                            //Make the transition between activities animated
                            ActivityOptions.makeSceneTransitionAnimation((Activity) childView.getContext(), childView, "sharedView").toBundle());

                    return true;
                }
                return super.onSingleTapUp(e);
            }
        });

        /**
         * OnItem Click feature
         */
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }
}
