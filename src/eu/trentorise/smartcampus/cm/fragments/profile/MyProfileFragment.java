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
package eu.trentorise.smartcampus.cm.fragments.profile;

import it.smartcampuslab.cm.R;

import java.io.ByteArrayOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.cm.Constants;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.helper.BitmapUtils;
import eu.trentorise.smartcampus.cm.helper.ImageCacheProvider;
import eu.trentorise.smartcampus.cm.helper.ImageCacheTask;
import eu.trentorise.smartcampus.cm.model.PictureProfile;

public class MyProfileFragment extends SherlockFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
		}
		return inflater.inflate(R.layout.myprofile, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setTitle(
				R.string.profile_title);

		updateProfile(CMHelper.getProfile());
		ImageView imgView = (ImageView) getView().findViewById(
				R.id.myprofile_pic);
		imgView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);

				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						Constants.EDIT_PROFILE_ACTIVITY_REQUEST_PIC);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);

		if (requestCode == Constants.EDIT_PROFILE_ACTIVITY_REQUEST_PIC
				&& resultCode != 0) {
			Uri imgUri = result.getData();
			try {
				ImageView imgView = (ImageView) getView().findViewById(
						R.id.myprofile_pic);
				BitmapFactory.Options options = new BitmapFactory.Options();
				String imagePath = BitmapUtils.getBitmapAbsolutePath(
						getActivity(), imgUri);

				Bitmap photo = BitmapFactory.decodeFile(imagePath, options);

				// resize picture to imageView dimensions

				Bitmap profilePicture = BitmapUtils.scale(getActivity(),
						imgUri, imgView.getWidth(), imgView.getHeight());
				imgView.setImageBitmap(profilePicture);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				profilePicture.compress(CompressFormat.PNG, 100, baos);
				baos.close();

				// cache image locally
				ImageCacheProvider.store(""
						+ CMHelper.getProfile().getSocialId(),
						baos.toByteArray());

				// upload picture to profile repository
				new UpdateProfileTask(CMHelper.getProfile(), baos.toByteArray())
						.execute();

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("MyProfileFragment", "Error reading image");
			}

		}

	}

	private void updateProfile(PictureProfile userProfile) {
		TextView nameTextView = (TextView) getView().findViewById(
				R.id.myprofile_name);
		nameTextView.setText(userProfile.getName());
		TextView surnameTextView = (TextView) getView().findViewById(
				R.id.myprofile_surname);
		surnameTextView.setText(userProfile.getSurname());

		// update profile pic if present
		ImageView imgView = (ImageView) getView().findViewById(
				R.id.myprofile_pic);

		imgView.setTag("" + CMHelper.getProfile().getUserId());
		if (userProfile.getPictureUrl() != null
				&& userProfile.getPictureUrl().length() > 0) {
			try {
				new ImageCacheTask(imgView, R.drawable.placeholder).execute(
						userProfile.getPictureUrl(), ""
								+ CMHelper.getProfile().getUserId());
			} catch (Exception e) {
				Log.e("MyProfileFragment", "Exception loading profile image");
			}
		}

	}

	class UpdateProfileTask extends AsyncTask<Void, Void, Void> {

		PictureProfile profile;
		byte[] content;

		public UpdateProfileTask(PictureProfile profile, byte[] content) {
			super();
			this.content = content;
			this.profile = profile;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				CMHelper.uploadPictureProfile(profile, content);
			} catch (Exception e) {
				Log.e("MyProfileFragment",
						"exception updloading profile picture");
			}
			return null;
		}

	}

}
