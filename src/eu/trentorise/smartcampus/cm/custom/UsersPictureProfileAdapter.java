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

import java.util.Collection;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.groups.MyGroupsAddToDialog;
import eu.trentorise.smartcampus.cm.helper.ImageCacheTask;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.social.model.Group;

public class UsersPictureProfileAdapter extends ArrayAdapter<PictureProfile> {

	Activity context;
	int layoutResourceId;
	private UserOptionsHandler handler;
	private Set<String> initGroups;

	public UsersPictureProfileAdapter(Activity context, int layoutResourceId,
			UserOptionsHandler handler, Set<String> initGroups) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.handler = handler;
		this.initGroups = initGroups;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		DataHolder holder = null;

		PictureProfile user_mp = (PictureProfile) getItem(position);

		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new DataHolder();
			holder.user_mp_pic = (ImageView) row.findViewById(R.id.user_mp_pic);
			holder.user_mp_more = (Button) row.findViewById(R.id.user_mp_more);
			holder.user_mp_name = (TextView) row
					.findViewById(R.id.user_mp_name);
			holder.user_mp_surname = (TextView) row
					.findViewById(R.id.user_mp_surname);
			holder.user_mp_faculty = (TextView) row
					.findViewById(R.id.user_mp_faculty);
			holder.user_mp_position = (TextView) row
					.findViewById(R.id.user_mp_position);

			row.setTag(holder);
		} else {
			holder = (DataHolder) row.getTag();
		}

		holder.user_mp_pic.setImageResource(R.drawable.placeholder);
		holder.user_mp_pic.setTag("" + user_mp.getId());
		if (user_mp.getPictureUrl() != null) {
			new ImageCacheTask(holder.user_mp_pic, R.drawable.placeholder)
					.execute(user_mp.getPictureUrl(), "" + user_mp.getId());
		}

		if (CMHelper.isKnown(user_mp)) {
			holder.user_mp_more
					.setOnClickListener(new KnownUserClickListener());
			holder.user_mp_more.setText(R.string.user_mp_more);
			holder.user_mp_more.setBackgroundColor(context.getResources()
					.getColor(R.color.appcolor));
		} else {
			holder.user_mp_more
					.setOnClickListener(new UnknownUserClickListener());
			holder.user_mp_more.setText(R.string.user_mp_more_new);
			holder.user_mp_more.setBackgroundColor(context.getResources()
					.getColor(R.color.sc_gray));
		}

		holder.user_mp_more.setTag(user_mp);

		holder.user_mp_name.setText(user_mp.getName()); // name
		holder.user_mp_surname.setText(user_mp.getSurname()); // surname
		// holder.user_mp_faculty.setText(user_mp.faculty); // faculty
		// holder.user_mp_position.setText(user_mp.position); // position

		// Log.e(this.getClass().getSimpleName(), "ROW DONE");
		return row;
	}

	static class DataHolder {
		ImageView user_mp_pic;
		Button user_mp_more;
		TextView user_mp_name;
		TextView user_mp_surname;
		TextView user_mp_faculty;
		TextView user_mp_position;
	}

	class KnownUserClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			final PictureProfile user = (PictureProfile) v.getTag();
			if (CMHelper.getGroups().isEmpty()) {
				Toast.makeText(context, "Devi prima creare un gruppo",
						Toast.LENGTH_SHORT).show();
			} else {
				Dialog dialog = createUserOptionsDialog(user);
				dialog.show();
			}
		}
	}

	class UnknownUserClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			final PictureProfile user = (PictureProfile) v.getTag();
			Dialog dialog = new MyGroupsAddToDialog(context,
			// handle assign user to groups
					new DialogHandler<Collection<Group>>() {
						@Override
						public void handleSuccess(Collection<Group> result) {
							handler.assignUserToGroups(user, result);
						}
					}, initGroups);
			dialog.show();
		}
	}

	public interface UserOptionsHandler {
		void assignUserToGroups(PictureProfile user, Collection<Group> groups);
	}

	private Dialog createUserOptionsDialog(final PictureProfile user) {
		Set<String> groups = CMHelper.getUserGroups(user);
		if (initGroups != null)
			groups.addAll(initGroups);
		Dialog myGroupsDlg = new MyGroupsAddToDialog(context,
				new DialogHandler<Collection<Group>>() {
					@Override
					public void handleSuccess(Collection<Group> result) {
						handler.assignUserToGroups(user, result);
					}
				}, groups);
		return myGroupsDlg;
	}

}
