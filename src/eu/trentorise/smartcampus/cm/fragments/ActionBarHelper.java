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
package eu.trentorise.smartcampus.cm.fragments;

import android.content.res.Configuration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.Constants;
import eu.trentorise.smartcampus.cm.custom.TabListener;
import eu.trentorise.smartcampus.cm.fragments.campus.CampusFragmentCommunities;
import eu.trentorise.smartcampus.cm.fragments.campus.CampusFragmentPeople;
import eu.trentorise.smartcampus.cm.fragments.campus.CampusFragmentSuggested;
import eu.trentorise.smartcampus.cm.fragments.home.HomeFragmentMe;
import eu.trentorise.smartcampus.cm.fragments.home.HomeFragmentMyCommunities;
import eu.trentorise.smartcampus.cm.fragments.home.HomeFragmentMyGroups;

public class ActionBarHelper {

	protected static final int mainlayout = android.R.id.content;
	public final static String ARG_TAB = "arg_tab"; 

	public static void emptyActionBar(SherlockFragment fragment) {
		SherlockFragmentActivity activity = fragment.getSherlockActivity();
		activity.getSupportActionBar().removeAllTabs();
		activity.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}
	
	public static void populateSharedContentActionBar(SherlockFragment fragment) {
		SherlockFragmentActivity activity = fragment.getSherlockActivity();
		if (fragment.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
			fragment.getSherlockActivity().getSupportActionBar().setTitle(R.string.shared_title);
		} else {
			activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
		
//		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		if (activity.getSupportActionBar() != null && activity.getSupportActionBar().getTabCount() > 0 && 
				activity.getSupportActionBar().getTabAt(0).getText().equals(activity.getString(R.string.home_tab_me))) return;

		activity.getSupportActionBar().removeAllTabs();
		
		ActionBar.Tab tab = activity.getSupportActionBar().newTab();
		tab.setText(R.string.home_tab_me);
		tab.setTabListener(new TabListener<HomeFragmentMe>(activity,
				Constants.HOME_FRAGMENT_ME_TAG, HomeFragmentMe.class, fragment.getClass().equals(HomeFragmentMe.class) ? fragment : null));
		activity.getSupportActionBar().addTab(tab);

		// My Groups
		tab = activity.getSupportActionBar().newTab();
		tab.setText(R.string.home_tab_mygroups);
		tab.setTabListener(new TabListener<HomeFragmentMyGroups>(activity,
				Constants.HOME_FRAGMENT_MYGROUPS_TAG,
				HomeFragmentMyGroups.class, fragment.getClass().equals(HomeFragmentMyGroups.class) ? fragment : null));
		activity.getSupportActionBar().addTab(tab);

		// My Groups
		tab = activity.getSupportActionBar().newTab();
		tab.setText(R.string.home_tab_mycommunities);
		tab.setTabListener(new TabListener<HomeFragmentMyCommunities>(activity,
				Constants.HOME_FRAGMENT_MYCOMMUNITIES_TAG,
				HomeFragmentMyCommunities.class,
				fragment.getClass().equals(HomeFragmentMyCommunities.class) ? fragment : null));
		activity.getSupportActionBar().addTab(tab);

	}
	
