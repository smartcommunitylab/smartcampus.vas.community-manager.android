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

public class UserMinimalProfile {
	public String picUrl;
	public String name;
	public String surname;
	public String faculty;
	public String position;
	public boolean known;
	
	public UserMinimalProfile() {
		super();
	}

	public UserMinimalProfile(String picUrl, String name, String surname, String faculty, String position, boolean known) {
		this.picUrl = picUrl;
		this.name = name;
		this.surname = surname;
		this.faculty = faculty;
		this.position = position;
		this.known = known;
	}
}
