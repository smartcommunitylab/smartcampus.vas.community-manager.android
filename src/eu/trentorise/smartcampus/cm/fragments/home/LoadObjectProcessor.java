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

import java.util.List;

import android.app.Activity;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.SharedContentsAdapter;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.home.LoadObjectProcessor.ContentRequest;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.social.model.Entity;
import eu.trentorise.smartcampus.social.model.ShareVisibility;

public class LoadObjectProcessor extends AbstractAsyncTaskProcessor<ContentRequest, List<Entity>> {

	private SharedContentsAdapter adapter;

	public LoadObjectProcessor(Activity activity, SharedContentsAdapter adapter) {
		super(activity);
		this.adapter = adapter;
	}

	@Override
	public List<Entity> performAction(ContentRequest... params) throws SecurityException, Exception {
		return CMHelper.readSharedObjects(params[0].visibility, params[0].position, params[0].size, params[0].type);
	}

	@Override
	public void handleResult(List<Entity> result) {
		if (result != null) {
			for (Entity shared: result) {
				adapter.add(shared);
			}
		}
		adapter.notifyDataSetChanged();
	}

	public static class ContentRequest {
		public ShareVisibility visibility = null;
		public int position = 0;
		public int size = 20;
		public String type = null;
	}
		

}
