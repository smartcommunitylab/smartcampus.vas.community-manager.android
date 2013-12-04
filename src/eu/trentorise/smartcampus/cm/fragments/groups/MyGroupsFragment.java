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
package eu.trentorise.smartcampus.cm.fragments.groups;

import java.util.Collection;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.CustomSpinnerAdapter;
import eu.trentorise.smartcampus.cm.custom.DialogHandler;
import eu.trentorise.smartcampus.cm.custom.UsersPictureProfileAdapter;
import eu.trentorise.smartcampus.cm.custom.UsersPictureProfileAdapter.UserOptionsHandler;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.campus.CampusFragmentPeople;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.social.model.Group;
import eu.trentorise.smartcampus.social.model.User;

public class MyGroupsFragment extends SherlockFragment {

	private ArrayAdapter<PictureProfile> usersListAdapter;
	private Spinner myGroupsSpinner;

	private ArrayAdapter<String> dataAdapter;

	private Group selected = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
		}

		return inflater.inflate(R.layout.users, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setTitle(
				R.string.groups_title);

		// dataAdapter = new ArrayAdapter<String>(getActivity(),
		// android.R.layout.simple_spinner_item);
		dataAdapter = new CustomSpinnerAdapter<String>(getActivity());
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		myGroupsSpinner = (Spinner) getView().findViewById(
				R.id.user_spinner_mygroups);
		myGroupsSpinner.setAdapter(dataAdapter);

		ListView usersListView = (ListView) getView().findViewById(
				R.id.users_listview);
		usersListAdapter = new UsersPictureProfileAdapter(
				getSherlockActivity(), R.layout.user_mp,
				new MyGroupsUserOptionsHandler(), null);
		usersListView.setAdapter(usersListAdapter);

		update(CMHelper.getGroups());

		myGroupsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				selected = null;
				try {
					selected = CMHelper.getGroups().get(position);
				} catch (Exception e) {
					e.printStackTrace();
				}
				updateUserList(selected);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

	}

	/*
	 * @Override public void onPrepareOptionsMenu(Menu menu) { menu.clear(); //
	 * MenuInflater inflater = getSherlockActivity().getSupportMenuInflater();
	 * // inflater.inflate(R.menu.gripmenu, menu);
	 * 
	 * MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_add, 1,
	 * R.string.mygroups_add); item.setIcon(R.drawable.ic_action_bar_add);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	 * 
	 * if (selected == null && CMHelper.getGroups() != null &&
	 * CMHelper.getGroups().size() > 0) selected = CMHelper.getGroups().get(0);
	 * if (selected != null &&
	 * !selected.getName().equals(CMConstants.MY_PEOPLE_GROUP_NAME)) { item =
	 * menu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_delete, 2,
	 * R.string.mygroups_delete_title);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); item =
	 * menu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_rename, 3,
	 * R.string.mygroups_rename_title);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); }
	 * 
	 * super.onPrepareOptionsMenu(menu); }
	 */

	// fixed menu for consistency with other apps
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.gripmenu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_add, Menu.NONE,
				R.string.mygroups_add_title);
		if (selected == null && CMHelper.getGroups() != null
				&& CMHelper.getGroups().size() > 0)
			selected = CMHelper.getGroups().get(0);
		if (selected != null
				&& !selected.getName().equals(CMConstants.MY_PEOPLE_GROUP_NAME)) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_rename, 3,
					R.string.mygroups_rename_title);
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_delete, 2,
					R.string.mygroups_delete_title);
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.mygroups_add_person,
					Menu.NONE, R.string.mygroups_add_person_title);
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mygroups_add:
			Dialog dialog = new MyGroupsAddDialog(getActivity(),
					new DialogHandler<String>() {
						@Override
						public void handleSuccess(String result) {
							new SCAsyncTask<String, Void, Collection<Group>>(
									getActivity(), new SaveGroupProcessor(
											getActivity())).execute(result);
						}
					}, null);
			dialog.setTitle(R.string.mygroups_add_title);
			dialog.show();
			return true;
		case R.id.mygroups_add_person:
			FragmentTransaction ft = getSherlockActivity()
					.getSupportFragmentManager().beginTransaction();
			Fragment fragment = new CampusFragmentPeople();
			// Replacing old fragment with new one
			ft.replace(R.id.content_frame, fragment);
			fragment.setArguments(CampusFragmentPeople.prepareArgs(selected
					.getSocialId()));
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(null);
			ft.commit();

			return true;
		case R.id.mygroups_delete:
			new SCAsyncTask<Group, Void, Collection<Group>>(getActivity(),
					new DeleteGroupProcessor(getActivity())).execute(selected);
			return true;
		case R.id.mygroups_rename:
			dialog = new MyGroupsAddDialog(getActivity(),
					new DialogHandler<String>() {
						@Override
						public void handleSuccess(String result) {
							new SCAsyncTask<String, Void, Collection<Group>>(
									getActivity(), new SaveGroupProcessor(
											getActivity(), selected))
									.execute(result);
						}
					}, selected);
			dialog.setTitle(R.string.mygroups_rename_title);
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class SaveGroupProcessor extends
			AbstractAsyncTaskProcessor<String, Collection<Group>> {

		private Group group;

		public SaveGroupProcessor(Activity activity) {
			super(activity);
			this.group = new Group();
		}

		public SaveGroupProcessor(Activity activity, Group group) {
			super(activity);
			this.group = group;
		}

		@Override
		public Collection<Group> performAction(String... params)
				throws SecurityException, Exception {
			Group newGroup = new Group();
			newGroup.setSocialId(group.getSocialId());
			newGroup.setName(params[0]);
			newGroup.setUsers(group.getUsers());

			CMHelper.saveGroup(newGroup);
			group.setName(newGroup.getName());
			return CMHelper.getGroups();
		}

		@Override
		public void handleResult(Collection<Group> result) {
			update(result);
		}
	}

	public class DeleteGroupProcessor extends
			AbstractAsyncTaskProcessor<Group, Collection<Group>> {

		public DeleteGroupProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Collection<Group> performAction(Group... params)
				throws SecurityException, Exception {
			CMHelper.deleteGroup(params[0]);
			return CMHelper.getGroups();
		}

		@Override
		public void handleResult(Collection<Group> result) {
			selected = null;
			update(result);
		}
	}

	private void update(Collection<Group> result) {
		// Group selected = result == null || result.isEmpty() ? null :
		// result.iterator().next();

		dataAdapter.clear();

		for (Group temp : CMHelper.getGroups()) {
			dataAdapter.add(temp.getName());
		}
		dataAdapter.notifyDataSetChanged();

		updateUserList(selected);
	}

	private void updateUserList(Group selected) {
		if (selected == null) {
			myGroupsSpinner.setSelection(0);
			if (CMHelper.getGroups().size() > 0) {
				selected = CMHelper.getGroups().get(0);
			}
		}
		usersListAdapter.clear();
		if (selected != null && selected.getUsers() != null) {
			for (User user : selected.getUsers()) {
				usersListAdapter.add(CMHelper.getPictureProfile(user
						.getSocialId()));
			}
			usersListAdapter.notifyDataSetChanged();
		}
		getSherlockActivity().invalidateOptionsMenu();
	}

	private class MyGroupsUserOptionsHandler implements UserOptionsHandler {
		@Override
		public void assignUserToGroups(PictureProfile user,
				Collection<Group> groups) {
			new SCAsyncTask<Object, Void, PictureProfile>(getActivity(),
					new AssignToGroups(getActivity())).execute(user, groups);
		}
	}

	private class AssignToGroups extends
			AbstractAsyncTaskProcessor<Object, PictureProfile> {

		public AssignToGroups(Activity activity) {
			super(activity);
		}

		@SuppressWarnings("unchecked")
		@Override
		public PictureProfile performAction(Object... params)
				throws SecurityException, Exception {
			CMHelper.assignToGroups((PictureProfile) params[0],
					(Collection<Group>) params[1]);
			return (PictureProfile) params[0];
		}

		@Override
		public void handleResult(PictureProfile result) {
			Group group = CMHelper.getGroups().get(
					myGroupsSpinner.getSelectedItemPosition());
			updateUserList(group);
		}

	}

}
