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
package eu.trentorise.smartcampus.cm.fragments.campus;

import eu.trentorise.smartcampus.cm.R;
import eu.trentorise.smartcampus.cm.fragments.AbstractTabbedFragment;
import eu.trentorise.smartcampus.cm.fragments.ActionBarHelper;

public class CampusFragmentSuggested extends AbstractTabbedFragment {

	@Override
	protected int getLayoutId() {
		return R.layout.contents;
	}

	@Override
	public void onStart() {
		super.onStart();
		ActionBarHelper.populateCampusActionBar(this);
	} 	

}
