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

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.view.ViewHelper;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.SharedContentsAdapter;
import eu.trentorise.smartcampus.cm.fragments.AbstractTabbedFragment;
import eu.trentorise.smartcampus.cm.fragments.ActionBarHelper;
import eu.trentorise.smartcampus.cm.fragments.home.LoadObjectProcessor.ContentRequest;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.CMConstants.ObjectFilterDescriptor;
import eu.trentorise.smartcampus.social.model.Entity;

public abstract class AbstractSharedContentFragment extends AbstractTabbedFragment implements OnScrollListener  {

	protected SharedContentsAdapter adapter = null;
	protected ContentRequest contentRequest = null;
	protected int lastSize = 0;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentRequest = new ContentRequest();
		contentRequest.size = 20;
	}

	protected abstract void populateContentRequest();
	protected abstract int getLayoutId();

	@Override
	public void onStart() {
		ActionBarHelper.populateSharedContentActionBar(this);
		restartContentRequest();

		ListView contentsListView = (ListView) getView().findViewById(R.id.content_listview);
		if (adapter == null) {
			adapter = new SharedContentsAdapter(getActivity());
			populateContentRequest();
			load();
		}
		contentsListView.setAdapter(adapter);
		contentsListView.setOnScrollListener(this);
		registerForContextMenu(contentsListView);
		adapter.registerDataSetObserver(new DataSetObserver() {

			@Override
			public void onChanged() {
				updateContentView();
				super.onChanged();
			}
			
		});
		
		setUpFilterView();
		updateContentView();
		contentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Entity content = adapter.getItem(position);
				ViewHelper.viewInApp(getActivity(), CMConstants.getTypeByTypeId(content.getEntityType()), content.getEntityId(), new Bundle());
			}
		});
		super.onStart();
	}
	
	protected void setUpFilterView() {
		LinearLayout filterView = (LinearLayout) getView().findViewById(R.id.shared_content_filters);
		if (filterView != null) {
			filterView.removeAllViews();
			for (ObjectFilterDescriptor desc: CMConstants.FILTER_DESCRIPTORS) {
				ImageButton btn = new ImageButton(getActivity());
				btn.setContentDescription(getActivity().getString(desc.contentDescription));
				if (contentRequest != null && contentRequest.type != null && contentRequest.type.equals(desc.type)) {
					btn.setBackgroundResource(desc.drawable_selected);
				} else {
					btn.setBackgroundResource(desc.drawable);
				}
				btn.setTag(desc.type);
				btn.setOnClickListener(filterClickListener);
				filterView.addView(btn);
				
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) { }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = 
				(firstVisibleItem + visibleItemCount >= totalItemCount) && // end of visible list reached 
				(lastSize < adapter.getCount()) &&
				adapter.getCount() >= contentRequest.size; // last load has been successful	
		if (loadMore) {
			lastSize = adapter.getCount();
			contentRequest.position += contentRequest.size; 
			load();
		}
	}

	protected void load() {
		if (contentRequest.position == 0) {
			adapter.clear();
		}
		new SCAsyncTask<ContentRequest, Void, List<Entity>>(getActivity(), getLoadProcessor()).execute(contentRequest);
	}

	protected LoadObjectProcessor getLoadProcessor() {
		return new LoadObjectProcessor(getActivity(),adapter);
	}

	protected class FilterClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			LinearLayout filterView = (LinearLayout) getView().findViewById(R.id.shared_content_filters);
			for (ObjectFilterDescriptor desc: CMConstants.FILTER_DESCRIPTORS) {
				ImageButton btn = (ImageButton)filterView.findViewWithTag(desc.type);
				if (btn.getTag().equals(v.getTag())) {
					// if button is selected unselect it and clear the filter
					if (v.getTag().equals(contentRequest.type)) {
						btn.setBackgroundResource(desc.drawable);
						contentRequest.type = null;
					} else {
						contentRequest.type = desc.type;
						btn.setBackgroundResource(desc.drawable_selected);
					}
				} else {
					btn.setBackgroundResource(desc.drawable);
				}
			}
			restartContentRequest();
			load();
		}

	}

	protected void restartContentRequest() {
		contentRequest.position = 0;
		lastSize = 0;
	}

	private void updateContentView() {
		if (getView() == null) return;
		if (adapter != null && !adapter.isEmpty()) {
			((LinearLayout)getView().findViewById(R.id.content)).removeView(getView().findViewById(R.id.content_empty));
		} else if (getView().findViewById(R.id.content_empty)==null) {
			TextView view = new TextView(getActivity());
			view.setId(R.id.content_empty);
			view.setText(R.string.content_empty);
			((LinearLayout)getView().findViewById(R.id.content)).addView(view);
		}
	}

	protected final FilterClickListener filterClickListener = new FilterClickListener();

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Entity content = adapter.getItem(info.position);
		return handleMenuItem(content, item.getItemId());
	}

	protected boolean handleMenuItem(Entity content, int itemId) {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.shared_content_menu_header);
		populateMenu(menu, view, menuInfo);
	}

	protected void populateMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
	}

}
