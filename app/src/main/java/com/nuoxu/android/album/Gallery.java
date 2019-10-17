package com.nuoxu.android.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;


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

    private SharedPreferences sharedPreferences;
    private boolean secondTime;

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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        secondTime = sharedPreferences.getBoolean("SecondTime",false);
        if(secondTime){
            new GuideView.Builder(this)
                    .setTitle("Click the image to see it in original size")
                    .setGravity(Gravity.auto) //optional
                    .setDismissType(DismissType.anywhere) //optional - default DismissType.targetView
                    .setContentTextSize(12)//optional
                    .setTitleTextSize(14)//optional
                    .setTargetView(galleryTitle)
                    .build()
                    .show();
        }

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
