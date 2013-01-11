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
package eu.trentorise.smartcampus.cm.fragments.topics;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.DialogHandler;
import eu.trentorise.smartcampus.cm.custom.SourceSelectExpandableListAdapter;
import eu.trentorise.smartcampus.cm.model.Community;
import eu.trentorise.smartcampus.cm.model.Group;
import eu.trentorise.smartcampus.cm.model.MinimalProfile;
import eu.trentorise.smartcampus.cm.model.SocialContainer;

public class SourceSelectDialog extends Dialog {


	private DialogHandler<SocialContainer> handler = null;
	private SocialContainer userData;
	private SocialContainer completeData;
	
	public SourceSelectDialog(Context context, DialogHandler<SocialContainer> handler, SocialContainer completeData, SocialContainer userData) {
		super(context);
		this.handler = handler;
		this.completeData = completeData;
		this.userData = userData;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_select);
		setTitle(R.string.source_select_title);
		
		final SourceSelectExpandableListAdapter adapter = new SourceSelectExpandableListAdapter(getContext(), completeData, userData); 
		ExpandableListView view = (ExpandableListView)findViewById(R.id.source_select_list);
		view.setAdapter(adapter);

		CheckBox all = (CheckBox)findViewById(R.id.source_select_public);
		all.setChecked(userData != null && userData.isAllUsers());

		Button ok = (Button) findViewById(R.id.source_select_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<Group> groups = adapter.getGroups();
				List<MinimalProfile> users = adapter.getUsers();
				List<Community> communities = adapter.getCommunities();
				userData.setCommunities(communities);
				userData.setGroups(groups);
				userData.setUsers(users);
				
				CheckBox all = (CheckBox)findViewById(R.id.source_select_public);
				userData.setAllUsers(all.isChecked());
				userData.setAllCommunities(all.isChecked());
				
				handler.handleSuccess(userData);
				dismiss();
			}
		});
		((Button) findViewById(R.id.source_select_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
	}
}
