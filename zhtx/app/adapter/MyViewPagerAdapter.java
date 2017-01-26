package com.zhtx.app.adapter;

import java.util.List;

import android.support.v4.app.Fragment ;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by Monkey on 2015/6/29.
 */
public class MyViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] mTitles;
    private List<Fragment> mFragments;

    public MyViewPagerAdapter(android.support.v4.app.FragmentManager fm, String[] mTitles, List<Fragment> mFragments) {
        super(fm);
        this.mTitles = mTitles;
        this.mFragments = mFragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
