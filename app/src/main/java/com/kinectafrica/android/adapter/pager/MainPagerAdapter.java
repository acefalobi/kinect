package com.kinectafrica.android.adapter.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kinectafrica.android.fragment.KinectFragment;
import com.kinectafrica.android.fragment.MessagesFragment;
import com.kinectafrica.android.fragment.ProfileFragment;

/**
 * Made by acefalobi on 3/21/2017.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MessagesFragment();
            case 1:
                return new KinectFragment();
            case 2:
                return new ProfileFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Messages";
            case 1:
                return "Kinect";
            case 2:
                return "Profile";
            default:
                return super.getPageTitle(position);
        }
    }
}
