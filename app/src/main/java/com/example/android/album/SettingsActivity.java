package com.example.android.album;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

public class SettingsActivity extends AppCompatActivity {

    public Boolean checkJourney;
    private static FirebaseDatabase mFirebaseDatabase;
    private static DatabaseReference mDatabaseRef;
    private static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static class SettingsFragment extends PreferenceFragmentCompat {


        private String userName;
        private String workspace;
        private static FirebaseUser user;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            //Initialization
            final EditTextPreference journey = findPreference("startJourney");
            final EditTextPreference joinJourney = findPreference("joinJourney");

            //Initialize Firebase
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();
            mDatabaseRef = mFirebaseDatabase.getReference().child(user.getEmail().substring(0, user.getEmail().indexOf("@")).replaceAll("[\\p{P}]", "")).child("preference");
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()){
                            com.example.android.album.Preference preference =eventSnapshot.getValue(com.example.android.album.Preference.class);
                            journey.setText(preference.getPreferenceStart());
                            journey.callChangeListener(preference.getPreferenceStart());
                            joinJourney.setText(preference.getPreferenceJoin());
                            joinJourney.callChangeListener(preference.getPreferenceJoin());

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            //Create a Journey
            journey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String workspace = newValue.toString().replaceAll("[\\p{P}]", "");
                    if(workspace.length() != 0){
                        joinJourney.setEnabled(false);
                    }else{
                        joinJourney.setEnabled(true);
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("JourneyName", workspace);
                    editor.apply();
                    pushPreference();
                    return true;
                }
            });

            //Join a Journey
            joinJourney.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String entireInput = newValue.toString();
                    //Split the input
                    if (entireInput.contains("/") && entireInput.contains("@")) {
                        userName = entireInput.substring(0, entireInput.indexOf("@")).replaceAll("[\\p{P}]", "");
                        workspace = entireInput.substring(entireInput.indexOf("/") + 1);
                    } else {
                        userName = " ";
                        workspace = " ";
                    }


                    if (entireInput.length() != 0) {
                        mDatabaseRef = mFirebaseDatabase.getReference();
                        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild(userName)) {
                                    Toast.makeText(getContext(), "Make sure the format email/journey name is correct", Toast.LENGTH_SHORT).show();
                                    joinJourney.setText("");
                                } else {
                                    if (dataSnapshot.child(userName).hasChild(workspace)) {
                                        Toast.makeText(getContext(), "Journey exists!", Toast.LENGTH_SHORT).show();
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("JoinJourney", entireInput);
                                        editor.apply();
                                        journey.setEnabled(false);
                                    } else {
                                        Toast.makeText(getContext(), "Journey doesn't exist", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("JoinJourney", entireInput);
                        editor.apply();
                        journey.setEnabled(true);
                    }

                    return true;
                }
            });
        }
        public static void pushPreference() {
            String preferenceOne = sharedPreferences.getString("JourneyName", "");
            String preferenceTwo = sharedPreferences.getString("JoinJourney", "");
            mDatabaseRef = mFirebaseDatabase.getReference().child(user.getEmail().substring(0, user.getEmail().indexOf("@")).replaceAll("[\\p{P}]", "")).child("preference");
            mDatabaseRef.removeValue();
            mDatabaseRef.push().setValue(new com.example.android.album.Preference(preferenceOne, preferenceTwo));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SettingsFragment.pushPreference();
    }
}


