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

import android.widget.LinearLayout;
import android.widget.TextView;
import it.smartcampuslab.cm.R;

public class ViewHelper {

	public static void addEmptyListView(LinearLayout parent, int res) {
		TextView view = new TextView(parent.getContext());
		view.setId(R.id.content_empty);
		view.setText(res);
		view.setPadding(5, 5, 5, 5);
		view.setTextColor(parent.getContext().getResources()
				.getColor(R.color.sc_dark_gray));
		parent.addView(view);
	}

	public static void removeEmptyListView(LinearLayout parent) {
		parent.removeView(parent.findViewById(R.id.content_empty));
	}

}
