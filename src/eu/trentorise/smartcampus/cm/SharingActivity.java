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
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.sharing.ShareEntityObject;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.SourceSelectExpandableListAdapter;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.Community;
import eu.trentorise.smartcampus.cm.model.Group;
import eu.trentorise.smartcampus.cm.model.MinimalProfile;
import eu.trentorise.smartcampus.cm.model.Profile;
import eu.trentorise.smartcampus.cm.model.SimpleSocialContainer;
import eu.trentorise.smartcampus.cm.model.SocialContainer;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class SharingActivity extends BaseCMActivity {
//	
	private SourceSelectExpandableListAdapter adapter = null;
	private ShareEntityObject share = null;
	
	@Override
	protected void loadData(String token) {
		new SCAsyncTask<Void, Void, SocialContainer>(this, new LoadUserCompleteData(this)).execute();
	}

	@Override
	protected void setUpContent() {
		setContentView(R.layout.source_select);
		getSupportActionBar().setDisplayUseLogoEnabled(true);

		if (getIntent() != null) {
			share = (ShareEntityObject) getIntent().getSerializableExtra(getString(eu.trentorise.smartcampus.android.common.R.string.share_entity_arg_entity));
		}
		if (share != null) {
			setTitle("Sharing "+ share.getType() +"'"+share.getTitle()+"'");
		} else {
			CMHelper.showFailure(this, R.string.app_failure_operation);
			finish();
			return;
		}

		Button ok = (Button) findViewById(R.id.source_select_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<Group> groups = adapter.getGroups();
				List<MinimalProfile> users = adapter.getUsers();
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

	private class LoadUserCompleteData extends AbstractAsyncTaskProcessor<Void, SocialContainer> {

		public LoadUserCompleteData(Activity activity) {
			super(activity);
		}

		@Override
		public SocialContainer performAction(Void... params) throws SecurityException, Exception {
			SocialContainer container = new SimpleSocialContainer();
			
			List<Group> groups = CMHelper.readGroups();
			List<Group> custom = new ArrayList<Group>();
			Group allKnown = null;
			for (Group g : groups) {
				if (g.getName().equals(CMConstants.MY_PEOPLE_GROUP_NAME)) {
					allKnown = g;
				}
				custom.add(g);
			}
			container.setGroups(custom);
			Profile profile = CMHelper.retrieveProfile();
			container.setCommunities(profile.getCommunities());
			List<MinimalProfile> users = new ArrayList<MinimalProfile>();
			if (allKnown != null && allKnown.getUsers() != null) users.addAll(allKnown.getUsers());
			container.setUsers(users);
			return container;
		}

		@Override
		public void handleResult(final SocialContainer result) {
			adapter = new SourceSelectExpandableListAdapter(SharingActivity.this, result, null); 
			ExpandableListView view = (ExpandableListView)findViewById(R.id.source_select_list);
			view.setAdapter(adapter);

		}
		
	}
		
	private class ShareProcessor extends SCAsyncTask<SocialContainer, Void, Void> {

		public ShareProcessor() {
			super(SharingActivity.this, new AbstractAsyncTaskProcessor<SocialContainer, Void>(SharingActivity.this) {
				@Override
				public Void performAction(SocialContainer... params) throws SecurityException, Exception {
					CMHelper.share(share, params[0]);
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
