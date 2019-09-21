package com.example.android.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;

public class NewEventActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER =  2;

    //setup firebase
    FirebaseStorage mStorage;
    StorageReference mStorageReference;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private String mCurrentUser;
    private String mDirectory;

    ArrayList<String> imageUri;

    EditText caption;

    LottieAnimationView uploadImageButton;
    LottieAnimationView createEventButton;

    Spinner yearSpinner;
    Spinner monthSpinner;
    Spinner daySpinner;

    ArrayList<String> yearList;
    ArrayList<String> monthList;
    ArrayList<String> dayList;

    String year = "";
    String month = "";
    String day = "";
    boolean selectionOfCreateEventButton = false;

    ArrayAdapter<String> dayAdapter;
    ArrayAdapter<String> monthAdapter;
    ArrayAdapter<String> yearAdapter;

    ArrayList<String> THIRTY_ONE_DAYS = new ArrayList<>(Arrays.asList("01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"));

    //This parts take care of circular revelation
    View rootLayout;

    private int revealX;
    private int revealY;

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mDirectory = mCurrentUser.substring(0, mCurrentUser.indexOf("@")).replaceAll("[\\p{P}]","");



        //initialize storage, database and their reference
        mStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mStorageReference = mStorage.getReference().child(mDirectory);

        imageUri = new ArrayList<>();

        caption = findViewById(R.id.caption);

        uploadImageButton = findViewById(R.id.upload_image);
        createEventButton = findViewById(R.id.create_event);

        //initialize spinner
        yearSpinner = (Spinner) findViewById(R.id.year);
        monthSpinner = (Spinner)findViewById(R.id.month);
        daySpinner = (Spinner)findViewById(R.id.day);

        //initialize Arraylist
        yearList = new ArrayList<String>();
        monthList = new ArrayList<String>(Arrays.asList("01","02","03","04","05","06","07","08","09","10","11","12"));
        dayList = new ArrayList<String>(THIRTY_ONE_DAYS);


        //fill the yearList
        for(int i = 2019; i < 2050; i++){
            yearList.add(Integer.toString(i));
        }
        // Create an ArrayAdapter for year
        yearAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        //Create an ArrayAdapter for month
        monthAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        monthSpinner.setAdapter(monthAdapter);

        //Create an ArrayAdapter for month
        dayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        daySpinner.setAdapter(dayAdapter);

       yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               year = parent.getItemAtPosition(position).toString();
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentItem = parent.getItemAtPosition(position).toString();
                month = currentItem;
                if(currentItem.equals("02")){
                    dayList.subList(0,27);
                    dayAdapter.notifyDataSetChanged();
                }else if(currentItem.equals(monthList.get(4)) || currentItem.equals(monthList.get(5)) || currentItem.equals(monthList.get(8)) || currentItem.equals(monthList.get(10))){
                    dayList.subList(0,29);
                    dayAdapter.notifyDataSetChanged();
                }else{
                    dayList = THIRTY_ONE_DAYS;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                day = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });


        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEventButton.playAnimation();
                createEventButton.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //Send back data whether this button is clicked
                        selectionOfCreateEventButton = true;
                        Intent intent = new Intent();
                        intent.putExtra("selection_of_create_event_button", selectionOfCreateEventButton);
                        setResult(Activity.RESULT_OK, intent);

                        //get caption and date
                        String cap = caption.getText().toString();
                        String date = year + month + day;
                        Event event = new Event(imageUri,cap,date);
                        myRef.child(mDirectory).push().setValue(event);
                        //kill the activity and remove it from the stack
                        onBackPressed();
                    }
                });


            }
        });

        //Reset animation when an image is uploaded
        uploadImageButton.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                uploadImageButton.setProgress(0);
            }
        });

        //Hide status and navigation bar
        Window window = NewEventActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        final Intent intent = getIntent();

        rootLayout = findViewById(R.id.root_layout);

        if (savedInstanceState == null &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }






    }

    //get the image from the method call startIntentForResult and upload it to Firebase storage
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(RESULT_OK != resultCode){
            return;
        }
        if (requestCode == RC_PHOTO_PICKER) {
            Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    final StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
                    photoRef.putFile(selectedImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return photoRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                imageUri.add(downloadUri.toString());
                                uploadImageButton.setSpeed(2);
                                uploadImageButton.playAnimation();

                            } else {
                                Toast.makeText(NewEventActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
        }
    }


    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
            });


            circularReveal.start();
        }
    }



    @Override
    public void onBackPressed() {
        unRevealActivity();
    }
}