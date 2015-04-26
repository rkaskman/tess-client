package com.roman.ttu.client.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.roman.ttu.client.model.UserImagesWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.roman.ttu.client.activity.PendingImagesActivity.ImagesFragment;

public class PendingImagesAdapter extends FragmentStatePagerAdapter  {

    public static final String IMAGES_KEY = "images";
    private List<UserImagesWrapper> userImagesWrapperList = new ArrayList<>();

    public PendingImagesAdapter(FragmentManager fm, Collection<UserImagesWrapper> userImagesWrappers) {
        super(fm);
        userImagesWrapperList.addAll(userImagesWrappers);
    }

    @Override
    public Fragment getItem(int position) {
        ImagesFragment imagesFragment = new ImagesFragment();
        Bundle args = new Bundle();
        args.putSerializable(IMAGES_KEY, userImagesWrapperList.get(position));
        imagesFragment.setArguments(args);
        return imagesFragment;
    }

    public UserImagesWrapper getUserImagesWrapperBy(int position) {
        return userImagesWrapperList.get(position);
    }

    public void remove(int position) {
        userImagesWrapperList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return userImagesWrapperList.size();
    }

    public boolean hasItems() {
        return !userImagesWrapperList.isEmpty();
    }
}
