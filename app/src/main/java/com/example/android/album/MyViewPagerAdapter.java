package com.example.android.album;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MyViewPagerAdapter extends PagerAdapter {
    private List<View> mList;

    public MyViewPagerAdapter(List<View> list) {
        mList = list;
    }
    //返回视图数量
    @Override
    public int getCount() {
        return mList.size();
    }
    //是否通过对象加载视图
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    //加载当前页面
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mList.get(position));
        return mList.get(position);
    }
}
