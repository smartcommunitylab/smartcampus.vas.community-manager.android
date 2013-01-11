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
package eu.trentorise.smartcampus.cm.model;

import java.util.List;

/**
 * 
 * Global profile class. It contains all the informations (stored or retrieving
 * from other services) to show in user's profile
 * 
 * @author Mirko Perillo
 * 
 */
public class Profile extends StoreProfile {

	private static final long serialVersionUID = 1880936222014114017L;
	private GreenGameStuff greenGame;
	private List<Community> communities;

	public GreenGameStuff getGreenGame() {
		return greenGame;
	}

	public void setGreenGame(GreenGameStuff greenGame) {
		this.greenGame = greenGame;
	}

	public List<Community> getCommunities() {
		return communities;
	}

	public void setCommunities(List<Community> communities) {
		this.communities = communities;
	}

	public String content() {
		// TODO
		return "";
	}

	public CharSequence details() {
		// TODO
		return "";
	}
}
