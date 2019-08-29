package com.example.android.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //TODO: listview 左边显示日期背景更换
    //TODO：让listview的每一个item透明度低一些
    //TODO：listview的title字体更换

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private TextView mUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //This create the icon at the upper left corner that would change as navigation drawer open
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.caption_hint,R.string.cancel);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        DisplayFragment displayFragment = new DisplayFragment();

        FragmentManager fmManager = getSupportFragmentManager();

        fmManager.beginTransaction()
                .add(R.id.display_fragment,displayFragment).commit();


        NavigationView nv_left = findViewById(R.id.navigation);
        View headerView = nv_left.getHeaderView(0);

        mUserName = headerView.findViewById(R.id.user_name);
        mUserName.setText(user.getDisplayName());

        nv_left.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_logout://条目的ID
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


