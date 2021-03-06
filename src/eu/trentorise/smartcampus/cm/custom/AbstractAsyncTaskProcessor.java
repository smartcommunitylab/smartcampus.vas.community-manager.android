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
import android.util.Log;
import eu.trentorise.smartcampus.android.common.HandleExceptionHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import it.smartcampuslab.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;

public abstract class AbstractAsyncTaskProcessor<Params, Result> implements
		SCAsyncTaskProcessor<Params, Result> {

	private Activity activity;

	public AbstractAsyncTaskProcessor(Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public void handleFailure(Exception e) {
		Log.e(activity.getClass().getName(), "" + e.getMessage());
		HandleExceptionHelper.showFailure(activity,
				R.string.app_failure_operation);
		e.printStackTrace();
	}

	@Override
	public void handleConnectionError() {
		HandleExceptionHelper.showDialogConnectivity(activity);

	}

	@Override
	public void handleSecurityError() {
		CMHelper.showFailure(activity, R.string.app_failure_security);
	}

}
