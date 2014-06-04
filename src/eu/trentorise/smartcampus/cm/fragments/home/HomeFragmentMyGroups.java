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
package eu.trentorise.smartcampus.cm.fragments.home;

import it.smartcampuslab.cm.R;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.view.ViewHelper;
import eu.trentorise.smartcampus.cm.custom.CustomSpinnerAdapter;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.socialservice.beans.Entity;
import eu.trentorise.smartcampus.socialservice.beans.Group;
import eu.trentorise.smartcampus.socialservice.beans.Visibility;

public class HomeFragmentMyGroups extends AbstractSharedContentFragment {

	private static final int MENU_ITEM_APP = 1;
	private static final int MENU_ITEM_AUTHOR = 2;

	private Spinner myGroupsSpinner;
	private ArrayAdapter<String> dataAdapter;

	Group selected = null;

	@Override
	protected int getLayoutId() {
		return R.layout.shared_content_groups;
	}

	@Override
	public void onStart() {
		super.onStart();

		myGroupsSpinner = (Spinner) getView().findViewById(
				R.id.shared_content_groups_spinner);
		if (dataAdapter == null) {
			// dataAdapter = new ArrayAdapter<String>(getActivity(),
			// android.R.layout.simple_spinner_item);
			dataAdapter = new CustomSpinnerAdapter<String>(getActivity());
			dataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			update(dataAdapter);
		}
		myGroupsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (selected == null
						|| selected.getId() != CMHelper.getGroups()
								.get(position).getId()) {
					populateContentRequest();
					restartContentRequest();
					load();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		myGroupsSpinner.setAdapter(dataAdapter);

	}

	private void update(ArrayAdapter<String> adapter) {
		adapter.clear();
		if (CMHelper.getProfile() != null && CMHelper.getGroups() != null) {
			for (Group temp : CMHelper.getGroups()) {
				adapter.add(temp.getName());
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void populateContentRequest() {
		Visibility vis = new Visibility();
		vis.setGroups(new ArrayList<String>());
		if (CMHelper.getGroups() != null && !CMHelper.getGroups().isEmpty()) {
			if (myGroupsSpinner != null
					&& !myGroupsSpinner.getAdapter().isEmpty()) {
				selected = CMHelper.getGroups().get(
						myGroupsSpinner.getSelectedItemPosition());
			} else {
				selected = CMHelper.getGroups().get(0);
			}
			vis.getGroups().add(selected.getId());
		}
		contentRequest.visibility = vis;
	}

	@Override
	protected boolean handleMenuItem(Entity content, int itemId) {
		switch (itemId) {
		case MENU_ITEM_APP:
			ViewHelper.viewInApp(getActivity(),
					CMConstants.getTypeByTypeId(content.getType()),
					content.getUri(), new Bundle());
			return true;
		case MENU_ITEM_AUTHOR:
			Toast.makeText(getActivity(),
					"Showing author: " + content.getOwner(), Toast.LENGTH_SHORT)
					.show();
			return true;
		default:
			return super.handleMenuItem(content, itemId);
		}
	}

	@Override
	protected void populateMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		menu.add(0, MENU_ITEM_APP, 0, R.string.shared_content_menu_app);
	}

}