	public static void populateCampusActionBar(SherlockFragment fragment) {
		SherlockFragmentActivity activity = fragment.getSherlockActivity();
		if (fragment.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
			activity.getSupportActionBar().setTitle(R.string.campus_title);
		} else {
			activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
		}

		
		if (activity.getSupportActionBar() != null && activity.getSupportActionBar().getTabCount() > 0 &&
				activity.getSupportActionBar().getTabAt(0).getText().equals(activity.getString(R.string.campus_tab_people))) return;
		activity.getSupportActionBar().removeAllTabs();
		
		int toSelect = activity.getIntent() !=null ? activity.getIntent().getIntExtra(ARG_TAB, -1) : -1;

		ActionBar.Tab current = null;
		
		// People
		ActionBar.Tab tab = activity.getSupportActionBar().newTab();
		tab.setText(R.string.campus_tab_people);
		tab.setTabListener(new TabListener<CampusFragmentPeople>(
				activity, 
				Constants.CAMPUS_FRAGMENT_PEOPLE_TAG, 
				CampusFragmentPeople.class,
				fragment.getClass().equals(CampusFragmentPeople.class) ? fragment : null));
		activity.getSupportActionBar().addTab(tab);
		if (R.string.campus_tab_people == toSelect) current = tab;

		// Suggested
		tab = activity.getSupportActionBar().newTab();
		tab.setText(R.string.campus_tab_suggested);
		tab.setTabListener(new TabListener<CampusFragmentSuggested>(
				activity, 
				Constants.CAMPUS_FRAGMENT_SUGGESTED_TAG,
				CampusFragmentSuggested.class,
				fragment.getClass().equals(CampusFragmentSuggested.class) ? fragment : null));
		activity.getSupportActionBar().addTab(tab);
		if (R.string.campus_tab_suggested == toSelect) current = tab;

		// Communities
		tab = activity.getSupportActionBar().newTab();
		tab.setText(R.string.campus_tab_communities);
		tab.setTabListener(new TabListener<CampusFragmentCommunities>(
				activity, 
				Constants.CAMPUS_FRAGMENT_COMMUNITIES_TAG,
				CampusFragmentCommunities.class,
				fragment.getClass().equals(CampusFragmentCommunities.class) ? fragment : null));
		activity.getSupportActionBar().addTab(tab);
		if (R.string.campus_tab_communities == toSelect) current = tab;
		
		if (current != null) {
			activity.getSupportActionBar().selectTab(current);
		}

	}	

	/*public static void populateSharedContentActionBar(SherlockFragment fragment) {
		activity = fragment.getSherlockActivity();
		if (activity.getSupportActionBar() != null
				&& activity.getSupportActionBar().getTabCount() > 0
				&& activity.getSupportActionBar().getTabAt(0).getText()
						.equals(activity.getString(R.string.home_tab_me)))
			return;
		 activity.getSupportActionBar().removeAllTabs();
		String[] entries = {
				activity.getResources().getString(R.string.home_tab_me),
				activity.getResources().getString(R.string.home_tab_mygroups),
				activity.getResources().getString(
						R.string.home_tab_mycommunities) };
		ArrayAdapter<CharSequence> tabs = new ArrayAdapter<CharSequence>(
				activity, R.layout.sherlock_spinner_item, entries);
		activity.getSupportActionBar().setListNavigationCallbacks(tabs,
				new OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						FragmentTransaction fragmentTransaction;
						AbstractSharedContentFragment fragment;
						switch (itemPosition) {
						case 0:
							fragmentTransaction = activity
									.getSupportFragmentManager()
									.beginTransaction();
							fragment = new HomeFragmentMe();
							fragmentTransaction
									.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
							fragmentTransaction.replace(android.R.id.content,
									fragment, Constants.HOME_FRAGMENT_ME_TAG);
							fragmentTransaction.addToBackStack(fragment
									.getTag());
							fragmentTransaction.commit();
							return true;
						case 1:
							fragmentTransaction = activity
									.getSupportFragmentManager()
									.beginTransaction();
							fragment = new HomeFragmentMyGroups();
							fragmentTransaction
									.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
							fragmentTransaction.replace(android.R.id.content,
									fragment,
									Constants.HOME_FRAGMENT_MYGROUPS_TAG);
							fragmentTransaction.addToBackStack(fragment
									.getTag());
							fragmentTransaction.commit();
							return true;
						case 2:
							fragmentTransaction = activity
									.getSupportFragmentManager()
									.beginTransaction();
							fragment = new HomeFragmentMyCommunities();
							fragmentTransaction
									.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
							fragmentTransaction.replace(android.R.id.content,
									fragment,
									Constants.HOME_FRAGMENT_MYCOMMUNITIES_TAG);
							fragmentTransaction.addToBackStack(fragment
									.getTag());
							fragmentTransaction.commit();
							return true;
						default:
							break;
						}
						return false;
					}
				});
	}*/


}
