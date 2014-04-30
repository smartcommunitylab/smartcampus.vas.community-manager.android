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

import it.smartcampuslab.cm.R;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.LauncherHelper;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;

public abstract class BaseCMActivity extends SherlockFragmentActivity {

	protected boolean initialized = false;

	// protected final int mainlayout = android.R.id.content;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("initialized", initialized);
	}

	private void initDataManagement(final Bundle savedInstanceState) {
		if (LauncherHelper.isLauncherInstalled(this, true)) {
			try {
				if (CMHelper.isFirstLaunch(this)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(R.string.welcome_title)
							.setMessage(R.string.welcome_msg)
							.setOnCancelListener(
									new DialogInterface.OnCancelListener() {

										@Override
										public void onCancel(DialogInterface arg0) {
											arg0.dismiss();
											try {
												initialize();
											} catch (ProtocolException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (AACException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									})
							.setPositiveButton(getString(R.string.ok),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.dismiss();
											try {
												initialize();
											} catch (ProtocolException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (AACException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									});
					builder.create().show();
					CMHelper.disableFirstLanch(this);
				} else {
					initialize();
				}
			} catch (Exception e) {
				CMHelper.endAppFailure(this, R.string.app_failure_setup);
			}
		}
	}

	private void initialize() throws ProtocolException, AACException {
		CMHelper.init(getApplicationContext());
		if (!CMHelper.getAccessProvider().login(this, null)) {
			initData();
		}
	}

	protected boolean initData() {
		try {
			loadData();
		} catch (Exception e1) {
			CMHelper.endAppFailure(this, R.string.app_failure_setup);
			return false;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null
				|| savedInstanceState.getBoolean("initialized") == false) {
			initDataManagement(savedInstanceState);
		}
		setUpContent();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(
						AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					CMHelper.endAppFailure(this, R.string.app_failure_security);
				} else {
					initData();
				}
			} else if (resultCode == RESULT_CANCELED) {
				CMHelper.endAppFailure(this, R.string.token_required);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				|| getSupportActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_STANDARD) {
			getSupportActionBar().setDisplayShowTitleEnabled(true);
		} else {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	protected abstract void loadData() throws Exception;

	protected abstract void setUpContent();

}
