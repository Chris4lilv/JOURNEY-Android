package com.example.android.album;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;

import android.widget.EditText;
import android.widget.ImageView;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;

public class NewEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String[] MONTH_OF_YEAR = new String[]{"January", "February","March","April","May","June","July","August","September","October","November","December"};

    private static final int RC_PHOTO_PICKER =  2;
    private int color = 0;

    //setup firebase
    FirebaseStorage mStorage;
    StorageReference mStorageReference;
    FirebaseDatabase database;
    DatabaseReference myRef;

    private String mCurrentUser;
    public String mDirectory;
    protected String mWorkSpace;

    ArrayList<String> imageUri;
    ArrayList<Uri> urlHolder;
    ArrayList<String> imageDisplay;

    EditText caption;

    LottieAnimationView createEventButton;

    private String yearSelected = "";
    private String monthSelected = "";
    private String daySelected = "";

    private DatePickerDialog dpd;
    private TextView dateTextView;
    private ImageView dateSelect;

    private RecyclerView imageRecyclerView;
    private SmallImageAdapter adapter;

    private GestureDetector gestureDetector;

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


        dateTextView = findViewById(R.id.dateTextView);
        dateSelect = findViewById(R.id.dateSelect);

        final Intent intent = getIntent();
        mWorkSpace = intent.getStringExtra("WorkSpace");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mDirectory = intent.getStringExtra("Directory");
//        mDirectory = mCurrentUser.substring(0, mCurrentUser.indexOf("@")).replaceAll("[\\p{P}]","");


        //initialize storage, database and their reference
        mStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mStorageReference = mStorage.getReference().child(mDirectory).child(mWorkSpace);

        imageUri = new ArrayList<>();
        imageDisplay = new ArrayList<>();
        urlHolder = new ArrayList<>();

        imageDisplay.add("android.resource://com.example.android.album/drawable/ic_add_img");

        caption = findViewById(R.id.caption);

        createEventButton = findViewById(R.id.create_event);

        imageRecyclerView = findViewById(R.id.image_holder_recycler_view);
        imageRecyclerView.setHasFixedSize(false);
        imageRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        imageRecyclerView.setAdapter(adapter = new SmallImageAdapter());
        adapter.replaceAll(imageDisplay);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View childView = imageRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if(childView != null){
                    if(imageRecyclerView.getChildLayoutPosition(childView) == 0){
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/jpeg");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                    }
                }
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                View childView = imageRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null) {
                    int position = imageRecyclerView.getChildLayoutPosition(childView);
                    if(position != 0){
                        //update the UI
                        imageDisplay.remove(position);
                        adapter.replaceAll(imageDisplay);
                        Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imageRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEventButton.setClickable(false);
                if(yearSelected.length() == 0 || monthSelected.length() == 0 || daySelected.length() == 0){
                    Toast.makeText(NewEventActivity.this, "Did you forget to pick date?", Toast.LENGTH_SHORT).show();
                    createEventButton.setClickable(true);
                }else{
                    createEventButton.playAnimation();
                    try {
                        Bitmap bm = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(urlHolder.get(0)));
                        DynamicColor dynamicColor = new DynamicColor(bm,getApplicationContext());
                        color = dynamicColor.getColor();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    ArrayList<String> uris = uploadImage(imageUri);
                    createEventButton.addAnimatorListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                                //get caption and date
                                String cap = caption.getText().toString();
                                String date = yearSelected + monthSelected + daySelected;
                                Event event = new Event(uris,cap,date, color);
                                myRef.child(mDirectory).child(mWorkSpace).push().setValue(event);
                                createEventButton.setClickable(true);
                            //kill the activity and remove it from the stack
                                onBackPressed();
                        }
                    });
                }
            }
        });

        dateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                if (dpd == null) {
                    dpd = DatePickerDialog.newInstance(
                            NewEventActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                } else {
                    dpd.initialize(
                            NewEventActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                }
                dpd.setOnCancelListener(dialog -> {
                    dpd = null;
                });
                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });

        //Hide status and navigation bar
        Window window = NewEventActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(RESULT_OK != resultCode){
            return;
        }
        if (requestCode == RC_PHOTO_PICKER) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null){
                imageDisplay.add(selectedImageUri.toString());
                urlHolder.add(selectedImageUri);
                adapter.replaceAll(imageDisplay);
            }
        }
    }

    public ArrayList<String>  uploadImage(ArrayList<String> imageUri){
        for(int i = 0; i < urlHolder.size(); i++){
            Uri selectedImageUri = urlHolder.get(i);
            StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
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
                    } else {
                        Toast.makeText(NewEventActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return imageUri;

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
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = "On "+ MONTH_OF_YEAR[monthOfYear] + " " + dayOfMonth+", "+ year  + ", ";
        dateTextView.setText(date);
        yearSelected = Integer.toString(year);
        monthSelected = Integer.toString(monthOfYear + 1);
        if(monthOfYear + 1 < 10){
            monthSelected = "0" + monthSelected;
        }
        daySelected = Integer.toString(dayOfMonth);
        if(dayOfMonth < 10){
            daySelected = "0" + daySelected;
        }
        dpd = null;
        dateSelect.setImageResource(R.drawable.ic_date_range_white_24dp);
    }

    @Override
    public void onBackPressed() {
        unRevealActivity();
    }
}