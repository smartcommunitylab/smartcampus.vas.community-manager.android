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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import it.smartcampuslab.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.helper.ImageCacheTask;
import eu.trentorise.smartcampus.cm.model.CMConstants;
import eu.trentorise.smartcampus.cm.model.CMConstants.ObjectFilterDescriptor;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.social.model.Concept;
import eu.trentorise.smartcampus.social.model.Entity;

public class SharedContentsAdapter extends ArrayAdapter<Entity> {

	Context context;

	public SharedContentsAdapter(Context context) {
		super(context, R.layout.content);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(R.layout.content, parent, false);

			holder = new DataHolder();
			holder.content_type_icon = (ImageView) row
					.findViewById(R.id.content_type_icon);
			holder.content_type_user = (ImageView) row
					.findViewById(R.id.content_type_user);
			holder.content_title = (TextView) row
					.findViewById(R.id.content_title);
			holder.content_tags = (TextView) row
					.findViewById(R.id.content_tags);
			holder.content_date = (TextView) row
					.findViewById(R.id.content_date);
			holder.content_user_name = (TextView) row
					.findViewById(R.id.content_user);

			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		Entity content = getItem(position);
		// Content content = contentsList.get(position);

		// for (String s : content.imagesLinks) {
		// Bitmap bitmap = null;
		// try {
		// GetBitmapFromUrl getBitmap = new GetBitmapFromUrl();
		// getBitmap.execute(s);
		// bitmap = getBitmap.get();
		// } catch (Exception e) {
		// Log.e(this.getClass().getSimpleName(), e.getMessage());
		// }
		// ImageView imageView = new ImageView(context);
		// imageView.setImageBitmap(bitmap);
		// holder.content_images.addView(imageView);
		// // Log.e(this.getClass().getSimpleName(), s + " DONE");
		// }
		String typeName = CMHelper.getEntityTypeName(content.getEntityType());
		ObjectFilterDescriptor descr = CMConstants
				.getObjectDescriptor(typeName);
		if (descr != null) {
			holder.content_type_icon.setImageResource(descr.object_drawable);
			holder.content_type_icon.setContentDescription(context
					.getString(descr.contentDescription));
		}
		holder.content_title.setText(content.getTitle());
		String tags = "";
		if (content.getTags() != null) {
			for (Concept s : content.getTags()) {
				tags += s.getName() + " ";
			}
			holder.content_tags.setText(tags);
		} else {
			holder.content_tags.setText(null);
		}

		if (content.getCreationDate() != null) {
			holder.content_date.setText(CMConstants.DATE_TIME_FORMAT
					.format(content.getCreationDate()));
		} else {
			holder.content_date.setText(null);
		}

		if (content.getUser() != null) {
			if (holder.content_type_user != null) {
				holder.content_type_user.setTag("" + content.getUser().getId());
			}

			PictureProfile pp = CMHelper.getPictureProfile(content.getUser()
					.getSocialId());
			holder.content_user_name.setText(pp.fullName());

			if (pp.getPictureUrl() != null) {
				new ImageCacheTask(holder.content_type_user,
						R.drawable.placeholder_small).execute(
						pp.getPictureUrl(), "" + content.getUser().getId());
			} else {
				holder.content_type_user
						.setImageResource(R.drawable.placeholder_small);
			}
		} else {
			holder.content_user_name.setText(null);
			holder.content_type_user.setImageBitmap(null);
		}

		// Log.e(this.getClass().getSimpleName(), "ROW DONE");
		return row;
	}

	static class DataHolder {
		ImageView content_type_icon;
		ImageView content_type_user;
		TextView content_title;
		TextView content_tags;
		TextView content_user_name;
		TextView content_date;
	}
}
