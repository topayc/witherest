package com.teamteam.witherest;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.util.Log;

import com.teamteam.witherest.service.callback.object.CategoryRoomListResponseObject;
import com.viewpagerindicator.IconPagerAdapter;


public class CheckRoomViewPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
	
	public String[] ListTitles;
	
	private int mCount = 2;
	private Activity activity;
	
	public CheckRoomListFragment[] fragments = new CheckRoomListFragment[2];

	protected static final int[] ICONS = new int[] {
        R.drawable.perm_group_calendar,
        R.drawable.perm_group_camera,
        R.drawable.perm_group_device_alarms,
        R.drawable.perm_group_location};
	
	public CheckRoomViewPagerAdapter (FragmentManager fm,CheckRoomListFragment[] fragments) {
	        super(fm);
	       /* Log.v("페이지 어댑터 ","생성자 호출");*/
	        this.fragments = fragments;
	        
	        this.ListTitles = new String[fragments.length];
	        for (int i = 0; i < ListTitles.length; i++){
	        	this.ListTitles[i] = this.fragments[i].getArguments().getString("title");
	        }
	    }

	public int getIconResId(int position) {
		  return ICONS[position];
	}

	@Override
	public Fragment getItem(int position) {
	/*	Log.v("페이지 어댑터 ", position + " 번 getItem 호출");*/
		  return fragments[position];
	
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
		/*Log.v("페이지 어댑터 ", position + " 번 getPageTitle");*/
      return ListTitles[position];
    }

	@Override
	public int getCount() {
		return this.fragments.length;
	}
	
	 public void setCount(int count) {
	        if (count > 0 && count <= 10) {
	            mCount = count;
	            notifyDataSetChanged();
	        }
	    }
}
