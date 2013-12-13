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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.HandleExceptionHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.BackListener;
import eu.trentorise.smartcampus.cm.fragments.campus.CampusFragmentPeople;
import eu.trentorise.smartcampus.cm.fragments.groups.MyGroupsFragment;
import eu.trentorise.smartcampus.cm.fragments.home.HomeFragmentMe;
import eu.trentorise.smartcampus.cm.fragments.profile.MyProfileFragment;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class HomeActivity extends BaseCMActivity {

	public static DrawerLayout mDrawerLayout;
	public static ListView mDrawerList;
	public static ActionBarDrawerToggle mDrawerToggle;
	public static String drawerState = "on";
	private CharSequence mTitle;
	private String[] mFragmentTitles;


	@Override
	protected void loadData() {
		new SCAsyncTask<Void, Void, Void>(this,
				new LoadProfileProcessor()).execute();
	}

	@Override
	protected void setUpContent() {
		setContentView(R.layout.main);
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mFragmentTitles = getResources().getStringArray(R.array.fragment_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new MenuDrawerAdapter(this, getResources()
				.getStringArray(R.array.fragment_array)));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		//
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				// getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				// getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerSlide(View drawerView, float slideOffset) {
				// getSupportActionBar().setTitle(mDrawerTitle);
				mDrawerLayout.bringChildToFront(drawerView);
				supportInvalidateOptionsMenu();
				super.onDrawerSlide(drawerView, slideOffset);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	public void onBackPressed() {
		// Toast toast = Toast.makeText(getApplicationContext(), "...",
		// Toast.LENGTH_SHORT);
		// toast.show();
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(android.R.id.content);
		// Checking if there is a fragment that it's listening for back button
		if (currentFragment != null && currentFragment instanceof BackListener) {
			((BackListener) currentFragment).onBack();
		}

		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.closeDrawer(mDrawerList);

		super.onBackPressed();
	}

	private void startFirstFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		HomeFragmentMe fragment = new HomeFragmentMe();
		getSupportActionBar().setTitle(R.string.shared_title);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.replace(R.id.content_frame, fragment, "Shared");
		// fragmentTransaction.addToBackStack(FMe.getTag());
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

		/* SHARED */
		if (fragmentString.equals(mFragmentTitles[0])) {
			HomeFragmentMe fragment = new HomeFragmentMe();
			getSupportActionBar().setTitle(R.string.shared_title);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment, "Shared");
			// fragmentTransaction.addToBackStack(FMe.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
			/* CAMPUS */
		} else if (fragmentString.equals(mFragmentTitles[1])) {
			if (getSupportActionBar().getNavigationMode() == getSupportActionBar().NAVIGATION_MODE_TABS)
				getSupportActionBar().setSelectedNavigationItem(0);
			CampusFragmentPeople fragment = new CampusFragmentPeople();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment, "Campus");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
			/* MY GROUPS */
		} else if (fragmentString.equals(mFragmentTitles[2])) {
			if (getSupportActionBar().getNavigationMode() == getSupportActionBar().NAVIGATION_MODE_TABS)
				getSupportActionBar().setSelectedNavigationItem(0);
			MyGroupsFragment fragment = new MyGroupsFragment();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment,
					"MyGroups");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
			/* MY PROFILE */
		} else if (fragmentString.equals(mFragmentTitles[3])) {
			if (getSupportActionBar().getNavigationMode() == getSupportActionBar().NAVIGATION_MODE_TABS)
				getSupportActionBar().setSelectedNavigationItem(0);
			MyProfileFragment fragment = new MyProfileFragment();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.content_frame, fragment,
					"MyProfile");
			// fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			mDrawerLayout.closeDrawer(mDrawerList);
			/* SYNCHRONIZE */
		} else if (fragmentString.equals(mFragmentTitles[4])) {
			if (getSupportActionBar().getNavigationMode() == getSupportActionBar().NAVIGATION_MODE_TABS)
				getSupportActionBar().setSelectedNavigationItem(0);
			sync();
			// Intent i = (new Intent(BaseCMActivity.this,
			// SettingsActivity.class));
			// startActivity(i);
			mDrawerLayout.closeDrawer(mDrawerList);
			/* TUTORIAL */
		} else if (fragmentString.equals(mFragmentTitles[5])) {
			if (getSupportActionBar().getNavigationMode() == getSupportActionBar().NAVIGATION_MODE_TABS)
				getSupportActionBar().setSelectedNavigationItem(0);
			Toast.makeText(getApplicationContext(), "Soon!", Toast.LENGTH_SHORT)
					.show();

			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	private class LoadProfileProcessor implements
			SCAsyncTaskProcessor<Void, Void> {

		@Override
		public Void performAction(Void... params)
				throws SecurityException, Exception {
			CMHelper.ensureProfile();
			return null;
		}

		@Override
		public void handleResult(Void result) {
			startFirstFragment();
		}

		@Override
		public void handleFailure(Exception e) {
			CMHelper.endAppFailure(HomeActivity.this,
					R.string.app_failure_setup);
		}

		@Override
		public void handleConnectionError() {
			HandleExceptionHelper.connectivityFailure(HomeActivity.this);
			finish();

		}

		@Override
		public void handleSecurityError() {
			CMHelper.endAppFailure(HomeActivity.this,
					R.string.app_failure_security);
		}

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

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}
	
	public void sync(){
		new SCAsyncTask<Void, Void, Boolean>(this, new SyncProcessor(this)).execute();
	}


	public class SyncProcessor extends
			AbstractAsyncTaskProcessor<Void, Boolean> {

		public SyncProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(Void... params) throws SecurityException, Exception {
			return CMHelper.syncData(getApplicationContext());
		}

		@Override
		public void handleResult(Boolean result) {
			Toast.makeText(getApplicationContext(),
					R.string.sync_ok, Toast.LENGTH_SHORT)
					.show();

		}
	}
	

}
