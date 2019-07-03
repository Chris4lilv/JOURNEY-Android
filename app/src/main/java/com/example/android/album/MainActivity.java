package com.example.android.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //TODO: make title bar disappeared
    //TODO: listview 左边显示日期背景更换
    //TODO：让listview的每一个item透明度低一些
    //TODO：listview的title字体更换
    //TODO：fragment背景更换
    //TODO: 首次进入app，event还未加载时用listview.setEmptyView来改善用户体验
    //TODO: 接上一条，在加载时设置indeterminate progress bar 圆形

    static final int NUM_ITEMS = 2;

    BubbleNavigationConstraintView bubNav;

    ViewPager mViewPager;
    MyAdapter mAdapter;
    ArrayList<View> viewsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new MyAdapter(getSupportFragmentManager());

        //instantiate Bubble constraint view
        bubNav = (BubbleNavigationConstraintView) findViewById(R.id.top_navigation_constraint);

        //instantiate ViewPager
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        //instantiate viewsList
        viewsList = new ArrayList<>();
        View displayView = LayoutInflater.from(this).inflate(R.layout.display_fragment,null);
        View addEventView = LayoutInflater.from(this).inflate(R.layout.add_fragment,null);
        viewsList.add(displayView);
        viewsList.add(addEventView);

        //apply adapter to the view pager
        mViewPager.setAdapter(mAdapter);



        bubNav.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                //navigation changed, do something????
                mViewPager.setCurrentItem(position);
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                bubNav.setCurrentActiveItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //bubView.setCurrentActiveItem(i);
            }
        });
    }




    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount(){
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if(position == 0) {
                fragment = new DisplayFragment();
            } else {
                fragment = new AddFragment();
            }
            return fragment;
        }
    }
}


