package com.lbconsulting.a1list.presentation.ui.adapters;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.gson.Gson;
import com.lbconsulting.a1list.domain.model.ListTitle;
import com.lbconsulting.a1list.presentation.ui.fragments.fragListItems;

import java.util.List;

import timber.log.Timber;

/**
 * A FragmentPagerAdapter that displays fragListItems.
 */
//public class SectionsPagerAdapter extends FragmentPagerAdapter {
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private List<ListTitle> mListTitles;

    public SectionsPagerAdapter(FragmentManager fm, List<ListTitle> listTitles) {
        super(fm);
        Timber.i("SectionsPagerAdapter() initialized.");
        mListTitles = listTitles;
    }

    public void replaceListTitle(int position, ListTitle listTitle) {
        mListTitles.set(position, listTitle);
    }

    @Override
    public fragListItems getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        fragListItems frag = null;
        ListTitle listTitle = mListTitles.get(position);
        if (listTitle != null) {
            Gson gson = new Gson();
            String listTitleJson = gson.toJson(listTitle);
            frag = fragListItems.newInstance(position, listTitleJson);
        }
        return frag;
    }

    @Override
    public int getCount() {
        return mListTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String listTitleName = null;
        ListTitle listTitle = mListTitles.get(position);
        if (listTitle != null) {
            listTitleName = listTitle.getName();
        }
        return listTitleName;
    }

    public ListTitle getListTitle(int position) {
        ListTitle listTitle = null;
        if (mListTitles.size() > 0 && position < mListTitles.size()) {
            listTitle = mListTitles.get(position);
        }
        return listTitle;
    }

    public int getPosition(ListTitle soughtListTitle) {
        return getPosition(soughtListTitle.getUuid());
    }

    public int getPosition(String soughtListTitleUuid) {
        int position = 0;
        boolean found = false;
        for (ListTitle listTitle : mListTitles) {
            if (listTitle.getUuid().equals(soughtListTitleUuid)) {
                found = true;
                break;
            }
            position++;
        }

        if (!found) {
            position = 0;
        }
        return position;
    }

}

