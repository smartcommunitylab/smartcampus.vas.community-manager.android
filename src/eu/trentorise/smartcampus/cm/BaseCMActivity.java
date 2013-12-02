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
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
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
		setUpContent();
	}

	
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
	
}
