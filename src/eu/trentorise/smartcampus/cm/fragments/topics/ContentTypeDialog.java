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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.DialogHandler;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.Topic;

public class ContentTypeDialog extends Dialog {

	private DialogHandler<Topic> handler = null;
	private Topic topic;
	
	public ContentTypeDialog(Context context, DialogHandler<Topic> handler, Topic topic) {
		super(context);
		this.handler = handler;
		this.topic = topic;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_topic_content_dialog);

		final CheckBox all = (CheckBox)findViewById(R.id.topic_content_dialog_all);
		all.setOnCheckedChangeListener(new OnAllCheckedListener());
		
		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) all.setChecked(false);
			}
		};
		((CheckBox)findViewById(R.id.topic_content_dialog_pois)).setOnCheckedChangeListener(listener);
		((CheckBox)findViewById(R.id.topic_content_dialog_events)).setOnCheckedChangeListener(listener);
		((CheckBox)findViewById(R.id.topic_content_dialog_portfolios)).setOnCheckedChangeListener(listener);
		((CheckBox)findViewById(R.id.topic_content_dialog_photo_video)).setOnCheckedChangeListener(listener);
		((CheckBox)findViewById(R.id.topic_content_dialog_exps)).setOnCheckedChangeListener(listener);
		((CheckBox)findViewById(R.id.topic_content_dialog_journeys)).setOnCheckedChangeListener(listener);
		((CheckBox)findViewById(R.id.topic_content_dialog_stories)).setOnCheckedChangeListener(listener);

		
		if (topic != null && topic.getContentTypes() != null) {
			HashSet<String> selected = new HashSet<String>(topic.getContentTypes());
			((CheckBox)findViewById(R.id.topic_content_dialog_pois)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_POI)));
			((CheckBox)findViewById(R.id.topic_content_dialog_events)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_EVENT)));
			((CheckBox)findViewById(R.id.topic_content_dialog_portfolios)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_PORTFOLIO)));
			((CheckBox)findViewById(R.id.topic_content_dialog_photo_video)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_PHOTO_VIDEO)));
			((CheckBox)findViewById(R.id.topic_content_dialog_exps)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_EXPERIENCE)));
			((CheckBox)findViewById(R.id.topic_content_dialog_journeys)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_JOURNEY)));
			((CheckBox)findViewById(R.id.topic_content_dialog_stories)).setChecked(selected.contains(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_STORY)));
		}

		Button cancelBtn = (Button) findViewById(R.id.topic_content_dialog_cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		Button okBtn = (Button) findViewById(R.id.topic_content_dialog_ok);
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<String> typeList = new ArrayList<String>();

				if (all.isChecked()) {
					typeList = CMConstants.getAllRemoteTypes();
				} else {
					if (((CheckBox)findViewById(R.id.topic_content_dialog_pois)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_POI));
					if (((CheckBox)findViewById(R.id.topic_content_dialog_events)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_EVENT));
					if (((CheckBox)findViewById(R.id.topic_content_dialog_exps)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_EXPERIENCE));
					if (((CheckBox)findViewById(R.id.topic_content_dialog_journeys)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_JOURNEY));
					if (((CheckBox)findViewById(R.id.topic_content_dialog_photo_video)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_PHOTO_VIDEO));
					if (((CheckBox)findViewById(R.id.topic_content_dialog_portfolios)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_PORTFOLIO));
					if (((CheckBox)findViewById(R.id.topic_content_dialog_stories)).isChecked()) typeList.add(CMConstants.getRemoteType(CMConstants.ENTTIY_TYPE_STORY));
				}
				
				if (typeList.size() > 0) {
					if (topic == null) topic = new Topic(); 
					topic.setContentTypes(typeList);
					handler.handleSuccess(topic);
					dismiss();
				} else {
					Toast toast = Toast.makeText(getContext(), getContext().getString(R.string.topic_content_dialog_type_required), Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		setTitle(R.string.topic_content_dialog_title);
	}
	
	private class OnAllCheckedListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				((CheckBox)findViewById(R.id.topic_content_dialog_pois)).setChecked(false);
				((CheckBox)findViewById(R.id.topic_content_dialog_events)).setChecked(false);
				((CheckBox)findViewById(R.id.topic_content_dialog_portfolios)).setChecked(false);
				((CheckBox)findViewById(R.id.topic_content_dialog_photo_video)).setChecked(false);
				((CheckBox)findViewById(R.id.topic_content_dialog_exps)).setChecked(false);
				((CheckBox)findViewById(R.id.topic_content_dialog_journeys)).setChecked(false);
				((CheckBox)findViewById(R.id.topic_content_dialog_stories)).setChecked(false);
			}
		}
		
	}
}
