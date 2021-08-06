package com.echo.library.pageview;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.echo.library.BaseVHDialogFragment;

import java.util.List;


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/9/16
 * change   :
 * describe :
 */
public class IFragmentPagerAdapter extends FragmentPagerAdapter {
    List<? extends BaseVHDialogFragment> mFragments;

    public IFragmentPagerAdapter(FragmentManager fm, List<? extends BaseVHDialogFragment> fragments) {
        super(fm);
        mFragments = fragments;

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getFragmentTitle();
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
