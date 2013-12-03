/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.cm;

import java.util.List;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.campus.CampusFragmentPeople;
import eu.trentorise.smartcampus.cm.fragments.groups.MyGroupsFragment;
import eu.trentorise.smartcampus.cm.fragments.home.HomeFragmentMe;
import eu.trentorise.smartcampus.cm.fragments.profile.MyProfileFragment;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.cm.settings.SettingsActivity;
import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.social.model.Group;

public abstract class BaseCMActivity extends SherlockFragmentActivity {

	protected boolean initialized = false;
	protected final int mainlayout = android.R.id.content;
	public static String myGroups;
	public static List<Group> myList;

	public static DrawerLayout mDrawerLayout;
	public static ListView mDrawerList;
	public static ActionBarDrawerToggle mDrawerToggle;
	public static String drawerState = "on";
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mFragmentTitles;
	private Fragment FMe;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("initialized", initialized);
	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			CMHelper.init(getApplicationContext());
			if (!CMHelper.getAccessProvider().login(this, null)) {
				initData();
			}
		} catch (Exception e) {
			CMHelper.endAppFailure(this, R.string.app_failure_setup);
		}
	}

	protected boolean initData() {
		try {
			loadData();
		} catch (Exception e1) {
			CMHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (appSharedPrefs.contains("profile")) {
			if (savedInstanceState == null
					|| savedInstanceState.getBoolean("initialized") == false) {
				initDataManagement(savedInstanceState);
			}
			myGroups = appSharedPrefs.getString("savedGroup", "");
			myList = JsonUtils.toObjectList(myGroups, Group.class);
			String myProfile = appSharedPrefs.getString("profile", "");
			HomeActivity.picP = JsonUtils.toObject(myProfile,
					PictureProfile.class);

		}

		mFragmentTitles = getResources().getStringArray(R.array.fragment_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new MenuDrawerAdapter(this, getResources()
				.getStringArray(R.array.fragment_array)));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		//
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				//getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				//getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerSlide(View drawerView, float slideOffset) {
				//getSupportActionBar().setTitle(mDrawerTitle);
				mDrawerLayout.bringChildToFront(drawerView);
				supportInvalidateOptionsMenu();
				super.onDrawerSlide(drawerView, slideOffset);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		startHomeFragment();
		setUpContent();
	}

	private void startHomeFragment() {
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		FMe = new HomeFragmentMe();
		Bundle args = new Bundle();
		FMe.setArguments(args);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.replace(R.id.content_frame, FMe);
		// ft.addToBackStack(fragment.getTag());
		ft.commit();

	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}
	

	private void selectItem(int position) {
		String fragmentString = mFragmentTitles[position];
		// // update the main content by replacing fragments
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		if (fragmentString.equals(mFragmentTitles[0])) {
			//HomeFragmentMe fragment = new HomeFragmentMe();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, FMe,
					"Shared");
//			fragmentTransaction.addToBackStack(FMe.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[1])) {
			CampusFragmentPeople fragment = new CampusFragmentPeople();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment,
					"Campus");
			//fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[2])) {
			MyGroupsFragment fragment = new MyGroupsFragment();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment,
					"MyGroups");
			//fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[3])) {
			MyProfileFragment fragment = new MyProfileFragment();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment,
					"MyProfile");
			//fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if (fragmentString.equals(mFragmentTitles[4])) {
			Intent i = (new Intent(BaseCMActivity.this, SettingsActivity.class));
			startActivity(i);
			mDrawerLayout.closeDrawer(mDrawerList);
		} 

	}

	public static void UpdateGroups(Context c) {
		List<Group> savedGroup = CMHelper.getGroups();

		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		Editor prefsEditor = appSharedPrefs.edit();
		String json = JsonUtils.toJSON(savedGroup);
		prefsEditor.putString("savedGroup", json);
		prefsEditor.commit();
		myGroups = appSharedPrefs.getString("savedGroup", "");
		myList = JsonUtils.toObjectList(myGroups, Group.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.EDIT_PROFILE_ACTIVITY_REQUEST_CREATE) {
			if (Constants.EDIT_PROFILE_ACTIVITY_RESULT_OK == resultCode
					&& data.getSerializableExtra(Constants.EDIT_PROFILE_PROFILE_EXTRA) != null) {
				final PictureProfile profile = (PictureProfile) data
						.getSerializableExtra(Constants.EDIT_PROFILE_PROFILE_EXTRA);
				CMHelper.setProfile(profile);
			} else if (Constants.EDIT_PROFILE_ACTIVITY_RESULT_CANCELLED == resultCode
					|| Constants.EDIT_PROFILE_ACTIVITY_RESULT_FAILURE == resultCode
					|| Constants.EDIT_PROFILE_ACTIVITY_RESULT_OK == resultCode) {
				CMHelper.endAppFailure(this,
						R.string.app_failure_profile_missing);
			}
			return;
		}

		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(
						AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					CMHelper.endAppFailure(this, R.string.app_failure_security);
				} else {
					initData();
				}
			} else if (resultCode == RESULT_CANCELED) {
				CMHelper.endAppFailure(this, R.string.token_required);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				|| getSupportActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setDisplayShowTitleEnabled(true);
		} else {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	protected abstract void loadData();

	protected abstract void setUpContent();


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			if (drawerState.equals("on")) {
				if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
					mDrawerLayout.closeDrawer(mDrawerList);
				} else {
					mDrawerLayout.openDrawer(mDrawerList);
				}
			} else {
				drawerState = "on";
				onBackPressed();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}
}
