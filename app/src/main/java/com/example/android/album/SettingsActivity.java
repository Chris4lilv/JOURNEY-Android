package com.example.android.album;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

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
        public FirebaseDatabase mFirebaseDatabase;
        public DatabaseReference mDatabaseRef;
        SharedPreferences sharedPreferences;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            //Initialization
            final EditTextPreference journey = findPreference("startJourney");
            EditTextPreference joinJourney = findPreference("joinJourney");

            //Create a Journey
            journey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final String workspace = newValue.toString().replaceAll("[\\p{P}]","");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("JourneyName", workspace);
                    editor.apply();
                    return true;
                }
            });

            //Join a Journey
            joinJourney.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String entireInput = newValue.toString();
                    //Split the input
                    final String userName = entireInput.substring(0, entireInput.indexOf("/")).replaceAll("[\\p{P}]","");
                    final String workspace = entireInput.substring(entireInput.indexOf("/") + 1);

                    if(userName.length() != 0){
                        mFirebaseDatabase = FirebaseDatabase.getInstance();
                        mDatabaseRef = mFirebaseDatabase.getReference();
                        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.hasChild(userName)){
                                    Toast.makeText(getContext(), "User doesn't exist", Toast.LENGTH_SHORT).show();
                                }else{
                                    if(dataSnapshot.child(userName).hasChild(workspace)){
                                        Toast.makeText(getContext(), "Journey exists!", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(getContext(), "Journey doesn't exists", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("JoinJourney", workspace);
                        editor.apply();
                    }
                    return true;
                }
            });
        }
    }
}

