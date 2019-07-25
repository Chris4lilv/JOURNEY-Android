package com.example.android.album;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayFragment extends Fragment {

    private final static int RC_GALLERY = 1;
    private static final int RC_CONGRATS =  2;


    private ListView mListView;
    private ListViewAdapter listViewAdapter;
    private ArrayList<Event> eventsList;
    private ArrayList<String> eventKeyList;

    private AlertDialog.Builder builder;
    private TextView emptyView;
    private ProgressBar loadingIndicator;
    private FloatingActionButton fabAdd;


    private CoordinatorLayout layoutMain;
    private RelativeLayout layoutButtons;
    private RelativeLayout layoutContent;
    private boolean isOpen = false;

    private static final int RC_PHOTO_PICKER =  2;

    //setup firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.display_fragment,null);

        emptyView = (TextView) view.findViewById(R.id.empty_view);

        loadingIndicator = (ProgressBar)view.findViewById(R.id.loading_indicator);

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
        mStorageReference = mFirebaseStorage.getReference().child("Lovely_pic");

        //Fab animation component
        fabAdd = view.findViewById(R.id.fab_add);

        //Activate the FAB
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentActivity(v);
            }
        });


        //This part checks the Internet connection
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
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

                    listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
                    mListView.setAdapter(listViewAdapter);
                    listViewAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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


                Intent intent = new Intent(getActivity(),Gallery.class);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("URLs",URLs);
                bundle.putString("Key",key);
                bundle.putString("Title",title);
//                intent.putExtra("URLs",URLs);
//                intent.putExtra("Key",key);
                Bundle animation = ActivityOptions.makeSceneTransitionAnimation(
                        getActivity(),view.findViewById(R.id.date_linear_layout), view.findViewById(R.id.date_linear_layout).getTransitionName()
                ).toBundle();

                intent.putExtras(bundle);
                startActivity(intent,animation);
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
                        mDatabaseRef.child(key).removeValue();
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
                                        Toast.makeText(getActivity(),"Failed",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
//                confirmDelete(position);
                //update the adapter
                listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
                mListView.setAdapter(listViewAdapter);
                return true;
            }
        });




        return view;
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

        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }


}
