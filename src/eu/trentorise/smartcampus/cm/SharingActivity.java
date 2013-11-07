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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.SourceSelectExpandableListAdapter;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.cm.model.SimpleSocialContainer;
import eu.trentorise.smartcampus.cm.model.SocialContainer;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.social.model.Community;
import eu.trentorise.smartcampus.social.model.Entity;
import eu.trentorise.smartcampus.social.model.Group;
import eu.trentorise.smartcampus.social.model.ShareVisibility;

public class SharingActivity extends BaseCMActivity {
//	
	private SourceSelectExpandableListAdapter adapter = null;
	private Entity share = null;
	
	@Override
	protected void loadData() {
		new SCAsyncTask<Void, Void, SocialContainer[]>(this, new LoadUserCompleteData(this)).execute();
	}

	private Entity getShare() {
		if (share == null) {
			if (getIntent() != null) {
				share = (Entity) getIntent().getSerializableExtra(getString(eu.trentorise.smartcampus.android.common.R.string.share_entity_arg_entity));
			}
		}
		return share;
	}
	
	@Override
	protected void setUpContent() {
		setContentView(R.layout.source_select);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		
		if (getShare() == null) {
			CMHelper.showFailure(this, R.string.app_failure_operation);
			finish();
			return;
		} else {
			setTitle("Sharing "+ share.getEntityType() +"'"+share.getTitle()+"'");
		}

		Button ok = (Button) findViewById(R.id.source_select_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<Group> groups = adapter.getGroups();
				List<PictureProfile> users = adapter.getUsers();
				List<Community> communities = adapter.getCommunities();
				SimpleSocialContainer userData = new SimpleSocialContainer(); 
				
				userData.setCommunities(communities);
				userData.setGroups(groups);
				userData.setUsers(users);
				
				CheckBox all = (CheckBox)findViewById(R.id.source_select_public);
				userData.setAllUsers(all.isChecked());
				new ShareProcessor().execute(userData);
			}
		});
		((Button) findViewById(R.id.source_select_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private class LoadUserCompleteData extends AbstractAsyncTaskProcessor<Void, SocialContainer[]> {

		public LoadUserCompleteData(Activity activity) {
			super(activity);
		}

		@Override
		public SocialContainer[] performAction(Void... params) throws SecurityException, Exception {
			SocialContainer complete = new SimpleSocialContainer();
			SocialContainer current = new SimpleSocialContainer();
			
			PictureProfile profile = CMHelper.getProfile();
			if (profile == null) profile = CMHelper.retrieveProfile();
			List<Group> groups = CMHelper.getGroups();
			
			ShareVisibility visibility = CMHelper.getEntitySharing(getShare().getEntityId());
			current.setAllUsers(visibility.isAllUsers());

			List<Group> completeGroups = new ArrayList<Group>();
			current.setGroups(new ArrayList<Group>());
//			Group allKnown = null;
			for (Group g : groups) {
//				if (g.getName().equals(CMConstants.MY_PEOPLE_GROUP_NAME)) {
//					allKnown = g;
//					if (visibility.isAllKnownUsers()) current.getGroups().add(g);
//				}
				completeGroups.add(g);
				if (visibility.getGroupIds() != null && visibility.getGroupIds().contains(g.getSocialId())) {
					current.getGroups().add(g);
				}
			}
			complete.setGroups(completeGroups);
			
			// CURRENTLY COMMUNITIES ARE NOT CONSIDERED
			// TODO change once the communities are re-integrated again
//			complete.setCommunities(profile.getCommunities());
//			current.setCommunities(new ArrayList<Community>());
//			for (Community c : complete.getCommunities()) {
//				if (visibility.getCommunityIds() != null && visibility.getCommunityIds().contains(c.getSocialId())) {
//					current.getCommunities().add(c);
//				}
//			}
			
//			List<PictureProfile> users = new ArrayList<PictureProfile>();
			List<PictureProfile> users = CMHelper.getKnownUsers();
			complete.setUsers(users);
			
			current.setUsers(new ArrayList<PictureProfile>());
			for (PictureProfile mp : complete.getUsers()) {
				if (visibility.getUserIds() != null && visibility.getUserIds().contains(mp.getSocialId())) {
					current.getUsers().add(mp);
				}
			}
			
			return new SocialContainer[]{complete,current};
		}

		@Override
		public void handleResult(SocialContainer[] result) {
			adapter = new SourceSelectExpandableListAdapter(SharingActivity.this, result[0], result[1]); 
			CheckBox all = (CheckBox)findViewById(R.id.source_select_public);
			all.setChecked(result[1].isAllUsers());

			ExpandableListView view = (ExpandableListView)findViewById(R.id.source_select_list);
			view.setAdapter(adapter);

		}
		
	}
		
	private class ShareProcessor extends SCAsyncTask<SocialContainer, Void, Void> {

		public ShareProcessor() {
			super(SharingActivity.this, new AbstractAsyncTaskProcessor<SocialContainer, Void>(SharingActivity.this) {
				@Override
				public Void performAction(SocialContainer... params) throws SecurityException, Exception {
					CMHelper.share(getShare(), params[0].toShareVisibility());
					return null;
				}

				@Override
				public void handleResult(Void result) {
					Toast.makeText(SharingActivity.this, R.string.sharing_success, Toast.LENGTH_SHORT).show();
					finish();
				}
				
			});
		}
		
	}
}
