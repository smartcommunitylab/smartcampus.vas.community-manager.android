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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.cm.Constants;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.topics.CreateTopicActivity;
import eu.trentorise.smartcampus.cm.model.Topic;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class TopicAdapter extends ArrayAdapter<Topic> {

	private Activity context;
	private int layoutResourceId;

	public TopicAdapter(Activity context, int layoutResourceId) {
		super(context, layoutResourceId, new ArrayList<Topic>());
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		new SCAsyncTask<Void, Void, List<Topic>>(context,
				new LoadTopicProcessor(context)).execute();
	}

	@Override
	public void add(Topic object) {
		super.add(object);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.topic_action = (ImageButton) row
					.findViewById(R.id.topic_action);
			holder.topic_body = row.findViewById(R.id.topic_body);
			holder.topic_name = (TextView) row.findViewById(R.id.topic_name);
			holder.topic_description = (TextView) row
					.findViewById(R.id.topic_description);
			holder.topic_sources = (TextView) row
					.findViewById(R.id.topic_sources);
			row.setTag(holder);
		} else
			holder = (DataHolder) row.getTag();

		Topic topic = getItem(position);
		holder.topic_action.setOnClickListener(new TopicActionClickListener(
				topic));
		holder.topic_name.setText(topic.getName()); // name
		holder.topic_description.setText(topic.description());
		holder.topic_sources.setText(topic.sources());
		holder.topic_body.setOnClickListener(new TopicBodyClickListener(topic));

		return row;
	}

	static class DataHolder {
		ImageButton topic_action;
		TextView topic_name;
		TextView topic_description;
		TextView topic_sources;
		View topic_body;
	}

	class TopicActionClickListener implements OnClickListener {
		private Topic topic;

		public TopicActionClickListener(Topic topic) {
			this.topic = topic;
		}

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle("Topic deletion");
			builder.setMessage("Are you sure you want to delete this topic?")
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							})
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									new SCAsyncTask<Topic, Void, Topic>(
											context, new RemoveTopicProcessor(
													context)).execute(topic);
								}
							}).create().show();
		}
	}

	class TopicBodyClickListener implements OnClickListener {
		private Topic topic;

		public TopicBodyClickListener(Topic topic) {
			this.topic = topic;
		}

		@Override
		public void onClick(View v) {
			Intent i = new Intent(context, CreateTopicActivity.class);
			i.putExtra(context.getString(R.string.create_topic_arg_topic),
					topic);
			context.startActivityForResult(i, Constants.TOPIC_ACTIVITY_REQUEST);
		}
	}

	private class LoadTopicProcessor extends
			AbstractAsyncTaskProcessor<Void, List<Topic>> {

		public LoadTopicProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<Topic> performAction(Void... params)
				throws SecurityException, Exception {
			return CMHelper.getTopics();
		}

		@Override
		public void handleResult(List<Topic> result) {
			clear();
			if (result != null) {
				for (Topic t : result)
					add(t);
			}
			notifyDataSetChanged();
		}
	}

	private class RemoveTopicProcessor extends
			AbstractAsyncTaskProcessor<Topic, Topic> {

		public RemoveTopicProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Topic performAction(Topic... params) throws SecurityException,
				Exception {
			if (CMHelper.removeTopic(params[0].getId())) {
				return params[0];
			}
			return null;
		}

		@Override
		public void handleResult(Topic result) {
			if (result != null) {
				remove(result);
				notifyDataSetChanged();
			}

		}
	}
}
