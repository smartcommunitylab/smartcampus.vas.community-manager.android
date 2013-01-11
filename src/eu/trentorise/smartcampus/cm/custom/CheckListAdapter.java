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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import eu.trentorise.smartcampus.cm.R;

public class CheckListAdapter extends ArrayAdapter<CheckListAdapter.CheckListItem> {

	Context context;
	int layoutResourceId;
	private LayoutInflater layoutInflater;

	public CheckListAdapter(Context context, LayoutInflater layoutInflater, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.layoutInflater = layoutInflater;
	}

	public CheckListAdapter(Context context, LayoutInflater layoutInflater, int layoutResourceId, CheckListItem[] values) {
		super(context, layoutResourceId, values);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.layoutInflater = layoutInflater;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
//		DataHolder holder = null;

		if (row == null) {
			row = layoutInflater.inflate(layoutResourceId, parent, false);
//
//			holder = new DataHolder();
//			holder.checkBox = (CheckBox) row.findViewById(R.id.checklist_checkBox);
//			holder.optionTextView = (TextView) row.findViewById(R.id.checklist_textView);
//
//			row.setTag(holder);
//		} else {
//			holder = (DataHolder) row.getTag();
		}

		CheckListItem content = getItem(position);
		CheckBox checkBox = (CheckBox) row.findViewById(R.id.checklist_checkBox);
		checkBox.setChecked(content.checked);
		checkBox.setEnabled(content.enabled);
		TextView textView = (TextView) row.findViewById(R.id.checklist_textView);
		textView.setText(content.text);
//		holder.optionTextView.setText(content);

		// Log.e(this.getClass().getSimpleName(), "ROW DONE");
		return row;
	}

//	static class DataHolder {
//		CheckBox checkBox;
//		TextView optionTextView;
//	}

	public static class CheckListItem {
		public String text;
		public boolean checked;
		public boolean enabled;
	}
	
}
