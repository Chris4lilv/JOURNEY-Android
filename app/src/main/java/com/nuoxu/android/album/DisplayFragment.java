package com.nuoxu.android.album;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class DisplayFragment extends Fragment{

    private static int RC_GALLERY = 0;
    private static final int RC_CONGRATS =  2;


    private ListView mListView;
    private ListViewAdapter listViewAdapter;
    private ArrayList<Event> eventsList;
    private ArrayList<String> eventKeyList;

    private TextView emptyView;
    private ProgressBar loadingIndicator;
    private FloatingActionButton fabAdd;

    private static final int RC_PHOTO_PICKER =  2;

    private AppBarLayout appBarLayout;

    //setup firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    public String mCurrentUser;
    public String mDirectory;
    public String mWorkspace;

    public boolean firstTime;
    public boolean secondTime;
    private TextView newEventGuide;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.display_fragment,null);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mDirectory = mCurrentUser.substring(0, mCurrentUser.indexOf("@")).replaceAll("[\\p{P}]","");
        mWorkspace = "personal";



        emptyView = (TextView) view.findViewById(R.id.empty_view);

        loadingIndicator = (ProgressBar)view.findViewById(R.id.loading_indicator);

        newEventGuide = view.findViewById(R.id.new_event_guide);

        eventsList = new ArrayList<>();
        eventKeyList = new ArrayList<>();

        //ListView component
        mListView = view.findViewById(R.id.listView);
        mListView.setEmptyView(emptyView);
        listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
        mListView.setAdapter(listViewAdapter);

        //Firebase component
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child(mDirectory).child(mWorkspace);

        //Fab animation component
        fabAdd = view.findViewById(R.id.fab_add);

        //Activate the FAB
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentActivity(v);
            }
        });

        appBarLayout = getActivity().findViewById(R.id.app_bar_layout);


        //This part checks the Internet connection
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
           changeWorkSpace();
        }else{
            //Hide the loading indicator before showing Internet connection error
            View loadingIndicator = view.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            emptyView.setText("No Internet Connection...");

        }




        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event currentEvent = eventsList.get(position);
                ArrayList<String> URLs = currentEvent.getUrl();
                String key = eventKeyList.get(position);
                String title = currentEvent.getCaption();
                int color = currentEvent.getColor();
                int comColor = currentEvent.getComColor();

                Intent intent = new Intent(getActivity(),Gallery.class);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("URLs",URLs);
                bundle.putString("Key",key);
                bundle.putString("Title",title);
                bundle.putInt("Color", color);
                bundle.putInt("ComColor", comColor);

                RC_GALLERY = position;
//                intent.putExtra("URLs",URLs);
//                intent.putExtra("Key",key);
                Bundle animation = ActivityOptions.makeSceneTransitionAnimation(
                        getActivity()
                ).toBundle();

                intent.putExtras(bundle);

                startActivityForResult(intent,RC_GALLERY,animation);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_delete_event_message)
                        .setTitle(R.string.dialog_delete_event_title);

                //When the user decide to delete
                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        Event currentEvent = eventsList.get(position);
                        //get the corresponding key and remove event in database
                        String key = eventKeyList.get(position);
                        mDatabaseRef.child(mDirectory).child(mWorkspace).child(key).removeValue();
                        eventsList.remove(position);

                        //remove the image related to this event in storage
                        if(currentEvent.getUrl() != null){
                            ArrayList<String> urls = currentEvent.getUrl();
                            for(int i = 0; i < urls.size(); i++){
                                StorageReference mPhotoRef = mFirebaseStorage.getReferenceFromUrl(urls.get(i));
                                mPhotoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                            }
                        }

                        //update the adapter
                        ListViewAdapter listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
                        mListView.setAdapter(listViewAdapter);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.edit().putBoolean("SecondTime", false).apply();
        listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("SecondTime")){
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        firstTime = sharedPreferences.getBoolean("FirstTime",false);
        if(firstTime){
            new GuideView.Builder(getContext())
                    .setTitle("Here you can add an event")
                    .setGravity(Gravity.auto) //optional
                    .setDismissType(DismissType.targetView) //optional - default DismissType.targetView
                    .setTargetView(fabAdd)
                    .setContentTextSize(12)//optional
                    .setTitleTextSize(14)//optional
                    .build()
                    .show();

        }


        return view;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_GALLERY){
            int position = RC_GALLERY;
            if(data != null){
                ArrayList<String> newURL = data.getStringArrayListExtra("newURL");
                Event currentEvent = eventsList.get(position);
                currentEvent.setUrl(newURL);
            }

        }
    }

    /**
     * Change to corresponding workspace after button on navigation view is pressed
     */
    public void changeWorkSpace(){
        mStorageReference = mFirebaseStorage.getReference().child(mDirectory).child(mWorkspace);

        mDatabaseRef.child(mDirectory).child(mWorkspace).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsList.clear();
                eventKeyList.clear();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()){
                    Event event = eventSnapshot.getValue(Event.class);
                    String eventKey = eventSnapshot.getKey();
                    eventKeyList.add(eventKey);
                    eventsList.add(event);
                }
                //Hide the loading indicator after the first time loading
                loadingIndicator.setVisibility(View.GONE);

//                listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
//                mListView.setAdapter(listViewAdapter);
                listViewAdapter.notifyDataSetChanged();
                secondTime = sharedPreferences.getBoolean("SecondTime",false);
                if(secondTime){
                    new GuideView.Builder(getContext())
                            .setTitle("Now you can check out your event here")
                            .setGravity(Gravity.auto) //optional
                            .setDismissType(DismissType.targetView) //optional - default DismissType.targetView
                            .setTargetView(newEventGuide)
                            .setContentTextSize(12)//optional
                            .setTitleTextSize(14)//optional
                            .build()
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Display @activity_new_event using FAB with circular reveal animation
     */
    public void presentActivity(View view) {
        View activityView = getActivity().findViewById(R.id.activity_view);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), view, "transition");
        int revealX = (int) (view.getX() + activityView.getWidth() / 2);
        int revealY = (int) (view.getY() + activityView.getHeight() / 2);

        Intent intent = new Intent(getActivity(), NewEventActivity.class);
        intent.putExtra(NewEventActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(NewEventActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);
        intent.putExtra("WorkSpace",mWorkspace);
        intent.putExtra("Directory", mDirectory);

        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }
}

