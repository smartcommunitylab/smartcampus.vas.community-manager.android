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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion.TYPE;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.OnTagsSelectedListener;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.cm.BaseCMActivity;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.DialogHandler;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.Community;
import eu.trentorise.smartcampus.cm.model.Concept;
import eu.trentorise.smartcampus.cm.model.Group;
import eu.trentorise.smartcampus.cm.model.MinimalProfile;
import eu.trentorise.smartcampus.cm.model.Profile;
import eu.trentorise.smartcampus.cm.model.SimpleSocialContainer;
import eu.trentorise.smartcampus.cm.model.SocialContainer;
import eu.trentorise.smartcampus.cm.model.Topic;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CreateTopicActivity extends BaseCMActivity implements OnTagsSelectedListener, TagProvider {

	private Topic topic;
	
	@Override
	protected void onResume() {
		super.onResume();
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayUseLogoEnabled(true); // system logo
		actionBar.setDisplayShowTitleEnabled(true); // system title
		actionBar.setDisplayShowHomeEnabled(true); // home icon bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // tabs bar
		if (topic == null || topic.getId() == null) {
			 setTitle(R.string.mytopics_add_title);
		} else {
			setTitle(R.string.mytopics_edit_title);
		}
	}

	@Override
	protected void loadData(String token) {
		new SCAsyncTask<Void, Void, SocialContainer>(this, new LoadUserCompleteData(this)).execute();
	}


	@Override
	protected void setUpContent() {
		setContentView(R.layout.topicform);
		
		FollowEntityObject follow = null;
		
		if (getIntent() != null) {
			topic = (Topic)getIntent().getSerializableExtra(getString(R.string.create_topic_arg_topic));
			follow = (FollowEntityObject) getIntent().getSerializableExtra(getString(eu.trentorise.smartcampus.android.common.R.string.follow_entity_arg_entity));
		}
		if (topic == null) {
			topic = new Topic();
		}
		if (follow != null) {
			topic.setEntities(new ArrayList<Concept>());
			Concept c = new Concept();
			c.setId(follow.getEntityId());
			c.setName(follow.getTitle());
			topic.getEntities().add(c);
			topic.setName("Following "+follow.getTitle());
		}
		
		EditText topicName = (EditText) findViewById(R.id.topic_name);
		topicName.setText(this.topic.getName());

		updateDefData(topic.tags());
		updateContentData(topic.getContentTypes());
		updateSourceData(topic);
		
		Button addTopicDef = (Button) findViewById(R.id.create_topic_add_def);
		addTopicDef.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TaggingDialog taggingDialog = new TaggingDialog(CreateTopicActivity.this, CreateTopicActivity.this, CreateTopicActivity.this, topic.tags());
				taggingDialog.show();
			}
		});

		Button addContentDef = (Button) findViewById(R.id.create_topic_add_content);
		addContentDef.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ContentTypeDialog taggingDialog = new ContentTypeDialog(CreateTopicActivity.this, new DialogHandler<Topic>() {
					
					@Override
					public void handleSuccess(Topic result) {
						CreateTopicActivity.this.topic.setContentTypes(result.getContentTypes());
						updateContentData(result.getContentTypes());
					}
				}, topic);
				taggingDialog.show();
			}
		});


		Button cancelBtn = (Button) findViewById(R.id.create_topic_cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Button okBtn = (Button) findViewById(R.id.create_topic_ok);
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText topicName = (EditText) findViewById(R.id.topic_name);
				String topicNameText = topicName.getText().toString().trim();

				if (topicNameText.length() == 0) {
					Toast.makeText(CreateTopicActivity.this, getString(R.string.mytopics_name_empty), Toast.LENGTH_SHORT).show();
					return;
				}
				if (!topic.isAllUsers() && (topic.getGroups() == null || topic.getGroups().isEmpty()) && 
					(topic.getCommunities() == null || topic.getCommunities().isEmpty()) && 
					(topic.getUsers() == null || topic.getUsers().isEmpty())) 
				{
					Toast.makeText(CreateTopicActivity.this, CreateTopicActivity.this.getString(R.string.mytopics_sources_required), Toast.LENGTH_LONG).show();
					return;
				}
				if (topic.getContentTypes() == null || topic.getContentTypes().isEmpty()) {
					Toast.makeText(CreateTopicActivity.this, getString(R.string.mytopics_content_type_required), Toast.LENGTH_SHORT).show();
					return;
				}
				
				topic.setName(topicNameText);
				new SCAsyncTask<Topic, Void, Topic>(CreateTopicActivity.this, new SaveTopicProcessor(CreateTopicActivity.this)).execute(topic);
			}
		});
	}
	
	private class LoadUserCompleteData extends AbstractAsyncTaskProcessor<Void, SocialContainer> {

		public LoadUserCompleteData(Activity activity) {
			super(activity);
		}

		@Override
		public SocialContainer performAction(Void... params) throws SecurityException, Exception {
			SocialContainer container = new SimpleSocialContainer();
			if (CMHelper.getAuthToken() == null) {
	    		CMHelper.init(getApplicationContext());
				CMHelper.getAccessProvider().getAuthToken(CreateTopicActivity.this, null);
			}
			
			List<Group> groups = CMHelper.readGroups();
			List<Group> custom = new ArrayList<Group>();
			Group allKnown = null;
			for (Group g : groups) {
				if (g.getName().equals(CMConstants.MY_PEOPLE_GROUP_NAME)) {
					allKnown = g;
				}
				custom.add(g);
			}
			container.setGroups(custom);
			Profile profile = CMHelper.retrieveProfile();
			container.setCommunities(profile.getCommunities());
			List<MinimalProfile> users = new ArrayList<MinimalProfile>();
			if (allKnown != null && allKnown.getUsers() != null) users.addAll(allKnown.getUsers());
			container.setUsers(users);
			return container;
		}

		@Override
		public void handleResult(final SocialContainer result) {
			Button addSourceDef = (Button) findViewById(R.id.create_topic_add_sources);
			addSourceDef.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SourceSelectDialog sourceSelectDialog = new SourceSelectDialog(
							CreateTopicActivity.this, 
							new DialogHandler<SocialContainer>() {
								@Override
								public void handleSuccess(SocialContainer result) {
									CreateTopicActivity.this.topic.setCommunities(result.getCommunities());
									CreateTopicActivity.this.topic.setGroups(result.getGroups());
									CreateTopicActivity.this.topic.setUsers(result.getUsers());
									CreateTopicActivity.this.topic.setAllUsers(result.isAllUsers());
									CreateTopicActivity.this.topic.setAllCommunities(result.isAllCommunities());
									CreateTopicActivity.this.topic.setAllKnownCommunities(result.isAllKnownCommunities());
									CreateTopicActivity.this.topic.setAllKnownUsers(result.isAllKnownUsers());

									
									updateSourceData(CreateTopicActivity.this.topic);
								}
							},
							result,
							CreateTopicActivity.this.topic
							);
					sourceSelectDialog.show();
				}
			});
		}
		
	}
	
	private class SaveTopicProcessor extends AbstractAsyncTaskProcessor<Topic, Topic> {

		
		public SaveTopicProcessor(Activity activity) {
			super(activity);
		}
		@Override
		public Topic performAction(Topic... params) throws SecurityException, Exception {
			return CMHelper.saveTopic(params[0]);
		}

		@Override
		public void handleResult(Topic result) {
			if (topic != null) {
				Intent i = new Intent();
				i.putExtra(getString(R.string.create_topic_arg_topic), topic);
				setResult(RESULT_OK, i);
				finish();
			} else {
				CMHelper.showFailure(CreateTopicActivity.this, R.string.app_failure_operation);
			}
		}
	}

	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return CMHelper.getSuggestions(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
		if (suggestions != null) {
			List<String> keywords = new ArrayList<String>();
			List<Concept> concepts = new ArrayList<Concept>();
			for (SemanticSuggestion ss : suggestions) {
				if (ss.getType() == TYPE.SEMANTIC) {
					Concept c = new Concept();
					c.setId(ss.getId());
					c.setDescription(ss.getDescription());
					c.setSummary(ss.getSummary());
					c.setName(ss.getName());
					concepts.add(c);
				} else if (ss.getType() == TYPE.ENTITY) {
					// TODO entity 
				} else {
					keywords.add(ss.getName());
				}
			}
			topic.setKeywords(keywords);
			topic.setConcepts(concepts);
		}
		updateDefData(topic.tags());
	}

	private void updateDefData(Collection<SemanticSuggestion> suggestions) {
		TextView text = (TextView)findViewById(R.id.create_topic_def_items);
		String txt = "";
		if (suggestions != null) {
			for (SemanticSuggestion ss : suggestions) {
				if (txt.length() > 0) txt += "\n";
				switch (ss.getType()) {
				case SEMANTIC:
					txt += ss.getName() + " ("+ss.getDescription()+")\n";
					break;
				case ENTITY: 
					txt += ss.getName();
					break;
				case KEYWORD:
					txt += ss.getName();
					break;
				default:
					break;
				}
			}
		}
		text.setText(txt);
		if(txt.length()>0)text.setVisibility(View.VISIBLE);
	}

	private void updateContentData(List<String> contentTypes) {
		TextView text = (TextView)findViewById(R.id.create_topic_content_items);
		String txt = "";
		if (contentTypes != null) {
			for (String t : contentTypes) {
				String localType = CMConstants.getLocalType(t);
				if (localType != null) {
					if (txt.length() > 0) txt += ", ";
					txt +=  localType;
				}
			}
		}
		text.setText(txt);
		if(txt.length()>0)text.setVisibility(View.VISIBLE);
	}
	
	private void updateSourceData(Topic topic) {
		TextView text = (TextView)findViewById(R.id.create_topic_source_items);
		String txt = "";
		
		if (topic.isAllUsers()) {
			txt += getString(R.string.source_select_public);
		}
		
		if (topic.getGroups() != null) {
			for (Group g : topic.getGroups()) {
				if (txt.length() > 0) txt += ", ";
				txt += g.getName();
			}
		}
		if (topic.getUsers() != null) {
			for (MinimalProfile p : topic.getUsers()) {
				if (txt.length() > 0) txt += ", ";
				txt += p.fullName();
			}
		}
		if (topic.getCommunities() != null) {
			for (Community c : topic.getCommunities()) {
				if (txt.length() > 0) txt += ", ";
				txt += c.getName();
			}
		}
		text.setText(txt);
		if(txt.length()>0)text.setVisibility(View.VISIBLE);
	}
}
