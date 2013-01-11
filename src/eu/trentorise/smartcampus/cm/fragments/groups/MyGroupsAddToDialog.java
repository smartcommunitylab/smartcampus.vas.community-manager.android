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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.CheckListAdapter;
import eu.trentorise.smartcampus.cm.custom.CheckListAdapter.CheckListItem;
import eu.trentorise.smartcampus.cm.custom.DialogHandler;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.Group;

public class MyGroupsAddToDialog extends Dialog {

	private DialogHandler<Collection<Group>> groupAssignmentHandler = null;
	private CheckListAdapter listAdatpter = null;
	private Activity context = null;
	
	public MyGroupsAddToDialog(Activity context, DialogHandler<Collection<Group>> groupAssignmentHandler) {
		super(context);
		this.groupAssignmentHandler = groupAssignmentHandler;
		this.context = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checklist_dialog);

		final ListView checkListView = (ListView) findViewById(R.id.checklist_listView);

		setTitle(R.string.mygroups_addto_options_title);

		listAdatpter = new CheckListAdapter(context, getLayoutInflater(), R.layout.checklist_row);
		// adapter
		checkListView.setAdapter(listAdatpter);

		// buttons
		Button cancelBtn = (Button) findViewById(R.id.checklist_cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		Button okBtn = (Button) findViewById(R.id.checklist_ok);
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Set<String> selected = new HashSet<String>();
				for (int i = 0; i < checkListView.getChildCount(); i++) {
					View listChild = checkListView.getChildAt(i);
					CheckBox checkbox = (CheckBox) listChild.findViewById(R.id.checklist_checkBox);
					TextView textview = (TextView) listChild.findViewById(R.id.checklist_textView);
					if (checkbox.isChecked()) {
						selected.add(textview.getText().toString());
					}
				}
				if (selected.size() > 0) {
					List<Group> result = new ArrayList<Group>();
					for (Group g : CMHelper.getGroups()) {
						if (selected.contains(g.getName())) result.add(g); 
					}
					groupAssignmentHandler.handleSuccess(result);
				}
				
				dismiss();
			}
		});
		
		for (Group g : CMHelper.getGroups()) {
			CheckListItem item = new CheckListItem();
			item.text = g.getName();
			item.enabled = g.getSocialId() != CMConstants.MY_PEOPLE_GROUP_ID;
			item.checked = g.getSocialId() == CMConstants.MY_PEOPLE_GROUP_ID;
//			if (g.getSocialId() == CMConstants.MY_PEOPLE_GROUP_ID) continue;
			listAdatpter.add(item);
		}
		listAdatpter.notifyDataSetChanged();
	}

//	private class LoadGroupsProcessor extends AbstractAsyncTaskProcessor<Void, Collection<Group>> {
//
//		public LoadGroupsProcessor(Activity activity) {
//			super(activity);
//		}
//
//		@Override
//		public Collection<Group> performAction(Void... params) throws SecurityException, Exception {
//			return CMHelper.readGroups();
//		}
//
//		@Override
//		public void handleResult(Collection<Group> result) {
//			groups = result == null ? new ArrayList<Group>() : new ArrayList<Group>(result);
//			for (Group g : groups) listAdatpter.add(g.getName());
//			listAdatpter.notifyDataSetChanged();
//		}
//		
//	}
}
