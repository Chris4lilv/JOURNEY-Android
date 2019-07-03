package com.example.android.album;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DisplayFragment extends Fragment {

    private final static int RC_GALLERY = 1;

    private ListView mListView;
    private ListViewAdapter listViewAdapter;
    private ArrayList<Event> eventsList;
    private ArrayList<String> eventKeyList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseRef;
    private AlertDialog.Builder builder;
    private FirebaseStorage mFirebaseStorage;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.display_fragment,null);

        //instantiate necessary object
        mListView = view.findViewById(R.id.listView);
        eventsList = new ArrayList<>();
        eventKeyList = new ArrayList<>();


        listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
        mListView.setAdapter(listViewAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDatabase.getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();

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

                listViewAdapter = new ListViewAdapter(getActivity(),eventsList);
                mListView.setAdapter(listViewAdapter);
                listViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //TODO: after finish above, rebuild the list_item file to contain an Glide widget and get to know how to display image using its URL

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event currentEvent = eventsList.get(position);
                ArrayList<String> URLs = currentEvent.getUrl();
                String key = eventKeyList.get(position);


                Intent intent = new Intent(getActivity(),Gallery.class);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("URLs",URLs);
                bundle.putString("Key",key);
//                intent.putExtra("URLs",URLs);
//                intent.putExtra("Key",key);
                intent.putExtras(bundle);
                startActivity(intent);
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

}
