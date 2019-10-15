package com.example.android.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.manager.LifecycleListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class MainActivity extends AppCompatActivity{

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private TextView mUserName;
    private TextView mUserEmail;
    private ImageView mUserProfile;

    private String journeyName;
    private String joinJourney;
    public Boolean switchToPersonal;

    private MenuItem newJourney;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private boolean firstTime;
    private boolean thirdTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        firstTime = intent.getBooleanExtra("signUp",false);
        SharedPreferences newUserGuide = PreferenceManager.getDefaultSharedPreferences(this);
        newUserGuide.edit().putBoolean("FirstTime", firstTime).apply();
        newUserGuide.edit().putBoolean("ThirdTime",false).apply();

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        switchToPersonal = false;

        //This create the icon at the upper left corner that would change as navigation drawer open
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.caption_hint,R.string.cancel);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Add displayFragment into FragmentManager
        final DisplayFragment displayFragment = new DisplayFragment();
        FragmentManager fmManager = getSupportFragmentManager();
        fmManager.beginTransaction()
                .add(R.id.display_fragment,displayFragment).commit();

        NavigationView nv_left = findViewById(R.id.navigation);
        View headerView = nv_left.getHeaderView(0);

        Menu menu = nv_left.getMenu();
        final SubMenu journeysMenu = menu.findItem(R.id.journeys_group).getSubMenu();

        mUserProfile = headerView.findViewById(R.id.profile);

        //Get journeyNames from sharedPreference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        journeyName = sharedPreferences.getString("JourneyName", "");
        joinJourney = sharedPreferences.getString("JoinJourney", "");

        if(journeyName.length() != 0 || joinJourney.length() != 0){
            if(journeyName.length() != 0){
                newJourney = journeysMenu.add(0,100, 0, journeyName);
                newJourney.setIcon(R.drawable.romance_heart_24);
            }
            if(joinJourney.length() != 0){
                newJourney = journeysMenu.add(0,101, 0, joinJourney.substring(joinJourney.indexOf("/") + 1));
                newJourney.setIcon(R.drawable.romance_heart_24);
            }
            mUserProfile.setImageResource(R.drawable.ic_people_black_24dp);
        }else{
            mUserProfile.setImageResource(R.drawable.ic_person_black_24dp);
        }

        mUserName = headerView.findViewById(R.id.user_name);
        mUserEmail = headerView.findViewById(R.id.user_email);
        mUserName.setText(user.getDisplayName());
        mUserEmail.setText(user.getEmail());


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("ThirdTime")){
                    thirdTime = sharedPreferences.getBoolean(key, false);
                    new GuideView.Builder(MainActivity.this)
                            .setTitle("Click on the button left to explore more")
                            .setGravity(Gravity.auto) //optional
                            .setDismissType(DismissType.targetView) //optional - default DismissType.targetView
                            .setTargetView(toolbar)
                            .setContentTextSize(12)//optional
                            .setTitleTextSize(14)//optional
                            .build()
                            .show();
                }
            }
        };

        newUserGuide.registerOnSharedPreferenceChangeListener(listener);


        nv_left.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case 101:
                        displayFragment.mWorkspace = joinJourney.substring(joinJourney.indexOf("/") + 1);
                        displayFragment.mDirectory = joinJourney.substring(0, joinJourney.indexOf("@")).replaceAll("[\\p{P}]", "");
                        switchToPersonal = false;
                        displayFragment.changeWorkSpace();
                        newJourney.setIcon(R.drawable.romance_heart_24_filled);
                        journeysMenu.getItem(0).setIcon(R.drawable.my_personal_icon_24);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawers();
                            }
                        }, 300);
                        break;
                    case 100:
                        displayFragment.mWorkspace = journeyName;
                        switchToPersonal = false;
                        displayFragment.changeWorkSpace();
                        newJourney.setIcon(R.drawable.romance_heart_24_filled);
                        journeysMenu.getItem(0).setIcon(R.drawable.my_personal_icon_24);
                        Handler handler1 = new Handler();
                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawers();
                            }
                        }, 300);
                        break;
                    case R.id.my_personal_journey:
                        displayFragment.mDirectory = user.getEmail().substring(0, user.getEmail().indexOf("@")).replaceAll("[\\p{P}]","");
                        displayFragment.mWorkspace = "personal";
                        switchToPersonal = true;
                        displayFragment.changeWorkSpace();
                        item.setIcon(R.drawable.my_personal_icon_filled_24);
                        if(newJourney != null){
                            newJourney.setIcon(R.drawable.romance_heart_24);
                        }
                        Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.closeDrawers();
                            }
                        }, 300);
                        break;
                    case R.id.nav_account_setting:
                        Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intentSettings);
                        finish();
                        break;
                    case R.id.nav_logout:
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        sharedPreferences.edit().clear().apply();

                        mAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return false;
            }
        });
    }
}


