package com.gsoc.vedantsingh.locatedvoicecms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.gsoc.vedantsingh.locatedvoicecms.advancedTools.AdvancedToolsFragment;
import com.gsoc.vedantsingh.locatedvoicecms.data.POIsContract;

public class AdminCollectionPagerAdapter extends FragmentStatePagerAdapter {

    public static final int PAGE_TREEEVIEW = 0;
    public static final int PAGE_TOURS = 1;
    public static final int PAGE_TOOLS = 2;
    public static final int PAGE_TASKS = 3;
    public static final int PAGE_BEACONS = 4;

    public AdminCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        switch (position) {
            case PAGE_TREEEVIEW:
                return NewPOISList.newInstance();
            case PAGE_TOURS:
                Fragment fragmentTours = new POISFragment();
                args.clear();
                args.putString("EDITABLE", "ADMIN/TOURS");
                fragmentTours.setArguments(args);
                return fragmentTours;
            case PAGE_TOOLS:
                return new LGTools();
            case PAGE_TASKS:
                return AdvancedToolsFragment.newInstance();
//            case PAGE_BEACONS:
//                return NearbyBeaconsFragment.newInstance();
            default:
                return null;
        }
    }

    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case PAGE_TREEEVIEW:
                return "CATEGORIES & POIS";
            case PAGE_TOURS:
                return "TOURS";
            case PAGE_TOOLS:
                return "TOOLS";
            case PAGE_TASKS:
                return "LG TASKS";
//            case PAGE_BEACONS:
//                return "SCAN BEACON";
            default:
                return "PAGE" + (position - 1);
        }
    }
}
