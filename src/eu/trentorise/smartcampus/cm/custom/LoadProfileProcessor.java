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
import eu.trentorise.smartcampus.android.common.HandleExceptionHelper;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class LoadProfileProcessor implements
		SCAsyncTaskProcessor<Void, Void> {

	/**
	 * 
	 */
	private final Activity ctx;

	/**
	 * @param homeActivity
	 */
	public LoadProfileProcessor(Activity ctx) {
		this.ctx = ctx;
	}

	@Override
	public Void performAction(Void... params)
			throws SecurityException, Exception {
		CMHelper.ensureProfile();
		return null;
	}

	@Override
	public void handleResult(Void result) {
	}

	@Override
	public void handleFailure(Exception e) {
		CMHelper.endAppFailure(ctx,
				R.string.app_failure_setup);
	}

	@Override
	public void handleConnectionError() {
		HandleExceptionHelper.connectivityFailure(ctx);
		ctx.finish();

	}

	@Override
	public void handleSecurityError() {
		CMHelper.endAppFailure(ctx,
				R.string.app_failure_security);
	}

}