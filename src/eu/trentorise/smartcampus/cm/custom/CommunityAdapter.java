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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.social.model.Community;
import eu.trentorise.smartcampus.social.model.Concept;

public class CommunityAdapter extends ArrayAdapter<Community> {

	private Activity context;
	private int layoutResourceId;
	private CommunityProvider provider;

	public CommunityAdapter(CommunityProvider provider, Activity context,
			int layoutResourceId) {
		super(context, layoutResourceId, new ArrayList<Community>());
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.provider = provider;
		new SCAsyncTask<Community, Void, List<Community>>(context,
				new LoadCommunityProcessor(context)).execute();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.community_action = (ImageButton) row
					.findViewById(R.id.community_action);
			holder.community_name = (TextView) row
					.findViewById(R.id.community_name);
			holder.community_tags = (TextView) row
					.findViewById(R.id.community_tags);
			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		Community comm = getItem(position);
		holder.community_action
				.setOnClickListener(new CommunityActionClickListener(comm));
		// holder.community_action.setText(context.getResources().getString(
		// provider.getCommunityActionResource()));
		if (provider.getCommunityActionResource() == R.string.myprofile_community_remove) {
			holder.community_action.setImageResource(R.drawable.ic_remove);
		} else if (provider.getCommunityActionResource() == R.string.community_action_add) {
			holder.community_action.setImageResource(R.drawable.ic_add_gray); // gray
		}

		holder.community_name.setText(comm.getName()); // name
		holder.community_tags.setText(createTagsString(comm.getTags()));

		return row;
	}

	private CharSequence createTagsString(List<Concept> tags) {
		if (tags == null || tags.size() == 0)
			return "";
		String res = "";
		for (int i = 0; i < tags.size(); i++) {
			res += tags.get(i).getName();
			if (i < tags.size() - 1)
				res += ", ";
		}
		return res;
	}

	static class DataHolder {
		ImageButton community_action;
		TextView community_name;
		TextView community_tags;
	}

	class CommunityActionClickListener implements OnClickListener {
		private Community comm;

		public CommunityActionClickListener(Community comm) {
			this.comm = comm;
		}

		@Override
		public void onClick(View v) {
			new SCAsyncTask<Community, Void, List<Community>>(context,
					new LoadCommunityProcessor(context)).execute(comm);
		}
	}

	public class LoadCommunityProcessor extends
			AbstractAsyncTaskProcessor<Community, List<Community>> {

		public LoadCommunityProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<Community> performAction(Community... params)
				throws SecurityException, Exception {
			if (params != null && params.length > 0 && params[0] != null) {
				provider.performCommunityAction(params[0]);
			}
			return provider.getCommunities();
		}

		@Override
		public void handleResult(List<Community> result) {
			clear();
			if (result != null) {
				for (Community c : result)
					add(c);
			}
			notifyDataSetChanged();
			eu.trentorise.smartcampus.cm.custom.ViewHelper
					.removeEmptyListView((LinearLayout) provider
							.getContainerView());
			if (result == null || result.isEmpty()) {
				eu.trentorise.smartcampus.cm.custom.ViewHelper
						.addEmptyListView(
								(LinearLayout) provider.getContainerView(),
								R.string.content_empty);
			}

		}
	}

	public interface CommunityProvider {
		List<Community> getCommunities() throws SecurityException, Exception;

		void performCommunityAction(Community community)
				throws SecurityException, Exception;

		int getCommunityActionResource();

		View getContainerView();
	}

}
