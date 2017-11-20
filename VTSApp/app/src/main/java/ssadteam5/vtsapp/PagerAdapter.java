package ssadteam5.vtsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

class PagerAdapter extends FragmentStatePagerAdapter {
    private final int mNumOfTabs;
    private final TripReport tab1;
    private final IdleReport tab2;


    public PagerAdapter(FragmentManager fm, int NumOfTabs, Bundle bundle) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        Bundle bun = bundle;
        tab1= new TripReport();
        tab1.setArguments(bun);
        tab2 = new IdleReport();
        tab2.setArguments(bun);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("Fragment", "PageAdapter");
        Log.d("FragmentPosition: ", position+"");
        switch (position) {
            case 0:
               Log.d("pager", "Trip");
               return tab1;
            case 1:
                Log.d("pager", "Idle");
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


}