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
package eu.trentorise.smartcampus.cm;

import android.support.v4.app.Fragment;
import eu.trentorise.smartcampus.android.common.HandleExceptionHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.cm.fragments.BackListener;
import eu.trentorise.smartcampus.cm.model.PictureProfile;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class HomeActivity extends BaseCMActivity {

	public static PictureProfile picP;

	@Override
	protected void loadData() {
		new SCAsyncTask<Void, Void, PictureProfile>(this,
				new LoadProfileProcessor()).execute();
	}

	@Override
	protected void setUpContent() {
	}

	@Override
	public void onBackPressed() {
		// Toast toast = Toast.makeText(getApplicationContext(), "...",
		// Toast.LENGTH_SHORT);
		// toast.show();
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentById(android.R.id.content);
		// Checking if there is a fragment that it's listening for back button
		if (currentFragment != null && currentFragment instanceof BackListener) {
			((BackListener) currentFragment).onBack();
		}

		super.onBackPressed();
	}

	private void startApp(PictureProfile profile) {
		CMHelper.setProfile(profile);
		// setUpContent();
	}

	private class LoadProfileProcessor implements
			SCAsyncTaskProcessor<Void, PictureProfile> {

		@Override
		public PictureProfile performAction(Void... params)
				throws SecurityException, Exception {
			return CMHelper.retrieveProfile();
		}

		@Override
		public void handleResult(PictureProfile result) {
			startApp(result);
		}

		@Override
		public void handleFailure(Exception e) {
			CMHelper.endAppFailure(HomeActivity.this,
					R.string.app_failure_setup);
		}

		@Override
		public void handleConnectionError() {
			HandleExceptionHelper.connectivityFailure(HomeActivity.this);
			finish();

		}

		@Override
		public void handleSecurityError() {
			CMHelper.endAppFailure(HomeActivity.this,
					R.string.app_failure_security);
		}

	}

	// @Override
	// public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem
	// item) {
	// switch (item.getItemId()) {
	// case android.R.id.home:
	// onBackPressed();
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }

}
