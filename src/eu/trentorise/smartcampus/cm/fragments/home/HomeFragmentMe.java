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
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.view.ViewHelper;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.SharedContentsAdapter;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.ShareVisibility;
import eu.trentorise.smartcampus.cm.model.SharedContent;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class HomeFragmentMe extends AbstractSharedContentFragment {

	
	private static final int MENU_ITEM_APP = 1;
	private static final int MENU_ITEM_SHARING = 2;

	@Override
	protected int getLayoutId() {
		return R.layout.shared_content_myobjects;
	}

	@Override
	protected void populateContentRequest() {
		ShareVisibility vis = new ShareVisibility();
		vis.setUserIds(new ArrayList<Long>());
		vis.getUserIds().add(CMHelper.getProfile().getSocialId());
		contentRequest.visibility = vis;
	}

	@Override
	protected LoadObjectProcessor getLoadProcessor() {
		return new LoadMyObjectsProcessor(getActivity(), adapter);
	}


	private class LoadMyObjectsProcessor extends LoadObjectProcessor {

		public LoadMyObjectsProcessor(Activity activity, SharedContentsAdapter adapter) {
			super(activity, adapter);
		}

		@Override
		public List<SharedContent> performAction(ContentRequest... params) throws SecurityException, Exception {
			return CMHelper.readMyObjects(params[0].position, params[0].size, params[0].type);
		}

	}

	@Override
	protected boolean handleMenuItem(SharedContent content, int itemId) {
		switch (itemId) {
		case MENU_ITEM_APP:
			ViewHelper.viewInApp(getActivity(), content.getEntityType(), content.getEntityId(), new Bundle());
			return true;
//		case MENU_ITEM_SHARING:
//			Toast.makeText(getActivity(), "Sharing options: "+content.getEntityId(), Toast.LENGTH_SHORT).show();
//			return true;
		default:
			return super.handleMenuItem(content, itemId);
		}
	}

	@Override
	protected void populateMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		menu.add(0, MENU_ITEM_APP, 0, R.string.shared_content_menu_app);
//		menu.add(0, MENU_ITEM_SHARING, 0, R.string.shared_content_menu_sharing);
	}

}
