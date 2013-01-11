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
import java.util.List;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.CommunityAdapter;
import eu.trentorise.smartcampus.cm.custom.CommunityAdapter.CommunityProvider;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.AbstractTabbedFragment;
import eu.trentorise.smartcampus.cm.fragments.ActionBarHelper;
import eu.trentorise.smartcampus.cm.model.Community;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CampusFragmentCommunities extends AbstractTabbedFragment {

	ArrayAdapter<Community> communityListAdapter;

	@Override
	protected int getLayoutId() {
		return R.layout.contents;
	}

	@Override
	public void onResume() {
		super.onResume();
		ActionBarHelper.populateCampusActionBar(this);

		// hack when coming from profile
		getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(2);
	}
	@Override
	public void onStart() {
		super.onStart();

		ListView usersListView = (ListView) getView().findViewById(R.id.content_listview);
		communityListAdapter = new CommunityAdapter(new CampusCommunityProvider(), getSherlockActivity(), R.layout.community);
		usersListView.setAdapter(communityListAdapter);
	}
	
	private class CampusCommunityProvider implements CommunityProvider {

		@Override
		public List<Community> getCommunities() throws SecurityException, Exception {
				return new ArrayList<Community>(CMHelper.getCommunities());
		}

		@Override
		public void performCommunityAction(Community community) throws SecurityException, Exception {
			if (CMHelper.addToCommunity(community.getId())) {
				List<Community> list = CMHelper.getProfile().getCommunities();
				if (list == null) list = new ArrayList<Community>();
				list.add(community);
				CMHelper.getProfile().setCommunities(list);
			}
		}

		@Override
		public int getCommunityActionResource() {
			return R.string.community_action_add;
		}

		@Override
		public View getContainerView() {
			return getView().findViewById(R.id.content);
		}
		
	}
}
