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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.HandleExceptionHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.Constants;
import eu.trentorise.smartcampus.cm.HomeActivity;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.model.Profile;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class EditProfileActivity extends Activity {

	Profile profile = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editprofileform);
		
		profile = (Profile)getIntent().getSerializableExtra(Constants.EDIT_PROFILE_PROFILE_EXTRA);
		if (profile != null) {
			((EditText)findViewById(R.id.editprofile_name)).setText(profile.getName());
			((EditText)findViewById(R.id.editprofile_surname)).setText(profile.getSurname());
		}
		
		findViewById(R.id.btn_editprofile_cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(Constants.EDIT_PROFILE_ACTIVITY_RESULT_CANCELLED);
				finish();
			}
		});
		
		findViewById(R.id.btn_editprofile_ok).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Integer missing = validate(); 
				if (missing != null) {
					Toast.makeText(EditProfileActivity.this, getResources().getString(missing)+ " is required.", Toast.LENGTH_SHORT).show();
					return;
				}

				if (profile == null) {
					profile = new Profile();
				}
				profile.setName(((EditText)findViewById(R.id.editprofile_name)).getText().toString().trim());
				profile.setSurname(((EditText)findViewById(R.id.editprofile_surname)).getText().toString().trim());
				new SCAsyncTask<Profile, Void, Profile>(EditProfileActivity.this, new SaveProfileProcessor()).execute(profile);
			}
		});
	}
	@Override
	public void onBackPressed() {
		setResult(Constants.EDIT_PROFILE_ACTIVITY_RESULT_CANCELLED);
		super.onBackPressed();
	}



	private Integer validate() {
		CharSequence name = ((EditText)findViewById(R.id.editprofile_name)).getText();
		if (name == null || name.toString().trim().length() == 0)return R.string.editprofile_name;
		CharSequence surname = ((EditText)findViewById(R.id.editprofile_surname)).getText();
		if (surname == null || surname.toString().trim().length() == 0)return R.string.editprofile_surname;
		return null;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	private class SaveProfileProcessor implements SCAsyncTaskProcessor<Profile, Profile> {

		@Override
		public Profile performAction(Profile... params) throws SecurityException, Exception {
			return CMHelper.storeProfile(params[0]);
		}

		@Override
		public void handleResult(Profile result) {
			Intent intent = new Intent();
			intent.putExtra(Constants.EDIT_PROFILE_PROFILE_EXTRA, profile);
			result.setCommunities(profile.getCommunities());
			result.setGreenGame(profile.getGreenGame());
			setResult(Constants.EDIT_PROFILE_ACTIVITY_RESULT_OK, intent);
			finish();
		}

		@Override
		public void handleFailure(Exception e) {
			CMHelper.endAppFailure(EditProfileActivity.this, R.string.app_failure_setup);
		}

		@Override
		public void handleConnectionError() {
			HandleExceptionHelper.showDialogConnectivity(EditProfileActivity.this);
			
		}
		
		@Override
		public void handleSecurityError() {
			SCAccessProvider accessProvider =  CMHelper.getAccessProvider();
			try {
				accessProvider.invalidateToken(EditProfileActivity.this, null);
				accessProvider.getAuthToken(EditProfileActivity.this, null);
			} catch (Exception e) {
				Log.e(HomeActivity.class.getName(), e.getMessage());
				CMHelper.endAppFailure(EditProfileActivity.this, R.string.app_failure_security);
			}
			
		}
		
	}
}
