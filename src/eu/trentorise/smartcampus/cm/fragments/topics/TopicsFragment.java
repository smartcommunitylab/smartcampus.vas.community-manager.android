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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.cm.Constants;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.TopicAdapter;
import eu.trentorise.smartcampus.cm.model.Topic;

public class TopicsFragment extends SherlockFragment {

	private TopicAdapter topicListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		return inflater.inflate(R.layout.contents, container, false);
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.mytopics_add, 1, R.string.mytopics_add);
		item.setIcon(R.drawable.ic_action_bar_add);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mytopics_add:
			Intent i = new Intent(getActivity(),CreateTopicActivity.class);
			startActivityForResult(i, Constants.TOPIC_ACTIVITY_REQUEST);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}	
	}

	@Override
	public void onStart() {
		super.onStart();

		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(R.string.topics_title);

		ListView topicListView = (ListView) getView().findViewById(R.id.content_listview);
		topicListAdapter = new TopicAdapter(getSherlockActivity(), R.layout.topic);
		topicListView.setAdapter(topicListAdapter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		if (requestCode == Constants.TOPIC_ACTIVITY_REQUEST && resultCode == CreateTopicActivity.RESULT_OK) {
			Topic topic = (Topic)result.getSerializableExtra(getString(R.string.create_topic_arg_topic));
			for (int i = 0; i < topicListAdapter.getCount(); i++) {
				if (topicListAdapter.getItem(i).getId().equals(topic.getId())) {
					topicListAdapter.remove(topicListAdapter.getItem(i));
					break;
				}
			} 
			topicListAdapter.add(topic);
			topicListAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, result);
	}

	
}
