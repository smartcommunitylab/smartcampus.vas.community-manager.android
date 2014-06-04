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
package eu.trentorise.smartcampus.cm.custom;

import it.smartcampuslab.cm.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.cm.model.SocialContainer;
import eu.trentorise.smartcampus.socialservice.beans.Community;
import eu.trentorise.smartcampus.socialservice.beans.Group;

public class SourceSelectExpandableListAdapter extends
		BaseExpandableListAdapter {
	private static final String GROUP_GROUPS = "Groups";
	private static final Object GROUP_PEOPLE = "People";

	private SocialContainer completeData;

	@SuppressWarnings("unchecked")
	private Set<Integer>[] checked = new Set[] { new HashSet<Integer>(),
			new HashSet<Integer>() };

	private Context ctx;

	public SourceSelectExpandableListAdapter(Context ctx,
			SocialContainer completeData, SocialContainer userData) {
		super();
		this.completeData = completeData;
		this.ctx = ctx;
		if (userData != null) {
			update(userData);
		}
	}

	public List<Group> getGroups() {
		List<Group> list = new ArrayList<Group>();
		for (Integer item : checked[0])
			list.add(completeData.getGroups().get(item));
		return list;
	}

	public List<PictureProfile> getUsers() {
		List<PictureProfile> list = new ArrayList<PictureProfile>();
		for (Integer item : checked[1])
			list.add(completeData.getUsers().get(item));
		return list;
	}

	public List<Community> getCommunities() {
		return completeData.getCommunities();
	}

	private void update(SocialContainer userData) {
		if (userData != null) {
			if (userData.getGroups() != null
					&& completeData.getGroups() != null) {
				for (int i = 0; i < completeData.getGroups().size(); i++) {
					for (Group g : userData.getGroups()) {
						if (completeData.getGroups().get(i).getId()
								.equals(g.getId()))
							checked[0].add(i);
					}
				}
			}
			if (userData.getUsers() != null && completeData.getUsers() != null) {
				for (int i = 0; i < completeData.getUsers().size(); i++) {
					for (PictureProfile p : userData.getUsers()) {
						if (completeData.getUsers().get(i).getUserId()
								.equals(p.getUserId()))
							checked[1].add(i);
					}
				}
			}
		}
	}

	@Override
	public int getGroupCount() {
		return 2;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		switch (groupPosition) {
		case 0:
			return completeData.getGroups() != null ? completeData.getGroups()
					.size() : 0;
		case 1:
			return completeData.getUsers() != null ? completeData.getUsers()
					.size() : 0;
		default:
			return 0;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		switch (groupPosition) {
		case 0:
			return GROUP_GROUPS;
		case 1:
			return GROUP_PEOPLE;
		default:
			return 0;
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		switch (groupPosition) {
		case 0:
			return completeData.getGroups() != null ? completeData.getGroups()
					.get(childPosition).getName() : null;
		case 1:
			return completeData.getUsers() != null ? completeData.getUsers()
					.get(childPosition).fullName() : null;
		default:
			return null;
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	private static final int[] EMPTY_STATE_SET = {};
	private static final int[] GROUP_EXPANDED_STATE_SET = { android.R.attr.state_expanded };
	private static final int[][] GROUP_STATE_SETS = { EMPTY_STATE_SET, // 0
			GROUP_EXPANDED_STATE_SET // 1
	};

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.text_row, parent, false);
		((TextView) view.findViewById(R.id.textlist_textView))
				.setText(getGroup(groupPosition).toString());

		ImageView indicator = (ImageView) view
				.findViewById(R.id.explist_indicator);

		if (getChildrenCount(groupPosition) == 0)
			indicator.setVisibility(View.INVISIBLE);
		else {
			indicator.setVisibility(View.VISIBLE);
			int stateSetIndex = (isExpanded ? 1 : 0);
			Drawable drawable = indicator.getDrawable();
			drawable.setState(GROUP_STATE_SETS[stateSetIndex]);
		}

		return view;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.checklist_row, parent, false);
		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.checklist_checkBox);
		checkBox.setChecked(checked[groupPosition].contains(childPosition));
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked)
					checked[groupPosition].add(childPosition);
				else
					checked[groupPosition].remove(childPosition);
			}
		});
		((TextView) view.findViewById(R.id.checklist_textView))
				.setText(getChild(groupPosition, childPosition).toString());
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
