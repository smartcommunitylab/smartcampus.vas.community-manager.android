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

import java.util.ArrayList;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.view.ViewHelper;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.Community;
import eu.trentorise.smartcampus.cm.model.ShareVisibility;
import eu.trentorise.smartcampus.cm.model.SharedContent;

public class HomeFragmentMyCommunities extends AbstractSharedContentFragment {

	private static final int MENU_ITEM_APP = 1;
	private static final int MENU_ITEM_TOPIC = 2;

	Community selected = null;
	
	@Override
	protected int getLayoutId() {
		return R.layout.shared_content_communities;
	}
	
	@Override
	protected void populateContentRequest() {
		ShareVisibility vis = new ShareVisibility();
		vis.setCommunityIds(new ArrayList<Long>());
		selected = CMHelper.getSCCommunity();
		vis.getCommunityIds().add(selected.getSocialId());
		contentRequest.visibility = vis;
	}

	@Override
	protected boolean handleMenuItem(SharedContent content, int itemId) {
		switch (itemId) {
		case MENU_ITEM_APP:
			ViewHelper.viewInApp(getActivity(), content.getEntityType(), content.getEntityId(), new Bundle());
			return true;
		case MENU_ITEM_TOPIC:
			FollowEntityObject obj = new FollowEntityObject(content.getEntityId(), content.getTitle(), content.getEntityType());
			FollowHelper.follow(getActivity(), obj);
			return true;
		default:
			return super.handleMenuItem(content, itemId);
		}
	}

	@Override
	protected void populateMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		menu.add(0, MENU_ITEM_APP, 0, R.string.shared_content_menu_app);
		menu.add(0, MENU_ITEM_TOPIC, 0, R.string.shared_content_menu_topic);
	}

}
