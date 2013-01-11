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
package eu.trentorise.smartcampus.cm.fragments.campus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.UsersMinimalProfileAdapter;
import eu.trentorise.smartcampus.cm.custom.UsersMinimalProfileAdapter.UserOptionsHandler;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.AbstractTabbedFragment;
import eu.trentorise.smartcampus.cm.fragments.ActionBarHelper;
import eu.trentorise.smartcampus.cm.model.Group;
import eu.trentorise.smartcampus.cm.model.MinimalProfile;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CampusFragmentPeople extends AbstractTabbedFragment {

	ArrayAdapter<MinimalProfile> usersListAdapter;
	List<MinimalProfile> usersList = new ArrayList<MinimalProfile>();

	@Override
	protected int getLayoutId() {
		return R.layout.people;
	}

	@Override
	public void onStart() {
		super.onStart();
		ActionBarHelper.populateCampusActionBar(this);

		ImageButton search = (ImageButton) getView().findViewById(R.id.people_search_img);
		search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new SCAsyncTask<String, Void, List<MinimalProfile>>(getActivity(), new LoadUserProcessor(getActivity())).execute(((EditText)getView().findViewById(R.id.people_search)).getText().toString());
			}
		});
		
		ListView usersListView = (ListView) getView().findViewById(R.id.people_listview);
		usersListAdapter = new UsersMinimalProfileAdapter(getActivity(), R.layout.user_mp, new PeopleUserOptionsHandler());
		usersListView.setAdapter(usersListAdapter);
		super.onStart();
	}

	private class LoadUserProcessor extends AbstractAsyncTaskProcessor<String, List<MinimalProfile>> {

		public LoadUserProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<MinimalProfile> performAction(String... params) throws SecurityException, Exception {
			return CMHelper.getPeople(params[0]);
		}

		@Override
		public void handleResult(List<MinimalProfile> result) {
			usersListAdapter.clear();
			if (result != null) {
				for (MinimalProfile mp : result) usersListAdapter.add(mp);
			}
			usersListAdapter.notifyDataSetChanged();
			
			eu.trentorise.smartcampus.cm.custom.ViewHelper.removeEmptyListView((LinearLayout)getView().findViewById(R.id.layout_people));
			if (result == null || result.isEmpty()) {
				eu.trentorise.smartcampus.cm.custom.ViewHelper.addEmptyListView((LinearLayout)getView().findViewById(R.id.layout_people), R.string.people_list_empty);
			}
		}

	}
	
	private class PeopleUserOptionsHandler implements UserOptionsHandler {

		@Override
		public void handleRemoveFromKnown(MinimalProfile user) {
			new SCAsyncTask<MinimalProfile, Void, MinimalProfile>(getActivity(), new RemoveFromKnown(getActivity())).execute(user);
		}

		@Override
		public void assignUserToGroups(MinimalProfile user, Collection<Group> groups) {
			new SCAsyncTask<Object, Void, MinimalProfile>(getActivity(), new AssignToGroups(getActivity())).execute(user,groups);
		}
		
	}

	private class AssignToGroups extends AbstractAsyncTaskProcessor<Object, MinimalProfile> {

		public AssignToGroups(Activity activity) {
			super(activity);
		}

		@SuppressWarnings("unchecked")
		@Override
		public MinimalProfile performAction(Object... params) throws SecurityException, Exception {
			if (CMHelper.assignToGroups((MinimalProfile)params[0], (Collection<Group>)params[1]))  {
				return (MinimalProfile)params[0];
			}
			return null;
		}

		@Override
		public void handleResult(MinimalProfile result) {
			if (result != null) {
				result.setKnown(true);
				usersListAdapter.notifyDataSetChanged();
			}
		}
		
	}

	
	private class RemoveFromKnown extends AbstractAsyncTaskProcessor<MinimalProfile, MinimalProfile> {

		public RemoveFromKnown(Activity activity) {
			super(activity);
		}

		@Override
		public MinimalProfile performAction(MinimalProfile... params) throws SecurityException, Exception {
			CMHelper.removeFromKnown(params[0]);
			return params[0];
		}

		@Override
		public void handleResult(MinimalProfile result) {
			result.setKnown(false);
			usersListAdapter.notifyDataSetChanged();
		}
	}
}
