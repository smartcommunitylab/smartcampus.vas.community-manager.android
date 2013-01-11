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

public class SimpleSocialContainer implements SocialContainer {

	List<Community> communities;
	List<MinimalProfile> users;
	List<Group> groups;
	private boolean allKnownUsers;
	private boolean allKnownCommunities;
	private boolean allUsers;
	private boolean allCommunities;
	
	public List<Community> getCommunities() {
		return communities;
	}
	public void setCommunities(List<Community> communities) {
		this.communities = communities;
	}
	public List<MinimalProfile> getUsers() {
		return users;
	}
	public void setUsers(List<MinimalProfile> users) {
		this.users = users;
	}
	public List<Group> getGroups() {
		return groups;
	}
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
	public boolean isAllUsers() {
		return allUsers;
	}
	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}
	public boolean isAllKnownUsers() {
		return allKnownUsers;
	}
	public void setAllKnownUsers(boolean allKnownUsers) {
		this.allKnownUsers = allKnownUsers;
	}
	public boolean isAllKnownCommunities() {
		return allKnownCommunities;
	}
	public void setAllKnownCommunities(boolean allKnownCommunities) {
		this.allKnownCommunities = allKnownCommunities;
	}
	public boolean isAllCommunities() {
		return allCommunities;
	}
	public void setAllCommunities(boolean allCommunities) {
		this.allCommunities = allCommunities;
	}
}
