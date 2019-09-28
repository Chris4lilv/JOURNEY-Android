package com.example.android.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class Gallery extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DemoAdapter adapter;
    GestureDetector gestureDetector;
    FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    ArrayList<String> urlsList;
    private TextView galleryTitle;
    String key;
    String title;

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


        //Set the event caption to be the gallery title
        galleryTitle = findViewById(R.id.gallery_title);
        galleryTitle.setText(title);


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        recyclerView.setHasFixedSize(true);


        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter = new DemoAdapter());

        adapter.replaceAll(urlsList);

        /**
         * Code Snippet to detect gesture on single item in recyclerview
         */
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null) {
                    int position = recyclerView.getChildLayoutPosition(childView);
                    Intent intent = new Intent(new Intent(childView.getContext(), SingleImageActivity.class));
                    intent.putExtra("IMAGE_URL",urlsList.get(position));
                    childView.getContext().startActivity(intent,
                            //Make the transition between activities animated
                            ActivityOptions.makeSceneTransitionAnimation((Activity) childView.getContext(), childView, "sharedView").toBundle());

                    return true;
                }
                return super.onSingleTapUp(e);
            }
//            //TODO: Introduce the feature of deleting single image in an event
//            @Override
//            public void onLongPress(MotionEvent e) {
//                super.onLongPress(e);
//                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
//                if (childView != null) {
//                    int position = recyclerView.getChildLayoutPosition(childView);
//                    StorageReference mPhotoRef = mFirebaseStorage.getReferenceFromUrl(urlsList.get(position));
//                    mPhotoRef.delete();
//                    mDatabaseRef.child(mDirectory).child(key).child("url").child(Integer.toString(position)).removeValue();
//                    //update the UI
//                    urlsList.remove(position);
//                    adapter.replaceAll(urlsList);
//                    Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
//                    Intent updateURL = new Intent();
//                        updateURL.putStringArrayListExtra("newURL", urlsList);
//                        setResult(Activity.RESULT_OK,updateURL);
//                }
//            }
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
